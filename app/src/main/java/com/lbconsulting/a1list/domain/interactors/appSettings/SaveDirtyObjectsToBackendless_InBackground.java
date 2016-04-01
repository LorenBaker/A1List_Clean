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

import java.util.ArrayList;
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
            if (dirtyAppSettings.getObjectId() == null || dirtyAppSettings.getObjectId().isEmpty()) {
                appSettingsRepository.insertInCloud(dirtyAppSettings);
            } else {
                appSettingsRepository.updateInCloud(dirtyAppSettings, false);
            }
        } else {
            Timber.i("SaveDirtyObjectsToBackendless(): No AppSettings to save.");
        }

        if (dirtyListListThemes.size() > 0) {
            List<ListTheme> listThemesNoObjectId = new ArrayList<>();
            List<ListTheme> listThemesWithObjectId = new ArrayList<>();
            for (ListTheme listTheme : dirtyListListThemes) {
                if (listTheme.getObjectId() == null || listTheme.getObjectId().isEmpty()) {
                    listThemesNoObjectId.add(listTheme);
                } else {
                    listThemesWithObjectId.add(listTheme);
                }
            }
            listThemeRepository.insertInCloud(listThemesNoObjectId);
            listThemeRepository.updateInCloud(listThemesWithObjectId, false);
            
        } else {
            Timber.i("SaveDirtyObjectsToBackendless(): No ListThemes to save.");
        }

        if (dirtyListTitles.size() > 0) {
            List<ListTitle> listTitlesNoObjectId = new ArrayList<>();
            List<ListTitle> listTitlesWithObjectId = new ArrayList<>();
            for (ListTitle listTitle : dirtyListTitles) {
                if (listTitle.getObjectId() == null || listTitle.getObjectId().isEmpty()) {
                    listTitlesNoObjectId.add(listTitle);
                } else {
                    listTitlesWithObjectId.add(listTitle);
                }
            }
            listTitleRepository.insertInCloud(listTitlesNoObjectId);
            listTitleRepository.updateInCloud(listTitlesWithObjectId, false);

        } else {
            Timber.i("SaveDirtyObjectsToBackendless(): No ListTitles to save.");
        }

        if (dirtyListItems.size() > 0) {
            List<ListItem> listItemsNoObjectId = new ArrayList<>();
            List<ListItem> listItemsWithObjectId = new ArrayList<>();
            for (ListItem listItem : dirtyListItems) {
                if (listItem.getObjectId() == null || listItem.getObjectId().isEmpty()) {
                    listItemsNoObjectId.add(listItem);
                } else {
                    listItemsWithObjectId.add(listItem);
                }
            }
            listItemRepository.insertInCloud(listItemsNoObjectId);
            listItemRepository.updateInCloud(listItemsWithObjectId, false);

        } else {
            Timber.i("SaveDirtyObjectsToBackendless(): No ListItems to save.");
        }

    }

}
