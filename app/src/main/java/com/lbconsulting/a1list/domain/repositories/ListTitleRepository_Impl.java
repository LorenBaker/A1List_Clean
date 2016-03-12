package com.lbconsulting.a1list.domain.repositories;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.storage.ListTitlesSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * This class provided CRUD operations for ListTitle
 * NOTE: All CRUD operations should run on a background thread
 */
public class ListTitleRepository_Impl implements ListTitleRepository {

    private final int FALSE = 0;
    private final int TRUE = 1;
    private final Context mContext;
    private final ListThemeRepository_Impl mListThemeRepository;

    public ListTitleRepository_Impl(Context context, ListThemeRepository_Impl listThemeRepository) {
        // private constructor
        this.mContext = context;
        this.mListThemeRepository = listThemeRepository;
    }

    // CRUD operations

    //region Create
    @Override
    public ListTitle insert(ListTitle listTitle) {
        // insert new listTitle into SQLite db
        ListTitle backendlessResponse = null;
        long newListTitleSqlId = -1;

        Uri uri = ListTitlesSqlTable.CONTENT_URI;
        ContentValues cv = new ContentValues();

        cv.put(ListTitlesSqlTable.COL_NAME, listTitle.getName());
        cv.put(ListTitlesSqlTable.COL_UUID, listTitle.getUuid());
        cv.put(ListTitlesSqlTable.COL_OBJECT_ID, listTitle.getObjectId());
        cv.put(ListTitlesSqlTable.COL_LIST_THEME_UUID, listTitle.getListTheme().getUuid());

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

        ContentResolver cr = mContext.getContentResolver();
        Uri newListTitleUri = cr.insert(uri, cv);

        if (newListTitleUri != null) {
            newListTitleSqlId = Long.parseLong(newListTitleUri.getLastPathSegment());
        }

        if (newListTitleSqlId > -1) {
            // successfully saved new ListTitle to the SQLite db
            Timber.i("insert(): ListTitleRepository_Impl: Successfully inserted \"%s\" into the SQLite db.", listTitle.getName());

            // if the network is available ... save new listTitle to Backendless
            if (CommonMethods.isNetworkAvailable()) {
                // save listTitle to Backendless
                backendlessResponse = saveListTitleToBackendless(listTitle);
                // TODO: send message to Backendless to notify other devices of the new ListTitle
            }

        } else {
            // failed to create listTitle in the SQLite db
            Timber.e("insert(): ListTitleRepository_Impl: FAILED to insert \"%s\" into the SQLite db.", listTitle.getName());
        }
        return backendlessResponse;
    }

    private ListTitle saveListTitleToBackendless(ListTitle listTitle) {
        // saveListTitleToBackendless object synchronously
        ListTitle response = null;
        try {
            String objectId = listTitle.getObjectId();
            boolean isNew = objectId == null || objectId.isEmpty();
            response = Backendless.Data.of(ListTitle.class).save(listTitle);
            Timber.i("saveListTitleToBackendless(): successfully saved \"%s\" to Backendless.", response.getName());
            // Update the SQLite db: set dirty to false, and updated date and time
            ContentValues cv = new ContentValues();
            Date updatedDate = response.getUpdated();
            if (updatedDate == null) {
                updatedDate = response.getCreated();
            }
            if (updatedDate != null) {
                long updated = updatedDate.getTime();
                cv.put(ListTitlesSqlTable.COL_UPDATED, updated);
            }
            cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, FALSE);

            // If a new ListTitle, update SQLite db with objectID
            if (isNew) {
                cv.put(ListTitlesSqlTable.COL_OBJECT_ID, response.getObjectId());
            }
            // update the SQLite db ... but don't send changes to Backendless
            updateSQLiteDb(response, cv);

        } catch (BackendlessException e) {
            Timber.e("saveListTitleToBackendless(): FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
                    listTitle.getName(), e.getCode(), e.getMessage());
            // Set dirty flag to true in SQLite db
            ContentValues cv = new ContentValues();
            cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
            updateSQLiteDb(listTitle, cv);

        } catch (Exception e) {
            Timber.e("saveAppSettingsToBackendless(): Exception: %s.", e.getMessage());
            // Set dirty flag to true in SQLite db
            ContentValues cv = new ContentValues();
            cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
            updateSQLiteDb(listTitle, cv);
        }
        return response;
    }


    //region Read
    @Override
    public ListTitle getListTitleByUuid(String uuid) {
        ListTitle foundListTitle = null;
        Cursor cursor = null;
        try {
            cursor = getListTitleCursorByUuid(uuid);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                foundListTitle = listTitleFromCursor(cursor);
            }
        } catch (Exception e) {
            Timber.e("getListTitleByUuid(): Exception: %s.", e.getMessage());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return foundListTitle;
    }

    private ListTitle listTitleFromCursor(Cursor cursor) {

        ListTitle listTitle = new ListTitle();
        listTitle.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_ID)));
        listTitle.setObjectId(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_OBJECT_ID)));
        listTitle.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_UUID)));
        listTitle.setName(cursor.getString(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_NAME)));

        String listThemeUuid = cursor.getString(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_LIST_THEME_UUID));
        ListTheme listTheme = mListThemeRepository.getListThemeByUuid(listThemeUuid);
        listTitle.setListTheme(listTheme);

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
        // since above set methods set ListTitleDirty to true, this statement must be last set statement
        // so that the cursor's themeDirty prevails
//        listTitle.setListTitleDirty(cursor.getInt(cursor.getColumnIndexOrThrow(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY)) > 0);

        return listTitle;
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
    public List<ListTitle> retrieveAllListTitles(boolean isMarkedForDeletion) {
        List<ListTitle> listTitles = new ArrayList<>();
        ListTitle listTitle;
        Cursor cursor = null;
        try {
            cursor = getAllListTitlesCursor(isMarkedForDeletion);
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

    private Cursor getAllListTitlesCursor(boolean isMarkedForDeletion) {
        Cursor cursor = null;
        Uri uri = ListTitlesSqlTable.CONTENT_URI;
        String[] projection = ListTitlesSqlTable.PROJECTION_ALL;
        String selection = ListTitlesSqlTable.COL_MARKED_FOR_DELETION + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(FALSE)};
        if (isMarkedForDeletion) {
            selectionArgs = new String[]{String.valueOf(TRUE)};
        }
        String sortOrder = ListTitlesSqlTable.SORT_ORDER_NAME_ASC;

        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getAllListTitlesCursor(): Exception: %s.", e.getMessage());
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
    public int getNumberOfStruckOutListTitles() {
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
            Timber.e("getNumberOfStruckOutListTitles(): Exception: %s.", e.getMessage());
        }

        return struckOutListTitles;
    }

    @Override
    public long retrieveListItemNextSortKey() {
                ListTitle listTitle = null;
        long listItemNextSortKey = 0;

        Cursor cursor = getListTitleCursorByUuid(listTitle.getUuid());
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            listTitle = listTitleFromCursor(cursor);
            listItemNextSortKey = listTitle.getListItemLastSortKey() + 1;
            listTitle.setListItemLastSortKey(listItemNextSortKey);
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        setListItemLastSortKey(listTitle, listItemNextSortKey);
        return listItemNextSortKey;
    }

    @Override
    public void setListItemLastSortKey(ListTitle listTitle, long sortKey) {
        ContentValues cv = new ContentValues();
        cv.put(ListTitlesSqlTable.COL_LIST_ITEM_LAST_SORT_KEY, sortKey);
        update(listTitle, cv);
    }

    private ListTitle getListTitle(String listTitleName) {
        ListTitle result = null;
        Cursor cursor = null;
        Uri uri = ListTitlesSqlTable.CONTENT_URI;
        String selection = ListTitlesSqlTable.COL_NAME + " = ?";
        String[] selectionArgs = new String[]{listTitleName};
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
            Timber.e("getListTitle(): Exception: %s.", e.getMessage());
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

    public boolean isValidListTitleName(String proposedListTitleName) {
        boolean isValidName = false;
        ListTitle listTitleFromName = getListTitle(proposedListTitleName);
        if (listTitleFromName == null) {
            // The proposed ListTitle name is not in the SQLite db.
            isValidName = true;
        }
        return isValidName;
    }
    //endregion

    @Override
    public boolean update(ListTitle listTitle, ContentValues cv) {

        boolean result = false;
        try {
            cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
            int numberOfRecordsUpdated = updateSQLiteDb(listTitle, cv);

            if (numberOfRecordsUpdated == 1 && CommonMethods.isNetworkAvailable()) {
                result = true;
                saveListTitleToBackendless(listTitle);
                // TODO: Send update message to other devices
            }
        } catch (Exception e) {
            Timber.e("update(): Exception: %s.", e.getMessage());
        }

        return result;

    }

    private int updateSQLiteDb(ListTitle listTitle, ContentValues cv) {
        int numberOfRecordsUpdated = 0;
        try {
            Uri uri = ListTitlesSqlTable.CONTENT_URI;
            String selection = ListTitlesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTitle.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("updateSQLiteDb(): Exception: %s.", e.getMessage());
        }
        if (numberOfRecordsUpdated != 1) {
            Timber.e("updateSQLiteDb(): Error updating ListTitle with uuid = %s", listTitle.getUuid());
        }
        return numberOfRecordsUpdated;

    }

    @Override
    public boolean update(ListTitle listTitle) {
        ContentValues cv = new ContentValues();

        cv.put(ListTitlesSqlTable.COL_NAME, listTitle.getName());
        cv.put(ListTitlesSqlTable.COL_LIST_THEME_UUID, listTitle.getListTheme().getUuid());

        cv.put(ListTitlesSqlTable.COL_FIRST_VISIBLE_POSITION, listTitle.getFirstVisiblePosition());
        cv.put(ListTitlesSqlTable.COL_LIST_VIEW_TOP, listTitle.getListViewTop());
        cv.put(ListTitlesSqlTable.COL_MANUAL_SORT_KEY, listTitle.getManualSortKey());
        cv.put(ListTitlesSqlTable.COL_LIST_LOCKED_STRING, listTitle.getListLockString());

        cv.put(ListTitlesSqlTable.COL_CHECKED, (listTitle.isChecked()) ? TRUE : FALSE);
        cv.put(ListTitlesSqlTable.COL_FORCED_VIEW_INFLATION, (listTitle.isForceViewInflation()) ? TRUE : FALSE);
        cv.put(ListTitlesSqlTable.COL_LIST_LOCKED, (listTitle.isListLocked()) ? TRUE : FALSE);
        cv.put(ListTitlesSqlTable.COL_LIST_PRIVATE_TO_THIS_DEVICE, (listTitle.isListPrivateToThisDevice()) ? TRUE : FALSE);
        cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
        cv.put(ListTitlesSqlTable.COL_MARKED_FOR_DELETION, (listTitle.isMarkedForDeletion()) ? TRUE : FALSE);
        cv.put(ListTitlesSqlTable.COL_SORT_ALPHABETICALLY, (listTitle.isSortListItemsAlphabetically()) ? TRUE : FALSE);
        cv.put(ListTitlesSqlTable.COL_STRUCK_OUT, (listTitle.isStruckOut()) ? TRUE : FALSE);

        return update(listTitle, cv);
    }


    @Override
    public int toggle(ListTitle listTheme, String fieldName) {
        int result = 0;
        ListTitle currentListTitle = getListTitleByUuid(listTheme.getUuid());
        if (currentListTitle == null) {
            Timber.e("toggle(): Unable to toggle field \"%s\". Could not find ListTitle \"%s\".", fieldName, listTheme.getName());
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
                update(listTheme, cv);
                break;

            case ListTitlesSqlTable.COL_FORCED_VIEW_INFLATION:
                newValue = !currentListTitle.isForceViewInflation();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListTitlesSqlTable.COL_FORCED_VIEW_INFLATION, newValue ? TRUE : FALSE);
                update(listTheme, cv);
                break;

            case ListTitlesSqlTable.COL_LIST_LOCKED:
                newValue = !currentListTitle.isListLocked();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListTitlesSqlTable.COL_LIST_LOCKED, newValue ? TRUE : FALSE);
                update(listTheme, cv);
                break;

            case ListTitlesSqlTable.COL_LIST_PRIVATE_TO_THIS_DEVICE:
                newValue = !currentListTitle.isListPrivateToThisDevice();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListTitlesSqlTable.COL_LIST_PRIVATE_TO_THIS_DEVICE, newValue ? TRUE : FALSE);
                update(listTheme, cv);
                break;

            case ListTitlesSqlTable.COL_MARKED_FOR_DELETION:
                newValue = !currentListTitle.isMarkedForDeletion();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListTitlesSqlTable.COL_MARKED_FOR_DELETION, newValue ? TRUE : FALSE);
                update(listTheme, cv);
                break;

            case ListTitlesSqlTable.COL_SORT_ALPHABETICALLY:
                newValue = !currentListTitle.isSortListItemsAlphabetically();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListTitlesSqlTable.COL_SORT_ALPHABETICALLY, newValue ? TRUE : FALSE);
                update(listTheme, cv);
                break;

            case ListTitlesSqlTable.COL_STRUCK_OUT:
                newValue = !currentListTitle.isStruckOut();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListTitlesSqlTable.COL_STRUCK_OUT, newValue ? TRUE : FALSE);
                update(listTheme, cv);
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
        for (ListTitle listTitle : listTitles) {
            ContentValues cv = new ContentValues();
            cv.put(ListTitlesSqlTable.COL_LIST_THEME_UUID, defaultListTheme.getUuid());
            update(listTitle, cv);
        }
    }

    @Override
    public int delete(ListTitle listTitle) {
        int numberOfDeletedListTitles = 0;
        try {
            Uri uri = ListTitlesSqlTable.CONTENT_URI;
            String selection = ListTitlesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTitle.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            numberOfDeletedListTitles = cr.delete(uri, selection, selectionArgs);
        } catch (Exception e) {
            Timber.e("delete(): Exception: %s.", e.getMessage());
        }

        return numberOfDeletedListTitles;
    }

    @Override
    public int markDeleted(ListTitle listTitle) {
        int numberOfDeletedListTitles = 0;
        try {
            Uri uri = ListTitlesSqlTable.CONTENT_URI;
            String selection = ListTitlesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTitle.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(ListTitlesSqlTable.COL_MARKED_FOR_DELETION, String.valueOf(TRUE));
            numberOfDeletedListTitles = cr.update(uri, cv, selection, selectionArgs);
        } catch (Exception e) {
            Timber.e("markDeleted(): Exception: %s.", e.getMessage());
        }

        return numberOfDeletedListTitles;
    }


}
