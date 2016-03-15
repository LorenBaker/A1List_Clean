package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.RetrieveListTitle;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository;

/**
 * An interactor that retrieves a ListTitle with the provided uuid
 */
public class RetrieveListTitle_InBackground extends AbstractInteractor implements RetrieveListTitle {

    private final Callback mCallback;
    private final ListTitleRepository mListTitleRepository;
    private final String mListTitleUuid;

    public RetrieveListTitle_InBackground(Executor threadExecutor, MainThread mainThread,
                                          Callback callback, ListTitleRepository listTitleRepository,
                                          String listTitleUuid) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListTitleRepository = listTitleRepository;
        mListTitleUuid = listTitleUuid;
    }


    @Override
    public void run() {

        // retrieve the original ListTitle
        final ListTitle listTitle = mListTitleRepository.getListTitleByUuid(mListTitleUuid);
        // check if we have failed to retrieve the ListTitle
        if (listTitle == null) {
            // notify the failure on the main thread
            notifyError("Unable to retrieve ListTitle with ID = " + mListTitleUuid);
        } else {
            // we have retrieved the original ListTitles. Now clone it.
                        postListTitleRetrieved(listTitle);
        }
    }

    private void postListTitleRetrieved(final ListTitle listTitle) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleRetrieved(listTitle);
            }
        });
    }

    private void notifyError(final String message) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleRetrievalFailed(message);
            }
        });
    }

}
