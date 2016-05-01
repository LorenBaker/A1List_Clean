package com.lbconsulting.a1list.domain.interactors.listItem.impl;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.backendlessMessaging.ListItemMessage;
import com.lbconsulting.a1list.backendlessMessaging.Messaging;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.SaveListItemToCloud;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.utils.CommonMethods;

import timber.log.Timber;

/**
 * An interactor that saves the provided ListItem to Backendless.
 */
public class SaveListItemToCloud_InBackground extends AbstractInteractor implements SaveListItemToCloud {
    private final Callback mCallback;
    private final ListItem mListItem;

    public SaveListItemToCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                            Callback callback, ListItem listItem) {
        super(threadExecutor, mainThread);
        mListItem = listItem;
        mCallback = callback;
    }


    @Override
    public void run() {
        int TRUE = 1;
        int FALSE = 0;

        ListItem response;

        if (mListItem == null) {
            Timber.e("run(): Unable to save ListItem. ListItem is null!");
            return;
        }
        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        // Check if the ListTitle associated with the ListItem has been saved in Backendless
        ListTitle listTitle = mListItem.retrieveListTitle();
        String listTitleObjectID = listTitle.getObjectId();
        if (listTitleObjectID == null || listTitleObjectID.isEmpty()) {
            // The ListTitle has not been saved to Backendless ... so
            // Retrieve the ListTitle from local storage and check again.
            listTitle = AndroidApplication.getListTitleRepository().retrieveListTitleByUuid(listTitle.getUuid());
            if (listTitleObjectID == null || listTitleObjectID.isEmpty()) {
                // The ListTitle from the Local Storage has NOT been saved to Backendless ... so
                // The ListItem cannot be saved to Backendless.
                Timber.e("run(): Cannot save ListItem \"%s\" to Backendless because ListTitle \"%s\" has not previously been saved to Backendless.",
                        mListItem.getName(), listTitle.getName());
                // Do not continue.
                return;
            } else {
                // The ListTitle from the Local Storage has been saved to Backendless ... so
                // associated it with the ListItem
                mListItem.setListTitleUuid(listTitle.getUuid());
            }
        }

        // saveListItemToBackendless
        String objectId = mListItem.getObjectId();
        boolean isNew = objectId == null || objectId.isEmpty();
        try {
            response = Backendless.Data.of(ListItem.class).save(mListItem);
            int numberOfClearedRecords = AndroidApplication.getListItemRepository().clearLocalStorageDirtyFlag(response);

            // send message to other devices
            int action = Messaging.ACTION_UPDATE;
            if (isNew) {
                action = Messaging.ACTION_CREATE;
            }
            ListItemMessage.sendMessage(mListItem, action);

            String successMessage = String.format("Successfully saved \"%s\" to Backendless.", response.getName());
            if (numberOfClearedRecords != 1) {
                successMessage = successMessage + " But FAILED to clear local storage dirty flag!";
            }
            postListItemSavedToBackendless(successMessage);

        } catch (BackendlessException e) {

            String errorMessage = String.format("FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
                    mListItem.getName(), e.getCode(), e.getMessage());
            postListItemSaveToBackendlessFailed(errorMessage);
        }
    }


//    private void updateSQLiteDb(ListItem listItem, ContentValues cv) {
//        int numberOfRecordsUpdated = 0;
//        try {
//            Uri uri = ListItemsSqlTable.CONTENT_URI;
//            String selection = ListItemsSqlTable.COL_UUID + " = ?";
//            String[] selectionArgs = new String[]{listItem.getUuid()};
//            ContentResolver cr = AndroidApplication.getContext().getContentResolver();
//            numberOfRecordsUpdated = cr.updateStorage(uri, cv, selection, selectionArgs);
//
//        } catch (Exception e) {
//            Timber.e("updateInLocalStorage(): Exception: %s.", e.getMessage());
//        }
//        if (numberOfRecordsUpdated != 1) {
//            Timber.e("updateInLocalStorage(): Error updating ListItem with uuid = %s", listItem.getUuid());
//        }
//    }

    private void postListItemSavedToBackendless(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemSavedToBackendless(successMessage);
            }
        });
    }

    private void postListItemSaveToBackendlessFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemSaveToBackendlessFailed(errorMessage);
            }
        });
    }

}
