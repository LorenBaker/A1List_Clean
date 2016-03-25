package com.lbconsulting.a1list.presentation.presenters.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.listTheme.impl.RetrieveListTheme_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.RetrieveListTheme;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository;
import com.lbconsulting.a1list.presentation.presenters.base.AbstractPresenter;
import com.lbconsulting.a1list.presentation.presenters.interfaces.ListThemeActivityPresenter;

import timber.log.Timber;

/**
 * Presents a cloned ListTheme
 */
public class ListThemeActivityPresenter_Impl extends AbstractPresenter implements ListThemeActivityPresenter,
        RetrieveListTheme.Callback {

    private ListThemeActivityPresenter.ListThemeActivityView mView;
    private ListThemeRepository mListThemeRepository;

    private RetrieveListTheme_InBackground mRetrieveListTheme_InBackground;

    public ListThemeActivityPresenter_Impl(Executor executor,
                                           MainThread mainThread,
                                           ListThemeActivityPresenter.ListThemeActivityView view,
                                           ListThemeRepository listThemeRepository,
                                           String listThemeUuid) {
        super(executor, mainThread);
        mView = view;
        mListThemeRepository = listThemeRepository;

        // initialize the interactor
        mRetrieveListTheme_InBackground = new RetrieveListTheme_InBackground(mExecutor, mMainThread,
                this, listThemeUuid);
    }

    @Override
    public void resume() {
        mView.showProgress("Retrieving Theme.");
        mRetrieveListTheme_InBackground.execute();
    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void onError(String message) {
        Timber.e("onError(): %s.", message);
    }

    @Override
    public void onListThemeRetrieved(ListTheme listTheme) {
        mView.hideProgress("");
        mView.displayRetrievedListTheme(listTheme);
    }

    @Override
    public void onListThemeRetrievalFailed(String errorMessage) {
        mView.hideProgress("");
        onError(errorMessage);
    }
}
