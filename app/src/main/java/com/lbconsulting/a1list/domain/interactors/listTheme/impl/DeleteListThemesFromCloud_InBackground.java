package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.backendlessMessaging.ListThemeMessage;
import com.lbconsulting.a1list.backendlessMessaging.Messaging;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.DeleteListThemesFromCloud;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.List;
import java.util.Locale;

/**
 * An interactor that saves the provided ListTheme to Backendless.
 */
public class DeleteListThemesFromCloud_InBackground extends AbstractInteractor implements DeleteListThemesFromCloud {
    private final Callback mCallback;
    private final List<ListTheme> mListThemes;

    public DeleteListThemesFromCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                                  Callback callback, List<ListTheme> listThemes) {
        super(threadExecutor, mainThread);
        mListThemes = listThemes;
        mCallback = callback;
    }

    @Override
    public void run() {

        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        for (ListTheme listTheme : mListThemes) {
            try {
                // Delete ListTheme from Backendless.
                Backendless.Data.of(ListTheme.class).remove(listTheme);

                // Delete ListTheme from local storage
                int numberOfListThemesDeletedFromLocalStorage = AndroidApplication.getListThemeRepository()
                        .deleteFromLocalStorage(listTheme);

                // Send deleteFromStorage message to other devices.
                ListThemeMessage.sendMessage(listTheme, Messaging.ACTION_DELETE);

                if (numberOfListThemesDeletedFromLocalStorage == 1) {
                    String successMessage = String.format(Locale.getDefault(),
                            "Successfully deleted \"%s\" from both Backendless and the SQLiteDB.",
                            listTheme.getName());
                    postListThemesDeletedFromCloud(successMessage);
                } else {
                    String errorMessage = String.format(Locale.getDefault(),
                            "Successfully deleted \"%s\" from Backendless BUT NOT from the SQLiteDB.",
                            listTheme.getName());
                    postListThemesDeletionFromCloudFailed(errorMessage);
                }

            } catch (BackendlessException e) {
                String errorMessage = String.format("FAILED to delete \"%s\" from Backendless. BackendlessException: %s",
                        listTheme.getName(), e.getMessage());
                postListThemesDeletionFromCloudFailed(errorMessage);
            }
        }
    }

    private void postListThemesDeletedFromCloud(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemesDeletedFromCloud(successMessage);
            }
        });
    }

    private void postListThemesDeletionFromCloudFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemesDeleteFromCloudFailed(errorMessage);
            }
        });
    }

}
