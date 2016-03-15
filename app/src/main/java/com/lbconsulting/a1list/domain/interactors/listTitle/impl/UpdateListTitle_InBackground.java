package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.UpdateListTitle;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository;

/**
 * An interactor that updates the provided ListTitle in the SQLite. All ListTitle fields are updated.
 */
public class UpdateListTitle_InBackground extends AbstractInteractor implements UpdateListTitle {

    private final Callback mCallback;
    private final ListTitleRepository mListTitleRepository;
    private final ListTitle mListTitle;

    public UpdateListTitle_InBackground(Executor threadExecutor, MainThread mainThread,
                                        Callback callback, ListTitleRepository listTitleRepository,
                                        ListTitle listTitle) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListTitleRepository = listTitleRepository;
        mListTitle = listTitle;
    }


    @Override
    public void run() {

        // update the provided ListThem in the SQLite db
        if (mListTitleRepository.update(mListTitle)) {
            String msg = String.format("\"%s\" successfully updated in SQL Db.", mListTitle.getName());
            postListTitleUpdated(msg);
        } else {
            String msg = String.format("\"%s\" FAILED to updated in SQL Db.", mListTitle.getName());
            postListTitleUpdateFailed(msg);
        }
    }

    private void postListTitleUpdated(final String message) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleUpdated(message);
            }
        });
    }

    private void postListTitleUpdateFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleUpdateFailed(errorMessage);
            }
        });
    }

}
