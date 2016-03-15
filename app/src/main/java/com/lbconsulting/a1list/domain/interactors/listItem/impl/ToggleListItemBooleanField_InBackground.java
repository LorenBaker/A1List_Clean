package com.lbconsulting.a1list.domain.interactors.listItem.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.ToggleListItemBooleanField;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository;

/**
 * This is an interactor toggles the provided boolean field attribute for the given ListItem.
 * <p/>
 */
public class ToggleListItemBooleanField_InBackground extends AbstractInteractor implements ToggleListItemBooleanField {

    private final ListItemRepository mListItemRepository;
    private final Callback mCallback;
    private ListItem mListItem;
    private String mListItemBooleanField;

    public ToggleListItemBooleanField_InBackground(Executor threadExecutor,
                                                   MainThread mainThread, Callback callback,
                                                   ListItemRepository listItemRepository,
                                                   ListItem listItem, String listItemBooleanField) {
        super(threadExecutor, mainThread);
        mListItemRepository = listItemRepository;
        mListItem = listItem;
        mListItemBooleanField = listItemBooleanField;
        mCallback = callback;
    }

    @Override
    public void run() {
        int toggleResult = mListItemRepository.toggle(mListItem, mListItemBooleanField);
        postToggleResult(toggleResult);
    }

    private void postToggleResult(final int toggleResult) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemBooleanFieldToggled(toggleResult);
            }
        });
    }
}