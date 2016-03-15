package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.RetrieveListTheme;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository;

/**
 * An interactor that retrieves a ListTheme with the provided uuid
 */
public class RetrieveListTheme_InBackground extends AbstractInteractor implements RetrieveListTheme {

    private final Callback mCallback;
    private final ListThemeRepository mListThemeRepository;
    private final String mListThemeUuid;

    public RetrieveListTheme_InBackground(Executor threadExecutor, MainThread mainThread,
                                          Callback callback, ListThemeRepository listThemeRepository,
                                          String listThemeUuid) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListThemeRepository = listThemeRepository;
        mListThemeUuid = listThemeUuid;
    }


    @Override
    public void run() {
        final ListTheme listTheme = mListThemeRepository.getListThemeByUuid(mListThemeUuid);
        if (listTheme != null) {
            postListThemeRetrieved(listTheme);
        } else {
            postListThemeRetrievalFailed("Unable to retrieve ListTheme with ID = " + mListThemeUuid);
        }
    }

    private void postListThemeRetrieved(final ListTheme listTheme) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeRetrieved(listTheme);
            }
        });
    }

    private void postListThemeRetrievalFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeRetrievalFailed(errorMessage);
            }
        });
    }

}
