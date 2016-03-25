package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.DeleteStruckOutListTitles;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository;

import java.util.List;

/**
 * An interactor that deletes struck out ListTitles
 */
public class DeleteStruckOutListTitles_InBackground extends AbstractInteractor implements DeleteStruckOutListTitles {

    private final Callback mCallback;
    private final ListTitleRepository mListTitleRepository;


    public DeleteStruckOutListTitles_InBackground(Executor threadExecutor, MainThread mainThread,
                                                  Callback callback) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListTitleRepository = AndroidApplication.getListTitleRepository();
    }


    @Override
    public void run() {

//         get all struck out ListTitles
        List<ListTitle> struckOutListTitles = mListTitleRepository.retrieveStruckOutListTitles();
        if (struckOutListTitles.size() > 0) {
            int numberOfListTitlesDeleted = 0;
                for (ListTitle listTitle : struckOutListTitles) {
                    // TODO: In the ListItems Table, delete any ListItems associated with the ListTitle being deleted.
                    numberOfListTitlesDeleted += mListTitleRepository.delete(listTitle);
                }

            if (struckOutListTitles.size() == numberOfListTitlesDeleted) {
                // Success
                String successMessage = String.format("All %d struck out ListTitles deleted.",
                        numberOfListTitlesDeleted);
                postStruckOutListTitlesDeleted(successMessage);
            } else {
                // Not all ListTitles deleted
                String errorMessage = String.format("Only %d of %d struck out ListTitles deleted.",
                        numberOfListTitlesDeleted, struckOutListTitles.size());
                postStruckOutListTitlesDeletionFailed(errorMessage);
            }

        } else {
            postStruckOutListTitlesDeletionFailed("No struck out ListTitles found.");
        }
    }


    private void postStruckOutListTitlesDeleted(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onStruckOutListTitlesDeleted(successMessage);
            }
        });
    }

    private void postStruckOutListTitlesDeletionFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onStruckOutListTitlesDeletionFailed(errorMessage);
            }
        });
    }
}
