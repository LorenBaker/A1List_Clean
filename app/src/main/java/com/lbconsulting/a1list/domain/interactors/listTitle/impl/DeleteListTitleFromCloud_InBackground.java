package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.backendlessMessaging.ListTitleMessage;
import com.lbconsulting.a1list.backendlessMessaging.ListTitlePositionMessage;
import com.lbconsulting.a1list.backendlessMessaging.Messaging;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.DeleteListTitleFromCloud;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.model.ListTitlePosition;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_Impl;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Locale;

/**
 * An interactor that saves the provided ListTitle to Backendless.
 */
public class DeleteListTitleFromCloud_InBackground extends AbstractInteractor implements DeleteListTitleFromCloud {
    private final Callback mCallback;
    private final ListTitle mListTitle;
    private final ListTitlePosition mListTitlePosition;

    public DeleteListTitleFromCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                                 Callback callback, ListTitle listTitle, ListTitlePosition listTitlePosition) {
        super(threadExecutor, mainThread);
        mCallback = callback;
        mListTitle = listTitle;
        mListTitlePosition = listTitlePosition;
    }


    @Override
    public void run() {

        ListTitle response;
        int TRUE = 1;
        int FALSE = 0;

        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        ListTitleRepository_Impl listTitleRepository = AndroidApplication.getListTitleRepository();
        try {
            // Delete ListTitle and its ListTitlePosition from cloud storage.
            Backendless.Data.of(ListTitle.class).remove(mListTitle);
            Backendless.Data.of(ListTitlePosition.class).remove(mListTitlePosition);

            // Delete ListTitle  and its ListTitlePosition from local storage
            int numberOfDeletedListTitlesFromLocalStorage = listTitleRepository.deleteFromLocalStorage(mListTitle);
            int numberOfDeletedListTitlePositionsFromLocalStorage = listTitleRepository.deleteFromLocalStorage(mListTitle, mListTitlePosition);

            // Send deleteFromStorage message to other devices.
            ListTitleMessage.sendMessage(mListTitle, Messaging.ACTION_DELETE);
            ListTitlePositionMessage.sendMessage(mListTitle, mListTitlePosition, Messaging.ACTION_DELETE);

            if (numberOfDeletedListTitlesFromLocalStorage == 1) {
                String successMessage = String.format(Locale.getDefault(),
                        "Successfully deleted \"%s\" from both Backendless and the SQLiteDB.",
                        mListTitle.getName());
                postListTitleDeletedFromBackendless(successMessage);
            } else {
                String errorMessage = String.format(Locale.getDefault(),
                        "Successfully deleted \"%s\" from Backendless BUT NOT from the SQLiteDB.",
                        mListTitle.getName());
                postListTitleDeletionFromBackendlessFailed(errorMessage);
            }

            if (numberOfDeletedListTitlePositionsFromLocalStorage == 1) {
                String successMessage = String.format(Locale.getDefault(),
                        "Successfully deleted \"%s's\" ListTitlePosition from both Backendless and the SQLiteDB.",
                        mListTitle.getName());
                postListTitleDeletedFromBackendless(successMessage);
            } else {
                String errorMessage = String.format(Locale.getDefault(),
                        "Successfully deleted \"%s's\" ListTitlePosition from Backendless BUT NOT from the SQLiteDB.",
                        mListTitle.getName());
                postListTitleDeletionFromBackendlessFailed(errorMessage);
            }


        } catch (BackendlessException e) {
            String errorMessage = String.format(Locale.getDefault(),
                    "FAILED to deleted \"%s\" from Backendless. BackendlessException: %s",
                    mListTitle.getName(), e.getMessage());
            postListTitleDeletionFromBackendlessFailed(errorMessage);
        }
    }

    private void postListTitleDeletedFromBackendless(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleDeletedFromBackendless(successMessage);
            }
        });
    }

    private void postListTitleDeletionFromBackendlessFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleDeleteFromBackendlessFailed(errorMessage);
            }
        });
    }

}
