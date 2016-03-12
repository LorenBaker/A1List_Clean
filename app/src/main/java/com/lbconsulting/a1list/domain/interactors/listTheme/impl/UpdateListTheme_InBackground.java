package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.UpdateListTheme_Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository;

/**
 * An interactor that retrieves all ListThemes
 */
public class UpdateListTheme_InBackground extends AbstractInteractor implements UpdateListTheme_Interactor {

    private final Callback mCallback;
    private final ListThemeRepository mListThemeRepository;
    private final ListTheme mListTheme;

    public UpdateListTheme_InBackground(Executor threadExecutor, MainThread mainThread,
                                        Callback callback, ListThemeRepository listThemeRepository,
                                        ListTheme listTheme) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListThemeRepository = listThemeRepository;
        mListTheme = listTheme;
    }


    @Override
    public void run() {

        if(mListTheme.isDefaultTheme()){
            // clear the previous default theme that's in the SQLite db.
            mListThemeRepository.clearDefaultFlag();
        }

        // update the provided ListThem in the SQLite db
        if (mListThemeRepository.update(mListTheme)) {
            String msg = String.format("\"%s\" successfully updated.", mListTheme.getName());
            postRetrievedListTheme(msg);
        } else {
            String msg = String.format("\"%s\" FAILED to updated.", mListTheme.getName());
            notifyError(msg);
        }
    }

    private void postRetrievedListTheme(final String message) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeUpdated(message);
            }
        });
    }

    private void notifyError(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeUpdateFailed(errorMessage);
            }
        });
    }

}
