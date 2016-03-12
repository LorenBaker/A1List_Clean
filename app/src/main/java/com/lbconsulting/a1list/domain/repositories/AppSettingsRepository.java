package com.lbconsulting.a1list.domain.repositories;

import android.content.ContentValues;

import com.lbconsulting.a1list.domain.model.AppSettings;

/**
 * Repository interface for AppSetting
 */
public interface AppSettingsRepository {

    AppSettings insert(AppSettings appSettings);

    boolean update(AppSettings appSettings, ContentValues contentValues);

    boolean update(AppSettings appSettings);

    AppSettings retrieveAppSettings();

    long retrieveTimeBetweenSynchronizations();

    long retrieveListTitleNextSortKey();

    void setListTitleLastSortKey(AppSettings appSettings,long sortKey);

    AppSettings retrieveDirtyAppSettings();
}
