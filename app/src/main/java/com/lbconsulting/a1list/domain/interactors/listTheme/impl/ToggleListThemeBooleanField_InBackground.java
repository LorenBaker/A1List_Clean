package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.ToggleListThemeBooleanField_Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository;

/**
 * This is an interactor toggles a ListTheme's strikeout attribute.
 * <p/>
 */
public class ToggleListThemeBooleanField_InBackground extends AbstractInteractor implements ToggleListThemeBooleanField_Interactor {

    private final ListThemeRepository mListThemeRepository;
    private ListTheme mListTheme;
    private String mListThemeBooleanField;
    private final Callback mCallback;

    public ToggleListThemeBooleanField_InBackground(Executor threadExecutor,
                                                    MainThread mainThread,Callback callback,
                                                    ListThemeRepository listThemeRepository,
                                                    ListTheme listTheme, String listThemeBooleanField) {
        super(threadExecutor, mainThread);
        mListThemeRepository = listThemeRepository;
        mListTheme = listTheme;
        mListThemeBooleanField = listThemeBooleanField;
        mCallback=callback;
    }

    @Override
    public void run() {
        // TODO: Implement this with your business logic
      int toggleResult=  mListThemeRepository.toggle(mListTheme, mListThemeBooleanField);
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