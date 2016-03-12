package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.CreateInitialListThemes_Interactor;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.AppSettingsRepository;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository;

import java.util.List;

import timber.log.Timber;

/**
 * An interactor that retrieves all ListThemes
 */
public class CreateInitialListThemes_InBackground extends AbstractInteractor implements CreateInitialListThemes_Interactor {

    private final Callback mCallback;
    private final AppSettingsRepository mAppSettingsRepository;
    private final ListThemeRepository mListThemeRepository;
    private final ListTitleRepository mListTitleRepository;
    private final Context mContext;

    public CreateInitialListThemes_InBackground(Executor threadExecutor, MainThread mainThread,
                                                Callback callback, AppSettingsRepository appSettingsRepository,
                                                ListThemeRepository listThemeRepository,
                                                ListTitleRepository listTitleRepository,
                                                Context context) {
        super(threadExecutor, mainThread);
        mCallback = callback;
        mAppSettingsRepository = appSettingsRepository;
        mListThemeRepository = listThemeRepository;
        mListTitleRepository = listTitleRepository;
        mContext = context;
    }

    @Override
    public void run() {
        try {
            // create app settings
            AppSettings appSettings = AppSettings.newInstance();
            AppSettings appSettingsResponse = mAppSettingsRepository.insert(appSettings);
            if (appSettingsResponse == null) {
                Timber.e("run(): FAILED to create AppSettings!");
            }


            //region create initial ListThemes
            int requestedListThemeCount = 0;
            int newBackendlessListThemeCount = 0;

            ListTheme newListTheme;

            // TODO: create resource strings for the initial ListThemes
            newListTheme = ListTheme.newInstance("Genoa",
                    Color.parseColor("#4c898e"), Color.parseColor("#125156"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            ListTheme response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Opal",
                    Color.parseColor("#cbdcd4"), Color.parseColor("#91a69d"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Shades of Blue",
                    -5777934, -10841921,
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Off White",
                    ContextCompat.getColor(mContext, R.color.white), -2436147,
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, true, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Whiskey",
                    Color.parseColor("#e9ac6d"), Color.parseColor("#ad7940"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Shakespeare",
                    Color.parseColor("#73c5d3"), Color.parseColor("#308d9e"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Sorbus",
                    Color.parseColor("#f0725b"), Color.parseColor("#bc3c21"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Dark Khaki",
                    Color.parseColor("#ced285"), Color.parseColor("#9b9f55"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Lemon Chiffon",
                    Color.parseColor("#fdfcdd"), Color.parseColor("#e3e2ac"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Paprika",
                    Color.parseColor("#994552"), Color.parseColor("#5f0c16"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Medium Wood",
                    Color.parseColor("#bfaa75"), Color.parseColor("#8a7246"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Breaker Bay",
                    Color.parseColor("#6d8b93"), Color.parseColor("#31535c"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Sandrift",
                    Color.parseColor("#cbb59d"), Color.parseColor("#92806c"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Pale Brown",
                    Color.parseColor("#ac956c"), Color.parseColor("#705c39"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Seagull",
                    Color.parseColor("#94dcea"), Color.parseColor("#4ea0ab"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Beige",
                    Color.parseColor("#fefefe"), Color.parseColor("#d3d8c2"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Orange",
                    Color.parseColor("#ff6c52"), Color.parseColor("#e0341e"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Arsenic",
                    Color.parseColor("#545c67"), Color.parseColor("#1d242c"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Acapulco",
                    Color.parseColor("#8dbab3"), Color.parseColor("#58857e"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }
            //endregion


            // TODO: Remove List Creation
/*
            ListTitle newListTitle = ListTitle.newInstance("List A", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);

            newListTitle = ListTitle.newInstance("List B", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List C", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List D", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List E", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List F", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List G", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List H", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List I", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List J", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List K", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List L", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List M", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List N", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List O", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);


            newListTitle = ListTitle.newInstance("List P", mListThemeRepository.retrieveDefaultListTheme(), mAppSettingsRepository);
            mListTitleRepository.insert(newListTitle);

*/

            final List<ListTheme> allListThemes = mListThemeRepository.retrieveAllListThemes(false);
            String createdListThemeMessage;
            // check if we have failed to retrieve any ListThemes
            if (allListThemes == null) {
                // notify the failure on the main thread
                notifyError("No ListThemes created!");

            } else {
                // we have created ListThemes. Notify the UI on the main thread.
                if (allListThemes.size() != requestedListThemeCount) {
                    createdListThemeMessage = String.format("Only %d out of %d requested Themes created in the local database.", allListThemes.size(), requestedListThemeCount);

                } else {
                    createdListThemeMessage = String.format("All %d Themes created in the local database.", requestedListThemeCount);
                }

                if (allListThemes.size() != newBackendlessListThemeCount) {
                    createdListThemeMessage = createdListThemeMessage + "\n" +
                            String.format("Only %d out of %d requested ListThemes saved to the Cloud.", newBackendlessListThemeCount, requestedListThemeCount);
                } else {
                    createdListThemeMessage = createdListThemeMessage + "\n" +
                            String.format("All %d ListThemes saved to the Cloud.", requestedListThemeCount);
                }

                postInitialListThemesCreated(createdListThemeMessage);

            }
        } catch (Exception e) {
            Timber.e("run(): Exception: %s.", e.getMessage());
        }
    }

    private void postInitialListThemesCreated(final String message) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onInitialListThemesCreated(message);
            }
        });
    }

    private void notifyError(String message) {

        final String msg = message;
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemesCreationFailed(msg);
            }
        });
    }
}
