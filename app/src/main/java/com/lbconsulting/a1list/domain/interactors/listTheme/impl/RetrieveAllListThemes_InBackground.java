package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.RetrieveAllListThemes_Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_interface;

import java.util.List;

/**
 * An interactor that retrieves all ListThemes
 */
public class RetrieveAllListThemes_InBackground extends AbstractInteractor implements RetrieveAllListThemes_Interactor {



    private final RetrieveAllListThemes_Interactor.Callback mCallback;
    private final ListThemeRepository_interface mListThemeRepository;
    private String mAction = NONE;
    private ListTheme mSelectedListTheme;

    public RetrieveAllListThemes_InBackground(Executor threadExecutor, MainThread mainThread,
                                              Callback callback, ListThemeRepository_interface listThemeRepository) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListThemeRepository = listThemeRepository;
    }


    @Override
    public void run() {

        // retrieve all ListThemes that are not marked for deletion
        final List<ListTheme> allListThemes = mListThemeRepository.getAllListThemes(false);
        // check if we have failed to retrieve any ListThemes
        if (allListThemes == null || allListThemes.size() == 0) {
            // notify the failure on the main thread
            notifyError();
        } else {
            // we have retrieved all ListThemes. Notify the UI on the main thread.
            postAllListThemes(allListThemes);
        }
    }

    private void postAllListThemes(final List<ListTheme> allListThemes) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onAllListThemesRetrieved(allListThemes);
            }
        });
    }

    private void notifyError() {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onRetrievalFailed("No Themes retrieved!");
            }
        });
    }
}
