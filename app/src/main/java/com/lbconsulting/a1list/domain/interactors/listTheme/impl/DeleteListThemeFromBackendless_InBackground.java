package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import android.content.ContentResolver;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.DeleteListThemeFromBackendless;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.storage.ListThemesSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;

/**
 * An interactor that saves the provided ListTheme to Backendless.
 */
public class DeleteListThemeFromBackendless_InBackground extends AbstractInteractor implements DeleteListThemeFromBackendless {
    private final Callback mCallback;
    private final ListTheme mListTheme;

    public DeleteListThemeFromBackendless_InBackground(Executor threadExecutor, MainThread mainThread,
                                                       ListTheme listTheme, Callback callback) {
        super(threadExecutor, mainThread);
        mListTheme = listTheme;
        mCallback = callback;
    }


    @Override
    public void run() {

        ListTheme response;
        int TRUE = 1;
        int FALSE = 0;

        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        try {
            // Delete ListTheme from Backendless.
            long timestamp = Backendless.Data.of(ListTheme.class).remove(mListTheme);

            try {
                // Delete ListTheme from the SQLite Db
                int numberOfDeletedListThemes = 0;

                Uri uri = ListThemesSqlTable.CONTENT_URI;
                String selection = ListThemesSqlTable.COL_UUID + " = ?";
                String[] selectionArgs = new String[]{mListTheme.getUuid()};
                ContentResolver cr = AndroidApplication.getContext().getContentResolver();
                numberOfDeletedListThemes = cr.delete(uri, selection, selectionArgs);

                if (numberOfDeletedListThemes == 1) {
                    String successMessage = "\"" + mListTheme.getName() + "\" successfully deleted from SQLiteDb and removed from Backendless at " + new Date(timestamp).toString();
                    postListThemeDeletedFromBackendless(successMessage);
                } else {
                    String errorMessage = "\"" + mListTheme.getName() + "\" NOT DELETED from SQLiteDb but removed from Backendless at " + new Date(timestamp).toString();
                    postListThemeDeletionFromBackendlessFailed(errorMessage);
                }

            } catch (Exception e) {
                String errorMessage = "\"" + mListTheme.getName() + "\" DELETION EXCEPTION: " + e.getMessage();
                postListThemeDeletionFromBackendlessFailed(errorMessage);
            }

        } catch (BackendlessException e) {
            String errorMessage = "\"" + mListTheme.getName() + "\" FAILED TO BE REMOVED from Backendless. " + e.getMessage();
            postListThemeDeletionFromBackendlessFailed(errorMessage);
        }
    }

    private void postListThemeDeletedFromBackendless(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeDeletedFromBackendless(successMessage);
            }
        });
    }

    private void postListThemeDeletionFromBackendlessFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeDeleteFromBackendlessFailed(errorMessage);
            }
        });
    }

}
