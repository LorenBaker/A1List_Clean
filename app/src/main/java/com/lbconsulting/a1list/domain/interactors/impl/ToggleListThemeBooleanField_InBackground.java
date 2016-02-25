package com.lbconsulting.a1list.domain.interactors.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repository.ListThemeRepository;

/**
 * This is an interactor toggles a ListTheme's strikeout attribute.
 * <p/>
 */
public class ToggleListThemeBooleanField_InBackground extends AbstractInteractor {

    private final ListThemeRepository mListThemeRepository;
    private ListTheme mListTheme;
    private String mListThemeBooleanField;

    public ToggleListThemeBooleanField_InBackground(Executor threadExecutor,
                                                    MainThread mainThread,
                                                    ListThemeRepository listThemeRepository,
                                                    ListTheme listTheme, String listThemeBooleanField) {
        super(threadExecutor, mainThread);
        mListThemeRepository = listThemeRepository;
        mListTheme = listTheme;
        mListThemeBooleanField = listThemeBooleanField;
    }

    @Override
    public void run() {
        // TODO: Implement this with your business logic
        mListThemeRepository.toggle(mListTheme, mListThemeBooleanField, true);
    }
}