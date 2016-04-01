package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import android.content.ContentResolver;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.DeleteListThemeFromCloud;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.storage.ListThemesSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;

/**
 * An interactor that saves the provided ListTheme to Backendless.
 */
public class DeleteListThemeFromCloud_InBackground extends AbstractInteractor implements DeleteListThemeFromCloud {
    private final Callback mCallback;
    private final ListTheme mListTheme;

    public DeleteListThemeFromCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                                 Callback callback, ListTheme listTheme) {
        super(threadExecutor, mainThread);
        mListTheme = listTheme;
        mCallback = callback;
    }


    @Override
    public void run() {
        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        try {
            // Delete ListTheme from Backendless.
            long timestamp = Backendless.Data.of(ListTheme.class).remove(mListTheme);

            try {
                // Delete ListTheme from the SQLite Db
                int numberOfDeletedListThemes;

                Uri uri = ListThemesSqlTable.CONTENT_URI;
                String selection = ListThemesSqlTable.COL_UUID + " = ?";
                String[] selectionArgs = new String[]{mListTheme.getUuid()};
                ContentResolver cr = AndroidApplication.getContext().getContentResolver();
                numberOfDeletedListThemes = cr.delete(uri, selection, selectionArgs);

                if (numberOfDeletedListThemes == 1) {
                    String successMessage = "\"" + mListTheme.getName() + "\" successfully deleted from SQLiteDb and removed from Backendless at " + new Date(timestamp).toString();
                    postListThemeDeletedFromCloud(successMessage);
                } else {
                    String errorMessage = "\"" + mListTheme.getName() + "\" NOT DELETED from SQLiteDb but removed from Backendless at " + new Date(timestamp).toString();
                    postListThemeDeletionFromCloudFailed(errorMessage);
                }

            } catch (Exception e) {
                String errorMessage = "\"" + mListTheme.getName() + "\" DELETION EXCEPTION: " + e.getMessage();
                postListThemeDeletionFromCloudFailed(errorMessage);
            }

        } catch (BackendlessException e) {
            String errorMessage = "\"" + mListTheme.getName() + "\" FAILED TO BE REMOVED from Backendless. " + e.getMessage();
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
