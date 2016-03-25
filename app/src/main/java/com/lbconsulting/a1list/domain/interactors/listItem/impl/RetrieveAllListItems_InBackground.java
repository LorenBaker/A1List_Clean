package com.lbconsulting.a1list.domain.interactors.listItem.impl;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.RetrieveAllListItems;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository;

import java.util.List;

/**
 * An interactor that retrieves all ListItems
 */
public class RetrieveAllListItems_InBackground extends AbstractInteractor implements RetrieveAllListItems {

    private final Callback mCallback;
    private final ListItemRepository mListItemRepository;
    private final ListTitle mListTitle;
    private ListItem mSelectedListItem;

    public RetrieveAllListItems_InBackground(Executor threadExecutor, MainThread mainThread,
                                             Callback callback, ListTitle listTitle) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListItemRepository = AndroidApplication.getListItemRepository();
        mListTitle = listTitle;
    }


    @Override
    public void run() {

        // retrieve all ListItems that are not marked for deletion
        final List<ListItem> allListItems = mListItemRepository.retrieveAllListItems(mListTitle, false);
        // check if we have failed to retrieve any ListItems
        if (allListItems == null || allListItems.size() == 0) {
            // notify the failure on the main thread
            postAllListItemsRetrievalFailed();
        } else {
            // we have retrieved all ListItems. Notify the UI on the main thread.
            postAllListItemsRetrieved(allListItems);
        }
    }

    private void postAllListItemsRetrieved(final List<ListItem> allListItems) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onAllListItemsRetrieved(allListItems);
            }
        });
    }

    private void postAllListItemsRetrievalFailed() {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                String errorMessage = String.format("No ListItems retrieved for \"%s\".",mListTitle.getName());
                mCallback.onAllListItemsRetrievalFailed(errorMessage);
            }
        });
    }
}
