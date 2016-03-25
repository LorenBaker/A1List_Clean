package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.InsertNewListTheme;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository;

/**
 * An interactor that creates a new ListTheme
 */
public class InsertNewListTheme_InBackground extends AbstractInteractor implements InsertNewListTheme {

    private final Callback mCallback;
    private final ListThemeRepository mListThemeRepository;
    private final ListTheme mListTheme;

    public InsertNewListTheme_InBackground(Executor threadExecutor, MainThread mainThread,
                                           Callback callback, ListTheme listTheme) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListThemeRepository = AndroidApplication.getListThemeRepository();
        mListTheme = listTheme;
    }


    @Override
    public void run() {

        if (mListTheme.isDefaultTheme()) {
            // clear the previous default theme that's in the SQLite db.
            mListThemeRepository.clearDefaultFlag();
        }

        // insert the new ListThem in the SQLite db
        if (mListThemeRepository.insert(mListTheme)) {
            String successMessage = String.format("Successfully inserted \"%s\" into SQLite Db.", mListTheme.getName());
            postListThemeInsertedIntoSQLiteDb(successMessage);

        } else {
            String errorMessage = String.format("FAILED to insert \"%s\" into SQLite Db.", mListTheme.getName());
            postListThemeInsertionIntoSQLiteDbFailed(errorMessage);
        }
    }

    private void postListThemeInsertedIntoSQLiteDb(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeInsertedIntoSQLiteDb(successMessage);
            }
        });
    }

    private void postListThemeInsertionIntoSQLiteDbFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeInsertionIntoSQLiteDbFailed(errorMessage);
            }
        });
    }


}
