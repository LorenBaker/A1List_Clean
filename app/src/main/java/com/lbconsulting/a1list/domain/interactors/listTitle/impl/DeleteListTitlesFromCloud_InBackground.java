package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import android.content.ContentResolver;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.DeleteListTitlesFromCloud;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.storage.ListTitlesSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;
import java.util.List;

/**
 * An interactor that saves the provided ListTitles to Backendless.
 */
public class DeleteListTitlesFromCloud_InBackground extends AbstractInteractor implements DeleteListTitlesFromCloud {
    private final Callback mCallback;
    private final List<ListTitle> mListTitles;

    public DeleteListTitlesFromCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                                  Callback callback, List<ListTitle> listTitles) {
        super(threadExecutor, mainThread);
        mListTitles = listTitles;
        mCallback = callback;
    }


    @Override
    public void run() {

        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        for (ListTitle listTile : mListTitles) {
            try {
                // Delete ListTitle from Backendless.
                long timestamp = Backendless.Data.of(ListTitle.class).remove(listTile);

                try {
                    // Delete ListTitle from the SQLite Db
                    int numberOfDeletedListTitles = 0;

                    Uri uri = ListTitlesSqlTable.CONTENT_URI;
                    String selection = ListTitlesSqlTable.COL_UUID + " = ?";
                    String[] selectionArgs = new String[]{listTile.getUuid()};
                    ContentResolver cr = AndroidApplication.getContext().getContentResolver();
                    numberOfDeletedListTitles = cr.delete(uri, selection, selectionArgs);

                    if (numberOfDeletedListTitles == 1) {
                        String successMessage = "\"" + listTile.getName() + "\" successfully deleted from SQLiteDb and removed from Backendless at " + new Date(timestamp).toString();
                        postListTitlesDeletedFromBackendless(successMessage);
                    } else {
                        String errorMessage = "\"" + listTile.getName() + "\" NOT DELETED from SQLiteDb but removed from Backendless at " + new Date(timestamp).toString();
                        postListTitlesDeletionFromBackendlessFailed(errorMessage);
                    }

                } catch (Exception e) {
                    String errorMessage = "\"" + listTile.getName() + "\" DELETION EXCEPTION: " + e.getMessage();
                    postListTitlesDeletionFromBackendlessFailed(errorMessage);
                }

            } catch (BackendlessException e) {
                String errorMessage = "\"" + listTile.getName() + "\" FAILED TO BE REMOVED from Backendless. " + e.getMessage();
                postListTitlesDeletionFromBackendlessFailed(errorMessage);
            }
        }
    }

    private void postListTitlesDeletedFromBackendless(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitlesDeletedFromBackendless(successMessage);
            }
        });
    }

    private void postListTitlesDeletionFromBackendlessFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitlesDeleteFromBackendlessFailed(errorMessage);
            }
        });
    }


}
