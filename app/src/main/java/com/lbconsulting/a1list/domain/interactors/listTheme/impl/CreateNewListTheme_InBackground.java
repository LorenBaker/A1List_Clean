package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.CreateNewListTheme_Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_interface;

/**
 * An interactor that creates a new ListTheme
 */
public class CreateNewListTheme_InBackground extends AbstractInteractor implements CreateNewListTheme_Interactor {

    private final Callback mCallback;
    private final ListThemeRepository_interface mListThemeRepository;
    private final ListTheme mListTheme;

    public CreateNewListTheme_InBackground(Executor threadExecutor, MainThread mainThread,
                                           Callback callback, ListThemeRepository_interface listThemeRepository,
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

        // insert the new ListThem in the SQLite db
        ListTheme newListTheme = mListThemeRepository.insert(mListTheme);
        if(newListTheme!=null){
            postListThemeCreated(newListTheme);
        }else{
            notifyError(String.format("FAILED to create ListTheme \"%s\".", mListTheme.getName()));
        }
    }

    private void postListThemeCreated(final ListTheme newListTheme) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeCreated(newListTheme);
            }
        });
    }

    private void notifyError(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemeCreationFailed(errorMessage);
            }
        });
    }

}
