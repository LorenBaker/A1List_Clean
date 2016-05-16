package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.backendlessMessaging.ListThemeMessage;
import com.lbconsulting.a1list.backendlessMessaging.Messaging;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.SaveListThemeToCloud;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.utils.CommonMethods;

import timber.log.Timber;

/**
 * An interactor that saves the provided ListTheme to Backendless.
 */
public class SaveListThemeToCloud_InBackground extends AbstractInteractor implements SaveListThemeToCloud {
    private final Callback mCallback;
    private final ListTheme mListTheme;

    public SaveListThemeToCloud_InBackground(Executor threadExecutor, MainThread mainThread,
                                             Callback callback, ListTheme listTheme) {
        super(threadExecutor, mainThread);
        mListTheme = listTheme;
        mCallback = callback;
    }


    @Override
    public void run() {
        // saveListThemeToBackendless
        ListTheme response;
        int TRUE = 1;
        int FALSE = 0;

        if (mListTheme == null) {
            Timber.e("run(): Unable to save ListTheme. ListTheme is null!");
            return;
        }
        if (!CommonMethods.isNetworkAvailable()) {
            return;
        }

        String objectId = mListTheme.getObjectId();
        boolean isNew = objectId == null || objectId.isEmpty();
        try {
            response = Backendless.Data.of(ListTheme.class).save(mListTheme);
            int numberOfClearedRecords = AndroidApplication.getListThemeRepository().clearLocalStorageDirtyFlag(response);

            // send message to other devices
            int action = Messaging.ACTION_UPDATE;
            if (isNew) {
                action = Messaging.ACTION_CREATE;
            }
            ListThemeMessage.sendMessage(mListTheme, action);

            String successMessage = String.format("Successfully saved \"%s\" to Backendless.", response.getName());
            if (numberOfClearedRecords != 1) {
                successMessage = successMessage + " But FAILED to clear local storage dirty flag!";
            }
            postListThemeSavedToCloud(successMessage);

        } catch (BackendlessException e) {

            String errorMessage = String.format("FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
                    mListTheme.getName(), e.getCode(), e.getMessage());
            postListThemeSaveToCloudFailed(errorMessage);
        }
    }


    private void postListThemeSavedToCloud(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeSavedToCloud(successMessage);
            }
        });
    }

    private void postListThemeSaveToCloudFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeSaveToCloudFailed(errorMessage);
            }
        });
    }

}
