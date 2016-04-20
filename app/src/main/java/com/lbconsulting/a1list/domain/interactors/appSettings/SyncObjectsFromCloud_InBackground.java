package com.lbconsulting.a1list.domain.interactors.appSettings;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.exceptions.BackendlessException;
import com.backendless.persistence.BackendlessDataQuery;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.model.ListTitlePosition;
import com.lbconsulting.a1list.domain.repositories.AppSettingsRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_Impl;
import com.lbconsulting.a1list.domain.storage.A1List_ContentProvider;
import com.lbconsulting.a1list.domain.storage.AppSettingsSqlTable;
import com.lbconsulting.a1list.domain.storage.ListItemsSqlTable;
import com.lbconsulting.a1list.domain.storage.ListThemesSqlTable;
import com.lbconsulting.a1list.domain.storage.ListTitlePositionsSqlTable;
import com.lbconsulting.a1list.domain.storage.ListTitlesSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.MySettings;
import com.lbconsulting.a1list.utils.SyncStats;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import timber.log.Timber;

/**
 * An interactor that downloads and syncs A1List objects from the Cloud.
 */
public class SyncObjectsFromCloud_InBackground extends AbstractInteractor implements SyncObjectsFromCloud {
    private final Callback mCallback;
    private final int FALSE = 0;

    public SyncObjectsFromCloud_InBackground(Executor threadExecutor, MainThread mainThread, Callback callback) {

        super(threadExecutor, mainThread);
        mCallback = callback;
    }


    @Override
    public void run() {


        final int PAGE_SIZE = 30;
        final int APP_SETTINGS = 0;
        final int LIST_THEMES = 1;
        final int LIST_TITLES = 2;
        final int LIST_TITLE_POSITIONS = 3;
        final int LIST_ITEMS = 4;

        SyncStats syncStats = new SyncStats();

        if (!CommonMethods.isNetworkAvailable()) {
            syncStats.numNetworkNotAvailableExceptions++;
            String errorMessage = "ABORTING SYNC OBJECTS FROM CLOUD. Network not available.";
            postOnSyncObjectsFromCloudFailed(errorMessage, syncStats);
            return;
        }

        CommonMethods.saveDirtyObjectsToCloud();

        Timber.i("run(): Starting download of Backendless objects.");

        // set all downloadOk booleans to false
        boolean[] downloadsOk = new boolean[5];
        for (int i = 0; i < downloadsOk.length; i++) {
            downloadsOk[i] = false;
        }

        // HashMaps of cloud A1List objects
        HashMap<String, AppSettings> appSettingsCloudMap = new HashMap<String, AppSettings>();
        HashMap<String, ListTheme> listThemesCloudMap = new HashMap<String, ListTheme>();
        HashMap<String, ListTitle> listTitlesCloudMap = new HashMap<String, ListTitle>();
        HashMap<String, ListTitlePosition> listTitlePositionsCloudMap = new HashMap<String, ListTitlePosition>();
        HashMap<String, ListItem> listItemsCloudMap = new HashMap<String, ListItem>();

        //region Download A1List objects from Backendless
        try {
            BackendlessDataQuery appSettingDataQuery = new BackendlessDataQuery();
            appSettingDataQuery.setPageSize(PAGE_SIZE);
            BackendlessCollection<AppSettings> appSettingsCollection = Backendless.Data.of(AppSettings.class).find(appSettingDataQuery);
            Iterator<AppSettings> appSettingIterator;
            while (appSettingsCollection.getCurrentPage().size() > 0) {
                appSettingIterator = appSettingsCollection.getCurrentPage().iterator();
                while (appSettingIterator.hasNext()) {
                    AppSettings appSettings = appSettingIterator.next();
                    appSettingsCloudMap.put(appSettings.getUuid(), appSettings);
                }
                appSettingsCollection = appSettingsCollection.nextPage();
            }
            Timber.i("run(): Downloaded %d AppSettings from Backendless.", appSettingsCloudMap.size());
            downloadsOk[APP_SETTINGS] = true;
        } catch (BackendlessException e) {
            syncStats.numAppSettingsBackendlessExceptions++;
            Timber.e("run(): FAILED to download AppSettings from Backendless. BackendlessException: %s", e.getMessage());
        } catch (Exception e) {
            syncStats.numAppSettingsDownloadExceptions++;
            Timber.e("run(): FAILED to sync AppSettings. Exception: %s", e.getMessage());
        }

        try {
            BackendlessDataQuery listThemeDataQuery = new BackendlessDataQuery();
            listThemeDataQuery.setPageSize(PAGE_SIZE);
            BackendlessCollection<ListTheme> listThemesCollection = Backendless.Data.of(ListTheme.class).find(listThemeDataQuery);
            Iterator<ListTheme> listThemeIterator;
            while (listThemesCollection.getCurrentPage().size() > 0) {
                listThemeIterator = listThemesCollection.getCurrentPage().iterator();
                while (listThemeIterator.hasNext()) {
                    ListTheme listTheme = listThemeIterator.next();
                    listThemesCloudMap.put(listTheme.getUuid(), listTheme);
                }
                listThemesCollection = listThemesCollection.nextPage();
            }
            Timber.i("run(): Downloaded %d ListThemes from Backendless.", listThemesCloudMap.size());
            downloadsOk[LIST_THEMES] = true;
        } catch (BackendlessException e) {
            syncStats.numListThemeBackendlessExceptions++;
            Timber.e("run(): FAILED to download ListThemes from Backendless. BackendlessException: %s", e.getMessage());
        } catch (Exception e) {
            syncStats.numListThemeDownloadExceptions++;
            Timber.e("run(): FAILED to sync ListThemes. Exception: %s", e.getMessage());
        }

        try {
            BackendlessDataQuery listTitleDataQuery = new BackendlessDataQuery();
            listTitleDataQuery.setPageSize(PAGE_SIZE);
            BackendlessCollection<ListTitle> listTitlesCollection = Backendless.Data.of(ListTitle.class).find(listTitleDataQuery);
            Iterator<ListTitle> listTitleIterator;
            while (listTitlesCollection.getCurrentPage().size() > 0) {
                listTitleIterator = listTitlesCollection.getCurrentPage().iterator();
                while (listTitleIterator.hasNext()) {
                    ListTitle listTitle = listTitleIterator.next();
                    listTitlesCloudMap.put(listTitle.getUuid(), listTitle);
                }
                listTitlesCollection = listTitlesCollection.nextPage();
            }
            Timber.i("run(): Downloaded %d ListTitles from Backendless.", listTitlesCloudMap.size());
            downloadsOk[LIST_TITLES] = true;
        } catch (BackendlessException e) {
            syncStats.numListTitleBackendlessExceptions++;
            Timber.e("run(): FAILED to download ListTitles from Backendless. BackendlessException: %s", e.getMessage());
        } catch (Exception e) {
            syncStats.numListTitleDownloadExceptions++;
            Timber.e("run(): FAILED to sync ListTitles. Exception: %s", e.getMessage());
        }

        try {
            BackendlessDataQuery listTitlePositionDataQuery = new BackendlessDataQuery();
            listTitlePositionDataQuery.setPageSize(PAGE_SIZE);
            BackendlessCollection<ListTitlePosition> listTitlePositionsCollection = Backendless.Data.of(ListTitlePosition.class).find(listTitlePositionDataQuery);
            Iterator<ListTitlePosition> listTitlePositionIterator;
            while (listTitlePositionsCollection.getCurrentPage().size() > 0) {
                listTitlePositionIterator = listTitlePositionsCollection.getCurrentPage().iterator();
                while (listTitlePositionIterator.hasNext()) {
                    ListTitlePosition listTitlePosition = listTitlePositionIterator.next();
                    listTitlePositionsCloudMap.put(listTitlePosition.getUuid(), listTitlePosition);
                }
                listTitlePositionsCollection = listTitlePositionsCollection.nextPage();
            }
            Timber.i("run(): Downloaded %d ListTitlePositions from Backendless.", listTitlePositionsCloudMap.size());
            downloadsOk[LIST_TITLE_POSITIONS] = true;
        } catch (BackendlessException e) {
            syncStats.numListTitlePositionBackendlessExceptions++;
            Timber.e("run(): FAILED to download ListTitlePositions from Backendless. BackendlessException: %s", e.getMessage());
        } catch (Exception e) {
            syncStats.numListTitlePositionDownloadExceptions++;
            Timber.e("run(): FAILED to sync ListTitlePositions. Exception: %s", e.getMessage());
        }

        try {
            BackendlessDataQuery listItemDataQuery = new BackendlessDataQuery();
            listItemDataQuery.setPageSize(PAGE_SIZE);
            BackendlessCollection<ListItem> listItemsCollection = Backendless.Data.of(ListItem.class).find(listItemDataQuery);
            Iterator<ListItem> listItemIterator;
            while (listItemsCollection.getCurrentPage().size() > 0) {
                listItemIterator = listItemsCollection.getCurrentPage().iterator();
                while (listItemIterator.hasNext()) {
                    ListItem listItem = listItemIterator.next();
                    listItemsCloudMap.put(listItem.getUuid(), listItem);
                }
                listItemsCollection = listItemsCollection.nextPage();
            }
            Timber.i("run(): Downloaded %d ListItems from Backendless.", listItemsCloudMap.size());
            downloadsOk[LIST_ITEMS] = true;
        } catch (BackendlessException e) {
            syncStats.numListItemBackendlessExceptions++;
            Timber.e("run(): FAILED to download ListItems from Backendless. BackendlessException: %s", e.getMessage());
        } catch (Exception e) {
            syncStats.numListItemDownloadExceptions++;
            Timber.e("run(): FAILED to sync ListItems. Exception: %s", e.getMessage());
        }
        //endregion

        if (!computeDownloadsOk(downloadsOk)) {
            String errorMessage = "ABORTING SYNC OBJECTS FROM CLOUD. " +
                    "Failed to properly download all A1List objects from Backendless.";
            postOnSyncObjectsFromCloudFailed(errorMessage, syncStats);
            Timber.e("run(): Exception: %s.", errorMessage);
            return;
        }

        // HashMaps of local A1List objects
        List<AppSettings> appSettingsLocalList = getAppSettingsLocalList();
        Timber.i("run(): Retrieved %d AppSettings from SQLiteDb.", appSettingsLocalList.size());

        List<ListTheme> listThemesLocalList = getListThemeLocalList();
        Timber.i("run(): Retrieved %d ListThemes from SQLiteDb.", listThemesLocalList.size());

        List<ListTitle> listTitlesLocalList = getListTitleLocalList();
        Timber.i("run(): Retrieved %d ListTitles from SQLiteDb.", listTitlesLocalList.size());

        List<ListTitlePosition> listTitlePositionsLocalList = getListTitlePositionLocalList();
        Timber.i("run(): Retrieved %d ListTitlePositions from SQLiteDb.", listTitlePositionsLocalList.size());

        List<ListItem> listItemsLocalList = getListItemLocalList();
        Timber.i("run(): Retrieved %d ListItems from SQLiteDb.", listItemsLocalList.size());

        Timber.i("run(): Computing merge solutions...");
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        computeAppSettingsMergeSolution(appSettingsLocalList, appSettingsCloudMap, batch, syncStats);
        computeListThemeMergeSolution(listThemesLocalList, listThemesCloudMap, batch, syncStats);
        computeListTitleMergeSolution(listTitlesLocalList, listTitlesCloudMap, batch, syncStats);
        computeListTitlePositionMergeSolution(listTitlePositionsLocalList, listTitlePositionsCloudMap, batch, syncStats);


        // Merge solution ready. Applying batch update
        Timber.i("run(): Merge solution ready. Applying batch update");

        try {
            ContentResolver contentResolver = AndroidApplication.getContext().getContentResolver();
            contentResolver.applyBatch(A1List_ContentProvider.AUTHORITY, batch);
        } catch (RemoteException e) {
            Timber.e("run(): RemoteException: %s.", e.getMessage());
        } catch (OperationApplicationException e) {
            Timber.e("run(): OperationApplicationException: %s.", e.getMessage());
        }

        batch = new ArrayList<ContentProviderOperation>();
        computeListItemMergeSolution(listItemsLocalList, listItemsCloudMap, batch, syncStats);
        try {
            ContentResolver contentResolver = AndroidApplication.getContext().getContentResolver();
            contentResolver.applyBatch(A1List_ContentProvider.AUTHORITY, batch);
        } catch (RemoteException e) {
            Timber.e("run(): RemoteException: %s.", e.getMessage());
        } catch (OperationApplicationException e) {
            Timber.e("run(): OperationApplicationException: %s.", e.getMessage());
        }

        MySettings.setLastTimeSynced(System.currentTimeMillis());
        String stats = syncStats.getAllStats();
        postOnSyncObjectsFromCloudSuccess(stats, syncStats);
    }


    //region Merge Solutions
    private void computeAppSettingsMergeSolution(List<AppSettings> appSettingsLocalList,
                                                 HashMap<String, AppSettings> appSettingsCloudMap,
                                                 ArrayList<ContentProviderOperation> batch, SyncStats syncStats) {

        // Iterate through the AppSettings local list searching for a match in the cloud map
        // If match found, check to see if the local AppSettings needs to be updated
        // If match not found, the AppSettings is no longer exists in the cloud, so delete it from local storage

        for (AppSettings localAppSettings : appSettingsLocalList) {
            AppSettings cloudAppSettings = appSettingsCloudMap.get(localAppSettings.getUuid());
            if (cloudAppSettings != null) {
                // Match found. The AppSettings exist both locally and in the cloud.
                // Remove from the AppSettings from the map to prevent insert later.
                appSettingsCloudMap.remove(localAppSettings.getUuid());

                // Check to see if the local AppSettings needs to be updated

                if (appSettingsRequiresUpdating(localAppSettings, cloudAppSettings)) {
                    // Update existing record
                    Timber.i("computeAppSettingsMergeSolution(): Scheduling update for: \"%s\".",
                            localAppSettings.getName());
                    Uri existingAppSettingsUri = AppSettingsSqlTable.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(localAppSettings.getSQLiteId())).build();
                    ContentValues cvCloudAppSettings = AppSettingsRepository_Impl.makeContentValues(cloudAppSettings);
                    if (cvCloudAppSettings.containsKey(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY)) {
                        cvCloudAppSettings.remove(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY);
                        cvCloudAppSettings.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, FALSE);
                    }
                    batch.add(ContentProviderOperation.newUpdate(existingAppSettingsUri)
                            .withValues(cvCloudAppSettings)
                            .build());
                    syncStats.numAppSettingsUpdates++;
                } else {
                    // Local AppSettings do not need updating
                    Timber.i("computeAppSettingsMergeSolution(): No update required for \"%s\" AppSettings.",
                            localAppSettings.getName());
                    syncStats.numAppSettingsNoUpdateRequired++;
                    // If dates are not the same then set local date equal to cloud date
                    Date cloudAppSettingsDate = cloudAppSettings.getUpdated();
                    if (cloudAppSettingsDate == null) {
                        cloudAppSettingsDate = cloudAppSettings.getCreated();
                    }
                    if (!cloudAppSettingsDate.equals(localAppSettings.getUpdated())) {
                        Timber.d("computeAppSettingsMergeSolution(): Cloud and local AppSettings dates not the same. Updating \"%s\""
                                , localAppSettings.getName());
                        Uri existingAppSettingsUri = AppSettingsSqlTable.CONTENT_URI.buildUpon()
                                .appendPath(String.valueOf(localAppSettings.getSQLiteId())).build();
                        ContentValues cv = new ContentValues();
                        cv.put(AppSettingsSqlTable.COL_UPDATED, cloudAppSettingsDate.getTime());
                        cv.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, FALSE);
                        batch.add(ContentProviderOperation.newUpdate(existingAppSettingsUri)
                                .withValues(cv).build());
                    }
                }
            } else {
                // Match NOT found. The AppSettings exist only locally and NOT in the cloud.
                // So, remove it from the database.
                Timber.i("computeAppSettingsMergeSolution(): Scheduling deletion for: \"%s\".",
                        localAppSettings.getName());
                Uri deleteAppSettingsUri = AppSettingsSqlTable.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(localAppSettings.getSQLiteId())).build();

                batch.add(ContentProviderOperation.newDelete(deleteAppSettingsUri).build());
                syncStats.numAppSettingsDeletes++;
            }
        }
        // Any remaining AppSettings in the appSettingsCloudMap need to be inserted into local storage
        for (AppSettings newAppSettings : appSettingsCloudMap.values()) {
            Timber.i("computeAppSettingsMergeSolution(): Scheduling insertion for: \"%s\".",
                    newAppSettings.getName());
            ContentValues cvNewAppSettings = AppSettingsRepository_Impl.makeContentValues(newAppSettings);
            if (cvNewAppSettings.containsKey(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY)) {
                cvNewAppSettings.remove(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY);
                cvNewAppSettings.put(AppSettingsSqlTable.COL_APP_SETTINGS_DIRTY, FALSE);
            }
            batch.add(ContentProviderOperation.newInsert(AppSettingsSqlTable.CONTENT_URI)
                    .withValues(cvNewAppSettings)
                    .build());
            syncStats.numAppSettingsInserts++;
        }
    }

    private void computeListThemeMergeSolution(List<ListTheme> listThemeLocalList,
                                               HashMap<String, ListTheme> listThemeCloudMap,
                                               ArrayList<ContentProviderOperation> batch, SyncStats syncStats) {

        // Iterate through the ListTheme local list searching for a match in the cloud map
        // If match found, check to see if the local ListTheme needs to be updated
        // If match not found, the ListTheme is no longer exists in the cloud, so delete it from local storage

        for (ListTheme localListTheme : listThemeLocalList) {
            ListTheme cloudListTheme = listThemeCloudMap.get(localListTheme.getUuid());
            if (cloudListTheme != null) {
                // Match found. The ListTheme exist both locally and in the cloud.
                // Remove from the ListTheme from the map to prevent insert later.
                listThemeCloudMap.remove(localListTheme.getUuid());

                // Check to see if the local ListTheme needs to be updated
                if (listThemeRequiresUpdating(localListTheme, cloudListTheme)) {
                    // Update existing record
                    Timber.i("computeListThemeMergeSolution(): Scheduling update for: \"%s\".",
                            localListTheme.getName());
                    Uri existingListThemeUri = ListThemesSqlTable.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(localListTheme.getSQLiteId())).build();
                    ContentValues cvCloudListTheme = ListThemeRepository_Impl.makeListThemeContentValues(cloudListTheme);
                    if (cvCloudListTheme.containsKey(ListThemesSqlTable.COL_THEME_DIRTY)) {
                        cvCloudListTheme.remove(ListThemesSqlTable.COL_THEME_DIRTY);
                        cvCloudListTheme.put(ListThemesSqlTable.COL_THEME_DIRTY, FALSE);
                    }
                    batch.add(ContentProviderOperation.newUpdate(existingListThemeUri)
                            .withValues(cvCloudListTheme)
                            .build());
                    syncStats.numListThemeUpdates++;
                } else {
                    // Local ListTheme do not need updating
                    Timber.i("computeListThemeMergeSolution(): No update required for \"%s\".",
                            localListTheme.getName());
                    syncStats.numListThemeNoUpdateRequired++;
                    // If dates are not the same then set local date equal to cloud date
                    Date cloudListThemeDate = cloudListTheme.getUpdated();
                    if (cloudListThemeDate == null) {
                        cloudListThemeDate = cloudListTheme.getCreated();
                    }
                    if (!cloudListThemeDate.equals(localListTheme.getUpdated())) {
                        Timber.d("computeListThemeMergeSolution(): Cloud and local ListTheme dates not the same. Updating \"%s\""
                                , localListTheme.getName());
                        Uri existingListThemeUri = ListThemesSqlTable.CONTENT_URI.buildUpon()
                                .appendPath(String.valueOf(localListTheme.getSQLiteId())).build();
                        ContentValues cv = new ContentValues();
                        cv.put(ListThemesSqlTable.COL_UPDATED, cloudListThemeDate.getTime());
                        cv.put(ListThemesSqlTable.COL_THEME_DIRTY, FALSE);
                        batch.add(ContentProviderOperation.newUpdate(existingListThemeUri)
                                .withValues(cv).build());
                    }
                }
            } else {
                // Match NOT found. The ListTheme exist only locally and NOT in the cloud.
                // So, remove it from the database.
                Timber.i("computeListThemeMergeSolution(): Scheduling deletion for: \"%s\".",
                        localListTheme.getName());
                Uri deleteListThemeUri = ListThemesSqlTable.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(localListTheme.getSQLiteId())).build();

                batch.add(ContentProviderOperation.newDelete(deleteListThemeUri).build());
                syncStats.numListThemeDeletes++;
            }
        }
        // Any remaining ListTheme in the listThemeCloudMap need to be inserted into local storage
        for (ListTheme newListTheme : listThemeCloudMap.values()) {
            Timber.i("computeListThemeMergeSolution(): Scheduling insertion for: \"%s\".",
                    newListTheme.getName());
            ContentValues cvNewListTheme = ListThemeRepository_Impl.makeListThemeContentValues(newListTheme);
            if (cvNewListTheme.containsKey(ListThemesSqlTable.COL_THEME_DIRTY)) {
                cvNewListTheme.remove(ListThemesSqlTable.COL_THEME_DIRTY);
                cvNewListTheme.put(ListThemesSqlTable.COL_THEME_DIRTY, FALSE);
            }
            batch.add(ContentProviderOperation.newInsert(ListThemesSqlTable.CONTENT_URI)
                    .withValues(cvNewListTheme)
                    .build());
            syncStats.numListThemeInserts++;
        }
    }

    private void computeListTitleMergeSolution(List<ListTitle> listTitleLocalList,
                                               HashMap<String, ListTitle> listTitleCloudMap,
                                               ArrayList<ContentProviderOperation> batch, SyncStats syncStats) {

        // Iterate through the ListTitle local list searching for a match in the cloud map
        // If match found, check to see if the local ListTitle needs to be updated
        // If match not found, the ListTitle is no longer exists in the cloud, so delete it from local storage

        for (ListTitle localListTitle : listTitleLocalList) {
            try {
                ListTitle cloudListTitle = listTitleCloudMap.get(localListTitle.getUuid());
                if (cloudListTitle != null) {
                    // Match found. The ListTitle exist both locally and in the cloud.
                    // Remove from the ListTitle from the map to prevent insert later.
                    listTitleCloudMap.remove(localListTitle.getUuid());

                    // Check to see if the local ListTitle needs to be updated
                    if (listTitleRequiresUpdating(localListTitle, cloudListTitle)) {
                        // Update existing record
                        Timber.i("computeListTitleMergeSolution(): Scheduling update for: \"%s\".",
                                localListTitle.getName());
                        Uri existingListTitleUri = ListTitlesSqlTable.CONTENT_URI.buildUpon()
                                .appendPath(String.valueOf(localListTitle.getSQLiteId())).build();
                        ContentValues cvCloudListTitle = ListTitleRepository_Impl.makeListTitleContentValues(cloudListTitle);
                        if (cvCloudListTitle.containsKey(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY)) {
                            cvCloudListTitle.remove(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY);
                            cvCloudListTitle.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, FALSE);
                        }
                        batch.add(ContentProviderOperation.newUpdate(existingListTitleUri)
                                .withValues(cvCloudListTitle)
                                .build());
                        syncStats.numListTitleUpdates++;
                    } else {
                        // Local ListTitle do not need updating
                        Timber.i("computeListTitleMergeSolution(): No update required for \"%s\".",
                                localListTitle.getName());
                        syncStats.numListTitleNoUpdateRequired++;

                        // If dates are not the same then set local date equal to cloud date
                        Date cloudListTitleDate = cloudListTitle.getUpdated();
                        if (cloudListTitleDate == null) {
                            cloudListTitleDate = cloudListTitle.getCreated();
                        }
                        if (!cloudListTitleDate.equals(localListTitle.getUpdated())) {
                            Timber.d("computeListTitleMergeSolution(): Cloud and local ListTitle dates not the same. Updating \"%s\""
                                    , localListTitle.getName());
                            Uri existingListTitleUri = ListTitlesSqlTable.CONTENT_URI.buildUpon()
                                    .appendPath(String.valueOf(localListTitle.getSQLiteId())).build();
                            ContentValues cv = new ContentValues();
                            cv.put(ListTitlesSqlTable.COL_UPDATED, cloudListTitleDate.getTime());
                            cv.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, FALSE);
                            batch.add(ContentProviderOperation.newUpdate(existingListTitleUri)
                                    .withValues(cv).build());
                        }
                    }
                } else {
                    // Match NOT found. The ListTitle exist only locally and NOT in the cloud.
                    // So, remove it from the database.
                    Timber.i("computeListTitleMergeSolution(): Scheduling deletion for: \"%s\".",
                            localListTitle.getName());
                    Uri deleteListTitleUri = ListTitlesSqlTable.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(localListTitle.getSQLiteId())).build();

                    batch.add(ContentProviderOperation.newDelete(deleteListTitleUri).build());
                    syncStats.numListTitleDeletes++;
                }
            } catch (Exception e) {
                Timber.e("computeListTitleMergeSolution(): Exception: %s.", e.getMessage());
            }
        }
        // Any remaining ListTitle in the listTitleCloudMap need to be inserted into local storage
        for (ListTitle newListTitle : listTitleCloudMap.values()) {
            try {
                Timber.i("computeListTitleMergeSolution(): Scheduling insertion for: \"%s\".",
                        newListTitle.getName());
                ContentValues cvNewListTitle = ListTitleRepository_Impl.makeListTitleContentValues(newListTitle);
                if (cvNewListTitle.containsKey(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY)) {
                    cvNewListTitle.remove(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY);
                    cvNewListTitle.put(ListTitlesSqlTable.COL_LIST_TITLE_DIRTY, FALSE);
                }
                batch.add(ContentProviderOperation.newInsert(ListTitlesSqlTable.CONTENT_URI)
                        .withValues(cvNewListTitle)
                        .build());
                syncStats.numListTitleInserts++;
            } catch (Exception e) {
                Timber.e("computeListTitleMergeSolution(): Exception: %s.", e.getMessage());
            }
        }
    }

    private void computeListTitlePositionMergeSolution(List<ListTitlePosition> listTitlePositionLocalList,
                                                       HashMap<String, ListTitlePosition> listTitlePositionCloudMap,
                                                       ArrayList<ContentProviderOperation> batch, SyncStats syncStats) {

        // Iterate through the ListTitlePosition local list searching for a match in the cloud map
        // If match found, check to see if the local ListTitlePosition needs to be updated
        // If match not found, the ListTitlePosition is no longer exists in the cloud, so delete it from local storage

        for (ListTitlePosition localListTitlePosition : listTitlePositionLocalList) {
            try {
                ListTitlePosition cloudListTitlePosition = listTitlePositionCloudMap.get(localListTitlePosition.getUuid());
                if (cloudListTitlePosition != null) {
                    // Match found. The ListTitlePosition exist both locally and in the cloud.
                    // Remove from the ListTitlePosition from the map to prevent insert later.
                    listTitlePositionCloudMap.remove(localListTitlePosition.getUuid());

                    // Check to see if the local ListTitlePosition needs to be updated
                    if (listTitlePositionRequiresUpdating(localListTitlePosition, cloudListTitlePosition)) {
                        // Update existing record
                        Timber.i("computeListTitlePositionMergeSolution(): Scheduling update for ListPosition ListTitleUuid = %s",
                                localListTitlePosition.getListTitleUuid());
                        Uri existingListTitlePositionUri = ListTitlePositionsSqlTable.CONTENT_URI.buildUpon()
                                .appendPath(String.valueOf(localListTitlePosition.getSQLiteId())).build();
                        ContentValues cvCloudListTitlePosition = ListTitleRepository_Impl.makeListTitlePositionContentValues(cloudListTitlePosition);
                        if (cvCloudListTitlePosition.containsKey(ListTitlePositionsSqlTable.COL_LIST_TITLE_POSITION_DIRTY)) {
                            cvCloudListTitlePosition.remove(ListTitlePositionsSqlTable.COL_LIST_TITLE_POSITION_DIRTY);
                            cvCloudListTitlePosition.put(ListTitlePositionsSqlTable.COL_LIST_TITLE_POSITION_DIRTY, FALSE);
                        }
                        batch.add(ContentProviderOperation.newUpdate(existingListTitlePositionUri)
                                .withValues(cvCloudListTitlePosition)
                                .build());
                        syncStats.numListTitlePositionUpdates++;
                    } else {
                        // Local ListTitlePosition does not need updating
                        Timber.i("computeListTitlePositionMergeSolution(): No update required for ListTitlePosition with ListTitle uuid = %s",
                                localListTitlePosition.getListTitleUuid());
                        syncStats.numListTitlePositionNoUpdateRequired++;

                        // If dates are not the same then set local date equal to cloud date
                        Date cloudListTitlePositionDate = cloudListTitlePosition.getUpdated();
                        if (cloudListTitlePositionDate == null) {
                            cloudListTitlePositionDate = cloudListTitlePosition.getCreated();
                        }
                        if (!cloudListTitlePositionDate.equals(localListTitlePosition.getUpdated())) {
                            Timber.d("computeListTitlePositionMergeSolution(): Cloud and local ListTitlePosition dates not the same. Updating ListTitlePosition with ListTitle uuid = %s",
                                    localListTitlePosition.getListTitleUuid());
                            Uri existingListTitlePositionUri = ListTitlePositionsSqlTable.CONTENT_URI.buildUpon()
                                    .appendPath(String.valueOf(localListTitlePosition.getSQLiteId())).build();
                            ContentValues cv = new ContentValues();
                            cv.put(ListTitlePositionsSqlTable.COL_UPDATED, cloudListTitlePositionDate.getTime());
                            cv.put(ListTitlePositionsSqlTable.COL_LIST_TITLE_POSITION_DIRTY, FALSE);
                            batch.add(ContentProviderOperation.newUpdate(existingListTitlePositionUri)
                                    .withValues(cv).build());
                        }
                    }
                } else {
                    // Match NOT found. The ListTitlePosition exist only locally and NOT in the cloud.
                    // So, remove it from the database.
                    Timber.i("computeListTitlePositionMergeSolution(): Scheduling deletion for ListTitlePosition with ListTitle uuid = %s",
                            localListTitlePosition.getListTitleUuid());
                    Uri deleteListTitlePositionUri = ListTitlePositionsSqlTable.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(localListTitlePosition.getSQLiteId())).build();

                    batch.add(ContentProviderOperation.newDelete(deleteListTitlePositionUri).build());
                    syncStats.numListTitlePositionDeletes++;
                }
            } catch (Exception e) {
                Timber.e("computeListTitlePositionMergeSolution(): Exception: %s.", e.getMessage());
            }
        }
        // Any remaining ListTitlePosition in the listTitlePositionCloudMap need to be inserted into local storage
        for (ListTitlePosition newListTitlePosition : listTitlePositionCloudMap.values()) {
            try {
                Timber.i("computeListTitlePositionMergeSolution(): Scheduling insertion for ListTitlePosition with ListTitle uuid = %s",
                        newListTitlePosition.getListTitleUuid());
                ContentValues cvNewListTitlePosition = ListTitleRepository_Impl.makeListTitlePositionContentValues(newListTitlePosition);
                if (cvNewListTitlePosition.containsKey(ListTitlePositionsSqlTable.COL_LIST_TITLE_POSITION_DIRTY)) {
                    cvNewListTitlePosition.remove(ListTitlePositionsSqlTable.COL_LIST_TITLE_POSITION_DIRTY);
                    cvNewListTitlePosition.put(ListTitlePositionsSqlTable.COL_LIST_TITLE_POSITION_DIRTY, FALSE);
                }
                batch.add(ContentProviderOperation.newInsert(ListTitlePositionsSqlTable.CONTENT_URI)
                        .withValues(cvNewListTitlePosition)
                        .build());
                syncStats.numListTitlePositionInserts++;
            } catch (Exception e) {
                Timber.e("computeListTitlePositionMergeSolution(): Exception: %s.", e.getMessage());
            }
        }
    }


    private void computeListItemMergeSolution(List<ListItem> listItemLocalList,
                                              HashMap<String, ListItem> listItemCloudMap,
                                              ArrayList<ContentProviderOperation> batch, SyncStats syncStats) {

        // Iterate through the ListItem local list searching for a match in the cloud map
        // If match found, check to see if the local ListItem needs to be updated
        // If match not found, the ListItem is no longer exists in the cloud, so delete it from local storage

        for (ListItem localListItem : listItemLocalList) {
            ListItem cloudListItem = listItemCloudMap.get(localListItem.getUuid());
            if (cloudListItem != null) {
                // Match found. The ListItem exist both locally and in the cloud.
                // Remove from the ListItem from the map to prevent insert later.
                listItemCloudMap.remove(localListItem.getUuid());

                // Check to see if the local ListItem needs to be updated
                if (listItemRequiresUpdating(localListItem, cloudListItem)) {
                    // Update existing record
                    Timber.i("computeListItemMergeSolution(): Scheduling update for: \"%s\".",
                            localListItem.getName());
                    Uri existingListItemUri = ListItemsSqlTable.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(localListItem.getSQLiteId())).build();
                    ContentValues cvCloudListItem = ListItemRepository_Impl.makeListItemContentValues(cloudListItem);
                    if (cvCloudListItem.containsKey(ListItemsSqlTable.COL_LIST_ITEM_DIRTY)) {
                        cvCloudListItem.remove(ListItemsSqlTable.COL_LIST_ITEM_DIRTY);
                        cvCloudListItem.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, FALSE);
                    }
                    batch.add(ContentProviderOperation.newUpdate(existingListItemUri)
                            .withValues(cvCloudListItem).build());
                    syncStats.numListItemUpdates++;
                } else {
                    // Local ListItem does not need updating
                    Timber.i("computeListItemMergeSolution(): No update required for \"%s\".",
                            localListItem.getName());
                    syncStats.numListItemNoUpdateRequired++;
                    // If dates are not the same then set local date equal to cloud date
                    Date cloudListItemDate = cloudListItem.getUpdated();
                    if (cloudListItemDate == null) {
                        cloudListItemDate = cloudListItem.getCreated();
                    }
                    if (!cloudListItemDate.equals(localListItem.getUpdated())) {
                        Timber.d("computeListItemMergeSolution(): Cloud and local ListItem dates not the same. Updating \"%s\""
                                , localListItem.getName());
                        Uri existingListItemUri = ListItemsSqlTable.CONTENT_URI.buildUpon()
                                .appendPath(String.valueOf(localListItem.getSQLiteId())).build();
                        ContentValues cv = new ContentValues();
                        cv.put(ListItemsSqlTable.COL_UPDATED, cloudListItemDate.getTime());
                        cv.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, FALSE);
                        batch.add(ContentProviderOperation.newUpdate(existingListItemUri)
                                .withValues(cv).build());
                    }
                }
            } else {
                // Match NOT found. The ListItem exist only locally and NOT in the cloud.
                // So, remove it from the database.
                Timber.i("computeListItemMergeSolution(): Scheduling deletion for: \"%s\".",
                        localListItem.getName());
                Uri deleteListItemUri = ListItemsSqlTable.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(localListItem.getSQLiteId())).build();

                batch.add(ContentProviderOperation.newDelete(deleteListItemUri).build());
                syncStats.numListItemDeletes++;
            }
        }
        // Any remaining ListItem in the listItemCloudMap need to be inserted into local storage
        for (ListItem newListItem : listItemCloudMap.values()) {
            Timber.i("computeListItemMergeSolution(): Scheduling insertion for: \"%s\".",
                    newListItem.getName());
            ContentValues cvNewListItem = ListItemRepository_Impl.makeListItemContentValues(newListItem);
            if (cvNewListItem.containsKey(ListItemsSqlTable.COL_LIST_ITEM_DIRTY)) {
                cvNewListItem.remove(ListItemsSqlTable.COL_LIST_ITEM_DIRTY);
                cvNewListItem.put(ListItemsSqlTable.COL_LIST_ITEM_DIRTY, FALSE);
            }
            batch.add(ContentProviderOperation.newInsert(ListItemsSqlTable.CONTENT_URI)
                    .withValues(cvNewListItem)
                    .build());
            syncStats.numListItemInserts++;
        }
    }

    private boolean appSettingsRequiresUpdating(AppSettings localAppSettings, AppSettings cloudAppSettings) {

        Date cloudAppSettingsDate = cloudAppSettings.getUpdated();
        if (cloudAppSettingsDate == null) {
            cloudAppSettingsDate = cloudAppSettings.getCreated();
        }
        if (cloudAppSettingsDate.after(localAppSettings.getUpdated())) {
            if (!cloudAppSettings.getLastListTitleViewedUuid().equals(localAppSettings.getLastListTitleViewedUuid())) {
                // TODO: verify that all AppSettings fields have been checked.
                return true;
            } else if (cloudAppSettings.getTimeBetweenSynchronizations() != localAppSettings.getTimeBetweenSynchronizations()) {
                return true;
            } else if (!cloudAppSettings.isListTitlesSortedAlphabetically() == localAppSettings.isListTitlesSortedAlphabetically()) {
                return true;
            } else if (!cloudAppSettings.getName().equals(localAppSettings.getName())) {
                return true;
            } else if (cloudAppSettings.getListTitleLastSortKey() != localAppSettings.getListTitleLastSortKey()) {
                return true;
            }
        }

        return false;
    }

    private boolean listThemeRequiresUpdating(ListTheme localListTheme, ListTheme cloudListTheme) {

        Date cloudListThemeDate = cloudListTheme.getUpdated();
        if (cloudListThemeDate == null) {
            cloudListThemeDate = cloudListTheme.getCreated();
        }

        if (cloudListThemeDate.after(localListTheme.getUpdated())) {
            // TODO: verify that all ListTheme fields have been checked.

            if (!cloudListTheme.isStruckOut() == localListTheme.isStruckOut()) {
                return true;
            } else if (!cloudListTheme.getName().equals(localListTheme.getName())) {
                return true;
            } else if (!cloudListTheme.isMarkedForDeletion() == localListTheme.isMarkedForDeletion()) {
                return true;
            } else if (!cloudListTheme.isBold() == localListTheme.isBold()) {
                return true;
            } else if (!cloudListTheme.isDefaultTheme() == localListTheme.isDefaultTheme()) {
                return true;
            } else if (!cloudListTheme.isTransparent() == localListTheme.isTransparent()) {
                return true;
            } else if (!cloudListTheme.isChecked() == localListTheme.isChecked()) {
                return true;
            } else if (cloudListTheme.getStartColor() != localListTheme.getStartColor()) {
                return true;
            } else if (cloudListTheme.getEndColor() != localListTheme.getEndColor()) {
                return true;
            } else if (cloudListTheme.getTextColor() != localListTheme.getTextColor()) {
                return true;
            } else if (cloudListTheme.getTextSize() != localListTheme.getTextSize()) {
                return true;
            } else if (cloudListTheme.getHorizontalPaddingInDp() != localListTheme.getHorizontalPaddingInDp()) {
                return true;
            } else if (cloudListTheme.getVerticalPaddingInDp() != localListTheme.getVerticalPaddingInDp()) {
                return true;
            }
        }

        return false;
    }

    private boolean listTitleRequiresUpdating(ListTitle localListTitle, ListTitle cloudListTitle) {

        Date cloudListTitleDate = cloudListTitle.getUpdated();
        if (cloudListTitleDate == null) {
            cloudListTitleDate = cloudListTitle.getCreated();
        }

        if (cloudListTitleDate.after(localListTitle.getUpdated())) {
            if (!cloudListTitle.isStruckOut() == localListTitle.isStruckOut()) {
                return true;
            } else if (!cloudListTitle.getName().equals(localListTitle.getName())) {
                return true;
            } else if (!cloudListTitle.getListThemeUuid().equals(localListTitle.getListThemeUuid())) {
                return true;
            } else if (!cloudListTitle.isMarkedForDeletion() == localListTitle.isMarkedForDeletion()) {
                return true;
            } else if (!cloudListTitle.isSortListItemsAlphabetically() == localListTitle.isSortListItemsAlphabetically()) {
                return true;
            } else if (!cloudListTitle.isChecked() == localListTitle.isChecked()) {
                return true;
            } else if (cloudListTitle.getManualSortKey() != localListTitle.getManualSortKey()) {
                return true;
            } else if (!cloudListTitle.getListLockString().equals(localListTitle.getListLockString())) {
                return true;
            } else if (!cloudListTitle.isListLocked() == localListTitle.isListLocked()) {
                return true;
            } else if (!cloudListTitle.isListPrivateToThisDevice() == localListTitle.isListPrivateToThisDevice()) {
                return true;
            } else if (cloudListTitle.getListItemLastSortKey() != localListTitle.getListItemLastSortKey()) {
                return true;
            }
        }

        return false;
    }

    private boolean listTitlePositionRequiresUpdating(ListTitlePosition localListTitlePosition, ListTitlePosition cloudListTitlePosition) {

        Date cloudListTitlePositionDate = cloudListTitlePosition.getUpdated();
        if (cloudListTitlePositionDate == null) {
            cloudListTitlePositionDate = cloudListTitlePosition.getCreated();
        }

        if (cloudListTitlePositionDate.after(localListTitlePosition.getUpdated())) {
            if (!cloudListTitlePosition.getListTitleUuid().equals(localListTitlePosition.getListTitleUuid())) {
                return true;
            } else if (cloudListTitlePosition.getListViewFirstVisiblePosition() != localListTitlePosition.getListViewFirstVisiblePosition()) {
                return true;
            }else if (cloudListTitlePosition.getListViewTop() != localListTitlePosition.getListViewTop()) {
                return true;
            }
        }

        return false;
    }

    private boolean listItemRequiresUpdating(ListItem localListItem, ListItem cloudListItem) {


        Date cloudListItemDate = cloudListItem.getUpdated();
        if (cloudListItemDate == null) {
            cloudListItemDate = cloudListItem.getCreated();
        }

        if (cloudListItemDate.after(localListItem.getUpdated())) {
            // TODO: verify that all ListItem fields have been checked.
            if (!cloudListItem.isStruckOut() == localListItem.isStruckOut()) {
                return true;
            } else if (!cloudListItem.getName().equals(localListItem.getName())) {
                return true;
            } else if (!cloudListItem.retrieveListTitle().getUuid().equals(localListItem.retrieveListTitle().getUuid())) {
                return true;
            } else if (!cloudListItem.isMarkedForDeletion() == localListItem.isMarkedForDeletion()) {
                return true;
            } else if (!cloudListItem.isFavorite() == localListItem.isFavorite()) {
                return true;
            } else if (!cloudListItem.isChecked() == localListItem.isChecked()) {
                return true;
            } else if (cloudListItem.getManualSortKey() != localListItem.getManualSortKey()) {
                return true;
            }
        }

        return false;
    }

    //endregion

    //region Creation of local Lists of A1List objects
    private List<AppSettings> getAppSettingsLocalList() {
        List<AppSettings> appSettingsLocalList = new ArrayList<>();

        Cursor cursor = null;
        Uri uri = AppSettingsSqlTable.CONTENT_URI;
        String[] projection = AppSettingsSqlTable.PROJECTION_ALL;
        String selection = null;
        String selectionArgs[] = null;
        String sortOrder = null;

        ContentResolver cr = AndroidApplication.getContext().getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getAppSettingsLocalList(): Exception: %s.", e.getMessage());
        }

        AppSettings appSettings;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                appSettings = AppSettingsRepository_Impl.appSettingsFromCursor(cursor);
                appSettingsLocalList.add(appSettings);
            }
            cursor.close();
        }
        return appSettingsLocalList;
    }

    private List<ListTheme> getListThemeLocalList() {
        List<ListTheme> ListThemeLocalList = new ArrayList<>();

        Cursor cursor = null;
        Uri uri = ListThemesSqlTable.CONTENT_URI;
        String[] projection = ListThemesSqlTable.PROJECTION_ALL;
        String selection = null;
        String selectionArgs[] = null;
        String sortOrder = null;

        ContentResolver cr = AndroidApplication.getContext().getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getListThemeLocalList(): Exception: %s.", e.getMessage());
        }

        ListTheme ListTheme;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ListTheme = ListThemeRepository_Impl.listThemeFromCursor(cursor);
                ListThemeLocalList.add(ListTheme);
            }
            cursor.close();
        }
        return ListThemeLocalList;
    }

    private List<ListTitle> getListTitleLocalList() {
        List<ListTitle> ListTitleLocalList = new ArrayList<>();

        Cursor cursor = null;
        Uri uri = ListTitlesSqlTable.CONTENT_URI;
        String[] projection = ListTitlesSqlTable.PROJECTION_ALL;
        String selection = null;
        String selectionArgs[] = null;
        String sortOrder = null;

        ContentResolver cr = AndroidApplication.getContext().getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getListTitleLocalList(): Exception: %s.", e.getMessage());
        }

        ListTitle ListTitle;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ListTitle = ListTitleRepository_Impl.listTitleFromCursor(cursor);
                ListTitleLocalList.add(ListTitle);
            }
            cursor.close();
        }
        return ListTitleLocalList;
    }

    private List<ListTitlePosition> getListTitlePositionLocalList() {
        List<ListTitlePosition> ListTitlePositionLocalList = new ArrayList<>();

        Cursor cursor = null;
        Uri uri = ListTitlePositionsSqlTable.CONTENT_URI;
        String[] projection = ListTitlePositionsSqlTable.PROJECTION_ALL;
        String selection = null;
        String selectionArgs[] = null;
        String sortOrder = null;

        ContentResolver cr = AndroidApplication.getContext().getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getListTitlePositionLocalList(): Exception: %s.", e.getMessage());
        }

        ListTitlePosition ListTitlePosition;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ListTitlePosition = ListTitleRepository_Impl.listTitlePositionFromCursor(cursor);
                ListTitlePositionLocalList.add(ListTitlePosition);
            }
            cursor.close();
        }
        return ListTitlePositionLocalList;
    }

    private List<ListItem> getListItemLocalList() {
        List<ListItem> ListItemLocalList = new ArrayList<>();

        Cursor cursor = null;
        Uri uri = ListItemsSqlTable.CONTENT_URI;
        String[] projection = ListItemsSqlTable.PROJECTION_ALL;
        String selection = null;
        String selectionArgs[] = null;
        String sortOrder = null;

        ContentResolver cr = AndroidApplication.getContext().getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getListItemLocalList(): Exception: %s.", e.getMessage());
        }

        ListItem ListItem;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ListItem = ListItemRepository_Impl.listItemFromCursor(cursor);
                ListItemLocalList.add(ListItem);
            }
            cursor.close();
        }
        return ListItemLocalList;
    }

    //endregion


    private boolean computeDownloadsOk(boolean[] downloadsOk) {
        boolean result = true;
        for (int i = 0; i < downloadsOk.length; i++) {
            result = result && downloadsOk[i];
        }
        return result;
    }


    private void postOnSyncObjectsFromCloudSuccess(final String successMessage, final SyncStats syncStats) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onSyncObjectsFromCloudSuccess(successMessage, syncStats);
            }
        });
    }

    private void postOnSyncObjectsFromCloudFailed(final String errorMessage, final SyncStats syncStats) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onSyncObjectsFromCloudFailed(errorMessage, syncStats);
            }
        });
    }
}
