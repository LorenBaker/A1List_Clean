package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.backendlessMessaging.ListThemeMessage;
import com.lbconsulting.a1list.backendlessMessaging.Messaging;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.SaveListThemesToCloud;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.storage.ListThemesSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * An interactor that saves the provided ListTheme List to Backendless.
 */
public class SaveListThemesToCloud_InBackground extends AbstractInteractor implements SaveListThemesToCloud {
    private final Callback mCallback;
    private final List<ListTheme> mListThemeList;

    public SaveListThemesToCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                              Callback callback, List<ListTheme> listThemeList) {
        super(threadExecutor, mainThread);
        mListThemeList = listThemeList;
        mCallback = callback;
    }


    @Override
    public void run() {
        // saveListThemeToBackendless
        ListTheme response;
        int TRUE = 1;
        int FALSE = 0;

        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }
        if (mListThemeList == null) {
            Timber.e("run(): Unable to save listThemeList. listThemeList is null!");
            return;
        }
        if (mListThemeList.size() == 0) {
            Timber.e("run(): No ListThemes to save!");
            return;
        }

        List<ListTheme> successfullySavedListThemes = new ArrayList<>();

        for (ListTheme listTheme : mListThemeList) {
            // saveListThemeToBackendless

            String objectId = listTheme.getObjectId();
            boolean isNew = objectId == null || objectId.isEmpty();
            try {
                response = Backendless.Data.of(ListTheme.class).save(listTheme);
                successfullySavedListThemes.add(response);
                try {
                    // Update the SQLite db: set dirty to false, and updated date and time
                    ContentValues cv = new ContentValues();
                    Date updatedDate = response.getUpdated();
                    if (updatedDate == null) {
                        updatedDate = response.getCreated();
                    }
                    if (updatedDate != null) {
                        long updated = updatedDate.getTime();
                        cv.put(ListThemesSqlTable.COL_UPDATED, updated);
                    }

                    cv.put(ListThemesSqlTable.COL_THEME_DIRTY, FALSE);

                    // If a new ListTheme, updateStorage SQLite db with objectID
                    if (isNew) {
                        cv.put(ListThemesSqlTable.COL_OBJECT_ID, response.getObjectId());
                    }
                    // updateStorage the SQLite db
                    updateSQLiteDb(response, cv);

                    // send message to other devices
                    int action = Messaging.ACTION_UPDATE;
                    if (isNew) {
                        action = Messaging.ACTION_CREATE;
                    }
                    ListThemeMessage.sendMessage(listTheme, action);

                    String successMessage = String.format("Successfully saved \"%s\" to Backendless.", response.getName());
                    Timber.i("run(): %s", successMessage);

                } catch (Exception e) {
                    // Set dirty flag to true in SQLite db
                    ContentValues cv = new ContentValues();
                    cv.put(ListThemesSqlTable.COL_THEME_DIRTY, TRUE);
                    updateSQLiteDb(listTheme, cv);

                    String errorMessage = String.format("saveListThemeToBackendless(): \"%s\" FAILED to save to Backendless. Exception: %s",
                            listTheme.getName(), e.getMessage());
                    Timber.e("run(): %s", errorMessage);
                }

            } catch (BackendlessException e) {
                String errorMessage = String.format("FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
                        listTheme.getName(), e.getCode(), e.getMessage());
                Timber.e("run(): %s", errorMessage);
            }
        }


        if (mListThemeList.size() == successfullySavedListThemes.size()) {
            String successMessage = String.format("Successfully saved %d ListThemes to Backendless.", successfullySavedListThemes.size());
            postListThemeListSavedToBackendless(successMessage, successfullySavedListThemes);
        } else {
            String errorMessage = String.format("Only saved %d out of %d ListThemes to Backendless",
                    successfullySavedListThemes.size(), mListThemeList.size());
            postListThemeListSaveToBackendlessFailed(errorMessage, successfullySavedListThemes);
        }

    }

    private void updateSQLiteDb(ListTheme listTheme, ContentValues cv) {
        int numberOfRecordsUpdated = 0;
        try {
            Uri uri = ListThemesSqlTable.CONTENT_URI;
            String selection = ListThemesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTheme.getUuid()};
            ContentResolver cr = AndroidApplication.getContext().getContentResolver();
            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("updateInLocalStorage(): Exception: %s.", e.getMessage());
        }
        if (numberOfRecordsUpdated != 1) {
            Timber.e("updateInLocalStorage(): Error updating ListTheme with uuid = %s", listTheme.getUuid());
        }
    }

    private void postListThemeListSavedToBackendless(final String successMessage, final List<ListTheme> successfullySavedListThemes) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemesSavedToCloud(successMessage, successfullySavedListThemes);
            }
        });
    }

    private void postListThemeListSaveToBackendlessFailed(final String errorMessage, final List<ListTheme> successfullySavedListThemes) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemesSaveToCloudFailed(errorMessage, successfullySavedListThemes);
            }
        });
    }

}
