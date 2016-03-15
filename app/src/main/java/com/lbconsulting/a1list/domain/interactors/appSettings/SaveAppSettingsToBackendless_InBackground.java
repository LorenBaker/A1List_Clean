package com.lbconsulting.a1list.domain.interactors.appSettings;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.storage.AppSettingsSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;

import timber.log.Timber;

/**
 * An interactor that saves the provided AppSettings to Backendless.
 */
public class SaveAppSettingsToBackendless_InBackground extends AbstractInteractor implements SaveAppSettingsToBackendless {
    private final Callback mCallback;
    private final AppSettings mAppSettings;

    public SaveAppSettingsToBackendless_InBackground(Executor threadExecutor, MainThread mainThread,
                                                     AppSettings appSettings, Callback callback) {
        super(threadExecutor, mainThread);
        mAppSettings = appSettings;
        mCallback = callback;
    }


    @Override
    public void run() {
        // saveAppSettingsToBackendless
        AppSettings response;
        int TRUE = 1;
        int FALSE = 0;
        if (mAppSettings == null) {
            Timber.e("run(): Unable to save AppSettings. AppSettings is null!");
            return;
        }
        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        String objectId = mAppSettings.getObjectId();
        boolean isNew = objectId == null || objectId.isEmpty();
        try {
            response = Backendless.Data.of(AppSettings.class).save(mAppSettings);
            try {
                // Update the SQLite db: set dirty to false, and updated date and time
                ContentValues cv = new ContentValues();
                Date updatedDate = response.getUpdated();
                if (updatedDate == null) {
                    updatedDate = response.getCreated();
                }
                if (updatedDate != null) {
                    long updated = updatedDate.getTime();
                    cv.put(AppSettingsSqlTable.COL_UPDATED, updated);
                }

                cv.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, FALSE);

                // If a new AppSettings, update SQLite db with objectID
                if (isNew) {
                    cv.put(AppSettingsSqlTable.COL_OBJECT_ID, response.getObjectId());
                }
                // update the SQLite db
                updateSQLiteDb(response, cv);

                String successMessage = String.format("Successfully saved AppSettings with Uuid = \"%s\" to Backendless.", response.getUuid());
                postAppSettingsSavedToBackendless(successMessage);

            } catch (Exception e) {
                // Set dirty flag to true in SQLite db
                ContentValues cv = new ContentValues();
                cv.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, TRUE);
                updateSQLiteDb(mAppSettings, cv);

                String errorMessage = String.format("saveAppSettingsToBackendless(): FAILED to save AppSettings with Uuid = \"%s\" to Backendless. Exception: %s",
                        mAppSettings.getUuid(), e.getMessage());
                postAppSettingsSaveToBackendlessFailed(errorMessage);
            }

        } catch (BackendlessException e) {
            String errorMessage = String.format("saveAppSettingsToBackendless(): FAILED to save AppSettings with Uuid = \"%s\" to Backendless. BackendlessException: %s",
                    mAppSettings.getUuid(), e.getMessage());
            postAppSettingsSaveToBackendlessFailed(errorMessage);
        }
    }

    private void updateSQLiteDb(AppSettings appSettings, ContentValues cv) {
        int numberOfRecordsUpdated = 0;
        try {
            Uri uri = AppSettingsSqlTable.CONTENT_URI;
            String selection = AppSettingsSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{appSettings.getUuid()};
            ContentResolver cr = AndroidApplication.getContext().getContentResolver();
            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("updateSQLiteDb(): Exception: %s.", e.getMessage());
        }
        if (numberOfRecordsUpdated != 1) {
            Timber.e("updateSQLiteDb(): Error updating AppSettings with uuid = %s", appSettings.getUuid());
        }
    }

    private void postAppSettingsSavedToBackendless(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onAppSettingsSavedToBackendless(successMessage);
            }
        });
    }

    private void postAppSettingsSaveToBackendlessFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onAppSettingsSaveToBackendlessFailed(errorMessage);
            }
        });
    }

}
