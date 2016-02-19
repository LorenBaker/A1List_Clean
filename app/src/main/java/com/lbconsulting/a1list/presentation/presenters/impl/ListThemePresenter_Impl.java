package com.lbconsulting.a1list.presentation.presenters.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.AllListThemeInteractor;
import com.lbconsulting.a1list.domain.interactors.impl.AllListThemeInteractor_Impl;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repository.ListThemeRepository;
import com.lbconsulting.a1list.presentation.presenters.ListThemesPresenter;
import com.lbconsulting.a1list.presentation.presenters.base.AbstractPresenter;

import java.util.List;

/**
 * Presents List<ListTheme>
 */
public class ListThemePresenter_Impl extends AbstractPresenter implements ListThemesPresenter,
        AllListThemeInteractor.Callback {

    private final ListThemesPresenter.ListThemeView mView;
    private final ListThemeRepository mListThemeRepository;

    public ListThemePresenter_Impl(Executor executor,
                                   MainThread mainThread,
                                   ListThemesPresenter.ListThemeView view,
                                   ListThemeRepository listThemeRepository) {
        super(executor, mainThread);
        mView = view;
        mListThemeRepository = listThemeRepository;
    }

    @Override
    public void resume() {
        mView.showProgress();

        // initialize the interactor
        AllListThemeInteractor interactor = new AllListThemeInteractor_Impl(mExecutor, mMainThread,
                this, mListThemeRepository);

        // run the interactor
        interactor.execute();
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

    }

    @Override
    public void onAllListThemesRetrieved(List<ListTheme> listThemes) {
        mView.hideProgress();
        mView.displayAllListThemes(listThemes);
    }

    @Override
    public void onRetrievalFailed(String errorMessage) {
        mView.hideProgress();
        onError(errorMessage);
    }
}
