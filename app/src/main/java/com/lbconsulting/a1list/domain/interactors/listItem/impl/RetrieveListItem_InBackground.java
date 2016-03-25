package com.lbconsulting.a1list.domain.interactors.listItem.impl;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.RetrieveListItem;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository;

/**
 * An interactor that retrieves a ListItem with the provided uuid
 */
public class RetrieveListItem_InBackground extends AbstractInteractor implements RetrieveListItem {

    private final Callback mCallback;
    private final ListItemRepository mListItemRepository;
    private final String mListItemUuid;

    public RetrieveListItem_InBackground(Executor threadExecutor, MainThread mainThread,
                                         Callback callback, String listItemUuid) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListItemRepository = AndroidApplication.getListItemRepository();
        mListItemUuid = listItemUuid;
    }


    @Override
    public void run() {

        // retrieve the original ListItem
        final ListItem listItem = mListItemRepository.retrieveListItemByUuid(mListItemUuid);
        // check if we have failed to retrieve the ListItem
        if (listItem == null) {
            // notify the failure on the main thread
            postListItemRetrievalFailed("Unable to retrieve ListItem with ID = " + mListItemUuid);
        } else {
            // we have retrieved the original ListItems. Now clone it.
                        postListItemRetrieved(listItem);
        }
    }

    private void postListItemRetrieved(final ListItem listItem) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemRetrieved(listItem);
            }
        });
    }

    private void postListItemRetrievalFailed(final String message) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemRetrievalFailed(message);
            }
        });
    }

}
