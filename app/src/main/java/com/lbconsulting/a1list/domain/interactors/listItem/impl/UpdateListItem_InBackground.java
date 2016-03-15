package com.lbconsulting.a1list.domain.interactors.listItem.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.UpdateListItem;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository;

/**
 * An interactor that updates the provided ListItem in the SQLite. All ListItem fields are updated.
 */
public class UpdateListItem_InBackground extends AbstractInteractor implements UpdateListItem {

    private final Callback mCallback;
    private final ListItemRepository mListItemRepository;
    private final ListItem mListItem;

    public UpdateListItem_InBackground(Executor threadExecutor, MainThread mainThread,
                                       Callback callback, ListItemRepository listItemRepository,
                                       ListItem listItem) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListItemRepository = listItemRepository;
        mListItem = listItem;
    }


    @Override
    public void run() {

        // update the provided ListThem in the SQLite db
        if (mListItemRepository.update(mListItem)) {
            String msg = String.format("\"%s\" successfully updated in SQL Db.", mListItem.getName());
            postListItemUpdated(msg);
        } else {
            String msg = String.format("\"%s\" FAILED to updated in SQL Db.", mListItem.getName());
            postListItemUpdateFailed(msg);
        }
    }

    private void postListItemUpdated(final String message) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemUpdated(message);
            }
        });
    }

    private void postListItemUpdateFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemUpdateFailed(errorMessage);
            }
        });
    }

}
