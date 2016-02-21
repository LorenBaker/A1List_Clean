package com.lbconsulting.a1list.presentation.presenters.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.AllListThemeInteractor;
import com.lbconsulting.a1list.domain.interactors.impl.AllListThemeInteractor_Impl;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repository.ListThemeRepository;
import com.lbconsulting.a1list.domain.storage.ListThemeSqlTable;
import com.lbconsulting.a1list.presentation.presenters.base.AbstractPresenter;
import com.lbconsulting.a1list.presentation.presenters.interfaces.ManageListThemesActvityPresenter;

import java.util.List;

import timber.log.Timber;

/**
 * Presents List<ListTheme>
 */
public class ManageListThemesActivityPresenter_Impl extends AbstractPresenter implements ManageListThemesActvityPresenter,
        AllListThemeInteractor.Callback {

    private final ManageListThemesActvityPresenter.ListThemeView mView;
    private final ListThemeRepository mListThemeRepository;

    private ListTheme mListTheme;
    private String mAction;
    private AllListThemeInteractor mInteractor;

    public ManageListThemesActivityPresenter_Impl(Executor executor,
                                                  MainThread mainThread,
                                                  ManageListThemesActvityPresenter.ListThemeView view,
                                                  ListThemeRepository listThemeRepository) {
        super(executor, mainThread);
        mView = view;
        mListThemeRepository = listThemeRepository;
        // initialize the interactor
        mInteractor = new AllListThemeInteractor_Impl(mExecutor, mMainThread,
                this, mListThemeRepository);
    }

//    public void toggleStrikeout(ListTheme listTheme) {
//        mListThemeRepository.toggle(listTheme, ListThemeSqlTable.COL_STRUCK_OUT,true);
//        resume();
//    }

//    public void setAction(ListTheme listTheme, String action){
//        mListTheme=listTheme;
//        mAction=action;
//    }
//    public void resumeWithAction(ListTheme listTheme, String action){
//        mListTheme = listTheme;
//        mAction = action;
//        resume();
//    }

    @Override
    public void resume() {
        mView.showProgress();

        // initialize the interactor
//        mInteractor = new AllListThemeInteractor_Impl(mExecutor, mMainThread,
//                this, mListThemeRepository);


        // run the interactor
//        interactor.setAction(mListTheme, mAction);
        mInteractor.execute();
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
        mView.hideProgress();
        mView.displayAllListThemes(listThemes);
    }

    @Override
    public void onRetrievalFailed(String errorMessage) {
        mView.hideProgress();
        onError(errorMessage);
    }


    public void toggleBold(ListTheme listTheme) {
        mListThemeRepository.toggle(listTheme, ListThemeSqlTable.COL_BOLD, true);
        resume();
    }

    public void toggleChecked(ListTheme listTheme) {
        mListThemeRepository.toggle(listTheme, ListThemeSqlTable.COL_CHECKED, true);
        resume();
    }

    public void toggleDefaultTheme(ListTheme listTheme) {
        mListThemeRepository.toggle(listTheme, ListThemeSqlTable.COL_DEFAULT_THEME, true);
        resume();
    }

    public void toggleMarkedForDeletion(ListTheme listTheme) {
        mListThemeRepository.toggle(listTheme, ListThemeSqlTable.COL_MARKED_FOR_DELETION, true);
        resume();
    }

    public void toggleStrikeout(ListTheme listTheme) {
        mListThemeRepository.toggle(listTheme, ListThemeSqlTable.COL_STRUCK_OUT, true);
        resume();
    }

    public void toggleTransparent(ListTheme listTheme) {
        mListThemeRepository.toggle(listTheme, ListThemeSqlTable.COL_TRANSPARENT, true);
        resume();
    }
}
