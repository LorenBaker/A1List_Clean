package com.lbconsulting.a1list.presentation.presenters.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.listItem.impl.RetrieveAllListItems_InBackground;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.RetrieveAllListItems;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository;
import com.lbconsulting.a1list.presentation.presenters.base.AbstractPresenter;
import com.lbconsulting.a1list.presentation.presenters.interfaces.ListItemsPresenter;

import java.util.List;

import timber.log.Timber;

/**
 * Presents List<ListItem>
 */
public class ListItemsPresenter_Impl extends AbstractPresenter implements ListItemsPresenter,
        RetrieveAllListItems.Callback {

    private final ListItemView mView;
    private final ListItemRepository mListItemRepository;

    private ListTitle mListTitle;
    private ListItem mListItem;
    private String mAction;
    private RetrieveAllListItems mRetrieveAllListItems_inBackground;

    public ListItemsPresenter_Impl(Executor executor,
                                   MainThread mainThread,
                                   ListItemView view,
                                   ListItemRepository listItemRepository,
                                   ListTitle listTitle) {
        super(executor, mainThread);
        mView = view;
        mListItemRepository = listItemRepository;
        mListTitle = listTitle;
        // initialize the interactor
        mRetrieveAllListItems_inBackground = new RetrieveAllListItems_InBackground(mExecutor, mMainThread,
                this, mListItemRepository, listTitle);
    }


    @Override
    public void resume() {
        mView.showProgress(String.format("Retrieving Items for \"%s\".", mListTitle.getName()));
        mRetrieveAllListItems_inBackground.execute();
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
    public void onAllListItemsRetrieved(List<ListItem> listItems) {
        mView.hideProgress(String.format("Completed retrieving %d Items for \"%s\".",listItems.size(), mListTitle.getName()));
        mView.displayListItems(listItems);
    }

    @Override
    public void onAllListItemsRetrievalFailed(String errorMessage) {
        mView.hideProgress(errorMessage);
        onError(errorMessage);
    }

}
