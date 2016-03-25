package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.ToggleListTitleBooleanField;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository;

/**
 * This is an interactor toggles the provided boolean field attribute for the given ListTitle.
 * <p/>
 */
public class ToggleListTitleBooleanField_InBackground extends AbstractInteractor implements ToggleListTitleBooleanField {

    private final ListTitleRepository mListTitleRepository;
    private final Callback mCallback;
    private ListTitle mListTitle;
    private String mListTitleBooleanField;

    public ToggleListTitleBooleanField_InBackground(Executor threadExecutor,
                                                    MainThread mainThread, Callback callback,
                                                    ListTitle listTitle, String listTitleBooleanField) {
        super(threadExecutor, mainThread);
        mListTitleRepository = AndroidApplication.getListTitleRepository();
        mListTitle = listTitle;
        mListTitleBooleanField = listTitleBooleanField;
        mCallback = callback;
    }

    @Override
    public void run() {
        int toggleResult = mListTitleRepository.toggle(mListTitle, mListTitleBooleanField);
        postToggleResult(toggleResult);
    }

    private void postToggleResult(final int toggleResult) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleBooleanFieldToggled(toggleResult);
            }
        });
    }
}