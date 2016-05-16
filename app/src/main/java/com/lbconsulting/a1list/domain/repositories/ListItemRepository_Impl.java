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
    private static ListTitleRepository_Impl mListTitleRepository;
    private final Context mContext;

    public ListItemRepository_Impl(Context context) {
        // private constructor
        this.mContext = context;
        mListTitleRepository = AndroidApplication.getListTitleRepository();
    }

    // CRUD operations

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

    public static ListItem listItemFromCursor(Cursor cursor) {
        ListItem listItem = new ListItem();


        String name = cursor.getString(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_NAME));
        listItem.setName(name);

        listItem.setSQLiteId(cursor.getLong(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_ID)));

        String objectId = cursor.getString(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_OBJECT_ID));
        if(objectId==null||objectId.isEmpty()){
            Timber.e("listItemFromCursor(): ListItem \"%s's\" objectId not set!", name);
        }
        listItem.setObjectId(objectId);
        listItem.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_UUID)));
        listItem.setListTitleUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_LIST_TITLE_UUID)));
        listItem.setUrlLink(cursor.getString(cursor.getColumnIndexOrThrow(ListItemsSqlTable.COL_URL_LINK)));
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

    //region Insert ListItem

    /**
     * TInserts a list of ListItem into local storage.
     * For each successfully inserted ListItem, this method inserts the ListItem into cloud storage.
     * @param listItems The list of ListItem to be stored.
     * @return Returns a list of ListItem successfully inserted into local storage.
     */
    @Override
    public List<ListItem> insertIntoStorage(List<ListItem> listItems) {
        List<ListItem> successfullyInsertedListItems = insertIntoLocalStorage(listItems);
        if (successfullyInsertedListItems.size() > 0) {
            insertIntoCloudStorage(successfullyInsertedListItems);
        }
        return successfullyInsertedListItems;
    }

    /**
     * Inserts a ListItem into local storage, and if successful, inserts the ListItem into cloud storage.
     *
     * @param listItem ListItem to be inserted into storage.
     * @return Returns TRUE if ListItem successfully inserted into local storage.
     */
    @Override
    public boolean insertIntoStorage(ListItem listItem) {
        boolean successfullySavedIntoLocalStorage = insertIntoLocalStorage(listItem);
        if (successfullySavedIntoLocalStorage) {
            insertIntoCloudStorage(listItem);
        }
        return successfullySavedIntoLocalStorage;
    }

    /**
     * Inserts a list of ListItem into local storage.
     * @param listItems The list of ListItem to be stored.
     * @return Returns a list of ListItem successfully stored in local storage.
     */
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

    /**
     * This method inserts a ListItem into local storage
     * with the ListItem’s local storage dirty flag set to TRUE.
     *
     * @param listItem ListItem to be inserted into local storage.
     * @return Returns TRUE if successful.
     */
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
            Timber.e("insertIntoLocalStorage(): ListItemRepository_Impl: FAILED to insertIntoStorage \"%s\" into the SQLite db.", listItem.getName());
        }

        return result;
    }

    /**
     * Inserts a list of ListItem into cloud storage.
     * For each successfully inserted ListItem, the method clears the local storage dirty flag and
     * sends an Insert message to other devices.
     * @param listItems List of ListItem for insertion.
     */
    @Override
    public void insertIntoCloudStorage(List<ListItem> listItems) {
        updateInCloudStorage(listItems, true);
    }

    /**
     * This method inserts a ListItem into cloud storage,
     * and if successful, clears the local storage dirty flag and
     * sends an Insert message to other devices.
     *
     * @param listItem ListItem to be inserted into cloud storage.
     */
    @Override
    public void insertIntoCloudStorage(ListItem listItem) {
        updateInCloudStorage(listItem, true);
    }
    //endregion

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
        Cursor cursor;
        Uri uri = ListItemsSqlTable.CONTENT_URI;
        String[] projection = ListItemsSqlTable.PROJECTION_ALL;

        String selection = ListItemsSqlTable.COL_LIST_TITLE_UUID + " = ? AND "
                + ListItemsSqlTable.COL_STRUCK_OUT + " = ?";
        String[] selectionArgs = new String[]{listTitle.getUuid(), String.valueOf(TRUE)};
        String sortOrder = ListItemsSqlTable.SORT_ORDER_NAME_ASC;

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

//    @Override
//    public int retrieveNumberOfStruckOutListItems(ListTitle listTitle) {
//        int struckOutListItems = 0;
//        Cursor cursor = null;
//        Uri uri = ListItemsSqlTable.CONTENT_URI;
//        String[] projection = new String[]{ListItemsSqlTable.COL_ID};
//        String selection = ListItemsSqlTable.COL_LIST_TITLE_UUID + " = ? AND "
//                + ListItemsSqlTable.COL_STRUCK_OUT + " = ?";
//        String[] selectionArgs = new String[]{listTitle.getUuid(), String.valueOf(TRUE)};
//        String sortOrder = null;
//        try {
//            ContentResolver cr = mContext.getContentResolver();
//            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
//            if (cursor != null) {
//                struckOutListItems = cursor.getCount();
//                cursor.close();
//            }
//
//        } catch (Exception e) {
//            Timber.e("retrieveNumberOfStruckOutListItems(): Exception: %s.", e.getMessage());
//        }
//
//        return struckOutListItems;
//    }

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
//        mListTitleRepository.updateStorage(listTitle);
//    }

    private ListItem getListItem(ListTitle listTitle, String listItemName) {
        // TODO: Does this getListItem method's selection need ListItem not marked for deletion. How to handle favorites.
        ListItem result = null;
        Cursor cursor;
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

    /**
     * Updates a list of ListItem in local and cloud storage.
     * @param listItems List of ListItem to be updated in local and cloud storage.
     */
    @Override
    public void updateStorage(List<ListItem> listItems) {
        List<ListItem> successfullyUpdatedListItemsInLocalStorage = updateInLocalStorage(listItems);
        if (successfullyUpdatedListItemsInLocalStorage.size() > 0) {
            updateInCloudStorage(successfullyUpdatedListItemsInLocalStorage, false);
        }
    }

    /**
     * This method updates a ListItem in local storage,
     * and if successful, updates the ListItem in cloud storage.
     *
     * @param listItem ListItem to be updated.
     */
    @Override
    public void updateStorage(ListItem listItem) {
        if (updateInLocalStorage(listItem) == 1) {
            updateInCloudStorage(listItem, false);
        }
    }

    /**
     * This method updates a list of ListItem in local storage.
     * @param listItems The list of ListItem to be updated.
     * @return Returns a list of ListItem successfully updated in local storage.
     */

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


    /**
     * This method updates a ListItem in local storage and sets the its
     * local storage dirty flag set to TRUE and sets its updateStorage time to
     * the device’s current time.
     *
     * @param listItem ListItem to be updated.
     * @return Returns the number of updated records.
     */
    @Override
    public int updateInLocalStorage(ListItem listItem) {
        listItem.setUpdated(Calendar.getInstance().getTime());
        ContentValues cv = makeListItemContentValues(listItem);
        int numberOfRecordsUpdated = updateInLocalStorage(listItem, cv);
        if (numberOfRecordsUpdated == 1) {
            Timber.i("updateInLocalStorage(): Successfully updated \"%s\" in the SQLiteDb.", listItem.getName());
        } else {
            Timber.e("updateInLocalStorage(): FAILED to updateStorage \"%s\" in the SQLiteDb.", listItem.getName());
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

    /**
     * This method updates the provided list of ListItem in cloud storage via a background thread.
     * For successfully updated ListItem, the method clears the local storage dirty flag and
     * sends an Insert message to other devices.
     * @param listItems List of ListItem to be updated
     * @param isNew TRUE if the ListItems are new and are to be inserted into cloud storage.
     */
    @Override
    public void updateInCloudStorage(List<ListItem> listItems, boolean isNew) {
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
                Timber.e("updateInCloudStorage(): Unable to updateStorage \"%s\" in the Cloud. No Backendless objectId available!",
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
//        EventBus.getDefault().post(new MyEvents.mainActivityPresenterResume());
    }

    @Override
    public void onListItemListSaveToBackendlessFailed(String errorMessage, List<ListItem> successfullySavedListItems) {
        Timber.e("onListItemListSaveToBackendlessFailed(): %s", errorMessage);
    }

    /**
     * Updates a ListItem in cloud storage via a background thread,
     * and if successful, calls the Clear Local Storage Dirty Flag method and
     * sends an Update message to other devices.
     *
     * @param listItem ListItem to be updated in cloud storage.
     * @param isNew    TRUE for new ListItems to be inserted into cloud storage.
     */
    @Override
    public void updateInCloudStorage(ListItem listItem, boolean isNew) {
        if (listItem != null) {
            // If the listItem is not new ... make sure that it has a Backendless objectId.
            if (!isNew) {
                if (listItem.getObjectId() == null || listItem.getObjectId().isEmpty()) {
                    ListItem existingListItem = retrieveListItemByUuid(listItem.getUuid());
                    listItem.setObjectId(existingListItem.getObjectId());
                }
                if (listItem.getObjectId() == null || listItem.getObjectId().isEmpty()) {
                    // The listItem is not new AND there is no Backendless objectId available ... so,
                    // Unable to updateStorage the listItem in Backendless
                    Timber.e("updateInCloudStorage(): Unable to updateStorage \"%s\" in the Cloud. No Backendless objectId available!",
                            listItem.getName());
                    return;
                }
            }
            new SaveListItemToCloud_InBackground(ThreadExecutor.getInstance(),
                    MainThreadImpl.getInstance(), this, listItem).execute();
        } else {
            Timber.e("updateInCloudStorage(): Unable to updateStorage ListItem in Cloud. The Provided ListItem is null!");
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


    //region Delete ListItem
    //    The general deletion process:
    //    i)	First set the the ListItem's deleteFromStorage flag in local storage;
    //    ii)	Second, if ListItem is a Favorite then updateStorage the ListItem in cloud storage,
    //          otherwise deleteFromStorage the ListItem in cloud storage;
    //    iii)	Third, if ListItem successfully deleted from cloud storage then deleteFromStorage from local storage.

    /**
     * This method deletes the provide list of ListItem from local and cloud storage.
     * @param listItems List of ListItem to be deleted.
     * @return returns the number of ListItems successfully marked for deletion in local storage.
     */
    @Override
    public int deleteFromStorage(List<ListItem> listItems) {
        List<ListItem> successfullyMarkedForDeletionListItems = setDeleteFlagInLocalStorage(listItems);
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
            updateInCloudStorage(listItemsForCloudUpdate, false);
        }

        if (listItemsForCloudDeletion.size() > 0) {
            deleteFromCloudStorage(listItemsForCloudDeletion);
        }

        return successfullyMarkedForDeletionListItems.size();
    }

    /**
     * This method first calls the Set Delete Flag in Local Storage method,
     * and if successful, calls either the Update in Cloud Storage method if the ListItem
     * is a Favorite otherwise it calls the Delete from Cloud Storage method.
     *
     * @param listItem ListItem to be deleted.
     * @return Returns the number of ListItems that were marked for deletion in local storage.
     */
    @Override
    public int deleteFromStorage(ListItem listItem) {
        int numberListItemsMarkedForDeletion = setDeleteFlagInLocalStorage(listItem);
        if (numberListItemsMarkedForDeletion == 1) {
            if (listItem.isFavorite()) {
                listItem.setMarkedForDeletion(true);
                listItem.setStruckOut(false);
                updateInCloudStorage(listItem, false);
            } else {
                deleteFromCloudStorage(listItem);
            }
        }

        return numberListItemsMarkedForDeletion;
    }

    /**
     * This method sets a list of ListItems deleteFromStorage flags' to TRUE,
     * for each successfully marked ListItem sets its struck out flag to FALSE and
     * sets sets the updated time to the device’s current time.
     *
     * @param listItems List of ListItem
     * @return Returns List of ListItem successfully marked for deletion in local storage.
     */
    @Override
    public List<ListItem> setDeleteFlagInLocalStorage(List<ListItem> listItems) {
        List<ListItem> listItemsWithDeleteFlagSet = new ArrayList<>();
        for (ListItem listItem : listItems) {
            int numberOfUpdatedRecords = setDeleteFlagInLocalStorage(listItem);
            if (numberOfUpdatedRecords == 1) {
                listItem.setMarkedForDeletion(true);
                listItem.setStruckOut(false);
                listItem.setUpdated(Calendar.getInstance().getTime());
                listItemsWithDeleteFlagSet.add(listItem);
            }
        }
        return listItemsWithDeleteFlagSet;
    }

    /**
     * This method
     * sets the ListItem’s local storage deleteFromStorage flag to TRUE,
     * sets the ListItem’s local storage dirty flag set to TRUE,
     * sets the ListItem's local storage struck out flag to FALSE, and
     * sets the updated time to the device’s current time.
     *
     * @param listItem ListItem to be marked for deletion.
     * @return Returns the number of updated ListItem records
     */
    @Override
    public int setDeleteFlagInLocalStorage(ListItem listItem) {
        int numberOfMarkedListItems = 0;
        try {
            Uri uri = ListItemsSqlTable.CONTENT_URI;
            String selection = ListItemsSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listItem.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(ListItemsSqlTable.COL_MARKED_FOR_DELETION, String.valueOf(TRUE));
            cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, String.valueOf(TRUE));
            cv.put(ListItemsSqlTable.COL_STRUCK_OUT, String.valueOf(FALSE));
            cv.put(ListItemsSqlTable.COL_UPDATED, Calendar.getInstance().getTimeInMillis());
            numberOfMarkedListItems = cr.update(uri, cv, selection, selectionArgs);
            if (numberOfMarkedListItems == 1) {
                Timber.i("setDeleteFlagInLocalStorage(): Successfully marked \"%s\" for deletion in SQLiteDb.", listItem.getName());
            } else {
                Timber.e("setDeleteFlagInLocalStorage(): FAILED to marked \"%s\" for deletion in SQLiteDb.", listItem.getName());
            }

        } catch (Exception e) {
            Timber.e("deleteFromStorage(): Exception: %s.", e.getMessage());
        }

        return numberOfMarkedListItems;

//        List<ListItem> successfullyMarkedForDeletionListItems = new ArrayList<>();
//        for (ListItem listItem : listItems) {
//            if (deleteFromLocalStorage(listItem) == 1) {
//                listItem.setMarkedForDeletion(true);
//                listItem.setStruckOut(false);
//                successfullyMarkedForDeletionListItems.add(listItem);
//            }
//        }
//
//        if (successfullyMarkedForDeletionListItems.size() == listItems.size()) {
//            Timber.i("deleteFromLocalStorage(): Successfully marked all %d ListItems for deletion in SQLiteDb.",
//                    successfullyMarkedForDeletionListItems.size());
//        } else {
//            Timber.e("deleteFromLocalStorage(): Only marked %d of of %d ListItems for deletion in SQLiteDb.",
//                    successfullyMarkedForDeletionListItems.size(), listItems.size());
//        }
//
//        return successfullyMarkedForDeletionListItems;
    }

    /**
     * This method deletes a list of ListItem from local storage.
     *
     * @param listItems ListItems to be deleted
     * @return Return a list of ListItem that were deleted from local storage.
     */
    @Override
    public List<ListItem> deleteFromLocalStorage(List<ListItem> listItems) {
        List<ListItem> listItemsDeletedFromLocalStorage = new ArrayList<>();
        for (ListItem listItem : listItems) {
            if (deleteFromLocalStorage(listItem) == 1) {
                listItemsDeletedFromLocalStorage.add(listItem);
            }
        }
        return listItemsDeletedFromLocalStorage;
    }

    /**
     * This method deletes a ListItem from local storage.
     *
     * @param listItem ListItem to be deleted.
     * @return Returns the number of deleted ListItems.
     */
    @Override
    public int deleteFromLocalStorage(ListItem listItem) {
        int numberOfDeletedListItems = 0;
        try {
            Uri uri = ListItemsSqlTable.CONTENT_URI;
            String selection = ListItemsSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listItem.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            numberOfDeletedListItems = cr.delete(uri, selection, selectionArgs);
            if (numberOfDeletedListItems == 1) {
                Timber.i("deleteFromLocalStorage(): Successfully deleted \"%s\" from the SQLiteDb.", listItem.getName());
            } else {
                Timber.e("deleteFromLocalStorage(): FAILED to deleteFromStorage \"%s\" from the SQLiteDb.", listItem.getName());
            }

        } catch (Exception e) {
            Timber.e("deleteFromStorage(): Exception: %s.", e.getMessage());
        }

        return numberOfDeletedListItems;
    }

    /**
     * This method
     * clears the local storage dirty flag,
     * sets the ListItem's cloud Object ID, and
     * updates the ListItem’s modified time to that provided by cloud storage.
     *
     * @param listItem ListItem successfully stored in the cloud.
     * @return Returns the number of updated records in local storage.
     */
    @Override
    public int clearLocalStorageDirtyFlag(ListItem listItem) {
        ContentValues cv = new ContentValues();
        Date updatedDate = listItem.getUpdated();
        if (updatedDate == null) {
            updatedDate = listItem.getCreated();
        }
        if (updatedDate != null) {
            long updated = updatedDate.getTime();
            cv.put(ListItemsSqlTable.COL_UPDATED, updated);
        }

        cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, FALSE);
        cv.put(ListItemsSqlTable.COL_OBJECT_ID, listItem.getObjectId());

        return updateInLocalStorage(listItem, cv);
    }

    @Override
    public int clearAllData() {
        int numberOfDeletedListItems = 0;
        try {
            Uri uri = ListItemsSqlTable.CONTENT_URI;
            String selection = null;
            String[] selectionArgs = null;
            ContentResolver cr = mContext.getContentResolver();
            numberOfDeletedListItems = cr.delete(uri, selection, selectionArgs);
            Timber.i("clearAllData(): Successfully deleted %d ListItems from the SQLiteDb.", numberOfDeletedListItems);

        } catch (Exception e) {
            Timber.e("clearAllData(): Exception: %s.", e.getMessage());
        }

        return numberOfDeletedListItems;
    }

    /**
     * This method deletes a list of ListItems in cloud storage via a background thread,
     * and for each successfully deleted ListItem calls the Delete from Local Storage method and
     * sends a Delete message to other devices.
     *
     * @param listItems List of ListItems to be deleted from cloud storage.
     */
    @Override
    public void deleteFromCloudStorage(List<ListItem> listItems) {
        new DeleteListItemsFromCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, listItems).execute();
    }

    /**
     * This method deletes the ListItem in cloud storage via a background thread,
     * and if successful, calls the Delete from Local Storage method and
     * sends a Delete message to other devices.
     *
     * @param listItem ListItem to be deleted from cloud storage.
     */
    @Override
    public void deleteFromCloudStorage(ListItem listItem) {
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
