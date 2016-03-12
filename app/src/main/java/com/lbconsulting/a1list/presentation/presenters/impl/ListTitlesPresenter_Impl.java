package com.lbconsulting.a1list.presentation.presenters.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.RetrieveAllListTitles_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.RetrieveAllListTitles_Interactor;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository;
import com.lbconsulting.a1list.presentation.presenters.base.AbstractPresenter;
import com.lbconsulting.a1list.presentation.presenters.interfaces.ListTitlesPresenter;

import java.util.List;

import timber.log.Timber;

/**
 * Presents List<ListTitle>
 */
public class ListTitlesPresenter_Impl extends AbstractPresenter implements ListTitlesPresenter,
        RetrieveAllListTitles_Interactor.Callback {

    private final ListTitleView mView;
    private final ListTitleRepository mListTitleRepository;

    private ListTitle mListTitle;
    private String mAction;
    private RetrieveAllListTitles_Interactor mRetrieveAllListTitles_inBackground;

    public ListTitlesPresenter_Impl(Executor executor,
                                    MainThread mainThread,
                                    ListTitleView view,
                                    ListTitleRepository listTitleRepository) {
        super(executor, mainThread);
        mView = view;
        mListTitleRepository = listTitleRepository;
        // initialize the interactor
        mRetrieveAllListTitles_inBackground = new RetrieveAllListTitles_InBackground(mExecutor, mMainThread,
                this, mListTitleRepository);
    }


    @Override
    public void resume() {
        mView.showProgress("Retrieving all Lists.");
        mRetrieveAllListTitles_inBackground.execute();
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
    public void onAllListTitlesRetrieved(List<ListTitle> listTitles) {
        mView.hideProgress();
        mView.displayAllListTitles(listTitles);
    }

    @Override
    public void onAllListTitlesRetrievalFailed(String errorMessage) {
        mView.hideProgress();
        onError(errorMessage);
    }

}
