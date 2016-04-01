package com.lbconsulting.a1list.presentation.presenters.impl;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_Impl;
import com.lbconsulting.a1list.presentation.presenters.base.AbstractPresenter;
import com.lbconsulting.a1list.presentation.presenters.interfaces.ListTitlesPresenter;

import java.util.List;

import timber.log.Timber;

/**
 * Presents List<ListTitle>
 */
public class ListTitlesPresenter_Impl extends AbstractPresenter implements ListTitlesPresenter {

    private final ListTitleView mView;
//    private final ListTitleRepository_Impl mListTitleRepository;

//    private ListTitle mListTitle;
//    private String mAction;
    private boolean mIsSortedAlphabetically;
    private ListTitleRepository_Impl mListTitleRepository;
//    private RetrieveAllListTitles mRetrieveAllListTitles_inBackground;

    public ListTitlesPresenter_Impl(Executor executor,
                                    MainThread mainThread,
                                    ListTitleView view,
                                    boolean isSortedAlphabetically) {
        super(executor, mainThread);
        mView = view;
        mIsSortedAlphabetically = isSortedAlphabetically;
        mListTitleRepository = AndroidApplication.getListTitleRepository();
//        mListTitleRepository = listTitleRepository;
        // initialize the interactor
//        mRetrieveAllListTitles_inBackground = new RetrieveAllListTitles_InBackground(mExecutor, mMainThread,
//                this, isSortedAlphabetically);
    }


    @Override
    public void resume() {
//        mView.showProgress("Retrieving Lists.");
        List<ListTitle> listTitles = mListTitleRepository.retrieveAllListTitles(false, mIsSortedAlphabetically);
        mView.onPresenterAllListTitlesRetrieved(listTitles);
//        mView.hideProgress("");
//        mRetrieveAllListTitles_inBackground.execute();
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

//    @Override
//    public void onAllListTitlesRetrieved(List<ListTitle> listTitles) {
//        mView.hideProgress("");
//        mView.onPresenterAllListTitlesRetrieved(listTitles);
//    }
//
//    @Override
//    public void onAllListTitlesRetrievalFailed(String errorMessage) {
//        mView.hideProgress("");
//        onError(errorMessage);
//    }

}
