package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.ToggleListThemeBooleanField;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository;

/**
 * This is an interactor toggles a ListTheme's strikeout attribute.
 * <p/>
 */
public class ToggleListThemeBooleanField_InBackground extends AbstractInteractor implements ToggleListThemeBooleanField {

    private final ListThemeRepository mListThemeRepository;
    private final Callback mCallback;
    private ListTheme mListTheme;
    private String mListThemeBooleanField;

    public ToggleListThemeBooleanField_InBackground(Executor threadExecutor,
                                                    MainThread mainThread, Callback callback,
                                                    ListTheme listTheme, String listThemeBooleanField) {
        super(threadExecutor, mainThread);
        mListThemeRepository = AndroidApplication.getListThemeRepository();
        mListTheme = listTheme;
        mListThemeBooleanField = listThemeBooleanField;
        mCallback = callback;
    }

    @Override
    public void run() {
        int toggleResult = mListThemeRepository.toggle(mListTheme, mListThemeBooleanField);
        postToggleResult(toggleResult);
    }

    private void postToggleResult(final int toggleResult) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeBooleanFieldToggled(toggleResult);
            }
        });
    }
}