package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.ApplyTextSizeAndMarginsToAllListThemes;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository;

/**
 * This is an interactor toggles a ListTheme's strikeout attribute.
 * <p/>
 */
public class ApplyTextSizeAndMarginsToAllListThemes_InBackground extends AbstractInteractor implements ApplyTextSizeAndMarginsToAllListThemes {

    private final ListThemeRepository mListThemeRepository;
    private final Callback mCallback;
    private ListTheme mListTheme;

    public ApplyTextSizeAndMarginsToAllListThemes_InBackground(Executor threadExecutor,
                                                               MainThread mainThread, Callback callback,
                                                               ListTheme listTheme) {
        super(threadExecutor, mainThread);
        mListThemeRepository = AndroidApplication.getListThemeRepository();
        mListTheme = listTheme;
        mCallback = callback;
    }

    @Override
    public void run() {
        int numberOfListThemesUpdated = mListThemeRepository.applyTextSizeAndMarginsToAllListThemes(mListTheme);
        String successMessage = AndroidApplication.getContext().getResources().getQuantityString(R.plurals.updatedListThemes, numberOfListThemesUpdated, numberOfListThemesUpdated);
        postSuccessResult(successMessage);
    }

    private void postSuccessResult(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onTextSizeAndMarginsApplied(successMessage);
            }
        });
    }

    private void postFailureResult(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onApplyTextSizeAndMarginsFailure(errorMessage);
            }
        });
    }
}