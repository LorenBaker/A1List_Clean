package com.lbconsulting.a1list.domain.interactors.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.interfaces.RetrieveListTheme_Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repository.ListThemeRepository;

/**
 * An interactor that retrieves all ListThemes
 */
public class RetrieveListTheme_InBackground extends AbstractInteractor implements RetrieveListTheme_Interactor {

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

        // retrieve the original ListTheme
        final ListTheme listTheme = mListThemeRepository.getListThemeByUuid(mListThemeUuid);
        // check if we have failed to retrieve the ListTheme
        if (listTheme == null) {
            // notify the failure on the main thread
            notifyError("Unable to retrieve ListTheme with ID = " + mListThemeUuid);
        } else {
            // we have retrieved the original ListThemes. Now clone it.
                        postRetrievedListTheme(listTheme);
        }
    }

    private void postRetrievedListTheme(final ListTheme listTheme) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeRetrieved(listTheme);
            }
        });
    }

    private void notifyError(final String message) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeRetrievalFailed(message);
            }
        });
    }

}
