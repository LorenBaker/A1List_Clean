package com.lbconsulting.a1list.domain.repositories;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.appSettings.SaveAppSettingsToCloud_InBackground;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.storage.AppSettingsSqlTable;
import com.lbconsulting.a1list.threading.MainThreadImpl;
import com.lbconsulting.a1list.utils.MySettings;

import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;


/**
 * This class provided CRUD operations for ListTheme
 * NOTE: All CRUD operations should run on a background thread
 */
public class AppSettingsRepository_Impl implements AppSettingsRepository,
        SaveAppSettingsToCloud_InBackground.Callback {

    private static final int FALSE = 0;
    private static final int TRUE = 1;
    private final Context mContext;

    public AppSettingsRepository_Impl(Context context) {
        // private constructor
        this.mContext = context;
    }

    //region Insert AppSettings
    @Override
    public boolean insert(AppSettings appSettings) {
        boolean successfullyInsertedAppSettingsIntoLocalStorage = insertIntoLocalStorage(appSettings);
        if (successfullyInsertedAppSettingsIntoLocalStorage) {
            insertInCloud(appSettings);
        }
        return successfullyInsertedAppSettingsIntoLocalStorage;
    }

    @Override
    public boolean insertIntoLocalStorage(AppSettings appSettings) {
        // insertIntoStorage new appSettings into SQLite db
        boolean result = false;
        long newAppSettingsId = -1;

        Uri uri = AppSettingsSqlTable.CONTENT_URI;
        appSettings.setUpdated(Calendar.getInstance().getTime());
        ContentValues cv = makeContentValues(appSettings);
        ContentResolver cr = mContext.getContentResolver();
        Uri newAppSettingsUri = cr.insert(uri, cv);

        if (newAppSettingsUri != null) {
            newAppSettingsId = Long.parseLong(newAppSettingsUri.getLastPathSegment());
        }

        if (newAppSettingsId > -1) {
            // successfully saved new AppSettings to the SQLite db
            result = true;
            Timber.i("insertIntoStorage(): AppSettingsRepository_Impl: Successfully inserted user %s's AppSettings into local storage.", appSettings.getName());
        } else {
            // failed to create appSettings in the SQLite db
            Timber.e("insertIntoStorage(): AppSettingsRepository_Impl: FAILED to insertIntoStorage user %s's AppSettings into local storage.", appSettings.getName());
        }
        return result;
    }

    public static ContentValues makeContentValues(AppSettings appSettings) {

        ContentValues cv = new ContentValues();

        cv.put(AppSettingsSqlTable.COL_OBJECT_ID, appSettings.getObjectId());
        cv.put(AppSettingsSqlTable.COL_UUID, appSettings.getUuid());
        cv.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, TRUE);

        cv.put(AppSettingsSqlTable.COL_NAME, appSettings.getName());
        cv.put(AppSettingsSqlTable.COL_TIME_BETWEEN_SYNCHRONIZATIONS, appSettings.getTimeBetweenSynchronizations());

        cv.put(AppSettingsSqlTable.COL_LAST_LIST_TITLE_VIEWED_UUID, appSettings.getLastListTitleViewedUuid());
        int listTitlesSortedAlphabeticallyValue = (appSettings.isListTitlesSortedAlphabetically()? TRUE : FALSE);
        cv.put(AppSettingsSqlTable.COL_LIST_TITLES_SORTED_ALPHABETICALLY,
                listTitlesSortedAlphabeticallyValue);
        cv.put(AppSettingsSqlTable.COL_LIST_TITLE_LAST_SORT_KEY, appSettings.getListTitleLastSortKey());

        Date updatedDateTime = appSettings.getUpdated();
        if (updatedDateTime != null) {
            cv.put(AppSettingsSqlTable.COL_UPDATED, updatedDateTime.getTime());
        }
        return cv;
    }

    @Override
    public void insertInCloud(AppSettings appSettings) {
        updateInCloud(appSettings,true);
    }
    //endregion

    //region Update AppSettings
    @Override
    public void updateInStorage(AppSettings appSettings) {
        if(appSettings!=null) {
            if (updateInLocalStorage(appSettings) == 1) {
                updateInCloud(appSettings,false);
            }
        }
    }

    @Override
    public int updateInLocalStorage(AppSettings appSettings) {
        appSettings.setUpdated(Calendar.getInstance().getTime());
        ContentValues cv = makeContentValues(appSettings);
        int numberOfRecordsUpdated = updateInLocalStorage(appSettings, cv);
        if (numberOfRecordsUpdated == 1) {
            Timber.i("updateInLocalStorage(): Successfully updated user %s's AppSettings into SQLiteDb.", appSettings.getName());
        }else{
            Timber.e("updateInLocalStorage(): FAILED to updateStorage user %s's AppSettings into SQLiteDb.", appSettings.getName());
        }
        return numberOfRecordsUpdated;
    }

    private int updateInLocalStorage(AppSettings appSettings, ContentValues cv) {
        int numberOfRecordsUpdated = 0;
        try {
            Uri uri = AppSettingsSqlTable.CONTENT_URI;
            ContentResolver cr = mContext.getContentResolver();
            String selection = AppSettingsSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{appSettings.getUuid()};
            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);
        } catch (Exception e) {
            Timber.e("updateInLocalStorage(): Exception: %s.", e.getMessage());
        }
        if (numberOfRecordsUpdated != 1) {
            Timber.e("updateInLocalStorage(): Error updating user %s's AppSettings into local storage.", appSettings.getName());
        }
        return numberOfRecordsUpdated;
    }

    public static AppSettings appSettingsFromCursor(Cursor cursor) {

        AppSettings appSettings = new AppSettings();
        appSettings.setSQLiteId(cursor.getLong(
                cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_ID)));
        appSettings.setObjectId(cursor.getString(
                cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_OBJECT_ID)));
        appSettings.setUuid(cursor.getString(
                cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_UUID)));

        appSettings.setDeviceUuid(MySettings.getDeviceUuid());
        appSettings.setMessageChannel(MySettings.getActiveUserID());

        appSettings.setName(cursor.getString(
                cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_NAME)));

        appSettings.setLastListTitleViewedUuid(cursor.getString(
                cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_LAST_LIST_TITLE_VIEWED_UUID)));

        appSettings.setTimeBetweenSynchronizations(cursor.getLong(
                cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_TIME_BETWEEN_SYNCHRONIZATIONS)));

        boolean listTitlesSortedAlphabeticallyValue = cursor.getInt(
                cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_LIST_TITLES_SORTED_ALPHABETICALLY)) > 0;
        appSettings.setListTitlesSortedAlphabetically(listTitlesSortedAlphabeticallyValue);

        appSettings.setListTitleLastSortKey(cursor.getLong(
                cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_LIST_TITLE_LAST_SORT_KEY)));

        long dateMillis = cursor.getLong(
                cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_UPDATED));
        Date updated = new Date(dateMillis);
        appSettings.setUpdated(updated);

        return appSettings;
    }


    @Override
    public void updateInCloud(AppSettings appSettings, boolean isNew) {
        
        if(!isNew){
            if (appSettings.getObjectId() == null || appSettings.getObjectId().isEmpty()) {
                AppSettings existingAppSettings = retrieveAppSettings();
                appSettings.setObjectId(existingAppSettings.getObjectId());
            }
            if (appSettings.getObjectId() == null || appSettings.getObjectId().isEmpty()) {
                // The appSettings is not new AND there is no Backendless objectId available ... so,
                // Unable to updateStorage the appSettings in Backendless
                Timber.e("updateInCloudStorage(): Unable to updateStorage \"%s\" AppSettings in the Cloud. No Backendless objectId available!",
                        appSettings.getName());
                return;
            }
        }

        new SaveAppSettingsToCloud_InBackground(ThreadExecutor.getInstance(), MainThreadImpl.getInstance(),
                this, appSettings).execute();
    }

    @Override
    public int clearAllData() {
        int numberOfDeletedAppSettings = 0;
        try {
            Uri uri = AppSettingsSqlTable.CONTENT_URI;
            String selection = null;
            String[] selectionArgs = null;
            ContentResolver cr = mContext.getContentResolver();
            numberOfDeletedAppSettings = cr.delete(uri, selection, selectionArgs);
            Timber.i("clearAllData(): Successfully deleted %d AppSettings from the SQLiteDb.", numberOfDeletedAppSettings);

        } catch (Exception e) {
            Timber.e("clearAllData(): Exception: %s.", e.getMessage());
        }

        return numberOfDeletedAppSettings;
    }

    @Override
    public void onAppSettingsSavedToCloud(String successMessage) {
        Timber.i("onAppSettingsSavedToCloud(): %s", successMessage);
    }

    @Override
    public void onAppSettingsSaveToCloudFailed(String errorMessage) {
        Timber.e("onAppSettingsSaveToCloudFailed(): %s", errorMessage);
    }
    //endregion


    //region Retrieve AppSettings
        @Override
    public AppSettings retrieveAppSettings() {
        AppSettings appSettings = null;
        Cursor cursor = getAllAppSettingsCursor();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            appSettings = appSettingsFromCursor(cursor);
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return appSettings;
    }

    private Cursor getAllAppSettingsCursor() {
        // Note: Because of Backendless security settings, there should only be one AppSettings
        // record for any user.
        Cursor cursor = null;
        Uri uri = AppSettingsSqlTable.CONTENT_URI;
        String[] projection = AppSettingsSqlTable.PROJECTION_ALL;
        String selection = null;
        String selectionArgs[] = null;
        String sortOrder = null;

        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getAllAppSettingsCursor(): Exception: %s.", e.getMessage());
        }
        return cursor;
    }


    @Override
    public long retrieveTimeBetweenSynchronizations() {
        long timeBetweenSynchronizations = 0;
        Cursor cursor = getAllAppSettingsCursor();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            AppSettings appSettings = appSettingsFromCursor(cursor);
            timeBetweenSynchronizations = appSettings.getTimeBetweenSynchronizations();
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return timeBetweenSynchronizations;
    }

    @Override
    public long retrieveNextListTitleSortKey() {
        AppSettings appSettings = null;
        long listTitleNextSortKey = 0;

        Cursor cursor = getAllAppSettingsCursor();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            appSettings = appSettingsFromCursor(cursor);
            listTitleNextSortKey = appSettings.getListTitleLastSortKey() + 1;
            appSettings.setListTitleLastSortKey(listTitleNextSortKey);
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        // save listTitleNextSortKey to the SQLite db
        // the appSettings will be saved to Backendless when the ListTitle is saved in its repository.
        ContentValues cv = new ContentValues();
        cv.put(AppSettingsSqlTable.COL_LIST_TITLE_LAST_SORT_KEY, listTitleNextSortKey);
        cv.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, TRUE);
        int numberOfRecordsUpdated = updateInLocalStorage(appSettings, cv);
        if (numberOfRecordsUpdated != 1) {
            Timber.e("retrieveNextListTitleSortKey(): number of AppSettings records updated does not equal 1.");
        }

        return listTitleNextSortKey;
    }


    @Override
    public AppSettings retrieveDirtyAppSettings() {
        AppSettings appSettings = null;

        try {
            Cursor cursor = getAllAppSettingsCursor();
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                boolean isDirty = cursor.getInt(cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY)) > 0;
                if (isDirty) {
                    appSettings = appSettingsFromCursor(cursor);
                }
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } catch (IllegalArgumentException e) {
            Timber.e("retrieveDirtyAppSettings(): Exception: %s.", e.getMessage());
        }
        return appSettings;
    }
    //endregion

}
