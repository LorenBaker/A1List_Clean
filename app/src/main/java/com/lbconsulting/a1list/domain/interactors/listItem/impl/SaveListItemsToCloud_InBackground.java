package com.lbconsulting.a1list.domain.interactors.listItem.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.backendlessMessaging.ListItemMessage;
import com.lbconsulting.a1list.backendlessMessaging.Messaging;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.SaveListItemsToCloud;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.storage.ListItemsSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * An interactor that saves the provided ListItem to Backendless.
 */
public class SaveListItemsToCloud_InBackground extends AbstractInteractor implements SaveListItemsToCloud {
    private final Callback mCallback;
    private final List<ListItem> mListItemList;

    public SaveListItemsToCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                             Callback callback, List<ListItem> listItemList) {
        super(threadExecutor, mainThread);
        mListItemList = listItemList;
        mCallback = callback;
    }


    @Override
    public void run() {
        // saveListItemToBackendless
        ListItem response;
        int TRUE = 1;
        int FALSE = 0;

        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }
        if (mListItemList == null) {
            Timber.e("run(): Unable to save listItemList. listItemList is null!");
            return;
        }
        if (mListItemList.size() == 0) {
            Timber.e("run(): No ListItems to save!");
            return;
        }

        List<ListItem> successfullySavedListItems = new ArrayList<>();

        for (ListItem listItem : mListItemList) {

            // Check if the ListTitle associated with the ListItem has been saved in Backendless
            ListTitle listTitle = listItem.retrieveListTitle();
            String listTitleObjectID = listTitle.getObjectId();
            if (listTitleObjectID == null || listTitleObjectID.isEmpty()) {
                // The ListTitle has not been saved to Backendless ... so
                // Retrieve the ListTitle from local storage and check again.
                listTitle = AndroidApplication.getListTitleRepository().retrieveListTitleByUuid(listTitle.getUuid());
                if (listTitleObjectID == null || listTitleObjectID.isEmpty()) {
                    // The ListTitle from the Local Storage has NOT been saved to Backendless ... so
                    // The ListItem cannot be saved to Backendless.
                    Timber.i("run(): Cannot save ListItem \"%s\" to Backendless because ListTitle \"%s\" has not previously been saved to Backendless.",
                            listItem.getName(), listTitle.getName());
                    // continue with the next ListItem
                    continue;
                } else {
                    // The ListTitle from the Local Storage has been saved to Backendless ... so
                    // associated it with the ListItem
                    listItem.setListTitleUuid(listTitle.getUuid());
                }
            }

            // saveListItemToBackendless
            String objectId = listItem.getObjectId();
            boolean isNew = objectId == null || objectId.isEmpty();
            try {
                response = Backendless.Data.of(ListItem.class).save(listItem);
                successfullySavedListItems.add(response);
                try {
                    // Update the SQLite db: set dirty to false, and updated date and time
                    ContentValues cv = new ContentValues();
                    Date updatedDate = response.getUpdated();
                    if (updatedDate == null) {
                        updatedDate = response.getCreated();
                    }
                    if (updatedDate != null) {
                        long updated = updatedDate.getTime();
                        cv.put(ListItemsSqlTable.COL_UPDATED, updated);
                    }

                    cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, FALSE);

                    // If a new ListItem, updateStorage SQLite db with objectID
                    if (isNew) {
                        cv.put(ListItemsSqlTable.COL_OBJECT_ID, response.getObjectId());
                    }
                    // updateStorage the SQLite db
                    updateSQLiteDb(response, cv);

                    // send message to other devices
                    int action = Messaging.ACTION_UPDATE;
                    if(isNew){
                        action = Messaging.ACTION_CREATE;
                    }
                    ListItemMessage.sendMessage(listItem, action);

                    String successMessage = String.format("Successfully saved \"%s\" to Backendless.", response.getName());
                    Timber.i("run(): %s", successMessage);

                } catch (Exception e) {
                    // Set dirty flag to true in SQLite db
                    ContentValues cv = new ContentValues();
                    cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, TRUE);
                    updateSQLiteDb(listItem, cv);

                    String errorMessage = String.format("saveListItemToBackendless(): \"%s\" FAILED to save to Backendless. Exception: %s",
                            listItem.getName(), e.getMessage());
                    Timber.e("run(): %s", errorMessage);
                }

            } catch (BackendlessException e) {
                String errorMessage = String.format("FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
                        listItem.getName(), e.getCode(), e.getMessage());
                Timber.e("run(): %s", errorMessage);
            }
        }


        if (mListItemList.size() == successfullySavedListItems.size()) {
            String successMessage = String.format("Successfully saved %d ListItems to Backendless.", successfullySavedListItems.size());
            postListItemListSavedToBackendless(successMessage, successfullySavedListItems);
        } else {
            String errorMessage = String.format("Only saved %d out of %d ListItems to Backendless",
                    successfullySavedListItems.size(), mListItemList.size());
            postListItemListSaveToBackendlessFailed(errorMessage, successfullySavedListItems);
        }

    }

    private void updateSQLiteDb(ListItem listItem, ContentValues cv) {
        int numberOfRecordsUpdated = 0;
        try {
            Uri uri = ListItemsSqlTable.CONTENT_URI;
            String selection = ListItemsSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listItem.getUuid()};
            ContentResolver cr = AndroidApplication.getContext().getContentResolver();
            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("updateInLocalStorage(): Exception: %s.", e.getMessage());
        }
        if (numberOfRecordsUpdated != 1) {
            Timber.e("updateInLocalStorage(): Error updating ListItem with uuid = %s", listItem.getUuid());
        }
    }

    private void postListItemListSavedToBackendless(final String successMessage, final List<ListItem> successfullySavedListItems) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemListSavedToBackendless(successMessage, successfullySavedListItems);
            }
        });
    }

    private void postListItemListSaveToBackendlessFailed(final String errorMessage, final List<ListItem> successfullySavedListItems) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemListSaveToBackendlessFailed(errorMessage, successfullySavedListItems);
            }
        });
    }

}
