package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.backendlessMessaging.ListThemeMessage;
import com.lbconsulting.a1list.backendlessMessaging.Messaging;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.DeleteListThemeFromCloud;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Locale;

/**
 * An interactor that saves the provided ListTheme to Backendless.
 */
public class DeleteListThemeFromCloud_InBackground extends AbstractInteractor implements DeleteListThemeFromCloud {
    private final Callback mCallback;
    private final ListTheme mListTheme;
    private final ListTheme mDefaultListTheme;

    public DeleteListThemeFromCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                                 Callback callback, ListTheme listTheme, ListTheme defaultListTheme) {
        super(threadExecutor, mainThread);
        mCallback = callback;
        mListTheme = listTheme;
        mDefaultListTheme = defaultListTheme;
    }


    @Override
    public void run() {
        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        try {
            // Delete ListTheme from Backendless.
            Backendless.Data.of(ListTheme.class).remove(mListTheme);

            // Delete ListTheme from local storage
            int numberOfListThemesDeletedFromLocalStorage = AndroidApplication.getListThemeRepository()
                    .deleteFromLocalStorage(mListTheme);

            // Send deleteFromStorage message to other devices.
            ListThemeMessage.sendMessage(mListTheme, Messaging.ACTION_DELETE);

            if (numberOfListThemesDeletedFromLocalStorage == 1) {
                String successMessage = String.format(Locale.getDefault(),
                        "Successfully deleted \"%s\" from both Backendless and the SQLiteDB.",
                        mListTheme.getName());
                postListThemeDeletedFromCloud(successMessage);
            } else {
                String errorMessage = String.format(Locale.getDefault(),
                        "Successfully deleted \"%s\" from Backendless BUT NOT from the SQLiteDB.",
                        mListTheme.getName());
                postListThemeDeletionFromCloudFailed(errorMessage);
            }

        } catch (BackendlessException e) {
            String errorMessage = String.format("FAILED to delete \"%s\" from Backendless. BackendlessException: %s",
                    mListTheme.getName(), e.getMessage());
            postListThemeDeletionFromCloudFailed(errorMessage);
        }
    }

    private void postListThemeDeletedFromCloud(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeDeletedFromCloud(successMessage);
            }
        });
    }

    private void postListThemeDeletionFromCloudFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeDeleteFromCloudFailed(errorMessage);
            }
        });
    }

}
