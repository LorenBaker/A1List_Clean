package com.lbconsulting.a1list.domain.interactors.listItem.impl;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.backendlessMessaging.ListItemMessage;
import com.lbconsulting.a1list.backendlessMessaging.Messaging;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.DeleteListItemFromCloud;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository_Impl;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Locale;

/**
 * An interactor that saves the provided ListItem to Backendless.
 */
public class DeleteListItemFromCloud_InBackground extends AbstractInteractor implements DeleteListItemFromCloud {
    private final Callback mCallback;
    private final ListItem mListItem;

    public DeleteListItemFromCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                                Callback callback, ListItem listItem) {
        super(threadExecutor, mainThread);
        mListItem = listItem;
        mCallback = callback;
    }


    @Override
    public void run() {

        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        ListItemRepository_Impl listItemRepository = AndroidApplication.getListItemRepository();
        try {
            // Delete ListItem from cloud storage.
            Backendless.Data.of(ListItem.class).remove(mListItem);

            // Delete ListItem from local storage
            int numberOfListItemsDeletedFromLocalStorage = listItemRepository.deleteFromLocalStorage(mListItem);

            // Send deleteFromStorage message to other devices.
            ListItemMessage.sendMessage(mListItem, Messaging.ACTION_DELETE);

            if (numberOfListItemsDeletedFromLocalStorage == 1) {
                String successMessage = String.format(Locale.getDefault(),
                        "Successfully deleted \"%s\" from both Backendless and the SQLiteDB.",
                        mListItem.getName());
                postListItemDeletedFromBackendless(successMessage);
            } else {
                String errorMessage = String.format(Locale.getDefault(),
                        "Successfully deleted \"%s\" from Backendless BUT NOT from the SQLiteDB.",
                        mListItem.getName());
                postListItemDeletionFromBackendlessFailed(errorMessage);
            }


        } catch (BackendlessException e) {
            String errorMessage = String.format(Locale.getDefault(),
                    "FAILED to deleted \"%s\" from Backendless. BackendlessException: %s",
                    mListItem.getName(), e.getMessage());
            postListItemDeletionFromBackendlessFailed(errorMessage);
        }
    }

    private void postListItemDeletedFromBackendless(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemDeletedFromCloud(successMessage);
            }
        });
    }

    private void postListItemDeletionFromBackendlessFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemDeleteFromCloudFailed(errorMessage);
            }
        });
    }

}
