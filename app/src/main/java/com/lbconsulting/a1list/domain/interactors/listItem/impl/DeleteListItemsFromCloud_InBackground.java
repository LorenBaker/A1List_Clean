package com.lbconsulting.a1list.domain.interactors.listItem.impl;

import android.content.ContentResolver;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.DeleteListItemsFromCloud;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.storage.ListItemsSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;
import java.util.List;

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

        for (ListItem mListItem : mListItems) {
            try {
                // Delete ListItem from Backendless.
                long timestamp = Backendless.Data.of(ListItem.class).remove(mListItem);

                try {
                    // Delete ListItem from the SQLite Db
                    int numberOfDeletedListItems = 0;

                    Uri uri = ListItemsSqlTable.CONTENT_URI;
                    String selection = ListItemsSqlTable.COL_UUID + " = ?";
                    String[] selectionArgs = new String[]{mListItem.getUuid()};
                    ContentResolver cr = AndroidApplication.getContext().getContentResolver();
                    numberOfDeletedListItems = cr.delete(uri, selection, selectionArgs);

                    if (numberOfDeletedListItems == 1) {
                        String successMessage = "\"" + mListItem.getName() + "\" successfully deleted from SQLiteDb and removed from Backendless at " + new Date(timestamp).toString();
                        postListItemsDeletedFromBackendless(successMessage);
                    } else {
                        String errorMessage = "\"" + mListItem.getName() + "\" NOT DELETED from SQLiteDb but removed from Backendless at " + new Date(timestamp).toString();
                        postListItemDeletionFromBackendlessFailed(errorMessage);
                    }

                } catch (Exception e) {
                    String errorMessage = "\"" + mListItem.getName() + "\" DELETION EXCEPTION: " + e.getMessage();
                    postListItemDeletionFromBackendlessFailed(errorMessage);
                }

            } catch (BackendlessException e) {
                String errorMessage = "\"" + mListItem.getName() + "\" FAILED TO BE REMOVED from Backendless. " + e.getMessage();
                postListItemDeletionFromBackendlessFailed(errorMessage);
            }
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
