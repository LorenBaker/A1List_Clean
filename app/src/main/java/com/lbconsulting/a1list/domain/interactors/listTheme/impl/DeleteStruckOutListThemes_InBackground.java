package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.DeleteStruckOutListThemes;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository;

import java.util.List;

import timber.log.Timber;

/**
 * An interactor that retrieves all ListThemes
 */
public class DeleteStruckOutListThemes_InBackground extends AbstractInteractor implements DeleteStruckOutListThemes {


    private final Callback mCallback;
    private final ListThemeRepository mListThemeRepository;
    private final ListTitleRepository mListTitleRepository;


    public DeleteStruckOutListThemes_InBackground(Executor threadExecutor, MainThread mainThread,
                                                  Callback callback) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListThemeRepository = AndroidApplication.getListThemeRepository();
        mListTitleRepository = AndroidApplication.getListTitleRepository();
    }


    @Override
    public void run() {

        // get all struck out ListThemes
        List<ListTheme> struckOutListThemes = mListThemeRepository.retrieveStruckOutListThemes();
        if (struckOutListThemes.size() > 0) {
            ListTheme defaultListTheme = mListThemeRepository.retrieveDefaultListTheme();
            if (defaultListTheme == null) {
                Timber.e("DeleteStruckOutListThemes_InBackground: Failed to retrieve default ListTheme!");
            }

            int numberOfListThemesDeleted = 0;
            for (ListTheme listTheme : struckOutListThemes) {
                mListTitleRepository.replaceListTheme(listTheme, defaultListTheme);
                numberOfListThemesDeleted += mListThemeRepository.delete(listTheme);
            }

            if (struckOutListThemes.size() == numberOfListThemesDeleted) {
                // Success
                String successMessage = String.format("All %d struck out ListThemes deleted.", numberOfListThemesDeleted);
                postListThemesDeleted(successMessage);
            } else {
                String errorMessage = String.format("Only %d of %d struck out ListThemes deleted.",
                        numberOfListThemesDeleted, struckOutListThemes.size());
                postStruckOutListThemesDeletionFailed(errorMessage);
            }

        } else {
            postStruckOutListThemesDeletionFailed("No struck out ListThemes found.");
        }
    }


    private void postListThemesDeleted(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onStruckOutListThemesDeleted(successMessage);
            }
        });
    }

    private void postStruckOutListThemesDeletionFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onStruckOutListThemesDeletionFailed(errorMessage);
            }
        });
    }
}
