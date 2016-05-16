package com.lbconsulting.a1list.domain.repositories;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.DeleteListTitleFromCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.SaveListTitlePositionToCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.SaveListTitleToCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.SaveListTitlesToCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.DeleteListTitleFromCloud;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.DeleteListTitlesFromCloud;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.SaveListTitlePositionToCloud;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.SaveListTitleToCloud;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.SaveListTitlesToCloud;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.model.ListTitleAndPosition;
import com.lbconsulting.a1list.domain.model.ListTitlePosition;
import com.lbconsulting.a1list.domain.storage.ListTitlePositionsSqlTable;
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
        SaveListTitlePositionToCloud.Callback,
        DeleteListTitleFromCloud.Callback,
        DeleteListTitlesFromCloud.Callback {

    private static final int FALSE = 0;
    private static final int TRUE = 1;
    private static ListThemeRepository_Impl mListThemeRepository = null;
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
        listTitle.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_UUID)));
        listTitle.setName(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_NAME)));

        listTitle.setListThemeUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_LIST_THEME_UUID)));

        listTitle.setListLockString(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_LIST_LOCKED_STRING)));
        listTitle.setManualSortKey(cursor.getLong(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_MANUAL_SORT_KEY)));
        listTitle.setListItemLastSortKey(cursor.getLong(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_LIST_ITEM_LAST_SORT_KEY)));

        listTitle.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_CHECKED)) > 0);
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

    public static ContentValues makeListTitleContentValues(ListTitle listTitle) {
        ContentValues cv = new ContentValues();

        try {
            cv.put(ListTitlesSqlTable.COL_NAME, listTitle.getName());
            cv.put(ListTitlesSqlTable.COL_UUID, listTitle.getUuid());
            cv.put(ListTitlesSqlTable.COL_OBJECT_ID, listTitle.getObjectId());
            cv.put(ListTitlesSqlTable.COL_LIST_THEME_UUID, listTitle.getListThemeUuid());

            cv.put(ListTitlesSqlTable.COL_MANUAL_SORT_KEY, listTitle.getManualSortKey());
            cv.put(ListTitlesSqlTable.COL_LIST_ITEM_LAST_SORT_KEY, listTitle.getListItemLastSortKey());
            cv.put(ListTitlesSqlTable.COL_LIST_LOCKED_STRING, listTitle.getListLockString());

            cv.put(ListTitlesSqlTable.COL_CHECKED, (listTitle.isChecked()) ? TRUE : FALSE);
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


    public static ContentValues makeListTitlePositionContentValues(ListTitlePosition listTitlePosition) {
        ContentValues cv = new ContentValues();
        cv.put(ListTitlePositionsSqlTable.COL_UUID, listTitlePosition.getUuid());
        cv.put(ListTitlePositionsSqlTable.COL_OBJECT_ID, listTitlePosition.getObjectId());
        cv.put(ListTitlePositionsSqlTable.COL_LIST_TITLE_UUID, listTitlePosition.getListTitleUuid());
        cv.put(ListTitlePositionsSqlTable.COL_FIRST_POSITION, listTitlePosition.getListViewFirstVisiblePosition());
        cv.put(ListTitlePositionsSqlTable.COL_LIST_VIEW_TOP, listTitlePosition.getListViewTop());
        cv.put(ListTitlePositionsSqlTable.COL_LIST_TITLE_POSITION_DIRTY, TRUE);

        Date updatedDateTime = listTitlePosition.getUpdated();
        if (updatedDateTime != null) {
            cv.put(ListTitlePositionsSqlTable.COL_UPDATED, updatedDateTime.getTime());
        }

        return cv;
    }

    public static ListTitlePosition listTitlePositionFromCursor(Cursor cursor) {

        ListTitlePosition listTitlePosition = new ListTitlePosition();

        try {
            listTitlePosition.setSQLiteId(cursor.getLong(cursor.getColumnIndexOrThrow(ListTitlePositionsSqlTable.COL_ID)));
            listTitlePosition.setObjectId(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlePositionsSqlTable.COL_OBJECT_ID)));
            listTitlePosition.setDeviceUuid(MySettings.getDeviceUuid());
            listTitlePosition.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlePositionsSqlTable.COL_UUID)));

            listTitlePosition.setListTitleUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlePositionsSqlTable.COL_LIST_TITLE_UUID)));
            listTitlePosition.setListViewFirstVisiblePosition(cursor.getInt(cursor.getColumnIndexOrThrow(ListTitlePositionsSqlTable.COL_FIRST_POSITION)));
            listTitlePosition.setListViewTop(cursor.getInt(cursor.getColumnIndexOrThrow(ListTitlePositionsSqlTable.COL_LIST_VIEW_TOP)));

            long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(ListTitlePositionsSqlTable.COL_UPDATED));
            Date updated = new Date(dateMillis);
            listTitlePosition.setUpdated(updated);
        } catch (IllegalArgumentException e) {
            Timber.e("listTitlePositionFromCursor(): IllegalArgumentException: %s.", e.getMessage());
        } catch (Exception e) {
            Timber.e("listTitlePositionFromCursor(): Exception: %s.", e.getMessage());
        }

        return listTitlePosition;
    }

    /**
     * This method inserts a list of ListTitle into local storage.
     * For each successfully inserted ListTitle, this method inserts the ListTitle into cloud storage.
     *
     * @param listTitles The list of ListTitle to be stored.
     * @return Returns a list of ListTitle successfully inserted into local storage.
     */
    @Override
    public List<ListTitle> insertIntoStorage(List<ListTitle> listTitles) {
        List<ListTitle> successfullyInsertedListItemsIntoLocalStorage = insertIntoLocalStorage(listTitles);
        if (successfullyInsertedListItemsIntoLocalStorage.size() > 0) {
            insertIntoCloudStorage(successfullyInsertedListItemsIntoLocalStorage);
        }
        return successfullyInsertedListItemsIntoLocalStorage;
    }

    /**
     * This method inserts a ListTitle into local storage, and if successful, inserts the ListTitle into cloud storage.
     *
     * @param listTitle ListTitle to be inserted into storage.
     * @return Returns TRUE if ListTitle successfully inserted into local storage.
     */
    @Override
    public boolean insertIntoStorage(ListTitle listTitle) {
        // insertIntoStorage new listTitle into SQLite db
        boolean successfullyInsertedIntoSQLiteDb = insertIntoLocalStorage(listTitle);
        if (successfullyInsertedIntoSQLiteDb) {
            insertIntoCloudStorage(listTitle);
            AppSettings dirtyListSettings = mAppSettingsRepository.retrieveDirtyAppSettings();
            mAppSettingsRepository.updateInStorage(dirtyListSettings);
        }
        return successfullyInsertedIntoSQLiteDb;
    }

    /**
     * This method inserts a list of ListTitleAndPosition into local storage.
     * For each successfully inserted ListTitleAndPosition, this method inserts the ListTitleAndPosition into cloud storage.
     *
     * @param listTitlesPositions The list of ListTitleAndPosition to be inserted into local storage.
     * @return Returns a list of ListTitleAndPosition successfully inserted into local storage.
     */
    public List<ListTitleAndPosition> insertListTitlePositions(List<ListTitleAndPosition> listTitlesPositions) {
        List<ListTitleAndPosition> successfullyInsertedIntoLocalStorage = insertListTitlesPositionsIntoLocalStorage(listTitlesPositions);
        if (successfullyInsertedIntoLocalStorage.size() > 0) {
            insertListTitlePositionsInCloud(successfullyInsertedIntoLocalStorage);
        }
        return successfullyInsertedIntoLocalStorage;
    }

    /**
     * Inserts a list of ListTitleAndPosition into local storage. For each ListTitleAndPosition
     * successfully inserted into local storage, this method inserts the ListTitleAndPosition into
     * cloud storage.
     *
     * @param listTitlesPositions The list of ListTitleAndPosition to be inserted.
     * @return Returns a list of ListTitleAndPosition successfully inserted into local storage.
     */
    public List<ListTitleAndPosition> insertListTitlesPositionsIntoLocalStorage(List<ListTitleAndPosition> listTitlesPositions) {
        List<ListTitleAndPosition> successfullyInsertedListTitlePositions = new ArrayList<>();
        for (ListTitleAndPosition listTitlesPosition : listTitlesPositions) {
            if (insertListTitlePositionIntoLocalStorage(listTitlesPosition)) {
                successfullyInsertedListTitlePositions.add(listTitlesPosition);
            }
        }

        if (successfullyInsertedListTitlePositions.size() == listTitlesPositions.size()) {
            Timber.i("insertListTitlesPositionsIntoLocalStorage(): Successfully inserted all %d ListTitlePositions into the SQLite db.", listTitlesPositions.size());
        } else {
            Timber.e("insertListTitlesPositionsIntoLocalStorage(): Only inserted %d out of %d ListTitlePositions into the SQLite db.",
                    successfullyInsertedListTitlePositions.size(), listTitlesPositions.size());
        }
        return successfullyInsertedListTitlePositions;
    }

    /**
     * This method inserts a ListTitleAndPosition into local storage, and if successful,
     * inserts the ListTitleAndPosition into cloud storage.
     *
     * @param listTitleAndPosition ListTitleAndPosition to be inserted into storage.
     * @return Returns TRUE if ListTitleAndPosition successfully inserted into local storage.
     */
    public boolean insertListTitlePosition(ListTitleAndPosition listTitleAndPosition) {
        // insertIntoStorage new listTitlePosition into SQLite db
        boolean successfullyInsertedIntoSQLiteDb = insertListTitlePositionIntoLocalStorage(listTitleAndPosition);
        if (successfullyInsertedIntoSQLiteDb) {
            insertListTitlePositionInCloud(listTitleAndPosition);
        }
        return successfullyInsertedIntoSQLiteDb;
    }

    /**
     * Inserts a ListTitleAndPosition into local storage.
     *
     * @param listTitleAndPosition The ListTitleAndPosition to be inserted.
     * @return Returns TRUE if successfully inserted into local storage.
     */
    public boolean insertListTitlePositionIntoLocalStorage(ListTitleAndPosition listTitleAndPosition) {
        boolean result = false;
        long newListTitlePositionSqlId = -1;

        ListTitle listTitle = listTitleAndPosition.getListTitle();
        if (listTitle == null) {
            Timber.e("insertListTitlePositionIntoLocalStorage(): FAILED to retrieve ListTitle!");
        }
        ListTitlePosition listTitlePosition = listTitleAndPosition.getListTitlePosition();

        Uri uri = ListTitlePositionsSqlTable.CONTENT_URI;
        listTitlePosition.setUpdated(Calendar.getInstance().getTime());
        ContentValues cv = makeListTitlePositionContentValues(listTitlePosition);

        ContentResolver cr = mContext.getContentResolver();
        Uri newListTitlePositionUri = cr.insert(uri, cv);

        if (newListTitlePositionUri != null) {
            newListTitlePositionSqlId = Long.parseLong(newListTitlePositionUri.getLastPathSegment());
        }
        if (newListTitlePositionSqlId > -1) {
            // successfully saved new ListTitlePosition to the SQLite db
            result = true;
            if (listTitle != null) {
                Timber.i("insertListTitlePositionIntoLocalStorage(): Successfully inserted \"%s's\" ListTitlePosition into the SQLite db.", listTitle.getName());
            } else {
                Timber.i("insertListTitlePositionIntoLocalStorage(): Successfully inserted ListTitlePosition with ListTitle uuid = %s into the SQLite db.", listTitlePosition.getListTitleUuid());
            }
        } else {
            // failed to create listTitlePosition in the SQLite db
            if (listTitle != null) {
                Timber.e("insertListTitlePositionIntoLocalStorage(): FAILED to insertIntoStorage \"%s's\" ListTitlePosition into the SQLite db.", listTitle.getName());
            } else {
                Timber.e("insertListTitlePositionIntoLocalStorage(): FAILED to insertIntoStorage ListTitlePosition with ListTitle uuid = %s into the SQLite db.", listTitlePosition.getListTitleUuid());
            }
        }

        return result;
    }

    /**
     * Inserts a list of ListTitleAndPosition into cloud storage via a background thread.
     * For each successfully inserted ListTitleAndPosition clears the local storage dirty flag and
     * sends an Insert message to other devices.
     *
     * @param listTitlesPositions The list of ListTitleAndPosition to be inserted.
     */
    private void insertListTitlePositionsInCloud(List<ListTitleAndPosition> listTitlesPositions) {
        for (ListTitleAndPosition listTitlesPosition : listTitlesPositions) {
            insertListTitlePositionInCloud(listTitlesPosition);
        }
    }

    /**
     * Inserts ListTitleAndPosition into cloud storage via a background thread,
     * and if successful, clears the local storage dirty flag and
     * sends an Insert message to other devices.
     *
     * @param listTitlesPosition
     */
    private void insertListTitlePositionInCloud(ListTitleAndPosition listTitlesPosition) {
        new SaveListTitlePositionToCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, listTitlesPosition).execute();
    }

    /**
     * Inserts a list of ListTitle into local storage.
     *
     * @param listTitles The list of ListTitle to be inserted.
     * @return Returns a list of ListTitle successfully inserted into local storage.
     */
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

    /**
     * Inserts a ListTitle into local storage.
     *
     * @param listTitle the ListTitle to be stored.
     * @return Returns TRUE if ListTitle successfully inserted into local storage.
     */
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
            Timber.e("insertIntoLocalStorage(): FAILED to insertIntoStorage \"%s\" into the SQLite db.", listTitle.getName());
        }

        return result;
    }

    //endregion

    /**
     * Inserts a list of ListTitle into cloud storage via a background thread.
     * For each successfully inserted ListTitle, the method clears the local storage dirty flag and
     * sends an Insert message to other devices.
     *
     * @param listTitles
     */
    @Override
    public void insertIntoCloudStorage(List<ListTitle> listTitles) {
        updateInCloud(listTitles, true);
    }

    /**
     * Inserts a ListTitle into cloud storage via a background thread.
     * For each successfully inserted ListTitle, the method clears the local storage dirty flag and
     * sends an Insert message to other devices.
     *
     * @param listTitle
     */
    @Override
    public void insertIntoCloudStorage(ListTitle listTitle) {
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
    public ListTitlePosition retrieveListTitlePosition(ListTitle listTitle) {
        ListTitlePosition listTitlePosition = null;
        Cursor cursor = null;
        Uri uri = ListTitlePositionsSqlTable.CONTENT_URI;
        String[] projection = ListTitlePositionsSqlTable.PROJECTION_ALL;
        String selection = ListTitlePositionsSqlTable.COL_LIST_TITLE_UUID + " = ?";
        String selectionArgs[] = new String[]{listTitle.getUuid()};
        String sortOrder = null;
        try {
            ContentResolver cr = mContext.getContentResolver();
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    listTitlePosition = listTitlePositionFromCursor(cursor);
                }
                cursor.close();
            }

        } catch (Exception e) {
            Timber.e("retrieveListTitlePosition(): Exception: %s.", e.getMessage());
        }

        return listTitlePosition;
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
            updateStorage(refreshedListTitle);
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

    /**
     * Updates a list of ListTitle in local and cloud storage.
     *
     * @param listTitles The list of ListTitle to be stored.
     */
    @Override
    public void updateStorage(List<ListTitle> listTitles) {
        List<ListTitle> successfullyUpdatedListTitlesInLocalStorage = updateInLocalStorage(listTitles);
        if (successfullyUpdatedListTitlesInLocalStorage.size() > 0) {
            updateInCloud(successfullyUpdatedListTitlesInLocalStorage, false);
        }
    }

    /**
     * Updates a ListTitle in local and cloud storage.
     *
     * @param listTitle The ListTitle to be stored.
     */
    @Override
    public void updateStorage(ListTitle listTitle) {
        if (updateInLocalStorage(listTitle) == 1) {
            updateInCloud(listTitle, false);
        }
    }

    @Override
    public void updateListTitlePosition(ListTitle listTitle, int firstVisiblePosition, int top) {
        if (updateListTitlePositionInLocalStorage(listTitle, firstVisiblePosition, top) == 1) {
            updateListTitlePositionInCloud(listTitle, firstVisiblePosition, top);
        }
    }

    public int updateListTitlePositionInLocalStorage(ListTitle listTitle, int firstVisiblePosition,
                                                     int top) {

        int numberOfRecordsUpdated = 0;
        if (listTitle != null) {
            ContentValues cv = new ContentValues();
            cv.put(ListTitlePositionsSqlTable.COL_FIRST_POSITION, firstVisiblePosition);
            cv.put(ListTitlePositionsSqlTable.COL_LIST_VIEW_TOP, top);
            cv.put(ListTitlePositionsSqlTable.COL_LIST_TITLE_POSITION_DIRTY, TRUE);
            cv.put(ListTitlePositionsSqlTable.COL_UPDATED, Calendar.getInstance().getTimeInMillis());
            try {
                Uri uri = ListTitlePositionsSqlTable.CONTENT_URI;
                String selection = ListTitlePositionsSqlTable.COL_LIST_TITLE_UUID + " = ?";
                String[] selectionArgs = new String[]{listTitle.getUuid()};
                ContentResolver cr = mContext.getContentResolver();
                numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);

            } catch (Exception e) {
                Timber.e("updateListTitlePositionInLocalStorage(): Exception: %s.", e.getMessage());
            }

            if (numberOfRecordsUpdated == 1) {
                Timber.i("updateListTitlePositionInLocalStorage(): Successfully updated \"%s\" Position in SQLiteDb.", listTitle.getName());
            } else {
                Timber.e("updateListTitlePositionInLocalStorage(): FAILED to updateStorage \"%s\" Position in SQLiteDb.", listTitle.getName());
            }
        } else {
            Timber.e("updateListTitlePositionInLocalStorage(): FAILED to updateStorage ListTitle Position. ListTitle is null!");
        }
        return numberOfRecordsUpdated;

    }

    private void updateListTitlePositionInCloud(ListTitle listTitle, int firstVisiblePosition, int top) {

        ListTitlePosition listTitlePosition = retrieveListTitlePosition(listTitle);
        if (listTitlePosition != null) {
            // If the listTitlePosition is not new ... make sure that it has a Backendless objectId.
            if (listTitlePosition.getObjectId() == null || listTitlePosition.getObjectId().isEmpty()) {
                Timber.e("updateListTitlePositionInCloud(): Unable to updateStorage \"%s's\" ListTitlePosition. ObjectId not available.",
                        listTitle.getName());
                return;
            }
            listTitlePosition.setListViewFirstVisiblePosition(firstVisiblePosition);
            listTitlePosition.setListViewTop(top);

        } else {
            Timber.e("updateListTitlePositionInCloud(): FAILED to retrieve \"%s's\" ListTitlePosition,",
                    listTitle.getName());
            return;
        }

        ListTitleAndPosition listTitlesPosition = new ListTitleAndPosition(listTitle, listTitlePosition);

        new SaveListTitlePositionToCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, listTitlesPosition).execute();
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
            Timber.e("updateInLocalStorage(): FAILED to updateStorage \"%s\" in SQLiteDb.", listTitle.getName());
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
                Timber.e("updateInCloudStorage(): Unable to updateStorage \"%s\" in the Cloud. No Backendless objectId available!",
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
        Timber.e("onListTitlesListSaveToBackendlessFailed(): %s.", errorMessage);
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
                // Unable to updateStorage the listTitle in Backendless
                Timber.e("updateInCloudStorage(): Unable to updateStorage \"%s\" in the Cloud. No Backendless objectId available!",
                        listTitle.getName());
                return;
            }
        }
        new SaveListTitleToCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, listTitle).execute();
    }

    @Override
    public void onListTitleSavedToCloud(String successMessage) {
        Timber.i("onListTitleSavedToCloud(): %s", successMessage);
    }

    @Override
    public void onListTitleSaveToCloudFailed(String errorMessage) {
        Timber.e("onListTitleSavedToCloud(): %s", errorMessage);
    }

//    @Override
//    public int toggle(ListTitle listTitle, String fieldName) {
//        int result = 0;
//        ListTitle currentListTitle = retrieveListTitleByUuid(listTitle.getUuid());
//        if (currentListTitle == null) {
//            Timber.e("toggle(): Unable to toggle field \"%s\". Could not find ListTitle \"%s\".", fieldName, listTitle.getName());
//            return 0;
//        }
//
//        boolean newValue;
//        ContentValues cv = new ContentValues();
//
//        switch (fieldName) {
//            case ListTitlesSqlTable.COL_CHECKED:
//                newValue = !currentListTitle.isChecked();
//                if (newValue) {
//                    result++;
//                } else {
//                    result--;
//                }
//                cv.put(ListTitlesSqlTable.COL_CHECKED, newValue ? TRUE : FALSE);
//                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
//                updateInLocalStorage(listTitle, cv);
//                break;
//
//            case ListTitlesSqlTable.COL_LIST_LOCKED:
//                newValue = !currentListTitle.isListLocked();
//                if (newValue) {
//                    result++;
//                } else {
//                    result--;
//                }
//                cv.put(ListTitlesSqlTable.COL_LIST_LOCKED, newValue ? TRUE : FALSE);
//                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
//                updateInLocalStorage(listTitle, cv);
//                break;
//
//            case ListTitlesSqlTable.COL_LIST_PRIVATE_TO_THIS_DEVICE:
//                newValue = !currentListTitle.isListPrivateToThisDevice();
//                if (newValue) {
//                    result++;
//                } else {
//                    result--;
//                }
//                cv.put(ListTitlesSqlTable.COL_LIST_PRIVATE_TO_THIS_DEVICE, newValue ? TRUE : FALSE);
//                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
//                updateInLocalStorage(listTitle, cv);
//                break;
//
//            case ListTitlesSqlTable.COL_MARKED_FOR_DELETION:
//                newValue = !currentListTitle.isMarkedForDeletion();
//                if (newValue) {
//                    result++;
//                } else {
//                    result--;
//                }
//                cv.put(ListTitlesSqlTable.COL_MARKED_FOR_DELETION, newValue ? TRUE : FALSE);
//                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
//                updateInLocalStorage(listTitle, cv);
//                break;
//
//            case ListTitlesSqlTable.COL_SORT_ALPHABETICALLY:
//                newValue = !currentListTitle.isSortListItemsAlphabetically();
//                if (newValue) {
//                    result++;
//                } else {
//                    result--;
//                }
//                cv.put(ListTitlesSqlTable.COL_SORT_ALPHABETICALLY, newValue ? TRUE : FALSE);
//                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
//                updateInLocalStorage(listTitle, cv);
//                break;
//
//            case ListTitlesSqlTable.COL_STRUCK_OUT:
//                newValue = !currentListTitle.isStruckOut();
//                if (newValue) {
//                    result++;
//                } else {
//                    result--;
//                }
//                cv.put(ListTitlesSqlTable.COL_STRUCK_OUT, newValue ? TRUE : FALSE);
//                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
//                updateInLocalStorage(listTitle, cv);
//                break;
//
//            default:
//                Timber.e("toggle(): Unknown Field Name! \"%s\"", fieldName);
//                break;
//        }
//        return result;
//    }

    @Override
    public void replaceListTheme(ListTheme deletedListTheme, ListTheme defaultListTheme) {
        // retrieve all ListTitles that use the deleted ListTheme
        List<ListTitle> listTitles = retrieveAllListTitles(deletedListTheme);
        if (listTitles.size() > 0) {
            for (ListTitle listTitle : listTitles) {
                listTitle.setListThemeUuid(defaultListTheme.getUuid());
            }
            updateStorage(listTitles);
        }
    }
    //endregion

    //region Delete ListTitle
    //    The general deletion process:
    //    i)	First, deleteFromStorage any ListItems associated with the ListTitle;
    //    ii)   Second, set the the ListTitle's deleteFromStorage flag in local storage;
    //    iii)	Third, deleteFromStorage the ListTitle from cloud storage;
    //    iv)	Fourth, if ListTitle successfully deleted from cloud storage then deleteFromStorage it from local storage.

    /**
     * Deletes a ListTitle and its associated ListItems and ListTitlePosition from local and cloud storage.
     *
     * @param listTitle The ListTitle to be deleted.
     * @return Returns the number of ListTitles marked for deletion in local storage.
     */
    @Override
    public int deleteFromStorage(ListTitle listTitle) {
        // deleteFromStorage any ListItems that are held in the listTitle to be deleted.
        ListItemRepository_Impl listItemRepository = AndroidApplication.getListItemRepository();
        List<ListItem> listItemsForDeletion = listItemRepository.retrieveListItems(listTitle);
        if (listItemsForDeletion.size() > 0) {
            for (ListItem listItem : listItemsForDeletion) {
                // Make sure all favorite flags are set to false
                // so listItem will be deleted from local and cloud storage.
                if (listItem.isFavorite()) {
                    listItem.setFavorite(false);
                }
            }
            // deleteFromStorage all ListItems
            listItemRepository.deleteFromStorage(listItemsForDeletion);
        }

        // Mark the ListTitle for deletion
        int numberOfListTitlesMarkedForDeletion = setDeleteFlagInLocalStorage(listTitle);
        if (numberOfListTitlesMarkedForDeletion == 1) {
            // deleteFromStorage the ListTitle and its ListTitlePosition from cloud storage.
            deleteFromCloudStorage(listTitle);
        }

        return numberOfListTitlesMarkedForDeletion;
    }

//
//    private int deleteFromLocalStorage(ListTitle listTitle, ListTitlePosition listTitlePosition) {
//        int numberOfDeletedListTitlePositions = 0;
//        try {
//            Uri uri = ListTitlePositionsSqlTable.CONTENT_URI;
//            String selection = ListTitlePositionsSqlTable.COL_UUID + " = ?";
//            String[] selectionArgs = new String[]{listTitlePosition.getUuid()};
//            ContentResolver cr = mContext.getContentResolver();
//            numberOfDeletedListTitlePositions = cr.deleteFromStorage(uri, selection, selectionArgs);
//            if (numberOfDeletedListTitlePositions == 1) {
//                Timber.i("deleteFromLocalStorage(): Successfully deleted \"%s's\" ListTitlePosition from the SQLiteDb.", listTitle.getName());
//            } else {
//                Timber.e("deleteFromLocalStorage(): FAILED to deleteFromStorage \"%s's\" ListTitlePosition from the SQLiteDb.", listTitle.getName());
//            }
//
//        } catch (Exception e) {
//            Timber.e("deleteFromLocalStorage(): Exception: %s.", e.getMessage());
//        }
//
//        return numberOfDeletedListTitlePositions;
//    }

    @Override
    public List<ListTitle> setDeleteFlagInLocalStorage(List<ListTitle> listTitles) {
        List<ListTitle> successfullyMarkedListTitles = new ArrayList<>();

        int numberOfListTitlesMarkedForDeletion;
        for (ListTitle listTitle : listTitles) {
            numberOfListTitlesMarkedForDeletion = setDeleteFlagInLocalStorage(listTitle);
            if (numberOfListTitlesMarkedForDeletion == 1) {
                successfullyMarkedListTitles.add(listTitle);
            }
        }

        if (successfullyMarkedListTitles.size() == listTitles.size()) {
            Timber.i("setDeleteFlagInLocalStorage(): Successfully marked all %d ListTitles for deletion.",
                    successfullyMarkedListTitles.size());
        } else {
            Timber.e("setDeleteFlagInLocalStorage(): Only marked %d of of %d ListTitles for deletion.",
                    successfullyMarkedListTitles.size(), listTitles.size());
        }

        return successfullyMarkedListTitles;
    }

    @Override
    public int setDeleteFlagInLocalStorage(ListTitle listTitle) {
        int numberOfListTitlesMarkedForDeletion = 0;
        try {
            Uri uri = ListTitlesSqlTable.CONTENT_URI;
            String selection = ListTitlesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTitle.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(ListTitlesSqlTable.COL_UPDATED, Calendar.getInstance().getTimeInMillis());
            cv.put(ListTitlesSqlTable.COL_MARKED_FOR_DELETION, String.valueOf(TRUE));
            cv.put(ListTitlesSqlTable.COL_STRUCK_OUT, String.valueOf(FALSE));
            numberOfListTitlesMarkedForDeletion = cr.update(uri, cv, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("setDeleteFlagInLocalStorage(): Exception: %s.", e.getMessage());
        }
        return numberOfListTitlesMarkedForDeletion;
    }

    @Override
    public List<ListTitle> deleteFromLocalStorage(List<ListTitle> listTitles) {
        List<ListTitle> successfullyDeletedListTitles = new ArrayList<>();
        for (ListTitle listTitle : listTitles) {
            if (deleteFromLocalStorage(listTitle) == 1) {
                successfullyDeletedListTitles.add(listTitle);
            }
        }
        return successfullyDeletedListTitles;
    }

    @Override
    public int deleteFromLocalStorage(ListTitle listTitle) {
        int numberOfDeletedListTitles = 0;
        try {
            Uri uri = ListTitlesSqlTable.CONTENT_URI;
            String selection = ListTitlesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTitle.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            numberOfDeletedListTitles = cr.delete(uri, selection, selectionArgs);
            if (numberOfDeletedListTitles == 1) {
                Timber.i("deleteFromLocalStorage(): Successfully deleted \"%s\" from the SQLiteDb.", listTitle.getName());
            } else {
                Timber.e("deleteFromLocalStorage(): FAILED to deleteFromStorage \"%s\" from the SQLiteDb.", listTitle.getName());
            }

        } catch (Exception e) {
            Timber.e("deleteFromStorage(): Exception: %s.", e.getMessage());
        }

        return numberOfDeletedListTitles;
    }

    @Override
    public int deleteFromLocalStorage(ListTitle listTitle, ListTitlePosition listTitlePosition) {
        int numberOfDeletedListTitlePositions = 0;
        try {
            Uri uri = ListTitlePositionsSqlTable.CONTENT_URI;
            String selection = ListTitlePositionsSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTitlePosition.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            numberOfDeletedListTitlePositions = cr.delete(uri, selection, selectionArgs);
            if (numberOfDeletedListTitlePositions == 1) {
                Timber.i("deleteFromLocalStorage(): Successfully deleted \"%s's\" ListTitlePosition from the SQLiteDb.", listTitle.getName());
            } else {
                Timber.e("deleteFromLocalStorage(): FAILED to deleteFromStorage \"%s's\" ListTitlePosition from the SQLiteDb.", listTitle.getName());
            }

        } catch (Exception e) {
            Timber.e("deleteFromStorage(): Exception: %s.", e.getMessage());
        }

        return numberOfDeletedListTitlePositions;
    }

    @Override
    public int clearLocalStorageDirtyFlag(ListTitle listTitle) {
        ContentValues cv = new ContentValues();
        Date updatedDate = listTitle.getUpdated();
        if (updatedDate == null) {
            updatedDate = listTitle.getCreated();
        }
        if (updatedDate != null) {
            long updated = updatedDate.getTime();
            cv.put(ListTitlesSqlTable.COL_UPDATED, updated);
        }

        cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, FALSE);
        cv.put(ListTitlesSqlTable.COL_OBJECT_ID, listTitle.getObjectId());

        return updateInLocalStorage(listTitle, cv);
    }

    @Override
    public int clearAllData() {
        int numberOfDeletedListTitles = 0;
        try {
            Uri uri = ListTitlesSqlTable.CONTENT_URI;
            String selection = null;
            String[] selectionArgs = null;
            ContentResolver cr = mContext.getContentResolver();
            numberOfDeletedListTitles = cr.delete(uri, selection, selectionArgs);
            Timber.i("clearAllData(): Successfully deleted %d ListTitles from the SQLiteDb.", numberOfDeletedListTitles);

        } catch (Exception e) {
            Timber.e("clearAllData(): Exception: %s.", e.getMessage());
        }

        int numberOfDeletedListTitlePositions = 0;
        try {
            Uri uri = ListTitlePositionsSqlTable.CONTENT_URI;
            String selection = null;
            String[] selectionArgs = null;
            ContentResolver cr = mContext.getContentResolver();
            numberOfDeletedListTitlePositions = cr.delete(uri, selection, selectionArgs);
            Timber.i("clearAllData(): Successfully deleted %d ListTitlePositions from the SQLiteDb.", numberOfDeletedListTitlePositions);

        } catch (Exception e) {
            Timber.e("clearAllData(): Exception: %s.", e.getMessage());
        }

        return numberOfDeletedListTitles;
    }

    public int deleteListTitlePositionFromLocalStorage(ListTitlePosition listTitlePosition) {
        int numberOfDeletedListTitlePositions = 0;
        try {
            Uri uri = ListTitlePositionsSqlTable.CONTENT_URI;
            String selection = ListTitlePositionsSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTitlePosition.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            numberOfDeletedListTitlePositions = cr.delete(uri, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("deleteListTitlePositionFromLocalStorage(): Exception: %s.", e.getMessage());
        }
        return numberOfDeletedListTitlePositions;
    }

    @Override
    public void deleteFromCloudStorage(List<ListTitle> listTitles) {
        for (ListTitle listTitle : listTitles) {
            deleteFromCloudStorage(listTitle);
        }
    }


    @Override
    public void deleteFromCloudStorage(ListTitle listTitle) {
        ListTitlePosition listTitlePosition = retrieveListTitlePosition(listTitle);
        new DeleteListTitleFromCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, listTitle, listTitlePosition).execute();
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

    @Override
    public void onListTitlePositionSavedToCloud(String successMessage) {
        Timber.i("onListTitlePositionSavedToCloud(): %s.", successMessage);
    }

    @Override
    public void onListTitlePositionSaveToCloudFailed(String errorMessage) {
        Timber.e("onListTitlePositionSaveToCloudFailed(): %s.", errorMessage);
    }



    //endregion


}
