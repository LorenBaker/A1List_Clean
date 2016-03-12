package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.RetrieveAllListTitles_Interactor;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository;

import java.util.List;

/**
 * An interactor that retrieves all ListTitles
 */
public class RetrieveAllListTitles_InBackground extends AbstractInteractor implements RetrieveAllListTitles_Interactor {

    private final Callback mCallback;
    private final ListTitleRepository mListTitleRepository;
    private ListTitle mSelectedListTitle;

    public RetrieveAllListTitles_InBackground(Executor threadExecutor, MainThread mainThread,
                                              Callback callback, ListTitleRepository listTitleRepository) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListTitleRepository = listTitleRepository;
    }


    @Override
    public void run() {

        // retrieve all ListTitles that are not marked for deletion
        final List<ListTitle> allListTitles = mListTitleRepository.retrieveAllListTitles(false);
        // check if we have failed to retrieve any ListTitles
        if (allListTitles == null || allListTitles.size() == 0) {
            // notify the failure on the main thread
            notifyError();
        } else {
            // we have retrieved all ListTitles. Notify the UI on the main thread.
            postAllListTitlesRetrieved(allListTitles);
        }
    }

    private void postAllListTitlesRetrieved(final List<ListTitle> allListTitles) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onAllListTitlesRetrieved(allListTitles);
            }
        });
    }

    private void notifyError() {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onAllListTitlesRetrievalFailed("No ListTitles retrieved!");
            }
        });
    }
}
