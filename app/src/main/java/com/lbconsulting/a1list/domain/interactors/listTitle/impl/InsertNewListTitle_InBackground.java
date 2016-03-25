package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.appSettings.SaveAppSettingsToBackendless_InBackground;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.InsertNewListTitle;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.AppSettingsRepository;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository;

import timber.log.Timber;

/**
 * An interactor that creates a new ListTitle
 */
public class InsertNewListTitle_InBackground extends AbstractInteractor implements InsertNewListTitle,
        SaveAppSettingsToBackendless_InBackground.Callback {

    private final Callback mCallback;
    private final ListTitleRepository mListTitleRepository;
    private final AppSettingsRepository mAppSettingRepository;
    private final ListTitle mNewListTitle;

    public InsertNewListTitle_InBackground(Executor threadExecutor, MainThread mainThread,
                                           Callback callback, ListTitle newListTitle) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mNewListTitle = newListTitle;
        mListTitleRepository = AndroidApplication.getListTitleRepository();
        mAppSettingRepository = AndroidApplication.getAppSettingsRepository();
    }


    @Override
    public void run() {
        // insert the new ListTitle in the SQLite db
        if(mListTitleRepository.insert(mNewListTitle)){
            // AppSettings tracks listTitleLastSortKey ... which was incremented when the new ListTitle was created
            // So save appSettings to Backendless
            AppSettings dirtyAppSettings = mAppSettingRepository.retrieveDirtyAppSettings();
            new SaveAppSettingsToBackendless_InBackground(mThreadExecutor, mMainThread, dirtyAppSettings, this).execute();

            String successMessage = String.format("Successfully inserted \"%s\" into SQLite db.",mNewListTitle.getName());
            postListTitleInsertedIntoSQLiteDb(successMessage);
        }else{
            String errorMessage = String.format("FAILED to insert \"%s\" into SQLite db.",mNewListTitle.getName());
            postListTitleInsertedIntoSQLiteDb(errorMessage);
        }
    }
    @Override
    public void onAppSettingsSavedToBackendless(String successMessage) {
        Timber.i("onAppSettingsSavedToBackendless(): %s.", successMessage);
    }

    @Override
    public void onAppSettingsSaveToBackendlessFailed(String errorMessage) {
        Timber.e("onAppSettingsSavedToBackendless(): %s.", errorMessage);
    }

    private void postListTitleInsertedIntoSQLiteDb(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleInsertedIntoSQLiteDb(successMessage);
            }
        });
    }

    private void postListTitleInsertionIntoSQLiteDbFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleInsertionIntoSQLiteDbFailed(errorMessage);
            }
        });
    }

}
