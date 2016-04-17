package com.lbconsulting.a1list.domain.repositories;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.DeleteListTitleFromCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.DeleteListTitlesFromCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.SaveListTitleToCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.SaveListTitlesToCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.DeleteListTitleFromCloud;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.DeleteListTitlesFromCloud;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.SaveListTitleToCloud;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.SaveListTitlesToCloud;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.storage.ListTitlesSqlTable;
import com.lbconsulting.a1list.threading.MainThreadImpl;
import com.lbconsulting.a1list.utils.MySettings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * This class provided CRUD operations for ListTitle
 * NOTE: All CRUD operations should run on a background thread
 */
public class ListTitleRepository_Impl implements ListTitleRepository,
        SaveListTitleToCloud.Callback,
        SaveListTitlesToCloud.Callback,
        DeleteListTitleFromCloud.Callback,
        DeleteListTitlesFromCloud.Callback {

    private static ListThemeRepository_Impl mListThemeRepository = null;
    private static final int FALSE = 0;
    private static final int TRUE = 1;
    private final Context mContext;
    private final AppSettingsRepository_Impl mAppSettingsRepository;

    public ListTitleRepository_Impl(Context context) {
        // private constructor
        this.mContext = context;
        this.mAppSettingsRepository = AndroidApplication.getAppSettingsRepository();
        this.mListThemeRepository = AndroidApplication.getListThemeRepository();
    }

    // CRUD operations

    //region Insert ListTitle

    public static ListTitle listTitleFromCursor(Cursor cursor) {

        ListTitle listTitle = new ListTitle();
        listTitle.setSQLiteId(cursor.getLong(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_ID)));
        listTitle.setObjectId(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_OBJECT_ID)));
        listTitle.setDeviceUuid(MySettings.getDeviceUuid());
        listTitle.setMessageChannel(MySettings.getActiveUserID());
        listTitle.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_UUID)));
        listTitle.setName(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_NAME)));

        listTitle.setListThemeUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_LIST_THEME_UUID)));

        listTitle.setListLockString(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_LIST_LOCKED_STRING)));
        listTitle.setManualSortKey(cursor.getLong(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_MANUAL_SORT_KEY)));
        listTitle.setListItemLastSortKey(cursor.getLong(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_LIST_ITEM_LAST_SORT_KEY)));

        listTitle.setFirstVisiblePosition(cursor.getInt(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_FIRST_VISIBLE_POSITION)));
        listTitle.setListViewTop(cursor.getInt(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_LIST_VIEW_TOP)));

        listTitle.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_CHECKED)) > 0);
        listTitle.setForceViewInflation(cursor.getInt(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_FORCED_VIEW_INFLATION)) > 0);
        listTitle.setListLocked(cursor.getInt(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_LIST_LOCKED)) > 0);
        listTitle.setListPrivateToThisDevice(cursor.getInt(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_LIST_PRIVATE_TO_THIS_DEVICE)) > 0);
        listTitle.setMarkedForDeletion(cursor.getInt(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_MARKED_FOR_DELETION)) > 0);
        listTitle.setSortListItemsAlphabetically(cursor.getInt(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_SORT_ALPHABETICALLY)) > 0);
        listTitle.setStruckOut(cursor.getInt(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_STRUCK_OUT)) > 0);

        long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_UPDATED));
        Date updated = new Date(dateMillis);
        listTitle.setUpdated(updated);

        return listTitle;
    }

    @Override
    public List<ListTitle> insert(List<ListTitle> listTitles) {
        List<ListTitle> successfullyInsertedListItemsIntoLocalStorage = insertIntoLocalStorage(listTitles);
        if (successfullyInsertedListItemsIntoLocalStorage.size() > 0) {
            insertInCloud(successfullyInsertedListItemsIntoLocalStorage);
        }
        return successfullyInsertedListItemsIntoLocalStorage;
    }

    @Override
    public boolean insert(ListTitle listTitle) {
        // insert new listTitle into SQLite db
        boolean successfullyInsertedIntoSQLiteDb = insertIntoLocalStorage(listTitle);
        if (successfullyInsertedIntoSQLiteDb) {
            insertInCloud(listTitle);
            AppSettings dirtyListSettings = mAppSettingsRepository.retrieveDirtyAppSettings();
            mAppSettingsRepository.update(dirtyListSettings);
        }
        return successfullyInsertedIntoSQLiteDb;
    }

    @Override
    public List<ListTitle> insertIntoLocalStorage(List<ListTitle> listTitles) {
        List<ListTitle> successfullyInsertedListTitles = new ArrayList<>();
        for (ListTitle listTitle : listTitles) {
            if (insertIntoLocalStorage(listTitle)) {
                successfullyInsertedListTitles.add(listTitle);
            }
        }

        if (successfullyInsertedListTitles.size() == listTitles.size()) {
            Timber.i("insertIntoLocalStorage(): Successfully inserted all %d ListTitles into the SQLite db.", listTitles.size());
        } else {
            Timber.e("insertIntoLocalStorage(): Only inserted %d out of %d ListTitles into the SQLite db.",
                    successfullyInsertedListTitles.size(), listTitles.size());
        }
        return successfullyInsertedListTitles;
    }

    @Override
    public boolean insertIntoLocalStorage(ListTitle listTitle) {
        boolean result = false;
        long newListTitleSqlId = -1;

        Uri uri = ListTitlesSqlTable.CONTENT_URI;
        listTitle.setUpdated(Calendar.getInstance().getTime());
        ContentValues cv = makeListTitleContentValues(listTitle);
        ContentResolver cr = mContext.getContentResolver();
        Uri newListTitleUri = cr.insert(uri, cv);

        if (newListTitleUri != null) {
            newListTitleSqlId = Long.parseLong(newListTitleUri.getLastPathSegment());
        }
        if (newListTitleSqlId > -1) {
            // successfully saved new ListTitle to the SQLite db
            result = true;
            Timber.i("insertIntoLocalStorage(): Successfully inserted \"%s\" into the SQLite db.", listTitle.getName());
        } else {
            // failed to create listTitle in the SQLite db
            Timber.i("insertIntoLocalStorage(): FAILED to insert \"%s\" into the SQLite db.", listTitle.getName());
        }

        return result;
    }

    public static ContentValues makeListTitleContentValues(ListTitle listTitle) {
        ContentValues cv = new ContentValues();

        try {
            cv.put(ListTitlesSqlTable.COL_NAME, listTitle.getName());
            cv.put(ListTitlesSqlTable.COL_UUID, listTitle.getUuid());
            cv.put(ListTitlesSqlTable.COL_OBJECT_ID, listTitle.getObjectId());
            cv.put(ListTitlesSqlTable.COL_LIST_THEME_UUID, listTitle.getListThemeUuid());

            cv.put(ListTitlesSqlTable.COL_FIRST_VISIBLE_POSITION, listTitle.getFirstVisiblePosition());
            cv.put(ListTitlesSqlTable.COL_LIST_VIEW_TOP, listTitle.getListViewTop());
            cv.put(ListTitlesSqlTable.COL_MANUAL_SORT_KEY, listTitle.getManualSortKey());
            cv.put(ListTitlesSqlTable.COL_LIST_ITEM_LAST_SORT_KEY, listTitle.getListItemLastSortKey());
            cv.put(ListTitlesSqlTable.COL_LIST_LOCKED_STRING, listTitle.getListLockString());

            cv.put(ListTitlesSqlTable.COL_CHECKED, (listTitle.isChecked()) ? TRUE : FALSE);
            cv.put(ListTitlesSqlTable.COL_FORCED_VIEW_INFLATION, (listTitle.isForceViewInflation()) ? TRUE : FALSE);
            cv.put(ListTitlesSqlTable.COL_LIST_LOCKED, (listTitle.isListLocked()) ? TRUE : FALSE);
            cv.put(ListTitlesSqlTable.COL_LIST_PRIVATE_TO_THIS_DEVICE, (listTitle.isListPrivateToThisDevice()) ? TRUE : FALSE);
            cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
            cv.put(ListTitlesSqlTable.COL_MARKED_FOR_DELETION, (listTitle.isMarkedForDeletion()) ? TRUE : FALSE);
            cv.put(ListTitlesSqlTable.COL_SORT_ALPHABETICALLY, (listTitle.isSortListItemsAlphabetically()) ? TRUE : FALSE);
            cv.put(ListTitlesSqlTable.COL_STRUCK_OUT, (listTitle.isStruckOut()) ? TRUE : FALSE);

            Date updatedDateTime = listTitle.getUpdated();
            if (updatedDateTime != null) {
                cv.put(ListTitlesSqlTable.COL_UPDATED, updatedDateTime.getTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cv;
    }

    @Override
    public void insertInCloud(List<ListTitle> listTitles) {
        updateInCloud(listTitles, true);
    }

    //endregion

    @Override
    public void insertInCloud(ListTitle listTitle) {
        updateInCloud(listTitle, true);
    }

    //region Read ListTitle
    @Override
    public ListTitle retrieveListTitleByUuid(String uuid) {
        ListTitle foundListTitle = null;
        Cursor cursor = null;
        try {
            cursor = getListTitleCursorByUuid(uuid);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                foundListTitle = listTitleFromCursor(cursor);
            }
        } catch (Exception e) {
            Timber.e("retrieveListTitleByUuid(): Exception: %s.", e.getMessage());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return foundListTitle;
    }

    private Cursor getListTitleCursorByUuid(String uuid) {
        Cursor cursor = null;
        Uri uri = ListTitlesSqlTable.CONTENT_URI;
        String[] projection = ListTitlesSqlTable.PROJECTION_ALL;
        String selection = ListTitlesSqlTable.COL_UUID + " = ? AND "
                + ListTitlesSqlTable.COL_MARKED_FOR_DELETION + " = ?";
        String selectionArgs[] = new String[]{uuid, String.valueOf(FALSE)};
        String sortOrder = null;
        try {
            ContentResolver cr = mContext.getContentResolver();
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);

        } catch (Exception e) {
            Timber.e("getListTitleCursorByUuid(): Exception: %s.", e.getMessage());
        }
        return cursor;
    }

    @Override
    public List<ListTitle> retrieveAllListTitles(boolean isMarkedForDeletion, boolean isListsSortedAlphabetically) {
        List<ListTitle> listTitles = new ArrayList<>();
        ListTitle listTitle;
        Cursor cursor = null;
        try {
            cursor = getAllListTitlesCursor(isMarkedForDeletion, isListsSortedAlphabetically);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    listTitle = listTitleFromCursor(cursor);
                    listTitles.add(listTitle);
                }
            }
        } catch (Exception e) {
            Timber.e("retrieveAllListTitles(): Exception: %s.", e.getMessage());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return listTitles;
    }

    private Cursor getAllListTitlesCursor(boolean isMarkedForDeletion, boolean isListsSortedAlphabetically) {
        Cursor cursor = null;
        Uri uri = ListTitlesSqlTable.CONTENT_URI;
        String[] projection = ListTitlesSqlTable.PROJECTION_ALL;
        String selection = ListTitlesSqlTable.COL_MARKED_FOR_DELETION + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(FALSE)};
        if (isMarkedForDeletion) {
            selectionArgs = new String[]{String.valueOf(TRUE)};
        }
        String sortOrder = ListTitlesSqlTable.SORT_ORDER_NAME_ASC;
        if (!isListsSortedAlphabetically) {
            sortOrder = ListTitlesSqlTable.SORT_MANUALLY_ASC;
        }

        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getAllListTitlesCursor(): Exception: %s.", e.getMessage());
        }
        return cursor;
    }

    @Override
    public List<ListTitle> retrieveDirtyListTitles() {
        List<ListTitle> dirtyListTitles = new ArrayList<>();
        Cursor cursor = getDirtyListTitlesCursor();
        ListTitle listTitle;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                listTitle = listTitleFromCursor(cursor);
                dirtyListTitles.add(listTitle);
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        return dirtyListTitles;
    }

    private Cursor getDirtyListTitlesCursor() {
        Cursor cursor = null;
        Uri uri = ListTitlesSqlTable.CONTENT_URI;
        String[] projection = ListTitlesSqlTable.PROJECTION_ALL;
        String selection = ListTitlesSqlTable.COL_LIST_TITLE_DIRTY + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(TRUE)};
        String sortOrder = null;
        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getDirtyListTitlesCursor(): Exception: %s.", e.getMessage());
        }
        return cursor;
    }

    public List<ListTitle> retrieveAllListTitles(ListTheme listTheme) {
        List<ListTitle> listTitles = new ArrayList<>();
        ListTitle listTitle;
        Cursor cursor = null;
        try {
            cursor = getAllListTitlesCursor(listTheme);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    listTitle = listTitleFromCursor(cursor);
                    listTitles.add(listTitle);
                }
            }
        } catch (Exception e) {
            Timber.e("retrieveAllListTitles(): Exception: %s.", e.getMessage());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return listTitles;
    }

    private Cursor getAllListTitlesCursor(ListTheme listTheme) {
        Cursor cursor = null;
        Uri uri = ListTitlesSqlTable.CONTENT_URI;
        String[] projection = ListTitlesSqlTable.PROJECTION_ALL;
        String selection = ListTitlesSqlTable.COL_MARKED_FOR_DELETION + " = ? AND "
                + ListTitlesSqlTable.COL_LIST_THEME_UUID + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(FALSE), listTheme.getUuid()};
        String sortOrder = ListTitlesSqlTable.SORT_ORDER_NAME_ASC;

        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getAllListTitlesCursor(): Exception: %s.", e.getMessage());
        }
        return cursor;
    }


    @Override
    public List<ListTitle> retrieveStruckOutListTitles() {
        List<ListTitle> struckOutListTitles = new ArrayList<>();
        Cursor cursor = null;
        Uri uri = ListTitlesSqlTable.CONTENT_URI;
        String[] projection = ListTitlesSqlTable.PROJECTION_ALL;
        String selection = ListTitlesSqlTable.COL_STRUCK_OUT + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(TRUE)};
        String sortOrder = null;

        ContentResolver cr = mContext.getContentResolver();
        ListTitle struckOutListTitle;
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    struckOutListTitle = listTitleFromCursor(cursor);
                    struckOutListTitles.add(struckOutListTitle);
                }
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

        } catch (Exception e) {
            Timber.e("retrieveStruckOutListTitles(): Exception: %s.", e.getMessage());
        }

        return struckOutListTitles;
    }

    @Override
    public int retrieveNumberOfStruckOutListTitles() {
        int struckOutListTitles = 0;
        Cursor cursor = null;
        Uri uri = ListTitlesSqlTable.CONTENT_URI;
        String[] projection = new String[]{ListTitlesSqlTable.COL_ID};
        String selection = ListTitlesSqlTable.COL_STRUCK_OUT + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(TRUE)};
        String sortOrder = null;
        try {
            ContentResolver cr = mContext.getContentResolver();
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                struckOutListTitles = cursor.getCount();
                cursor.close();
            }

        } catch (Exception e) {
            Timber.e("retrieveNumberOfStruckOutListTitles(): Exception: %s.", e.getMessage());
        }

        return struckOutListTitles;
    }


    @Override
    public long retrieveListItemNextSortKey(ListTitle listTitle, boolean saveToBackendless) {
        ListTitle refreshedListTitle = null;
        long listItemNextSortKey = 0;

        Cursor cursor = getListTitleCursorByUuid(listTitle.getUuid());
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            refreshedListTitle = listTitleFromCursor(cursor);
            listItemNextSortKey = refreshedListTitle.getListItemLastSortKey() + 1;
            refreshedListTitle.setListItemLastSortKey(listItemNextSortKey);
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        if (saveToBackendless) {
            update(refreshedListTitle);
        } else {
            updateInLocalStorage(refreshedListTitle);
        }

        return listItemNextSortKey;
    }


    private ListTitle getListTitle(String listTitleName) {
        ListTitle result = null;
        Cursor cursor = null;
        Uri uri = ListTitlesSqlTable.CONTENT_URI;
        String selection = ListTitlesSqlTable.COL_MARKED_FOR_DELETION + " = ? AND "
                + ListTitlesSqlTable.COL_NAME + " = ?";

        String[] selectionArgs = new String[]{String.valueOf(FALSE), listTitleName};
        String[] projection = ListTitlesSqlTable.PROJECTION_ALL;
        String sortOrder = null;

        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    result = listTitleFromCursor(cursor);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Timber.e("retrieveListTitle(): Exception: %s.", e.getMessage());
        }

        return result;
    }

    public boolean isValidListTitleName(ListTitle originalListTitle, String proposedListTitleName) {
        boolean isValidName = false;
        ListTitle listTitleFromName = getListTitle(proposedListTitleName);
        if (listTitleFromName == null) {
            // The proposed ListTitle name is not in the SQLite db.
            isValidName = true;
        } else {
            // A ListTitle with the proposed name exists in the SQLite db ...
            // so, check its Uuid with the original ListTitle
            if (originalListTitle.getUuid().equals(listTitleFromName.getUuid())) {
                // both the original and existing ListTitle are the same object
                isValidName = true;
            }
        }

        return isValidName;
    }

    public boolean listTitleExists(String proposedListTitleName) {
        boolean listTitleExists = false;
        ListTitle listTitleFromName = getListTitle(proposedListTitleName);
        if (listTitleFromName != null) {
            listTitleExists = true;
        }
        return listTitleExists;
    }
    //endregion


    //region Update ListTitle
    @Override
    public void update(List<ListTitle> listTitles) {
        List<ListTitle> successfullyUpdatedListTitlesInLocalStorage = updateInLocalStorage(listTitles);
        if (successfullyUpdatedListTitlesInLocalStorage.size() > 0) {
            updateInCloud(successfullyUpdatedListTitlesInLocalStorage, false);
        }
    }

    @Override
    public void update(ListTitle listTitle) {
        if (updateInLocalStorage(listTitle) == 1) {
            updateInCloud(listTitle, false);
        }
    }

    @Override
    public List<ListTitle> updateInLocalStorage(List<ListTitle> listTitles) {
        List<ListTitle> successfullyUpdatedListTitles = new ArrayList<>();
        for (ListTitle listTitle : listTitles) {
            if (updateInLocalStorage(listTitle) == 1) {
                successfullyUpdatedListTitles.add(listTitle);
            }
        }

        if (successfullyUpdatedListTitles.size() == listTitles.size()) {
            Timber.i("updateInLocalStorage(): All %d ListTitles successfully updated in SQLiteDb.", listTitles.size());
        } else {
            Timber.e("updateInLocalStorage(): Only %d of %d ListTitles successfully updated in SQLiteDb.",
                    successfullyUpdatedListTitles.size(), listTitles.size());
        }
        return successfullyUpdatedListTitles;
    }

    @Override
    public int updateInLocalStorage(ListTitle listTitle) {
        listTitle.setUpdated(Calendar.getInstance().getTime());
        ContentValues cv = makeListTitleContentValues(listTitle);
        int numberOfRecordsUpdated = updateInLocalStorage(listTitle, cv);
        if (numberOfRecordsUpdated == 1) {
            Timber.i("updateInLocalStorage(): Successfully updated \"%s\" in SQLiteDb.", listTitle.getName());
        } else {
            Timber.e("updateInLocalStorage(): FAILED to update \"%s\" in SQLiteDb.", listTitle.getName());
        }
        return numberOfRecordsUpdated;

    }

    private int updateInLocalStorage(ListTitle listTitle, ContentValues cv) {
        int numberOfRecordsUpdated = 0;
        try {
            Uri uri = ListTitlesSqlTable.CONTENT_URI;
            String selection = ListTitlesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTitle.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("updateInLocalStorage(): Exception: %s.", e.getMessage());
        }

        return numberOfRecordsUpdated;
    }

    @Override
    public void updateInCloud(List<ListTitle> listTitles, boolean isNew) {
        List<ListTitle> listTitlesThatDoNotHaveObjectIds = new ArrayList<>();
        List<ListTitle> listTitlesThatHaveObjectIds = new ArrayList<>();

        if (!isNew) {
            // If the listTitle is not new ... make sure that it has a Backendless objectId.
            for (ListTitle listTitle : listTitles) {
                if (listTitle.getObjectId() == null || listTitle.getObjectId().isEmpty()) {
                    ListTitle existingListTitle = retrieveListTitleByUuid(listTitle.getUuid());
                    if (existingListTitle.getObjectId() == null || existingListTitle.getObjectId().isEmpty()) {
                        listTitlesThatDoNotHaveObjectIds.add(listTitle);
                    } else {
                        listTitle.setObjectId(existingListTitle.getObjectId());
                        listTitlesThatHaveObjectIds.add(listTitle);
                    }
                } else {
                    listTitlesThatHaveObjectIds.add(listTitle);
                }
            }

            new SaveListTitlesToCloud_InBackground(ThreadExecutor.getInstance(),
                    MainThreadImpl.getInstance(), this, listTitlesThatHaveObjectIds).execute();

            for (ListTitle listTitle : listTitlesThatDoNotHaveObjectIds) {
                Timber.e("updateInCloud(): Unable to update \"%s\" in the Cloud. No Backendless objectId available!",
                        listTitle.getName());
            }

        } else {
            new SaveListTitlesToCloud_InBackground(ThreadExecutor.getInstance(),
                    MainThreadImpl.getInstance(), this, listTitles).execute();
        }

    }

    @Override
    public void onListTitlesListSavedToBackendless(String successMessage, List<ListTitle> successfullySavedListTitles) {
        Timber.i("onListTitlesListSavedToBackendless(): %s.", successMessage);
    }

    @Override
    public void onListTitlesListSaveToBackendlessFailed(String errorMessage, List<ListTitle> successfullySavedListTitles) {
        Timber.i("onListTitlesListSaveToBackendlessFailed(): %s.", errorMessage);
    }

    @Override
    public void updateInCloud(ListTitle listTitle, boolean isNew) {
        // If the listTitle is not new ... make sure that it has a Backendless objectId.
        if (!isNew) {
            if (listTitle.getObjectId() == null || listTitle.getObjectId().isEmpty()) {
                ListTitle existingListTitle = retrieveListTitleByUuid(listTitle.getUuid());
                listTitle.setObjectId(existingListTitle.getObjectId());
            }
            if (listTitle.getObjectId() == null || listTitle.getObjectId().isEmpty()) {
                // The listTitle is not new AND there is no Backendless objectId available ... so,
                // Unable to update the listTitle in Backendless
                Timber.e("updateInCloud(): Unable to update \"%s\" in the Cloud. No Backendless objectId available!",
                        listTitle.getName());
                return;
            }
        }
        new SaveListTitleToCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, listTitle).execute();
        ;
    }

    @Override
    public void onListTitleSavedToCloud(String successMessage) {
        Timber.i("onListTitleSavedToCloud(): %s", successMessage);
    }

    @Override
    public void onListTitleSaveToCloudFailed(String errorMessage) {
        Timber.e("onListTitleSavedToCloud(): %s", errorMessage);
    }

    @Override
    public int toggle(ListTitle listTitle, String fieldName) {
        int result = 0;
        ListTitle currentListTitle = retrieveListTitleByUuid(listTitle.getUuid());
        if (currentListTitle == null) {
            Timber.e("toggle(): Unable to toggle field \"%s\". Could not find ListTitle \"%s\".", fieldName, listTitle.getName());
            return 0;
        }

        boolean newValue;
        ContentValues cv = new ContentValues();

        switch (fieldName) {
            case ListTitlesSqlTable.COL_CHECKED:
                newValue = !currentListTitle.isChecked();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListTitlesSqlTable.COL_CHECKED, newValue ? TRUE : FALSE);
                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
                updateInLocalStorage(listTitle, cv);
                break;

            case ListTitlesSqlTable.COL_FORCED_VIEW_INFLATION:
                newValue = !currentListTitle.isForceViewInflation();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListTitlesSqlTable.COL_FORCED_VIEW_INFLATION, newValue ? TRUE : FALSE);
                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
                updateInLocalStorage(listTitle, cv);
                break;

            case ListTitlesSqlTable.COL_LIST_LOCKED:
                newValue = !currentListTitle.isListLocked();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListTitlesSqlTable.COL_LIST_LOCKED, newValue ? TRUE : FALSE);
                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
                updateInLocalStorage(listTitle, cv);
                break;

            case ListTitlesSqlTable.COL_LIST_PRIVATE_TO_THIS_DEVICE:
                newValue = !currentListTitle.isListPrivateToThisDevice();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListTitlesSqlTable.COL_LIST_PRIVATE_TO_THIS_DEVICE, newValue ? TRUE : FALSE);
                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
                updateInLocalStorage(listTitle, cv);
                break;

            case ListTitlesSqlTable.COL_MARKED_FOR_DELETION:
                newValue = !currentListTitle.isMarkedForDeletion();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListTitlesSqlTable.COL_MARKED_FOR_DELETION, newValue ? TRUE : FALSE);
                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
                updateInLocalStorage(listTitle, cv);
                break;

            case ListTitlesSqlTable.COL_SORT_ALPHABETICALLY:
                newValue = !currentListTitle.isSortListItemsAlphabetically();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListTitlesSqlTable.COL_SORT_ALPHABETICALLY, newValue ? TRUE : FALSE);
                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
                updateInLocalStorage(listTitle, cv);
                break;

            case ListTitlesSqlTable.COL_STRUCK_OUT:
                newValue = !currentListTitle.isStruckOut();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListTitlesSqlTable.COL_STRUCK_OUT, newValue ? TRUE : FALSE);
                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
                updateInLocalStorage(listTitle, cv);
                break;

            default:
                Timber.e("toggle(): Unknown Field Name! \"%s\"", fieldName);
                break;
        }
        return result;
    }

    @Override
    public void replaceListTheme(ListTheme deletedListTheme, ListTheme defaultListTheme) {
        // retrieve all ListTitles that use the deleted ListTheme
        List<ListTitle> listTitles = retrieveAllListTitles(deletedListTheme);
        if (listTitles.size() > 0) {
            for (ListTitle listTitle : listTitles) {
                listTitle.setListThemeUuid(defaultListTheme.getUuid());
            }
            update(listTitles);
        }
    }
    //endregion

    //region Delete ListTitle
    @Override
    public int delete(List<ListTitle> listTitles) {
        List<ListTitle> successfullyMarkedForDeletionListTitles = deleteFromLocalStorage(listTitles);
        if (successfullyMarkedForDeletionListTitles.size() > 0) {
            deleteFromCloud(successfullyMarkedForDeletionListTitles);
        }

        return successfullyMarkedForDeletionListTitles.size();
    }

    @Override
    public int delete(ListTitle listTitle) {
        // TODO: get all the favorite items in the listTitle and remove the favorite flag.
        // delete any items that are held in the listTitle to be deleted.
        ListItemRepository_Impl listItemRepository = AndroidApplication.getListItemRepository();
        List<ListItem> itemsForDeletion = listItemRepository.retrieveListItems(listTitle);
        if (itemsForDeletion.size() > 0) {
            listItemRepository.delete(itemsForDeletion);
        }
        int numberOfDeletedListTitles = deleteFromLocalStorage(listTitle);
        if (numberOfDeletedListTitles == 1) {
            deleteFromCloud(listTitle);
        }

        return numberOfDeletedListTitles;
    }

    @Override
    public List<ListTitle> deleteFromLocalStorage(List<ListTitle> listTitles) {
        List<ListTitle> successfullyMarkedListTitles = new ArrayList<>();

        ListItemRepository_Impl listItemRepository = AndroidApplication.getListItemRepository();
        for (ListTitle listTitle : listTitles) {
            List<ListItem> itemsForDeletion = listItemRepository.retrieveListItems(listTitle);
            if (itemsForDeletion.size() > 0) {
                listItemRepository.delete(itemsForDeletion);
            }

            if (deleteFromLocalStorage(listTitle) == 1) {
                successfullyMarkedListTitles.add(listTitle);
            }
        }

        if (successfullyMarkedListTitles.size() == listTitles.size()) {
            Timber.i("deleteFromLocalStorage(): Successfully marked all %d ListTitles for deletion.",
                    successfullyMarkedListTitles.size());
        } else {
            Timber.e("deleteFromLocalStorage(): Only marked %d of of %d ListTitles for deletion.",
                    successfullyMarkedListTitles.size(), listTitles.size());
        }

        return successfullyMarkedListTitles;
    }

    @Override
    public int deleteFromLocalStorage(ListTitle listTitle) {
        int numberOfDeletedListTitles = 0;
        try {
            Uri uri = ListTitlesSqlTable.CONTENT_URI;
            String selection = ListTitlesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTitle.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            ContentValues cv = new ContentValues();
            Calendar rightNow = Calendar.getInstance();
            cv.put(ListTitlesSqlTable.COL_UPDATED, rightNow.getTimeInMillis());
            cv.put(ListTitlesSqlTable.COL_MARKED_FOR_DELETION, String.valueOf(TRUE));
            cv.put(ListTitlesSqlTable.COL_STRUCK_OUT, String.valueOf(FALSE));
            numberOfDeletedListTitles = cr.update(uri, cv, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("deleteFromLocalStorage(): Exception: %s.", e.getMessage());
        }
        return numberOfDeletedListTitles;
    }

    @Override
    public void deleteFromCloud(List<ListTitle> listTitles) {
        new DeleteListTitlesFromCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, listTitles).execute();
    }

    @Override
    public void deleteFromCloud(ListTitle listTitle) {
        new DeleteListTitleFromCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, listTitle).execute();
    }

    @Override
    public void onListTitlesDeletedFromBackendless(String successMessage) {
        Timber.i("onListTitlesDeletedFromBackendless(): %s.", successMessage);
    }

    @Override
    public void onListTitlesDeleteFromBackendlessFailed(String errorMessage) {
        Timber.e("onListTitlesDeleteFromBackendlessFailed(): %s.", errorMessage);
    }

    @Override
    public void onListTitleDeletedFromBackendless(String successMessage) {
        Timber.i("onListTitleDeletedFromBackendless(): %s.", successMessage);
    }

    @Override
    public void onListTitleDeleteFromBackendlessFailed(String errorMessage) {
        Timber.e("onListTitleDeleteFromBackendlessFailed(): %s.", errorMessage);
    }
    //endregion


}
