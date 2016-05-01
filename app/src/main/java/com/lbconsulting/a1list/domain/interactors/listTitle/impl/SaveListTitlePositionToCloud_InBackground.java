package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.backendlessMessaging.ListTitlePositionMessage;
import com.lbconsulting.a1list.backendlessMessaging.Messaging;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.SaveListTitlePositionToCloud;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.model.ListTitleAndPosition;
import com.lbconsulting.a1list.domain.model.ListTitlePosition;
import com.lbconsulting.a1list.domain.storage.ListTitlePositionsSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;

import timber.log.Timber;

/**
 * An interactor that saves the provided ListTitle to Backendless.
 */
public class SaveListTitlePositionToCloud_InBackground extends AbstractInteractor implements SaveListTitlePositionToCloud {
    private final Callback mCallback;
    private final ListTitle listTitle;
    private final ListTitlePosition listTitlePosition;

    public SaveListTitlePositionToCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                                     Callback callback,
                                                     ListTitleAndPosition listTitlesPosition) {
        super(threadExecutor, mainThread);
        this.listTitle = listTitlesPosition.getListTitle();
        this.listTitlePosition = listTitlesPosition.getListTitlePosition();
        mCallback = callback;
    }


    @Override
    public void run() {
        // saveListTitlePositionToBackendless
        ListTitlePosition response;
        int TRUE = 1;
        int FALSE = 0;

        if (listTitlePosition == null) {
            Timber.e("run(): Unable to save ListTitlePosition. ListTitlePosition is null!");
            return;
        }
        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        String objectId = listTitlePosition.getObjectId();
        boolean isNew = objectId == null || objectId.isEmpty();
        try {
            response = Backendless.Data.of(ListTitlePosition.class).save(listTitlePosition);
            try {
                // Update the SQLite db: updated date and time
                ContentValues cv = new ContentValues();
                Date updatedDate = response.getUpdated();
                if (updatedDate == null) {
                    updatedDate = response.getCreated();
                }
                if (updatedDate != null) {
                    long updated = updatedDate.getTime();
                    cv.put(ListTitlePositionsSqlTable.COL_UPDATED, updated);
                }

                cv.put(ListTitlePositionsSqlTable.COL_LIST_TITLE_POSITION_DIRTY, FALSE);

                // If a new ListTitlePosition, updateStorage SQLite db with objectID
                if (isNew) {
                    cv.put(ListTitlePositionsSqlTable.COL_OBJECT_ID, response.getObjectId());
                }
                // updateStorage the SQLite db
                updateSQLiteDb(response, cv, listTitle);

                // send message to other devices
                int action = Messaging.ACTION_UPDATE;
                if (isNew) {
                    action = Messaging.ACTION_CREATE;
                }
                ListTitlePositionMessage.sendMessage(listTitle, listTitlePosition, action);

                String successMessage = String.format("Successfully saved \"%s's\" ListTitlePosition to Backendless.", listTitle.getName());
                postListTitleSavedToCloud(successMessage);

            } catch (Exception e) {

                String errorMessage = String.format("saveListTitlePositionToBackendless(): FAILED to save \"%s's\" ListTitlePosition to Backendless. Exception: %s", listTitle.getName(), e.getMessage());
                postListTitlePositionSaveToCloudFailed(errorMessage);
            }

        } catch (BackendlessException e) {

            String errorMessage = String.format("FAILED to save \"%s's\" ListTitlePosition to Backendless. BackendlessException: Code: %s; Message: %s.",
                    listTitle.getName(), e.getCode(), e.getMessage());
            postListTitlePositionSaveToCloudFailed(errorMessage);
        }
    }


    private void updateSQLiteDb(ListTitlePosition listTitlePosition, ContentValues cv, ListTitle listTitle) {
        int numberOfRecordsUpdated = 0;
        try {
            Uri uri = ListTitlePositionsSqlTable.CONTENT_URI;
            String selection = ListTitlePositionsSqlTable.COL_LIST_TITLE_UUID + " = ?";
            String[] selectionArgs = new String[]{listTitlePosition.getListTitleUuid()};
            ContentResolver cr = AndroidApplication.getContext().getContentResolver();
            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("updateSQLiteDb(): Exception: %s.", e.getMessage());
        }
        if (numberOfRecordsUpdated != 1) {
            Timber.e("updateSQLiteDb(): Error updating \"%s's\" ListTitlePosition", listTitle.getName());
        }
    }

    private void postListTitleSavedToCloud(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitlePositionSavedToCloud(successMessage);
            }
        });
    }

    private void postListTitlePositionSaveToCloudFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitlePositionSaveToCloudFailed(errorMessage);
            }
        });
    }

}
