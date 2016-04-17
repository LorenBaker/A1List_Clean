package com.lbconsulting.a1list.domain.repositories;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.listItem.impl.DeleteListItemFromCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listItem.impl.DeleteListItemsFromCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listItem.impl.SaveListItemToCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listItem.impl.SaveListItemsToCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.DeleteListItemFromCloud;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.DeleteListItemsFromCloud;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.SaveListItemToCloud;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.SaveListItemsToCloud;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.storage.ListItemsSqlTable;
import com.lbconsulting.a1list.threading.MainThreadImpl;
import com.lbconsulting.a1list.utils.MySettings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * This class provided CRUD operations for ListItem
 * NOTE: All CRUD operations should run on a background thread
 */
public class ListItemRepository_Impl implements ListItemRepository,
        SaveListItemsToCloud.Callback,
        SaveListItemToCloud.Callback,
        DeleteListItemFromCloud.Callback,
        DeleteListItemsFromCloud.Callback {

    private static final int FALSE = 0;
    private static final int TRUE = 1;
    private static ListTitleRepository_Impl mListTitleRepository = null;
    private final Context mContext;

    public ListItemRepository_Impl(Context context) {
        // private constructor
        this.mContext = context;
        this.mListTitleRepository = AndroidApplication.getListTitleRepository();
    }

    // CRUD operations

    public static ListItem listItemFromCursor(Cursor cursor) {
        ListItem listItem = new ListItem();

        listItem.setSQLiteId(cursor.getLong(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_ID)));
        listItem.setObjectId(cursor.getString(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_OBJECT_ID)));
        listItem.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_UUID)));
        listItem.setName(cursor.getString(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_NAME)));

        listItem.setListTitleUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_LIST_TITLE_UUID)));
        listItem.setDeviceUuid(MySettings.getDeviceUuid());
        listItem.setMessageChannel(MySettings.getActiveUserID());

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

    public static ContentValues makeListItemContentValues(ListItem listItem) {
        ContentValues cv = new ContentValues();

        cv.put(ListItemsSqlTable.COL_NAME, listItem.getName());
        cv.put(ListItemsSqlTable.COL_UUID, listItem.getUuid());
        cv.put(ListItemsSqlTable.COL_OBJECT_ID, listItem.getObjectId());
        cv.put(ListItemsSqlTable.COL_LIST_TITLE_UUID, listItem.retrieveListTitle().getUuid());
        cv.put(ListItemsSqlTable.COL_MANUAL_SORT_KEY, listItem.getManualSortKey());

        cv.put(ListItemsSqlTable.COL_CHECKED, (listItem.isChecked()) ? TRUE : FALSE);
        cv.put(ListItemsSqlTable.COL_FAVORITE, (listItem.isFavorite()) ? TRUE : FALSE);
        cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, TRUE);
        cv.put(ListItemsSqlTable.COL_MARKED_FOR_DELETION, (listItem.isMarkedForDeletion()) ? TRUE : FALSE);
        cv.put(ListItemsSqlTable.COL_STRUCK_OUT, (listItem.isStruckOut()) ? TRUE : FALSE);

        cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, TRUE);

        Date updatedDateTime = listItem.getUpdated();
        if (updatedDateTime != null) {
            cv.put(ListItemsSqlTable.COL_UPDATED, updatedDateTime.getTime());
        }
        return cv;
    }

    //region Insert ListItem
    @Override
    public List<ListItem> insert(List<ListItem> listItems) {
        List<ListItem> successfullyInsertedListItems = insertIntoLocalStorage(listItems);
        if (successfullyInsertedListItems.size() > 0) {
            insertInCloud(successfullyInsertedListItems);
        }
        return successfullyInsertedListItems;
    }

    @Override
    public boolean insert(ListItem listItem) {
        // insert new listItem into SQLite db
        boolean successfullySavedIntoLocalStorage = insertIntoLocalStorage(listItem);
        if (successfullySavedIntoLocalStorage) {
            insertInCloud(listItem);
        }
        return successfullySavedIntoLocalStorage;
    }

    @Override
    public List<ListItem> insertIntoLocalStorage(List<ListItem> listItems) {
        List<ListItem> successfullyInsertedListItems = new ArrayList<>();
        for (ListItem listItem : listItems) {
            if (insertIntoLocalStorage(listItem)) {
                successfullyInsertedListItems.add(listItem);
            }
        }

        if (successfullyInsertedListItems.size() == listItems.size()) {
            Timber.i("insertIntoLocalStorage(): Successfully inserted all %d ListItems into the SQLite db.", listItems.size());
        } else {
            Timber.e("insertIntoLocalStorage(): Only inserted %d out of %d ListItems into the SQLite db.",
                    successfullyInsertedListItems.size(), listItems.size());
        }
        return successfullyInsertedListItems;
    }

    @Override
    public boolean insertIntoLocalStorage(ListItem listItem) {
        boolean result = false;
        long newListItemSqlId = -1;

        Uri uri = ListItemsSqlTable.CONTENT_URI;
        listItem.setUpdated(Calendar.getInstance().getTime());
        ContentValues cv = makeListItemContentValues(listItem);
        ContentResolver cr = mContext.getContentResolver();
        Uri newListItemUri = cr.insert(uri, cv);

        if (newListItemUri != null) {
            newListItemSqlId = Long.parseLong(newListItemUri.getLastPathSegment());
        }

        if (newListItemSqlId > -1) {
            // successfully saved new ListItem to the SQLite db
            result = true;
            Timber.i("insertIntoLocalStorage(): ListItemRepository_Impl: Successfully inserted \"%s\" into the SQLite db.", listItem.getName());
        } else {
            // failed to create listItem in the SQLite db
            Timber.e("insertIntoLocalStorage(): ListItemRepository_Impl: FAILED to insert \"%s\" into the SQLite db.", listItem.getName());
        }

        return result;
    }

    @Override
    public void insertInCloud(List<ListItem> listItems) {
        updateInCloud(listItems, true);
    }

    //endregion

    @Override
    public void insertInCloud(ListItem listItem) {
        updateInCloud(listItem, true);
    }

    //region Read ListItem
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

    @Override
    public List<ListItem> retrieveListItems(ListTitle listTitle) {
        List<ListItem> listItemsInList = new ArrayList<>();
        Cursor cursor = getListItemsInListCursor(listTitle);
        ListItem listItem;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                listItem = listItemFromCursor(cursor);
                listItemsInList.add(listItem);
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        return listItemsInList;
    }

    private Cursor getListItemsInListCursor(ListTitle listTitle) {
        Cursor cursor = null;
        Uri uri = ListItemsSqlTable.CONTENT_URI;
        String[] projection = ListItemsSqlTable.PROJECTION_ALL;
        String selection = ListItemsSqlTable.COL_LIST_TITLE_UUID + " = ?";
        String selectionArgs[] = new String[]{listTitle.getUuid()};
        String sortOrder = null;
        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getListItemsInListCursor(): Exception: %s.", e.getMessage());
        }
        return cursor;
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
    public List<ListItem> retrieveFavoriteListItems(ListTitle listTitle) {
        List<ListItem> favoriteListItems = new ArrayList<>();
        Cursor cursor = getFavoriteListItemsCursor(listTitle);
        ListItem listItem;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                listItem = listItemFromCursor(cursor);
                favoriteListItems.add(listItem);
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        return favoriteListItems;
    }


    private Cursor getFavoriteListItemsCursor(ListTitle listTitle) {
        Cursor cursor = null;
        Uri uri = ListItemsSqlTable.CONTENT_URI;
        String[] projection = ListItemsSqlTable.PROJECTION_ALL;
        String selection = ListItemsSqlTable.COL_FAVORITE + " = ? AND " + ListItemsSqlTable.COL_LIST_TITLE_UUID + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(TRUE), listTitle.getUuid()};
        String sortOrder = ListItemsSqlTable.SORT_ORDER_NAME_ASC;
        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getFavoriteListItemsCursor(): Exception: %s.", e.getMessage());
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
    public int retrieveNumberOfStruckOutListItems(ListTitle listTitle) {
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
            Timber.e("retrieveNumberOfStruckOutListItems(): Exception: %s.", e.getMessage());
        }

        return struckOutListItems;
    }

//    @Override
//    public long retrieveListItemNextSortKey(ListTitle listTitle) {
//        long listItemNextSortKey = listTitle.getListItemLastSortKey() + 1;
//        listTitle.setListItemLastSortKey(listItemNextSortKey);
//        setListItemLastSortKey(listTitle, listItemNextSortKey);
//        return listItemNextSortKey;
//    }


//    public void setListItemLastSortKey(ListTitle listTitle, long sortKey) {
//        ContentValues cv = new ContentValues();
//        cv.put(ListTitlesSqlTable.COL_LIST_ITEM_LAST_SORT_KEY, sortKey);
//        mListTitleRepository.update(listTitle);
//    }

    private ListItem getListItem(ListTitle listTitle, String listItemName) {
        // TODO: Does this getListItem method's selection need ListItem not marked for deletion. How to handle favorites.
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
        ListItem listItemFromName = getListItem(originalListItem.retrieveListTitle(), proposedListItemName);
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

    public boolean itemExists(ListTitle listTitle, String itemName) {
        boolean itemExists = false;
        ListItem listItemFromName = getListItem(listTitle, itemName);
        if (listItemFromName != null) {
            // The proposed ListItem name is in the SQLite db.
            itemExists = true;
        }
        return itemExists;
    }

    //endregion


    //region Update ListItem

    @Override
    public void update(List<ListItem> listItems) {
        List<ListItem> successfullyUpdatedListItemsInLocalStorage = updateInLocalStorage(listItems);
        if (successfullyUpdatedListItemsInLocalStorage.size() > 0) {
            updateInCloud(successfullyUpdatedListItemsInLocalStorage, false);
        }
    }

    @Override
    public void update(ListItem listItem) {
        if (updateInLocalStorage(listItem) == 1) {
            updateInCloud(listItem, false);
        }
    }


    //region Update in Local Storage
    @Override
    public List<ListItem> updateInLocalStorage(List<ListItem> listItems) {
        List<ListItem> successfullyUpdatedListItems = new ArrayList<>();
        for (ListItem listItem : listItems) {
            if (updateInLocalStorage(listItem) == 1) {
                successfullyUpdatedListItems.add(listItem);
            }
        }

        if (successfullyUpdatedListItems.size() == listItems.size()) {
            Timber.i("updateInLocalStorage(): All %d ListItems updated.", listItems.size());
        } else {
            Timber.e("updateInLocalStorage(): Only %d of %d ListItems updated.",
                    successfullyUpdatedListItems.size(), listItems.size());
        }
        return successfullyUpdatedListItems;
    }

    @Override
    public int updateInLocalStorage(ListItem listItem) {
        listItem.setUpdated(Calendar.getInstance().getTime());
        ContentValues cv = makeListItemContentValues(listItem);
        int numberOfRecordsUpdated = updateInLocalStorage(listItem, cv);
        if (numberOfRecordsUpdated == 1) {
            Timber.i("updateInLocalStorage(): Successfully updated \"%s\" in the SQLiteDb.", listItem.getName());
        } else {
            Timber.e("updateInLocalStorage(): FAILED to update \"%s\" in the SQLiteDb.", listItem.getName());
        }
        return numberOfRecordsUpdated;
    }

    private int updateInLocalStorage(ListItem listItem, ContentValues cv) {
        int numberOfRecordsUpdated = 0;
        try {
            Uri uri = ListItemsSqlTable.CONTENT_URI;
            String selection = ListItemsSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listItem.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("updateInLocalStorage(): Exception: %s.", e.getMessage());
        }

        return numberOfRecordsUpdated;

    }
    //endregion

    //region Update ListItem in Cloud

    @Override
    public void updateInCloud(List<ListItem> listItems, boolean isNew) {
        List<ListItem> listItemsThatDoNotHaveObjectIds = new ArrayList<>();
        List<ListItem> listItemsThatHaveObjectIds = new ArrayList<>();

        if (!isNew) {
            // If the listItem is not new ... make sure that it has a Backendless objectId.
            for (ListItem listItem : listItems) {
                if (listItem.getObjectId() == null || listItem.getObjectId().isEmpty()) {
                    ListItem existingListItem = retrieveListItemByUuid(listItem.getUuid());
                    if (existingListItem.getObjectId() == null || existingListItem.getObjectId().isEmpty()) {
                        listItemsThatDoNotHaveObjectIds.add(listItem);
                    } else {
                        listItem.setObjectId(existingListItem.getObjectId());
                        listItemsThatHaveObjectIds.add(listItem);
                    }
                } else {
                    listItemsThatHaveObjectIds.add(listItem);
                }
            }

            new SaveListItemsToCloud_InBackground(ThreadExecutor.getInstance(),
                    MainThreadImpl.getInstance(), this, listItemsThatHaveObjectIds).execute();

            for (ListItem listItem : listItemsThatDoNotHaveObjectIds) {
                Timber.e("updateInCloud(): Unable to update \"%s\" in the Cloud. No Backendless objectId available!",
                        listItem.getName());
            }

        } else {
            new SaveListItemsToCloud_InBackground(ThreadExecutor.getInstance(),
                    MainThreadImpl.getInstance(), this, listItems).execute();
        }


    }

    @Override
    public void onListItemListSavedToBackendless(String successMessage, List<ListItem> successfullySavedListItems) {
        Timber.i("onListItemListSavedToBackendless(): %s", successMessage);
    }

    @Override
    public void onListItemListSaveToBackendlessFailed(String errorMessage, List<ListItem> successfullySavedListItems) {
        Timber.e("onListItemListSaveToBackendlessFailed(): %s", errorMessage);
    }

    @Override
    public void updateInCloud(ListItem listItem, boolean isNew) {
        if (listItem != null) {
            // If the listItem is not new ... make sure that it has a Backendless objectId.
            if (!isNew) {
                if (listItem.getObjectId() == null || listItem.getObjectId().isEmpty()) {
                    ListItem existingListItem = retrieveListItemByUuid(listItem.getUuid());
                    listItem.setObjectId(existingListItem.getObjectId());
                }
                if (listItem.getObjectId() == null || listItem.getObjectId().isEmpty()) {
                    // The listItem is not new AND there is no Backendless objectId available ... so,
                    // Unable to update the listItem in Backendless
                    Timber.e("updateInCloud(): Unable to update \"%s\" in the Cloud. No Backendless objectId available!",
                            listItem.getName());
                    return;
                }
            }
            new SaveListItemToCloud_InBackground(ThreadExecutor.getInstance(),
                    MainThreadImpl.getInstance(), this, listItem).execute();
        } else {
            Timber.e("updateInCloud(): Unable to update ListItem in Cloud. The Provided ListItem is null!");
        }
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
                cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, TRUE);
                updateInLocalStorage(listItem, cv);
                break;

            case ListItemsSqlTable.COL_FAVORITE:
                newValue = listItem.isFavorite();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListItemsSqlTable.COL_FAVORITE, newValue ? TRUE : FALSE);
                cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, TRUE);
                updateInLocalStorage(listItem, cv);
                break;


            case ListItemsSqlTable.COL_MARKED_FOR_DELETION:
                newValue = listItem.isMarkedForDeletion();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListItemsSqlTable.COL_MARKED_FOR_DELETION, newValue ? TRUE : FALSE);
                cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, TRUE);
                updateInLocalStorage(listItem, cv);
                break;


            case ListItemsSqlTable.COL_STRUCK_OUT:
                newValue = listItem.isStruckOut();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListItemsSqlTable.COL_STRUCK_OUT, newValue ? TRUE : FALSE);
                cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, TRUE);
                updateInLocalStorage(listItem, cv);
                break;

            default:
                Timber.e("toggle(): Unknown Field Name! \"%s\"", fieldName);
                break;
        }
        return result;
    }

    //endregion


    //region Delete ListItem
    @Override
    public int delete(List<ListItem> listItems) {
        List<ListItem> successfullyMarkedForDeletionListItems = deleteFromLocalStorage(listItems);
        List<ListItem> listItemsForCloudUpdate = new ArrayList<>();
        List<ListItem> listItemsForCloudDeletion = new ArrayList<>();

        for (ListItem markedListItem : successfullyMarkedForDeletionListItems) {
            if (markedListItem.isFavorite()) {
                listItemsForCloudUpdate.add(markedListItem);
            } else {
                listItemsForCloudDeletion.add(markedListItem);
            }
        }

        if (listItemsForCloudUpdate.size() > 0) {
            updateInCloud(listItemsForCloudUpdate, false);
        }

        if (listItemsForCloudDeletion.size() > 0) {
            deleteFromCloud(listItemsForCloudDeletion);
        }

        return successfullyMarkedForDeletionListItems.size();
    }

    @Override
    public int delete(ListItem listItem) {
        int numberOfDeletedListItems = deleteFromLocalStorage(listItem);
        if (numberOfDeletedListItems == 1) {
            if (listItem.isFavorite()) {
                listItem.setMarkedForDeletion(true);
                listItem.setStruckOut(false);
                updateInCloud(listItem, false);
            } else {
                deleteFromCloud(listItem);
            }
        }

        return numberOfDeletedListItems;
    }

    @Override
    public List<ListItem> deleteFromLocalStorage(List<ListItem> listItems) {
        List<ListItem> successfullyMarkedForDeletionListItems = new ArrayList<>();
        for (ListItem listItem : listItems) {
            if (deleteFromLocalStorage(listItem) == 1) {
                listItem.setMarkedForDeletion(true);
                listItem.setStruckOut(false);
                successfullyMarkedForDeletionListItems.add(listItem);
            }
        }

        if (successfullyMarkedForDeletionListItems.size() == listItems.size()) {
            Timber.i("deleteFromLocalStorage(): Successfully marked all %d ListItems for deletion in SQLiteDb.",
                    successfullyMarkedForDeletionListItems.size());
        } else {
            Timber.e("deleteFromLocalStorage(): Only marked %d of of %d ListItems for deletion in SQLiteDb.",
                    successfullyMarkedForDeletionListItems.size(), listItems.size());
        }

        return successfullyMarkedForDeletionListItems;
    }

    @Override
    public int deleteFromLocalStorage(ListItem listItem) {
        int numberOfDeletedListItems = 0;
        try {
            Uri uri = ListItemsSqlTable.CONTENT_URI;
            String selection = ListItemsSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listItem.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(ListItemsSqlTable.COL_UPDATED, Calendar.getInstance().getTimeInMillis());
            cv.put(ListItemsSqlTable.COL_MARKED_FOR_DELETION, String.valueOf(TRUE));
            cv.put(ListItemsSqlTable.COL_STRUCK_OUT, String.valueOf(FALSE));
            numberOfDeletedListItems = cr.update(uri, cv, selection, selectionArgs);
            if (numberOfDeletedListItems == 1) {
                Timber.i("deleteFromLocalStorage(): Successfully marked \"%s\" for deletion in SQLiteDb.", listItem.getName());
            } else {
                Timber.e("deleteFromLocalStorage(): FAILED to marked \"%s\" for deletion in SQLiteDb.", listItem.getName());
            }

        } catch (Exception e) {
            Timber.e("delete(): Exception: %s.", e.getMessage());
        }

        return numberOfDeletedListItems;
    }

    @Override
    public void deleteFromCloud(List<ListItem> listItems) {
        new DeleteListItemsFromCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, listItems).execute();
    }

    @Override
    public void deleteFromCloud(ListItem listItem) {
        new DeleteListItemFromCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, listItem).execute();
    }

    @Override
    public void onListItemDeletedFromCloud(String successMessage) {
        Timber.i("onListItemDeletedFromCloud(): %s.", successMessage);
    }

    @Override
    public void onListItemDeleteFromCloudFailed(String errorMessage) {
        Timber.e("onListItemDeleteFromCloudFailed(): %s.", errorMessage);
    }

    @Override
    public void onListItemsDeletedFromCloud(String successMessage) {
        Timber.i("onListItemsDeletedFromCloud(): %s.", successMessage);
    }

    @Override
    public void onListItemsDeleteFromCloudFailed(String errorMessage) {
        Timber.e("onListItemsDeleteFromCloudFailed(): %s.", errorMessage);
    }


    //endregion


}
