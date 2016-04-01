package com.lbconsulting.a1list.domain.interactors.appSettings;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.AppSettingsRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_Impl;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.List;

import timber.log.Timber;

/**
 * An interactor that saves the provided AppSettings to Backendless.
 */
public class SaveDirtyObjectsToBackendless_InBackground extends AbstractInteractor {

    public SaveDirtyObjectsToBackendless_InBackground(Executor threadExecutor, MainThread mainThread) {
        super(threadExecutor, mainThread);
    }


    @Override
    public void run() {

        if (!CommonMethods.isNetworkAvailable()) {
            Timber.e("SaveDirtyObjectsToBackendless_InBackground(): Unable to save objects. Network not available.");
            return;
        }

        AppSettingsRepository_Impl appSettingsRepository = AndroidApplication.getAppSettingsRepository();
        ListThemeRepository_Impl listThemeRepository = AndroidApplication.getListThemeRepository();
        ListTitleRepository_Impl listTitleRepository = AndroidApplication.getListTitleRepository();
        ListItemRepository_Impl listItemRepository = AndroidApplication.getListItemRepository();

        AppSettings dirtyAppSettings = appSettingsRepository.retrieveDirtyAppSettings();
        List<ListTheme> dirtyListListThemes = listThemeRepository.retrieveDirtyListThemes();
        List<ListTitle> dirtyListTitles = listTitleRepository.retrieveDirtyListTitles();
        List<ListItem> dirtyListItems = listItemRepository.retrieveDirtyListItems();

        if (dirtyAppSettings != null) {
            appSettingsRepository.updateInCloud(dirtyAppSettings);
//            saveAppSettings(dirtyAppSettings);
        } else {
            Timber.i("SaveDirtyObjectsToBackendless(): No AppSettings to save.");
        }

        if (dirtyListListThemes.size() > 0) {
            listThemeRepository.updateInCloud(dirtyListListThemes);
//            saveListThemes(dirtyListListThemes);
        } else {
            Timber.i("SaveDirtyObjectsToBackendless(): No ListThemes to save.");
        }

        if (dirtyListTitles.size() > 0) {
            listTitleRepository.updateInCloud(dirtyListTitles);
//            saveListTitles(dirtyListTitles);
        } else {
            Timber.i("SaveDirtyObjectsToBackendless(): No ListTitles to save.");
        }

        if (dirtyListItems.size() > 0) {
            listItemRepository.updateInCloud(dirtyListItems);
//            saveListItems(dirtyListItems);
        } else {
            Timber.i("SaveDirtyObjectsToBackendless(): No ListItems to save.");
        }

    }

//    private void saveAppSettings(AppSettings dirtyAppSettings) {
//        // saveAppSettingsToBackendless
//        AppSettings appSettingsResponse = null;
//        int TRUE = 1;
//        int FALSE = 0;
//        if (dirtyAppSettings == null) {
//            Timber.e("run(): Unable to save AppSettings. AppSettings is null!");
//            return;
//        }
//        if (!CommonMethods.isNetworkAvailable()) {
//            return;
//        }
//
//        String objectId = dirtyAppSettings.getObjectId();
//        boolean isNew = objectId == null || objectId.isEmpty();
//        try {
//            appSettingsResponse = Backendless.Data.of(AppSettings.class).save(dirtyAppSettings);
//            try {
//                // Update the SQLite db: set dirty to false, and updated date and time
//                ContentValues cv = new ContentValues();
//                Date updatedDate = appSettingsResponse.getUpdated();
//                if (updatedDate == null) {
//                    updatedDate = appSettingsResponse.getCreated();
//                }
//                if (updatedDate != null) {
//                    long updated = updatedDate.getTime();
//                    cv.put(AppSettingsSqlTable.COL_UPDATED, updated);
//                }
//
//                cv.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, FALSE);
//
//                // If a new AppSettings, update SQLite db with objectID
//                if (isNew) {
//                    cv.put(AppSettingsSqlTable.COL_OBJECT_ID, appSettingsResponse.getObjectId());
//                }
//                // update the SQLite db
//                updateAppSettingsSQLiteDb(appSettingsResponse, cv);
//
//                String successMessage = String.format("Successfully saved \"%s's\" AppSettings to Backendless.", appSettingsResponse.getName());
//                Timber.i("saveAppSettings(): %s", successMessage);
//
//            } catch (Exception e) {
//                // Set dirty flag to true in SQLite db
//                ContentValues cv = new ContentValues();
//                cv.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, TRUE);
//                updateAppSettingsSQLiteDb(appSettingsResponse, cv);
//
//                String errorMessage = String.format("saveAppSettingsToBackendless(): FAILED to save \"%s's\" AppSettings to Backendless. Exception: %s",
//                        appSettingsResponse.getName(), e.getMessage());
//                Timber.e("saveAppSettings(): %s", errorMessage);
//            }
//
//        } catch (BackendlessException e) {
//            String appSettingsUser = "";
//            if (appSettingsResponse != null) {
//                appSettingsUser = appSettingsResponse.getName();
//            }
//            String errorMessage = String.format("saveAppSettingsToBackendless(): FAILED to save \"%s's\" AppSettings to Backendless. BackendlessException: %s",
//                    appSettingsUser, e.getMessage());
//            Timber.e("saveAppSettings(): %s", errorMessage);
//        }
//    }
//
//    private void updateAppSettingsSQLiteDb(AppSettings appSettings, ContentValues cv) {
//        int numberOfRecordsUpdated = 0;
//        try {
//            Uri uri = AppSettingsSqlTable.CONTENT_URI;
//            String selection = AppSettingsSqlTable.COL_UUID + " = ?";
//            String[] selectionArgs = new String[]{appSettings.getUuid()};
//            ContentResolver cr = AndroidApplication.getContext().getContentResolver();
//            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);
//
//        } catch (Exception e) {
//            Timber.e("updateAppSettingsSQLiteDb(): Error updating \"%s's\" AppSettings. Exception: %s.",
//                    appSettings.getName(), e.getMessage());
//        }
//        if (numberOfRecordsUpdated != 1) {
//            Timber.e("updateAppSettingsSQLiteDb(): Error updating \"%s's\" AppSettings.", appSettings.getName());
//        }
//    }
//
//    private void saveListThemes(List<ListTheme> dirtyListThemes) {
//        for (ListTheme listTheme : dirtyListThemes) {
//            saveListTheme(listTheme);
//        }
//    }
//
//    private void saveListTheme(ListTheme listTheme) {
//        // saveListThemeToBackendless
//        ListTheme listThemeResponse;
//        int TRUE = 1;
//        int FALSE = 0;
//
//        if (listTheme == null) {
//            Timber.e("run(): Unable to save ListTheme. ListTheme is null!");
//            return;
//        }
//        if (!CommonMethods.isNetworkAvailable()) {
//            return;
//        }
//
//        String objectId = listTheme.getObjectId();
//        boolean isNew = objectId == null || objectId.isEmpty();
//        try {
//            listThemeResponse = Backendless.Data.of(ListTheme.class).save(listTheme);
//            try {
//                // Update the SQLite db: set dirty to false, and updated date and time
//                ContentValues cv = new ContentValues();
//                Date updatedDate = listThemeResponse.getUpdated();
//                if (updatedDate == null) {
//                    updatedDate = listThemeResponse.getCreated();
//                }
//                if (updatedDate != null) {
//                    long updated = updatedDate.getTime();
//                    cv.put(ListThemesSqlTable.COL_UPDATED, updated);
//                }
//
//                cv.put(ListThemesSqlTable.COL_THEME_DIRTY, FALSE);
//
//                // If a new ListTheme, update SQLite db with objectID
//                if (isNew) {
//                    cv.put(ListThemesSqlTable.COL_OBJECT_ID, listThemeResponse.getObjectId());
//                }
//                // update the SQLite db
//                updateListThemeSQLiteDb(listThemeResponse, cv);
//
//                String successMessage = String.format("Successfully saved \"%s\" to Backendless.", listThemeResponse.getName());
//                Timber.i("saveListTheme(): %s", successMessage);
//
//            } catch (Exception e) {
//                // Set dirty flag to true in SQLite db
//                ContentValues cv = new ContentValues();
//                cv.put(ListThemesSqlTable.COL_THEME_DIRTY, TRUE);
//                updateListThemeSQLiteDb(listThemeResponse, cv);
//
//                String errorMessage = String.format("saveListThemeToBackendless(): \"%s\" FAILED to save to Backendless. Exception: %s",
//                        listTheme.getName(), e.getMessage());
//                Timber.e("saveListTheme(): %s", errorMessage);
//            }
//
//        } catch (BackendlessException e) {
//            String errorMessage = String.format("FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
//                    listTheme.getName(), e.getCode(), e.getMessage());
//            Timber.e("saveListTheme(): %s", errorMessage);
//        }
//    }
//
//    private void updateListThemeSQLiteDb(ListTheme listTheme, ContentValues cv) {
//        int numberOfRecordsUpdated = 0;
//        try {
//            Uri uri = ListThemesSqlTable.CONTENT_URI;
//            String selection = ListThemesSqlTable.COL_UUID + " = ?";
//            String[] selectionArgs = new String[]{listTheme.getUuid()};
//            ContentResolver cr = AndroidApplication.getContext().getContentResolver();
//            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);
//
//        } catch (Exception e) {
//            Timber.e("updateInLocalStorage(): Error updating \"%s\". Exception: %s.", listTheme.getName(), e.getMessage());
//        }
//        if (numberOfRecordsUpdated != 1) {
//            Timber.e("updateInLocalStorage(): Error updating \"%s\"", listTheme.getName());
//        }
//    }
//
//    private void saveListTitles(List<ListTitle> dirtyListTitles) {
//        for (ListTitle listTitle : dirtyListTitles) {
//            saveListTitle(listTitle);
//        }
//    }
//
//    private void saveListTitle(ListTitle listTitle) {
//        // saveListTitleToBackendless
//        ListTitle listTitleResponse;
//        int TRUE = 1;
//        int FALSE = 0;
//
//        if (listTitle == null) {
//            Timber.e("run(): Unable to save ListTitle. ListTitle is null!");
//            return;
//        }
//        if (!CommonMethods.isNetworkAvailable()) {
//            return;
//        }
//
//        String objectId = listTitle.getObjectId();
//        boolean isNew = objectId == null || objectId.isEmpty();
//        try {
//            listTitleResponse = Backendless.Data.of(ListTitle.class).save(listTitle);
//            try {
//                // Update the SQLite db: set dirty to false, and updated date and time
//                ContentValues cv = new ContentValues();
//                Date updatedDate = listTitleResponse.getUpdated();
//                if (updatedDate == null) {
//                    updatedDate = listTitleResponse.getCreated();
//                }
//                if (updatedDate != null) {
//                    long updated = updatedDate.getTime();
//                    cv.put(ListTitlesSqlTable.COL_UPDATED, updated);
//                }
//
//                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, FALSE);
//
//                // If a new ListTitle, update SQLite db with objectID
//                if (isNew) {
//                    cv.put(ListTitlesSqlTable.COL_OBJECT_ID, listTitleResponse.getObjectId());
//                }
//                // update the SQLite db
//                updateListTitleSQLiteDb(listTitleResponse, cv);
//
//                String successMessage = String.format("Successfully saved \"%s\" to Backendless.", listTitleResponse.getName());
//                Timber.i("saveListTitle(): %s", successMessage);
//
//            } catch (Exception e) {
//                // Set dirty flag to true in SQLite db
//                ContentValues cv = new ContentValues();
//                cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, TRUE);
//                updateListTitleSQLiteDb(listTitle, cv);
//
//                String errorMessage = String.format("saveListTitleToBackendless(): \"%s\" FAILED to save to Backendless. Exception: %s", listTitle.getName(), e.getMessage());
//                Timber.e("saveListTitle(): %s", errorMessage);
//            }
//
//        } catch (BackendlessException e) {
//            String errorMessage = String.format("FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
//                    listTitle.getName(), e.getCode(), e.getMessage());
//            Timber.e("saveListTitle(): %s", errorMessage);
//        }
//    }
//
//    private void updateListTitleSQLiteDb(ListTitle listTitle, ContentValues cv) {
//        int numberOfRecordsUpdated = 0;
//        try {
//            Uri uri = ListTitlesSqlTable.CONTENT_URI;
//            String selection = ListTitlesSqlTable.COL_UUID + " = ?";
//            String[] selectionArgs = new String[]{listTitle.getUuid()};
//            ContentResolver cr = AndroidApplication.getContext().getContentResolver();
//            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);
//
//        } catch (Exception e) {
//            Timber.e("updateListTitleSQLiteDb(): Error updating \"%s\". Exception: %s.", listTitle.getName(), e.getMessage());
//        }
//        if (numberOfRecordsUpdated != 1) {
//            Timber.e("updateListTitleSQLiteDb(): Error updating \"%s\".", listTitle.getName());
//        }
//    }
//
//    private void saveListItems(List<ListItem> dirtyListItems) {
//        int successfullySavedListItems = 0;
//        for (ListItem listItem : dirtyListItems) {
//            if(saveListItem(listItem)){
//                successfullySavedListItems++;
//            }
//        }
//
//        if(dirtyListItems.size()== successfullySavedListItems){
//            Timber.i("saveListItems(): Successfully saved all %d ListItems to Backendless.", successfullySavedListItems);
//        }else{
//            Timber.e("saveListItems(): Only saved %d of the %d dirty ListItems to Backendless.", successfullySavedListItems, dirtyListItems.size());
//        }
//    }
//
//    private boolean saveListItem(ListItem listItem) {
//        boolean result = false;
//        // saveListItemToBackendless
//        ListItem response;
//        int TRUE = 1;
//        int FALSE = 0;
//
//        if (listItem == null) {
//            Timber.e("run(): Unable to save ListItem. ListItem is null!");
//            return result;
//        }
//        if (!CommonMethods.isNetworkAvailable()) {
//            return result;
//        }
//
//        String objectId = listItem.getObjectId();
//        boolean isNew = objectId == null || objectId.isEmpty();
//        try {
//            response = Backendless.Data.of(ListItem.class).save(listItem);
//            try {
//                // Update the SQLite db: set dirty to false, and updated date and time
//                ContentValues cv = new ContentValues();
//                Date updatedDate = response.getUpdated();
//                if (updatedDate == null) {
//                    updatedDate = response.getCreated();
//                }
//                if (updatedDate != null) {
//                    long updated = updatedDate.getTime();
//                    cv.put(ListItemsSqlTable.COL_UPDATED, updated);
//                }
//
//                cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, FALSE);
//
//                // If a new ListItem, update SQLite db with objectID
//                if (isNew) {
//                    cv.put(ListItemsSqlTable.COL_OBJECT_ID, response.getObjectId());
//                }
//                // update the SQLite db
//                updateListItemSQLiteDb(response, cv);
//
//                String successMessage = String.format("Successfully saved \"%s\" to Backendless.", response.getName());
//                Timber.i("saveListItem(): %s", successMessage);
//                result = true;
//
//            } catch (Exception e) {
//                // Set dirty flag to true in SQLite db
//                ContentValues cv = new ContentValues();
//                cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, TRUE);
//                updateListItemSQLiteDb(listItem, cv);
//
//                String errorMessage = String.format("saveListItemToBackendless(): \"%s\" FAILED to save to Backendless. Exception: %s", listItem.getName(), e.getMessage());
//                Timber.e("saveListItem(): %s", errorMessage);
//            }
//
//        } catch (BackendlessException e) {
//
//            String errorMessage = String.format("FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
//                    listItem.getName(), e.getCode(), e.getMessage());
//            Timber.e("saveListItem(): %s", errorMessage);
//        }
//        return result;
//    }
//
//    private void updateListItemSQLiteDb(ListItem listItem, ContentValues cv) {
//        int numberOfRecordsUpdated = 0;
//        try {
//            Uri uri = ListItemsSqlTable.CONTENT_URI;
//            String selection = ListItemsSqlTable.COL_UUID + " = ?";
//            String[] selectionArgs = new String[]{listItem.getUuid()};
//            ContentResolver cr = AndroidApplication.getContext().getContentResolver();
//            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);
//
//        } catch (Exception e) {
//            Timber.e("updateListItemSQLiteDb(): Error updating \"%s\". Exception: %s.", listItem.getName(), e.getMessage());
//
//        }
//        if (numberOfRecordsUpdated != 1) {
//            Timber.e("updateListItemSQLiteDb(): Error updating \"%s\".", listItem.getName());
//        }
//    }
}
