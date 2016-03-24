package com.lbconsulting.a1list.domain.repositories;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.listItem.impl.DeleteListItemFromBackendless_InBackground;
import com.lbconsulting.a1list.domain.interactors.listItem.impl.SaveListItemToBackendless_InBackground;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.DeleteListItemFromBackendless;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.SaveListItemToBackendless;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.storage.ListItemsSqlTable;
import com.lbconsulting.a1list.domain.storage.ListTitlesSqlTable;
import com.lbconsulting.a1list.threading.MainThreadImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * This class provided CRUD operations for ListItem
 * NOTE: All CRUD operations should run on a background thread
 */
public class ListItemRepository_Impl implements ListItemRepository,
        SaveListItemToBackendless.Callback,
        DeleteListItemFromBackendless.Callback {

    private final int FALSE = 0;
    private final int TRUE = 1;
    private final Context mContext;
    private final ListTitleRepository_Impl mListTitleRepository;

    public ListItemRepository_Impl(Context context, ListTitleRepository_Impl listTitleRepository) {
        // private constructor
        this.mContext = context;
        this.mListTitleRepository = listTitleRepository;
    }

    // CRUD operations

    //region Create ListItem
    @Override
    public boolean insert(ListItem listItem) {
        // insert new listItem into SQLite db
        boolean successfullySavedListItemToSQLiteDb = insertIntoSQLiteDb(listItem);
        if (successfullySavedListItemToSQLiteDb) {
            saveListItemToBackendless(listItem);
        }
        return successfullySavedListItemToSQLiteDb;
    }

    @Override
    public boolean insertIntoSQLiteDb(ListItem listItem) {
        boolean result = false;
        long newListItemSqlId = -1;

        Uri uri = ListItemsSqlTable.CONTENT_URI;
        ContentValues cv = new ContentValues();

        cv.put(ListItemsSqlTable.COL_NAME, listItem.getName());
        cv.put(ListItemsSqlTable.COL_UUID, listItem.getUuid());
        cv.put(ListItemsSqlTable.COL_OBJECT_ID, listItem.getObjectId());
        cv.put(ListItemsSqlTable.COL_LIST_TITLE_UUID, listItem.getListTitle().getUuid());
        cv.put(ListItemsSqlTable.COL_MANUAL_SORT_KEY, listItem.getManualSortKey());

        cv.put(ListItemsSqlTable.COL_CHECKED, (listItem.isChecked()) ? TRUE : FALSE);
        cv.put(ListItemsSqlTable.COL_FAVORITE, (listItem.isFavorite()) ? TRUE : FALSE);
        cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, TRUE);
        cv.put(ListItemsSqlTable.COL_MARKED_FOR_DELETION, (listItem.isMarkedForDeletion()) ? TRUE : FALSE);
        cv.put(ListItemsSqlTable.COL_STRUCK_OUT, (listItem.isStruckOut()) ? TRUE : FALSE);

        Date updatedDateTime = listItem.getUpdated();
        if (updatedDateTime != null) {
            cv.put(ListItemsSqlTable.COL_UPDATED, updatedDateTime.getTime());
        }

        ContentResolver cr = mContext.getContentResolver();
        Uri newListItemUri = cr.insert(uri, cv);

        if (newListItemUri != null) {
            newListItemSqlId = Long.parseLong(newListItemUri.getLastPathSegment());
        }

        if (newListItemSqlId > -1) {
            // successfully saved new ListItem to the SQLite db
            result = true;
            Timber.i("insertIntoSQLiteDb(): ListItemRepository_Impl: Successfully inserted \"%s\" into the SQLite db.", listItem.getName());
        } else {
            // failed to create listItem in the SQLite db
            Timber.i("insertIntoSQLiteDb(): ListItemRepository_Impl: FAILED to insert \"%s\" into the SQLite db.", listItem.getName());
        }

        return result;
    }

    @Override
    public void insertIntoSQLiteDb(List<ListItem> listItems) {
        for (ListItem listItem : listItems) {
            insertIntoSQLiteDb(listItem);
        }
    }

    //region Save ListItem to Backendless
    private void saveListItemToBackendless(ListItem listItem) {
        new SaveListItemToBackendless_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), listItem, this).execute();
    }

    @Override
    public void onListItemSavedToBackendless(String successMessage) {
        Timber.i("onListItemSavedToBackendless(): %s", successMessage);
    }

    @Override
    public void onListItemSaveToBackendlessFailed(String errorMessage) {
        Timber.e("onListItemSavedToBackendless(): %s", errorMessage);
    }
    //endregion

    //endregion

    //region Read
    @Override
    public ListItem retrieveListItemByUuid(String uuid) {
        ListItem foundListItem = null;
        Cursor cursor = null;
        try {
            cursor = getListItemCursorByUuid(uuid);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                foundListItem = listItemFromCursor(cursor);
            }
        } catch (Exception e) {
            Timber.e("retrieveListItemByUuid(): Exception: %s.", e.getMessage());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return foundListItem;
    }

    private ListItem listItemFromCursor(Cursor cursor) {
        ListItem listItem = new ListItem();

        listItem.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_ID)));
        listItem.setObjectId(cursor.getString(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_OBJECT_ID)));
        listItem.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_UUID)));
        listItem.setName(cursor.getString(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_NAME)));

        String listTitleUuid = cursor.getString(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_LIST_TITLE_UUID));
        ListTitle listTitle = mListTitleRepository.getListTitleByUuid(listTitleUuid);
        listItem.setListTitle(listTitle);

        listItem.setManualSortKey(cursor.getLong(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_MANUAL_SORT_KEY)));

        listItem.setStruckOut(cursor.getInt(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_STRUCK_OUT)) > 0);
        listItem.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_CHECKED)) > 0);
        listItem.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_FAVORITE)) > 0);
        listItem.setMarkedForDeletion(cursor.getInt(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_MARKED_FOR_DELETION)) > 0);

        long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_UPDATED));
        Date updated = new Date(dateMillis);
        listItem.setUpdated(updated);

        return listItem;
    }

    private Cursor getListItemCursorByUuid(String uuid) {
        Cursor cursor = null;
        Uri uri = ListItemsSqlTable.CONTENT_URI;
        String[] projection = ListItemsSqlTable.PROJECTION_ALL;
        String selection = ListItemsSqlTable.COL_UUID + " = ? AND "
                + ListItemsSqlTable.COL_MARKED_FOR_DELETION + " = ?";
        String selectionArgs[] = new String[]{uuid, String.valueOf(FALSE)};
        String sortOrder = null;
        try {
            ContentResolver cr = mContext.getContentResolver();
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);

        } catch (Exception e) {
            Timber.e("getListItemCursorByUuid(): Exception: %s.", e.getMessage());
        }
        return cursor;
    }

    @Override
    public List<ListItem> retrieveAllListItems(ListTitle listTitle, boolean isMarkedForDeletion) {
        List<ListItem> listItems = new ArrayList<>();
        ListItem listItem;
        Cursor cursor = null;
        try {
            cursor = getAllListItemsCursor(listTitle, isMarkedForDeletion);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    listItem = listItemFromCursor(cursor);
                    listItems.add(listItem);
                }
            }
        } catch (Exception e) {
            Timber.e("retrieveAllListItems(): Exception: %s.", e.getMessage());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return listItems;
    }

    @Override
    public List<ListItem> retrieveDirtyListItems() {
        List<ListItem> dirtyListItems = new ArrayList<>();
        Cursor cursor = getDirtyListItemsCursor();
        ListItem listItem;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                listItem = listItemFromCursor(cursor);
                dirtyListItems.add(listItem);
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        return dirtyListItems;
    }

    private Cursor getAllListItemsCursor(ListTitle listTitle, boolean isMarkedForDeletion) {
        Cursor cursor = null;
        Uri uri = ListItemsSqlTable.CONTENT_URI;
        String[] projection = ListItemsSqlTable.PROJECTION_ALL;
        String selection = ListItemsSqlTable.COL_LIST_TITLE_UUID + " = ? AND "
                + ListItemsSqlTable.COL_MARKED_FOR_DELETION + " = ?";
        String selectionArgs[] = new String[]{listTitle.getUuid(), String.valueOf(FALSE)};
        if (isMarkedForDeletion) {
            selectionArgs = new String[]{listTitle.getUuid(), String.valueOf(TRUE)};
        }
        String sortOrder = ListItemsSqlTable.SORT_ORDER_NAME_ASC;
        if (!listTitle.isSortListItemsAlphabetically()) {
            sortOrder = ListItemsSqlTable.SORT_ORDER_MANUAL_ASC;
        }

        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getAllListItemsCursor(): Exception: %s.", e.getMessage());
        }
        return cursor;
    }

    private Cursor getDirtyListItemsCursor() {
        Cursor cursor = null;
        Uri uri = ListItemsSqlTable.CONTENT_URI;
        String[] projection = ListItemsSqlTable.PROJECTION_ALL;
        String selection = ListItemsSqlTable.COL_LIST_ITEM_DIRTY + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(TRUE)};
        String sortOrder = null;
        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getDirtyListItemsCursor(): Exception: %s.", e.getMessage());
        }
        return cursor;
    }

    @Override
    public List<ListItem> retrieveStruckOutListItems(ListTitle listTitle) {
        List<ListItem> struckOutListItems = new ArrayList<>();
        Cursor cursor = null;
        Uri uri = ListItemsSqlTable.CONTENT_URI;
        String[] projection = ListItemsSqlTable.PROJECTION_ALL;

        String selection = ListItemsSqlTable.COL_LIST_TITLE_UUID + " = ? AND "
                + ListItemsSqlTable.COL_STRUCK_OUT + " = ?";
        String[] selectionArgs = new String[]{listTitle.getUuid(), String.valueOf(TRUE)};
        String sortOrder = null;

        ContentResolver cr = mContext.getContentResolver();
        ListItem struckOutListItem;
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    struckOutListItem = listItemFromCursor(cursor);
                    struckOutListItems.add(struckOutListItem);
                }
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

        } catch (Exception e) {
            Timber.e("retrieveStruckOutListItems(): Exception: %s.", e.getMessage());
        }

        return struckOutListItems;
    }

    @Override
    public int getNumberOfStruckOutListItems(ListTitle listTitle) {
        int struckOutListItems = 0;
        Cursor cursor = null;
        Uri uri = ListItemsSqlTable.CONTENT_URI;
        String[] projection = new String[]{ListItemsSqlTable.COL_ID};
        String selection = ListItemsSqlTable.COL_LIST_TITLE_UUID + " = ? AND "
                + ListItemsSqlTable.COL_STRUCK_OUT + " = ?";
        String[] selectionArgs = new String[]{listTitle.getUuid(), String.valueOf(TRUE)};
        String sortOrder = null;
        try {
            ContentResolver cr = mContext.getContentResolver();
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                struckOutListItems = cursor.getCount();
                cursor.close();
            }

        } catch (Exception e) {
            Timber.e("getNumberOfStruckOutListItems(): Exception: %s.", e.getMessage());
        }

        return struckOutListItems;
    }

    @Override
    public long retrieveListItemNextSortKey(ListTitle listTitle) {
        long listItemNextSortKey = listTitle.getListItemLastSortKey() + 1;
        listTitle.setListItemLastSortKey(listItemNextSortKey);
        setListItemLastSortKey(listTitle, listItemNextSortKey);
        return listItemNextSortKey;
    }

    public void setListItemLastSortKey(ListTitle listTitle, long sortKey) {
        ContentValues cv = new ContentValues();
        cv.put(ListTitlesSqlTable.COL_LIST_ITEM_LAST_SORT_KEY, sortKey);
        mListTitleRepository.update(listTitle);
    }

    private ListItem getListItem(ListTitle listTitle, String listItemName) {
        ListItem result = null;
        Cursor cursor = null;
        Uri uri = ListItemsSqlTable.CONTENT_URI;
        String selection = ListItemsSqlTable.COL_LIST_TITLE_UUID + " = ? AND "
                + ListItemsSqlTable.COL_NAME + " = ?";
        String[] selectionArgs = new String[]{listTitle.getUuid(), listItemName};
        String[] projection = ListItemsSqlTable.PROJECTION_ALL;
        String sortOrder = null;

        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    result = listItemFromCursor(cursor);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Timber.e("getListItem(): Exception: %s.", e.getMessage());
        }

        return result;
    }

    public boolean isValidListItemName(ListItem originalListItem, String proposedListItemName) {
        boolean isValidName = false;
        ListItem listItemFromName = getListItem(originalListItem.getListTitle(), proposedListItemName);
        if (listItemFromName == null) {
            // The proposed ListItem name is not in the SQLite db.
            isValidName = true;
        } else {
            // A ListItem with the proposed name exists in the SQLite db ...
            // so, check its Uuid with the original ListItem
            if (originalListItem.getUuid().equals(listItemFromName.getUuid())) {
                // both the original and existing ListItem are the same object
                isValidName = true;
            }
        }

        return isValidName;
    }

    //endregion

//    @Override

    @Override
    public boolean update(ListItem listItem) {
        ContentValues cv = new ContentValues();

        cv.put(ListItemsSqlTable.COL_NAME, listItem.getName());
        cv.put(ListItemsSqlTable.COL_UUID, listItem.getUuid());
        cv.put(ListItemsSqlTable.COL_OBJECT_ID, listItem.getObjectId());
        cv.put(ListItemsSqlTable.COL_LIST_TITLE_UUID, listItem.getListTitle().getUuid());
        cv.put(ListItemsSqlTable.COL_MANUAL_SORT_KEY, listItem.getManualSortKey());

        cv.put(ListItemsSqlTable.COL_CHECKED, (listItem.isChecked()) ? TRUE : FALSE);
        cv.put(ListItemsSqlTable.COL_FAVORITE, (listItem.isFavorite()) ? TRUE : FALSE);
        cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, TRUE);
        cv.put(ListItemsSqlTable.COL_MARKED_FOR_DELETION, (listItem.isMarkedForDeletion()) ? TRUE : FALSE);
        cv.put(ListItemsSqlTable.COL_STRUCK_OUT, (listItem.isStruckOut()) ? TRUE : FALSE);

        return update(listItem, cv);
    }

    private boolean update(ListItem listItem, ContentValues cv) {

        boolean result = false;
        try {
            cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, TRUE);
            int numberOfRecordsUpdated = updateSQLiteDb(listItem, cv);

            if (numberOfRecordsUpdated == 1) {
                result = true;
                saveListItemToBackendless(listItem);
            }
        } catch (Exception e) {
            Timber.e("update(): Exception: %s.", e.getMessage());
        }

        return result;

    }

    private int updateSQLiteDb(ListItem listItem, ContentValues cv) {
        int numberOfRecordsUpdated = 0;
        try {
            Uri uri = ListItemsSqlTable.CONTENT_URI;
            String selection = ListItemsSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listItem.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("updateSQLiteDb(): Exception: %s.", e.getMessage());
        }
        if (numberOfRecordsUpdated != 1) {
            Timber.e("updateSQLiteDb(): Error updating ListItem with uuid = %s", listItem.getUuid());
        }
        return numberOfRecordsUpdated;

    }


    @Override
    public int toggle(ListItem listItem, String fieldName) {
        int result = 0;
        boolean newValue;
        ContentValues cv = new ContentValues();

        switch (fieldName) {
            case ListItemsSqlTable.COL_CHECKED:
                newValue = listItem.isChecked();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListItemsSqlTable.COL_CHECKED, newValue ? TRUE : FALSE);
                update(listItem, cv);
                break;

            case ListItemsSqlTable.COL_FAVORITE:
                newValue = listItem.isFavorite();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListItemsSqlTable.COL_FAVORITE, newValue ? TRUE : FALSE);
                update(listItem, cv);
                break;


            case ListItemsSqlTable.COL_MARKED_FOR_DELETION:
                newValue = listItem.isMarkedForDeletion();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListItemsSqlTable.COL_MARKED_FOR_DELETION, newValue ? TRUE : FALSE);
                update(listItem, cv);
                break;


            case ListItemsSqlTable.COL_STRUCK_OUT:
                newValue = listItem.isStruckOut();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListItemsSqlTable.COL_STRUCK_OUT, newValue ? TRUE : FALSE);
                update(listItem, cv);
                break;

            default:
                Timber.e("toggle(): Unknown Field Name! \"%s\"", fieldName);
                break;
        }
        return result;
    }


    @Override
    public int delete(ListItem listItem) {
        int numberOfDeletedListItems = 0;
        try {
            Uri uri = ListItemsSqlTable.CONTENT_URI;
            String selection = ListItemsSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listItem.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(ListItemsSqlTable.COL_MARKED_FOR_DELETION, String.valueOf(TRUE));
            numberOfDeletedListItems = cr.update(uri, cv, selection, selectionArgs);

            new DeleteListItemFromBackendless_InBackground(ThreadExecutor.getInstance(),
                    MainThreadImpl.getInstance(), listItem, this).execute();

        } catch (Exception e) {
            Timber.e("delete(): Exception: %s.", e.getMessage());
        }

        return numberOfDeletedListItems;
    }

    @Override
    public void onListItemDeletedFromBackendless(String successMessage) {
        Timber.i("onListItemDeletedFromBackendless(): %s.", successMessage);
    }

    @Override
    public void onListItemDeleteFromBackendlessFailed(String errorMessage) {
        Timber.e("onListItemDeleteFromBackendlessFailed(): %s.", errorMessage);

    }


}
