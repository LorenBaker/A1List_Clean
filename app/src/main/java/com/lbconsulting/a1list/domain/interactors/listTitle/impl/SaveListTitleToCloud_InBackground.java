package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.backendless.messaging.MessageStatus;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.backendlessMessaging.ListTitleMessage;
import com.lbconsulting.a1list.backendlessMessaging.Messaging;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.SaveListTitleToCloud;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.storage.ListTitlesSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;

import timber.log.Timber;

/**
 * An interactor that saves the provided ListTitle to Backendless.
 */
public class SaveListTitleToCloud_InBackground extends AbstractInteractor implements SaveListTitleToCloud {
    private final Callback mCallback;
    private final ListTitle mListTitle;

    public SaveListTitleToCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                             Callback callback, ListTitle listTitle) {
        super(threadExecutor, mainThread);
        mListTitle = listTitle;
        mCallback = callback;
    }


    @Override
    public void run() {
        // saveListTitleToBackendless
        ListTitle response;
        int TRUE = 1;
        int FALSE = 0;

        if (mListTitle == null) {
            Timber.e("run(): Unable to save ListTitle. ListTitle is null!");
            return;
        }
        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        String objectId = mListTitle.getObjectId();
        boolean isNew = objectId == null || objectId.isEmpty();
        try {
            response = Backendless.Data.of(ListTitle.class).save(mListTitle);
            try {
                // Update the SQLite db: set dirty to false, and updated date and time
                ContentValues cv = new ContentValues();
                Date updatedDate = response.getUpdated();
                if (updatedDate == null) {
                    updatedDate = response.getCreated();
                }
                if (updatedDate != null) {
                    long updated = updatedDate.getTime();
                    cv.put(ListTitlesSqlTable.COL_UPDATED, updated);
                }

                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, FALSE);

                // If a new ListTitle, update SQLite db with objectID
                if (isNew) {
                    cv.put(ListTitlesSqlTable.COL_OBJECT_ID, response.getObjectId());
                }
                // update the SQLite db
                updateSQLiteDb(response, cv);

                // send message to other devices
                sendListTitleMessage(mListTitle, isNew);

                String successMessage = String.format("Successfully saved \"%s\" to Backendless.", response.getName());
                postListTitleSavedToCloud(successMessage);

            } catch (Exception e) {
                // Set dirty flag to true in SQLite db
                ContentValues cv = new ContentValues();
                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
                updateSQLiteDb(mListTitle, cv);

                String errorMessage = String.format("saveListTitleToBackendless(): \"%s\" FAILED to save to Backendless. Exception: %s", mListTitle.getName(), e.getMessage());
                postListTitleSaveToCloudFailed(errorMessage);
            }

        } catch (BackendlessException e) {

            String errorMessage = String.format("FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
                    mListTitle.getName(), e.getCode(), e.getMessage());
            postListTitleSaveToCloudFailed(errorMessage);
        }
    }

    private void sendListTitleMessage(ListTitle listTitle, boolean isNew) {
        String messageChannel = listTitle.getMessageChannel();
        int action = Messaging.ACTION_UPDATE;
        if (isNew) {
            action = Messaging.ACTION_CREATE;
        }
        int target = Messaging.TARGET_ALL_DEVICES;
        String listTitleMessageJson = ListTitleMessage.toJson(listTitle, action, target);
        MessageStatus messageStatus = Backendless.Messaging.publish(messageChannel, listTitleMessageJson);
        if (messageStatus.getErrorMessage() == null) {
            // successfully sent message to Backendless.
            if (isNew) {
                Timber.i("sendListTitleMessage(): CREATE \"%s\" message successfully sent.", listTitle.getName());
            } else {
                Timber.i("sendListTitleMessage(): UPDATE \"%s\" message successfully sent.", listTitle.getName());
            }
        } else {
            // error sending message to Backendless.
            Timber.e("sendListTitleMessage(): FAILED to send message for \"%s\". %s.",
                    listTitle.getName(), messageStatus.getErrorMessage());
        }
    }

    private void updateSQLiteDb(ListTitle listTitle, ContentValues cv) {
        int numberOfRecordsUpdated = 0;
        try {
            Uri uri = ListTitlesSqlTable.CONTENT_URI;
            String selection = ListTitlesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTitle.getUuid()};
            ContentResolver cr = AndroidApplication.getContext().getContentResolver();
            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("updateInLocalStorage(): Exception: %s.", e.getMessage());
        }
        if (numberOfRecordsUpdated != 1) {
            Timber.e("updateInLocalStorage(): Error updating ListTitle with uuid = %s", listTitle.getUuid());
        }
    }

    private void postListTitleSavedToCloud(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleSavedToCloud(successMessage);
            }
        });
    }

    private void postListTitleSaveToCloudFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleSaveToCloudFailed(errorMessage);
            }
        });
    }

}
