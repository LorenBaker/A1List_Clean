package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import android.content.ContentResolver;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.DeleteListTitleFromBackendless;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.storage.ListTitlesSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;

/**
 * An interactor that saves the provided ListTitle to Backendless.
 */
public class DeleteListTitleFromBackendless_InBackground extends AbstractInteractor implements DeleteListTitleFromBackendless {
    private final Callback mCallback;
    private final ListTitle mListTitle;

    public DeleteListTitleFromBackendless_InBackground(Executor threadExecutor, MainThread mainThread,
                                                       ListTitle listTitle, Callback callback) {
        super(threadExecutor, mainThread);
        mListTitle = listTitle;
        mCallback = callback;
    }


    @Override
    public void run() {

        ListTitle response;
        int TRUE = 1;
        int FALSE = 0;

        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        try {
            // Delete ListTitle from Backendless.
            long timestamp = Backendless.Data.of(ListTitle.class).remove(mListTitle);

            try {
                // Delete ListTitle from the SQLite Db
                int numberOfDeletedListTitles = 0;

                Uri uri = ListTitlesSqlTable.CONTENT_URI;
                String selection = ListTitlesSqlTable.COL_UUID + " = ?";
                String[] selectionArgs = new String[]{mListTitle.getUuid()};
                ContentResolver cr = AndroidApplication.getContext().getContentResolver();
                numberOfDeletedListTitles = cr.delete(uri, selection, selectionArgs);

                if (numberOfDeletedListTitles == 1) {
                    String successMessage = "\"" + mListTitle.getName() + "\" successfully deleted from SQLiteDb and removed from Backendless at " + new Date(timestamp).toString();
                    postListTitleDeletedFromBackendless(successMessage);
                } else {
                    String errorMessage = "\"" + mListTitle.getName() + "\" NOT DELETED from SQLiteDb but removed from Backendless at " + new Date(timestamp).toString();
                    postListTitleDeletionFromBackendlessFailed(errorMessage);
                }

            } catch (Exception e) {
                String errorMessage = "\"" + mListTitle.getName() + "\" DELETION EXCEPTION: " + e.getMessage();
                postListTitleDeletionFromBackendlessFailed(errorMessage);
            }

        } catch (BackendlessException e) {
            String errorMessage = "\"" + mListTitle.getName() + "\" FAILED TO BE REMOVED from Backendless. " + e.getMessage();
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
