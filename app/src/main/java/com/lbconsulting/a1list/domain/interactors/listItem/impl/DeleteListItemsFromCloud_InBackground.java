package com.lbconsulting.a1list.domain.interactors.listItem.impl;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.backendlessMessaging.ListItemMessage;
import com.lbconsulting.a1list.backendlessMessaging.Messaging;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.DeleteListItemsFromCloud;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository_Impl;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * An interactor that saves the provided ListItem to Backendless.
 */
public class DeleteListItemsFromCloud_InBackground extends AbstractInteractor implements DeleteListItemsFromCloud {
    private final Callback mCallback;
    private final List<ListItem> mListItems;

    public DeleteListItemsFromCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                                 Callback callback, List<ListItem> listItems) {
        super(threadExecutor, mainThread);
        mListItems = listItems;
        mCallback = callback;
    }


    @Override
    public void run() {

        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        List<ListItem> successfullyRemovedListItems = new ArrayList<>();
        ListItemRepository_Impl listItemRepository = AndroidApplication.getListItemRepository();
        for (ListItem listItem : mListItems) {
            try {
                // Delete ListItem from Backendless.
                Backendless.Data.of(ListItem.class).remove(listItem);
                Timber.i("run(): Successfully deleted \"%s\" from Backendless.", listItem.getName());
                successfullyRemovedListItems.add(listItem);

            } catch (BackendlessException e) {
                Timber.e("run(): BackendlessException: FAILED to deleted \"%s\" from Backendless. BackendlessException: %s",
                        listItem.getName(), e.getMessage());
            }
        }

        // Delete successfully removed ListItems from local storage.
        List<ListItem> listItemsDeletedFromLocalStorage = listItemRepository.deleteFromLocalStorage(successfullyRemovedListItems);

        // Send deleteFromStorage messages to other devices.
        for (ListItem removedListItem : successfullyRemovedListItems) {
            ListItemMessage.sendMessage(removedListItem, Messaging.ACTION_DELETE);
        }

        // Post results
        if (mListItems.size() == successfullyRemovedListItems.size() && mListItems.size() == listItemsDeletedFromLocalStorage.size()) {
            String successMessage =
                    String.format("Successfully deleted all %d ListItems from both Backendless and the SQLiteDb.",
                            mListItems.size());
            postListItemsDeletedFromBackendless(successMessage);
        } else if (mListItems.size() == successfullyRemovedListItems.size()) {
            // Failed to deleteFromStorage some listItems from local storage.
            String errorMessage = String.format("Successfully deleted all %d ListItems from Backendless BUT only %d ListItems from the SQLiteDB.",
                    mListItems.size(), listItemsDeletedFromLocalStorage.size());
            postListItemDeletionFromBackendlessFailed(errorMessage);
        } else if (successfullyRemovedListItems.size() == listItemsDeletedFromLocalStorage.size()) {
            // Failed to deleteFromStorage some listItems from cloud storage.
            String errorMessage = String.format("Only deleted %d of %d ListItems from Backendless and the SQLiteDB.",
                    listItemsDeletedFromLocalStorage.size(), mListItems.size());
            postListItemDeletionFromBackendlessFailed(errorMessage);
        } else {
            // Failed to deleteFromStorage some listItems from cloud storage. And from those successfully deleted
            // from cloud storage, Failed to deleteFromStorage some from local storage.
            String errorMessage = String.format("Only deleted %d of %d ListItems from Backendless AND Only deleted %d of %d ListItems from SQLiteDb",
                    successfullyRemovedListItems.size(), mListItems.size(), listItemsDeletedFromLocalStorage.size(), mListItems.size());
            postListItemDeletionFromBackendlessFailed(errorMessage);
        }
    }

    private void postListItemsDeletedFromBackendless(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemsDeletedFromCloud(successMessage);
            }
        });
    }

    private void postListItemDeletionFromBackendlessFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemsDeleteFromCloudFailed(errorMessage);
            }
        });
    }

}
