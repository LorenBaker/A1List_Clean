package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.appSettings.SaveAppSettingsToBackendless;
import com.lbconsulting.a1list.domain.interactors.appSettings.SaveDirtyObjectsToBackendless_InBackground;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.CreateInitialListThemes;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.SaveListThemeListToBackendless;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.AppSettingsRepository;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository;

import java.util.List;

import timber.log.Timber;

/**
 * An interactor that retrieves all ListThemes
 */
public class CreateInitialListThemes_InBackground extends AbstractInteractor implements CreateInitialListThemes,
        SaveAppSettingsToBackendless.Callback, SaveListThemeListToBackendless.Callback {

    private final Callback mCallback;
    private final AppSettingsRepository mAppSettingsRepository;
    private final ListThemeRepository mListThemeRepository;
    //    private final ListTitleRepository mListTitleRepository;
    private final Context mContext;

    private List<ListTheme> mAllListThemes;

    public CreateInitialListThemes_InBackground(Executor threadExecutor, MainThread mainThread,
                                                Callback callback, AppSettingsRepository appSettingsRepository,
                                                ListThemeRepository listThemeRepository,
                                                Context context) {
        super(threadExecutor, mainThread);
        mCallback = callback;
        mAppSettingsRepository = appSettingsRepository;
        mListThemeRepository = listThemeRepository;
        mContext = context;
    }

    @Override
    public void run() {
        try {
            // create app settings
            AppSettings appSettings = AppSettings.newInstance();
            if (!mAppSettingsRepository.insertIntoSQLiteDb(appSettings)) {
                Timber.e("run(): FAILED to create AppSettings!");
            }

            //region create initial ListThemes
            int requestedInsertListThemeCount = 0;
            ListTheme newListTheme;

            // TODO: create resource strings for the initial ListThemes
            newListTheme = ListTheme.newInstance("Genoa",
                    Color.parseColor("#4c898e"), Color.parseColor("#125156"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Opal",
                    Color.parseColor("#cbdcd4"), Color.parseColor("#91a69d"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Shades of Blue",
                    -5777934, -10841921,
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Off White",
                    ContextCompat.getColor(mContext, R.color.white), -2436147,
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, true, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Whiskey",
                    Color.parseColor("#e9ac6d"), Color.parseColor("#ad7940"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Shakespeare",
                    Color.parseColor("#73c5d3"), Color.parseColor("#308d9e"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Sorbus",
                    Color.parseColor("#f0725b"), Color.parseColor("#bc3c21"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Dark Khaki",
                    Color.parseColor("#ced285"), Color.parseColor("#9b9f55"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Lemon Chiffon",
                    Color.parseColor("#fdfcdd"), Color.parseColor("#e3e2ac"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Paprika",
                    Color.parseColor("#994552"), Color.parseColor("#5f0c16"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Medium Wood",
                    Color.parseColor("#bfaa75"), Color.parseColor("#8a7246"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Breaker Bay",
                    Color.parseColor("#6d8b93"), Color.parseColor("#31535c"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Sandrift",
                    Color.parseColor("#cbb59d"), Color.parseColor("#92806c"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Pale Brown",
                    Color.parseColor("#ac956c"), Color.parseColor("#705c39"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Seagull",
                    Color.parseColor("#94dcea"), Color.parseColor("#4ea0ab"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Beige",
                    Color.parseColor("#fefefe"), Color.parseColor("#d3d8c2"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Orange",
                    Color.parseColor("#ff6c52"), Color.parseColor("#e0341e"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Arsenic",
                    Color.parseColor("#545c67"), Color.parseColor("#1d242c"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }

            newListTheme = ListTheme.newInstance("Acapulco",
                    Color.parseColor("#8dbab3"), Color.parseColor("#58857e"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedInsertListThemeCount++;
            if (!mListThemeRepository.insertIntoSQLiteDb(newListTheme)) {
                Timber.e("run(): FAILED to create ListTheme \"%s\"!", newListTheme.getName());
            }
            //endregion


            mAllListThemes = mListThemeRepository.retrieveAllListThemes(false);
            String createdListThemeMessage;
            // check if we have failed to retrieve any ListThemes
            if (mAllListThemes == null) {
                // notify the failure on the main thread
                posListThemesCreationFailed("No Themes created!");

            } else {
                // we have created ListThemes. Notify the UI on the main thread.
                if (mAllListThemes.size() != requestedInsertListThemeCount) {
                    createdListThemeMessage = String.format("Only %d out of %d requested Themes created in the local database.", mAllListThemes.size(), requestedInsertListThemeCount);

                } else {
                    createdListThemeMessage = String.format("All %d Themes created in the local database.", requestedInsertListThemeCount);
                }

                postInitialListThemesCreated(createdListThemeMessage);

                new SaveDirtyObjectsToBackendless_InBackground(mThreadExecutor,mMainThread).execute();

//                AppSettings dirtyAppSettings = mAppSettingsRepository.retrieveDirtyAppSettings();
//                if (dirtyAppSettings != null) {
//                    new SaveAppSettingsToBackendless_InBackground(mThreadExecutor, mMainThread, dirtyAppSettings, this).execute();
//                }
            }
        } catch (Exception e) {
            Timber.e("run(): Exception: %s.", e.getMessage());
        }
    }

    @Override
    public void onAppSettingsSavedToBackendless(final String successMessage) {
        Timber.i("onAppSettingsSavedToBackendless(): %s", successMessage);
        new SaveListThemeListToBackendless_InBackground(mThreadExecutor, mMainThread, mAllListThemes, this).execute();
    }

    @Override
    public void onAppSettingsSaveToBackendlessFailed(final String errorMessage) {
        Timber.e("onAppSettingsSaveToBackendlessFailed(): %s", errorMessage);
        new SaveListThemeListToBackendless_InBackground(mThreadExecutor, mMainThread, mAllListThemes, this).execute();
    }

    @Override
    public void onListThemeListSavedToBackendless(String successMessage, List<ListTheme> successfullySavedListThemes) {
        Timber.i("onListThemeListSavedToBackendless(): %s", successMessage);
    }

    @Override
    public void onListThemeListSaveToBackendlessFailed(String errorMessage, List<ListTheme> successfullySavedListThemes) {
        Timber.e("onListThemeListSaveToBackendlessFailed(): %s", errorMessage);
    }

    private void postInitialListThemesCreated(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onInitialListThemesCreated(successMessage);
            }
        });
    }

    private void posListThemesCreationFailed(final String errorMessage) {

        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemesCreationFailed(errorMessage);
            }
        });
    }


}
