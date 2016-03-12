package com.lbconsulting.a1list.domain.repositories;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.storage.AppSettingsSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;

import timber.log.Timber;


/**
 * This class provided CRUD operations for ListTheme
 * NOTE: All CRUD operations should run on a background thread
 */
public class AppSettingsRepository_Impl implements AppSettingsRepository {

    private final int FALSE = 0;
    private final int TRUE = 1;
    private final Context mContext;

    public AppSettingsRepository_Impl(Context context) {
        // private constructor
        this.mContext = context;
    }

    @Override
    public AppSettings insert(AppSettings appSettings) {
        // insert new appSettings into SQLite db
        AppSettings backendlessResponse = null;
        long newAppSettingsId = -1;

        Uri uri = AppSettingsSqlTable.CONTENT_URI;
        ContentValues cv = new ContentValues();

        cv.put(AppSettingsSqlTable.COL_UUID, appSettings.getUuid());
        cv.put(AppSettingsSqlTable.COL_OBJECT_ID, appSettings.getObjectId());
        cv.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, TRUE);

        cv.put(AppSettingsSqlTable.COL_TIME_BETWEEN_SYNCHRONIZATIONS, appSettings.getTimeBetweenSynchronizations());
        cv.put(AppSettingsSqlTable.COL_LIST_TITLE_LAST_SORT_KEY, appSettings.getListTitleLastSortKey());

        Date updatedDateTime = appSettings.getUpdated();
        if (updatedDateTime != null) {
            cv.put(AppSettingsSqlTable.COL_UPDATED, updatedDateTime.getTime());
        }

        ContentResolver cr = mContext.getContentResolver();
        Uri newAppSettingsUri = cr.insert(uri, cv);

        if (newAppSettingsUri != null) {
            newAppSettingsId = Long.parseLong(newAppSettingsUri.getLastPathSegment());
        }

        if (newAppSettingsId > -1) {
            // successfully saved new AppSettings to the SQLite db
            Timber.i("insert(): AppSettingsRepository_Impl: Successfully inserted \"%s\" into the SQLite db.", appSettings.getUuid());

            // if the network is available ... save new appSettings to Backendless
            if (CommonMethods.isNetworkAvailable()) {
                // save appSettings to Backendless
                backendlessResponse = saveAppSettingsToBackendless(appSettings);
                // TODO: send message to Backendless to notify other devices of the new AppSettings
            }

        } else {
            // failed to create appSettings in the SQLite db
            Timber.e("insert(): AppSettingsRepository_Impl: FAILED to insert \"%s\" into the SQLite db.", appSettings.getUuid());
        }
        return backendlessResponse;
    }

    private AppSettings saveAppSettingsToBackendless(AppSettings appSettings) {
        // saveAppSettingsToBackendless object synchronously
        AppSettings response = null;
        try {
            String objectId = appSettings.getObjectId();
            boolean isNew = objectId == null || objectId.isEmpty();
            response = Backendless.Data.of(AppSettings.class).save(appSettings);
            Timber.i("saveAppSettingsToBackendless(): successfully saved \"%s\" to Backendless.", response.getUuid());

            // Update the SQLite db: set dirty to false, and updated date and time
            ContentValues cv = new ContentValues();
            Date updatedDate = response.getUpdated();
            if (updatedDate == null) {
                updatedDate = response.getCreated();
            }
            if (updatedDate != null) {
                long updatedValue = updatedDate.getTime();
                cv.put(AppSettingsSqlTable.COL_UPDATED, updatedValue);
            }
            cv.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, FALSE);

            // If a new AppSettings, update SQLite db with objectID
            if (isNew) {
                cv.put(AppSettingsSqlTable.COL_OBJECT_ID, response.getObjectId());
            }

            // update the SQLite db
            updateSQLiteDb(response, cv);

        } catch (BackendlessException e) {
            Timber.e("saveAppSettingsToBackendless(): FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
                    appSettings.getUuid(), e.getCode(), e.getMessage());
            // Set dirty flag to true in SQLite db
            ContentValues cv = new ContentValues();
            cv.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, TRUE);
            updateSQLiteDb(appSettings, cv);

        } catch (Exception e) {
            Timber.e("saveAppSettingsToBackendless(): Exception: %s.", e.getMessage());
            // Set dirty flag to true in SQLite db
            ContentValues cv = new ContentValues();
            cv.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, TRUE);
            updateSQLiteDb(appSettings, cv);
        }
        return response;
    }

    private int updateSQLiteDb(AppSettings appSettings, ContentValues cv) {
        int numberOfRecordsUpdated = 0;
        try {
            Uri uri = AppSettingsSqlTable.CONTENT_URI;
            ContentResolver cr = mContext.getContentResolver();
            String selection = AppSettingsSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{appSettings.getUuid()};
            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);
        } catch (Exception e) {
            Timber.e("updateSQLiteDb(): Exception: %s.", e.getMessage());
        }
        if (numberOfRecordsUpdated != 1) {
            Timber.e("updateSQLiteDb(): Error updating AppSettings with uuid = %s", appSettings.getUuid());
        }
        return numberOfRecordsUpdated;
    }

    private AppSettings appSettingsFromCursor(Cursor cursor) {

        AppSettings appSettings = new AppSettings();
        appSettings.setId(cursor.getLong(cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_ID)));
        appSettings.setObjectId(cursor.getString(cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_OBJECT_ID)));
        appSettings.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_UUID)));

        appSettings.setTimeBetweenSynchronizations(cursor.getLong(cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_TIME_BETWEEN_SYNCHRONIZATIONS)));
        appSettings.setListTitleLastSortKey(cursor.getLong(cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_LIST_TITLE_LAST_SORT_KEY)));

        long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(AppSettingsSqlTable.COL_UPDATED));
        Date updated = new Date(dateMillis);
        appSettings.setUpdated(updated);

        return appSettings;
    }

    @Override
    public boolean update(AppSettings appSettings, ContentValues cv) {
        boolean result = false;
        try {
            cv.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, TRUE);
            int numberOfRecordsUpdated = updateSQLiteDb(appSettings, cv);

            if (numberOfRecordsUpdated == 1 && CommonMethods.isNetworkAvailable()) {
                result = true;
                saveAppSettingsToBackendless(appSettings);
                // TODO: Send update message to other devices
            }
        } catch (Exception e) {
            Timber.e("update(): Exception: %s.", e.getMessage());
        }

        return result;
    }

    @Override
    public boolean update(AppSettings appSettings) {
        ContentValues cv = new ContentValues();
        cv.put(AppSettingsSqlTable.COL_TIME_BETWEEN_SYNCHRONIZATIONS, appSettings.getTimeBetweenSynchronizations());
        cv.put(AppSettingsSqlTable.COL_LIST_TITLE_LAST_SORT_KEY, appSettings.getListTitleLastSortKey());

        return update(appSettings, cv);
    }

    private Cursor getAllAppSettingsCursor() {
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
    public long retrieveListTitleNextSortKey() {
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
        setListTitleLastSortKey(appSettings, listTitleNextSortKey);
        return listTitleNextSortKey;
    }

    @Override
    public void setListTitleLastSortKey(AppSettings appSettings, long sortKey) {
        ContentValues cv = new ContentValues();
        cv.put(AppSettingsSqlTable.COL_LIST_TITLE_LAST_SORT_KEY, sortKey);
        update(appSettings, cv);
    }

//    @Override
//    public long retrieveListItemNextSortKey() {
//        AppSettings appSettings = null;
//        long listItemNextSortKey = 0;
//
//        Cursor cursor = getAllAppSettingsCursor();
//        if (cursor != null && cursor.getCount() > 0) {
//            cursor.moveToFirst();
//            appSettings = appSettingsFromCursor(cursor);
//            listItemNextSortKey = appSettings.getListItemLastSortKey() + 1;
//            appSettings.setListItemLastSortKey(listItemNextSortKey);
//        }
//        if (cursor != null && !cursor.isClosed()) {
//            cursor.close();
//        }
//        setListItemLastSortKey(appSettings, listItemNextSortKey);
//        return listItemNextSortKey;
//    }
//
//
//    @Override
//    public void setListItemLastSortKey(AppSettings appSettings, long sortKey) {
//        ContentValues cv = new ContentValues();
//        cv.put(AppSettingsSqlTable.COL_LIST_ITEM_LAST_SORT_KEY, sortKey);
//        update(appSettings, cv);
//    }


}
