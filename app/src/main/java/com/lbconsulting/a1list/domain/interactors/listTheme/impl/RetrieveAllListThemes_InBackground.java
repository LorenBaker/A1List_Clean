package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.RetrieveAllListThemes;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository;

import java.util.List;

/**
 * An interactor that retrieves all ListThemes
 */
public class RetrieveAllListThemes_InBackground extends AbstractInteractor implements RetrieveAllListThemes {

    private final RetrieveAllListThemes.Callback mCallback;
    private final ListThemeRepository mListThemeRepository;
    private ListTheme mSelectedListTheme;

    public RetrieveAllListThemes_InBackground(Executor threadExecutor, MainThread mainThread,
                                              Callback callback) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListThemeRepository = AndroidApplication.getListThemeRepository();
    }


    @Override
    public void run() {

        // retrieve all ListThemes that are not marked for deletion
        final List<ListTheme> allListThemes = mListThemeRepository.retrieveAllListThemes(false);
        // check if we have failed to retrieve any ListThemes
        if (allListThemes == null || allListThemes.size() == 0) {
            // notify the failure on the main thread
            postRetrievalFailed();
        } else {
            // we have retrieved all ListThemes. Notify the UI on the main thread.
            postAllListThemesRetrieved(allListThemes);
        }
    }

    private void postAllListThemesRetrieved(final List<ListTheme> allListThemes) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onAllListThemesRetrieved(allListThemes);
            }
        });
    }

    private void postRetrievalFailed() {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onRetrievalFailed("No Themes retrieved!");
            }
        });
    }
}
