package com.lbconsulting.a1list.presentation.presenters.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.listTheme.impl.RetrieveAllListThemes_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.RetrieveAllListThemes;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository;
import com.lbconsulting.a1list.presentation.presenters.base.AbstractPresenter;
import com.lbconsulting.a1list.presentation.presenters.interfaces.ListThemesPresenter;

import java.util.List;

import timber.log.Timber;

/**
 * Presents List<ListTheme>
 */
public class ListThemesPresenter_Impl extends AbstractPresenter implements ListThemesPresenter,
        RetrieveAllListThemes.Callback {

    private final ListThemesPresenter.ListThemeView mView;
    private final ListThemeRepository mListThemeRepository;

    private ListTheme mListTheme;
    private String mAction;
    private RetrieveAllListThemes mRetrieveAllListThemes_inBackground;

    public ListThemesPresenter_Impl(Executor executor,
                                    MainThread mainThread,
                                    ListThemesPresenter.ListThemeView view,
                                    ListThemeRepository listThemeRepository) {
        super(executor, mainThread);
        mView = view;
        mListThemeRepository = listThemeRepository;
        // initialize the interactor
        mRetrieveAllListThemes_inBackground = new RetrieveAllListThemes_InBackground(mExecutor, mMainThread,
                this, mListThemeRepository);
    }


    @Override
    public void resume() {
        mView.showProgress("Retrieving all Themes.");
        mRetrieveAllListThemes_inBackground.execute();
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
    public void onAllListThemesRetrieved(List<ListTheme> listThemes) {
        mView.hideProgress("");
        mView.displayAllListThemes(listThemes);
    }

    @Override
    public void onRetrievalFailed(String errorMessage) {
        mView.hideProgress("");
        onError(errorMessage);
    }


//    public void toggleBold(ListTheme listTheme) {
//        mListThemeRepository.toggle(listTheme, ListThemesSqlTable.COL_BOLD, true);
//        resume();
//    }
//
//    public void toggleChecked(ListTheme listTheme) {
//        mListThemeRepository.toggle(listTheme, ListThemesSqlTable.COL_CHECKED, true);
//        resume();
//    }
//
//    public void toggleDefaultTheme(ListTheme listTheme) {
//        mListThemeRepository.toggle(listTheme, ListThemesSqlTable.COL_DEFAULT_THEME, true);
//        resume();
//    }
//
//    public void toggleMarkedForDeletion(ListTheme listTheme) {
//        mListThemeRepository.toggle(listTheme, ListThemesSqlTable.COL_MARKED_FOR_DELETION, true);
//        resume();
//    }
//
//    public void toggleTransparent(ListTheme listTheme) {
//        mListThemeRepository.toggle(listTheme, ListThemesSqlTable.COL_TRANSPARENT, true);
//        resume();
//    }
}
