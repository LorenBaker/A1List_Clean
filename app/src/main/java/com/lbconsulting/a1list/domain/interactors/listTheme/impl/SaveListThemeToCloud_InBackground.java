package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.SaveListThemeToCloud;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.storage.ListThemesSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;

import timber.log.Timber;

/**
 * An interactor that saves the provided ListTheme to Backendless.
 */
public class SaveListThemeToCloud_InBackground extends AbstractInteractor implements SaveListThemeToCloud {
    private final Callback mCallback;
    private final ListTheme mListTheme;

    public SaveListThemeToCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                             Callback callback, ListTheme listTheme) {
        super(threadExecutor, mainThread);
        mListTheme = listTheme;
        mCallback = callback;
    }


    @Override
    public void run() {
        // saveListThemeToBackendless
        ListTheme response;
        int TRUE = 1;
        int FALSE = 0;

        if (mListTheme == null) {
            Timber.e("run(): Unable to save ListTheme. ListTheme is null!");
            return;
        }
        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        String objectId = mListTheme.getObjectId();
        boolean isNew = objectId == null || objectId.isEmpty();
        try {
            response = Backendless.Data.of(ListTheme.class).save(mListTheme);
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

                // If a new ListTheme, update SQLite db with objectID
                if (isNew) {
                    cv.put(ListThemesSqlTable.COL_OBJECT_ID, response.getObjectId());
                }
                // update the SQLite db
                updateSQLiteDb(response, cv);

                String successMessage = String.format("Successfully saved \"%s\" to Backendless.", response.getName());
                postListThemeSavedToCloud(successMessage);

            } catch (Exception e) {
                // Set dirty flag to true in SQLite db
                ContentValues cv = new ContentValues();
                cv.put(ListThemesSqlTable.COL_THEME_DIRTY, TRUE);
                updateSQLiteDb(mListTheme, cv);

                String errorMessage = String.format("saveListThemeToBackendless(): \"%s\" FAILED to save to Backendless. Exception: %s", mListTheme.getName(), e.getMessage());
                postListThemeSaveToCloudFailed(errorMessage);
            }

        } catch (BackendlessException e) {

            String errorMessage = String.format("FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
                    mListTheme.getName(), e.getCode(), e.getMessage());
            postListThemeSaveToCloudFailed(errorMessage);
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

    private void postListThemeSavedToCloud(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeSavedToCloud(successMessage);
            }
        });
    }

    private void postListThemeSaveToCloudFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeSaveToCloudFailed(errorMessage);
            }
        });
    }

}
