package com.lbconsulting.a1list.domain.repositories;

import com.lbconsulting.a1list.domain.model.AppSettings;

/**
 * Repository interface for AppSetting
 */
public interface AppSettingsRepository {

    //region Insert AppSettings
    boolean insert(AppSettings appSettings);

    boolean insertIntoLocalStorage(AppSettings appSettings);

    void insertInCloud(AppSettings appSettings);
    //endregion

    //region Retrieve AppSettings
    AppSettings retrieveAppSettings();

    AppSettings retrieveDirtyAppSettings();

    long retrieveNextListTitleSortKey();

    long retrieveTimeBetweenSynchronizations();
    //endregion

    //region Update AppSettings
    void update(AppSettings appSettings);

    int updateInLocalStorage(AppSettings appSettings);

    void updateInCloud(AppSettings appSettings);
    //endregion

}
