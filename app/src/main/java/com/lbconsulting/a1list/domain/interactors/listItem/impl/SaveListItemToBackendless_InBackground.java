package com.lbconsulting.a1list.domain.interactors.listItem.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.SaveListItemToBackendless;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.storage.ListItemsSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;

import timber.log.Timber;

/**
 * An interactor that saves the provided ListItem to Backendless.
 */
public class SaveListItemToBackendless_InBackground extends AbstractInteractor implements SaveListItemToBackendless {
    private final Callback mCallback;
    private final ListItem mListItem;

    public SaveListItemToBackendless_InBackground(Executor threadExecutor, MainThread mainThread,
                                                   Callback callback, ListItem listItem) {
        super(threadExecutor, mainThread);
        mListItem = listItem;
        mCallback = callback;
    }


    @Override
    public void run() {
        // saveListItemToBackendless
        ListItem response;
        int TRUE = 1;
        int FALSE = 0;

        if (mListItem == null) {
            Timber.e("run(): Unable to save ListItem. ListItem is null!");
            return;
        }
        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        String objectId = mListItem.getObjectId();
        boolean isNew = objectId == null || objectId.isEmpty();
        try {
            response = Backendless.Data.of(ListItem.class).save(mListItem);
            try {
                // Update the SQLite db: set dirty to false, and updated date and time
                ContentValues cv = new ContentValues();
                Date updatedDate = response.getUpdated();
                if (updatedDate == null) {
                    updatedDate = response.getCreated();
                }
                if (updatedDate != null) {
                    long updated = updatedDate.getTime();
                    cv.put(ListItemsSqlTable.COL_UPDATED, updated);
                }

                cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, FALSE);

                // If a new ListItem, update SQLite db with objectID
                if (isNew) {
                    cv.put(ListItemsSqlTable.COL_OBJECT_ID, response.getObjectId());
                }
                // update the SQLite db
                updateSQLiteDb(response, cv);

                String successMessage = String.format("Successfully saved \"%s\" to Backendless.", response.getName());
                postListItemSavedToBackendless(successMessage);

            } catch (Exception e) {
                // Set dirty flag to true in SQLite db
                ContentValues cv = new ContentValues();
                cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, TRUE);
                updateSQLiteDb(mListItem, cv);

                String errorMessage = String.format("saveListItemToBackendless(): \"%s\" FAILED to save to Backendless. Exception: %s", mListItem.getName(), e.getMessage());
                postListItemSaveToBackendlessFailed(errorMessage);
            }

        } catch (BackendlessException e) {

            String errorMessage = String.format("FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
                    mListItem.getName(), e.getCode(), e.getMessage());
            postListItemSaveToBackendlessFailed(errorMessage);
        }
    }

    private void updateSQLiteDb(ListItem listItem, ContentValues cv) {
        int numberOfRecordsUpdated = 0;
        try {
            Uri uri = ListItemsSqlTable.CONTENT_URI;
            String selection = ListItemsSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listItem.getUuid()};
            ContentResolver cr = AndroidApplication.getContext().getContentResolver();
            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("updateInLocalStorage(): Exception: %s.", e.getMessage());
        }
        if (numberOfRecordsUpdated != 1) {
            Timber.e("updateInLocalStorage(): Error updating ListItem with uuid = %s", listItem.getUuid());
        }
    }

    private void postListItemSavedToBackendless(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemSavedToBackendless(successMessage);
            }
        });
    }

    private void postListItemSaveToBackendlessFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemSaveToBackendlessFailed(errorMessage);
            }
        });
    }

}
