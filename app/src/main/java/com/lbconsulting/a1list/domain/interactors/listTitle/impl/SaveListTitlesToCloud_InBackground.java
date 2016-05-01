package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.backendlessMessaging.ListTitleMessage;
import com.lbconsulting.a1list.backendlessMessaging.Messaging;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.SaveListTitlesToCloud;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.storage.ListTitlesSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * An interactor that saves the provided ListTitle to Backendless.
 */
public class SaveListTitlesToCloud_InBackground extends AbstractInteractor implements SaveListTitlesToCloud {
    private final Callback mCallback;
    private final List<ListTitle> mListTitles;

    public SaveListTitlesToCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                              Callback callback, List<ListTitle> listTitles) {
        super(threadExecutor, mainThread);
        mListTitles = listTitles;
        mCallback = callback;
    }


    @Override
    public void run() {
        // saveListTitleToBackendless
        ListTitle response;
        int TRUE = 1;
        int FALSE = 0;

        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }
        if (mListTitles == null) {
            Timber.e("run(): Unable to save listTitles to the Cloud. ListTitles is null!");
            return;
        }
        if (mListTitles.size() == 0) {
            Timber.e("run(): No ListTitles to save!");
            return;
        }

        List<ListTitle> successfullySavedListTitles = new ArrayList<>();

        for (ListTitle listTitle : mListTitles) {
            // saveListTitleToBackendless
            if (listTitle == null) {
                Timber.e("run(): Unable to save ListTitle. ListTitle is null!");
                return;
            }
            if (!CommonMethods.isNetworkAvailable()) {
                return;
            }

            String objectId = listTitle.getObjectId();
            boolean isNew = objectId == null || objectId.isEmpty();
            try {
                response = Backendless.Data.of(ListTitle.class).save(listTitle);
                successfullySavedListTitles.add(response);
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

                    // If a new ListTitle, updateStorage SQLite db with objectID
                    if (isNew) {
                        cv.put(ListTitlesSqlTable.COL_OBJECT_ID, response.getObjectId());
                    }
                    // updateStorage the SQLite db
                    updateSQLiteDb(response, cv);

                    // send message to other devices
                    int action = Messaging.ACTION_UPDATE;
                    if(isNew){
                        action = Messaging.ACTION_CREATE;
                    }
                    ListTitleMessage.sendMessage(listTitle, action);

                    String successMessage = String.format("Successfully saved \"%s\" to Backendless.", response.getName());
                    Timber.i("run(): %s", successMessage);

                } catch (Exception e) {
                    // Set dirty flag to true in SQLite db
                    ContentValues cv = new ContentValues();
                    cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
                    updateSQLiteDb(listTitle, cv);

                    String errorMessage = String.format("saveListTitleToBackendless(): \"%s\" FAILED to save to Backendless. Exception: %s",
                            listTitle.getName(), e.getMessage());
                    Timber.e("run(): %s", errorMessage);
                }

            } catch (BackendlessException e) {
                String errorMessage = String.format("FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
                        listTitle.getName(), e.getCode(), e.getMessage());
                Timber.e("run(): %s", errorMessage);
            }
        }


        if (mListTitles.size() == successfullySavedListTitles.size()) {
            String successMessage = String.format("Successfully saved %d ListTitles to Backendless.", successfullySavedListTitles.size());
            postListTitlesListSavedToBackendless(successMessage, successfullySavedListTitles);
        } else {
            String errorMessage = String.format("Only saved %d out of %d ListTitles to Backendless",
                    successfullySavedListTitles.size(), mListTitles.size());
            postListTitlesListSaveToBackendlessFailed(errorMessage, successfullySavedListTitles);
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

    private void postListTitlesListSavedToBackendless(final String successMessage, final List<ListTitle> successfullySavedListTitles) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitlesListSavedToBackendless(successMessage, successfullySavedListTitles);
            }
        });
    }

    private void postListTitlesListSaveToBackendlessFailed(final String errorMessage, final List<ListTitle> successfullySavedListTitles) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitlesListSaveToBackendlessFailed(errorMessage, successfullySavedListTitles);
            }
        });
    }

}
