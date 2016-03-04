package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.ToggleListTitleBooleanField_Interactor;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_interface;

/**
 * This is an interactor toggles the provided boolean field attribute for the given ListTitle.
 * <p/>
 */
public class ToggleListTitleBooleanField_InBackground extends AbstractInteractor implements ToggleListTitleBooleanField_Interactor {

    private final ListTitleRepository_interface mListTitleRepository;
    private final Callback mCallback;
    private ListTitle mListTitle;
    private String mListTitleBooleanField;

    public ToggleListTitleBooleanField_InBackground(Executor threadExecutor,
                                                    MainThread mainThread, Callback callback,
                                                    ListTitleRepository_interface listTitleRepository,
                                                    ListTitle listTitle, String listTitleBooleanField) {
        super(threadExecutor, mainThread);
        mListTitleRepository = listTitleRepository;
        mListTitle = listTitle;
        mListTitleBooleanField = listTitleBooleanField;
        mCallback = callback;
    }

    @Override
    public void run() {
        int toggleResult = mListTitleRepository.toggle(mListTitle, mListTitleBooleanField, true);
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