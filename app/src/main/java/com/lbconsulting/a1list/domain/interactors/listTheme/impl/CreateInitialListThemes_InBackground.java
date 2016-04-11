package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.appSettings.SaveAppSettingsToCloud;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.CreateInitialListThemes;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.SaveListThemesToCloud;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.AppSettingsRepository;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * An interactor that retrieves all ListThemes
 */
public class CreateInitialListThemes_InBackground extends AbstractInteractor implements CreateInitialListThemes,
        SaveAppSettingsToCloud.Callback, SaveListThemesToCloud.Callback {

    private final Callback mCallback;
    private final AppSettingsRepository mAppSettingsRepository;
    private final ListThemeRepository mListThemeRepository;
    //    private final ListTitleRepository mListTitleRepository;
    private final Context mContext;

    private List<ListTheme> mListThemesInsertedIntoLocalStorage;

    public CreateInitialListThemes_InBackground(Executor threadExecutor, MainThread mainThread,
                                                Callback callback) {
        super(threadExecutor, mainThread);
        mCallback = callback;
        mAppSettingsRepository = AndroidApplication.getAppSettingsRepository();
        mListThemeRepository = AndroidApplication.getListThemeRepository();
        mContext = AndroidApplication.getContext();
    }

    @Override
    public void run() {
        try {
            // create app settings
            AppSettings appSettings = AppSettings.newInstance();
            if (!mAppSettingsRepository.insert(appSettings)) {
                Timber.e("run(): FAILED to create AppSettings!");
            }

            //region create initial ListThemes
//            int requestedInsertListThemeCount = 0;
            List<ListTheme> newListThemes = new ArrayList<>();
            ListTheme newListTheme;

            // TODO: create resource strings for the initial ListThemes
            newListTheme = ListTheme.newInstance("Genoa",
                    Color.parseColor("#4c898e"), Color.parseColor("#125156"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Opal",
                    Color.parseColor("#cbdcd4"), Color.parseColor("#91a69d"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Shades of Blue",
                    -5777934, -10841921,
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Off White",
                    ContextCompat.getColor(mContext, R.color.white), -2436147,
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, true, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Whiskey",
                    Color.parseColor("#e9ac6d"), Color.parseColor("#ad7940"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Shakespeare",
                    Color.parseColor("#73c5d3"), Color.parseColor("#308d9e"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Sorbus",
                    Color.parseColor("#f0725b"), Color.parseColor("#bc3c21"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Dark Khaki",
                    Color.parseColor("#ced285"), Color.parseColor("#9b9f55"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Lemon Chiffon",
                    Color.parseColor("#fdfcdd"), Color.parseColor("#e3e2ac"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Paprika",
                    Color.parseColor("#994552"), Color.parseColor("#5f0c16"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Medium Wood",
                    Color.parseColor("#bfaa75"), Color.parseColor("#8a7246"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Breaker Bay",
                    Color.parseColor("#6d8b93"), Color.parseColor("#31535c"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Sandrift",
                    Color.parseColor("#cbb59d"), Color.parseColor("#92806c"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Pale Brown",
                    Color.parseColor("#ac956c"), Color.parseColor("#705c39"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Seagull",
                    Color.parseColor("#94dcea"), Color.parseColor("#4ea0ab"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Beige",
                    Color.parseColor("#fefefe"), Color.parseColor("#d3d8c2"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Orange",
                    Color.parseColor("#ff6c52"), Color.parseColor("#e0341e"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Arsenic",
                    Color.parseColor("#545c67"), Color.parseColor("#1d242c"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            newListTheme = ListTheme.newInstance("Acapulco",
                    Color.parseColor("#8dbab3"), Color.parseColor("#58857e"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            newListThemes.add(newListTheme);

            //endregion


            mListThemesInsertedIntoLocalStorage = mListThemeRepository.insert(newListThemes);
            String createdListThemeMessage;
            // check if we have failed to retrieve any ListThemes
            if (mListThemesInsertedIntoLocalStorage == null) {
                // notify the failure on the main thread
                posListThemesCreationFailed("No Themes created!");

            } else {
                // we have created ListThemes. Notify the UI on the main thread.
                if (mListThemesInsertedIntoLocalStorage.size() == newListThemes.size()) {
                    createdListThemeMessage = String.format("All %d Themes created in the local storage.",
                            newListThemes.size());
//                    appSettings.setAppInitializationComplete(true);
//                    mAppSettingsRepository.insertIntoLocalStorage(appSettings);
//                    mAppSettingsRepository.updateInLocalStorage(appSettings);
                } else {
                    createdListThemeMessage = String.format("Only %d out of %d requested Themes created in the local storage.",
                            mListThemesInsertedIntoLocalStorage.size(), newListThemes.size());
                }

                postInitialListThemesCreated(createdListThemeMessage);
            }

//            AppSettings dirtyAppSettings = mAppSettingsRepository.retrieveDirtyAppSettings();
//            if (dirtyAppSettings != null) {
//                mAppSettingsRepository.updateInCloud(dirtyAppSettings, false);
//            }

//                new SaveDirtyObjectsToBackendless_InBackground(mThreadExecutor, mMainThread).execute();

        } catch (Exception e) {
            Timber.e("run(): Exception: %s.", e.getMessage());
        }
    }

    @Override
    public void onAppSettingsSavedToCloud(final String successMessage) {
        Timber.i("onAppSettingsSavedToCloud(): %s", successMessage);
        new SaveListThemesToCloud_InBackground(mThreadExecutor, mMainThread, this, mListThemesInsertedIntoLocalStorage).execute();
    }

    @Override
    public void onAppSettingsSaveToCloudFailed(final String errorMessage) {
        Timber.e("onAppSettingsSaveToCloudFailed(): %s", errorMessage);
        new SaveListThemesToCloud_InBackground(mThreadExecutor, mMainThread, this, mListThemesInsertedIntoLocalStorage).execute();
    }

    @Override
    public void onListThemesSavedToCloud(String successMessage, List<ListTheme> successfullySavedListThemes) {
        Timber.i("onListThemesSavedToCloud(): %s", successMessage);
    }

    @Override
    public void onListThemesSaveToCloudFailed(String errorMessage, List<ListTheme> successfullySavedListThemes) {
        Timber.e("onListThemesSaveToCloudFailed(): %s", errorMessage);
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
