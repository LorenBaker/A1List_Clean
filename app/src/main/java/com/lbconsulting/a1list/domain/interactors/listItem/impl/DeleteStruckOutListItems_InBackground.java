package com.lbconsulting.a1list.domain.interactors.listItem.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.DeleteStruckOutListItems;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository;

import java.util.List;

/**
 * An interactor that deletes struck out ListItems
 */
public class DeleteStruckOutListItems_InBackground extends AbstractInteractor implements DeleteStruckOutListItems {

    private final Callback mCallback;
    private final ListItemRepository mListItemRepository;
    private final ListTitle mListTitle;


    public DeleteStruckOutListItems_InBackground(Executor threadExecutor, MainThread mainThread,
                                                 Callback callback, ListItemRepository listItemRepository,
                                                 ListTitle listTitle) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListItemRepository = listItemRepository;
        mListTitle = listTitle;
    }


    @Override
    public void run() {

//         get all struck out ListItems
        List<ListItem> struckOutListItems = mListItemRepository.retrieveStruckOutListItems(mListTitle);
        if (struckOutListItems.size() > 0) {
            int numberOfListItemsDeleted = 0;
            for (ListItem listItem : struckOutListItems) {
                numberOfListItemsDeleted += mListItemRepository.delete(listItem);
            }

            if (struckOutListItems.size() == numberOfListItemsDeleted) {
                // Success
                String successMessage = String.format("All %d struck out ListItems deleted.",
                        numberOfListItemsDeleted);
                postStruckOutListItemsDeleted(successMessage);
            } else {
                // Not all ListItems deleted
                String errorMessage = String.format("Only %d of %d struck out ListItems deleted.",
                        numberOfListItemsDeleted, struckOutListItems.size());
                postStruckOutListItemsDeletionFailed(errorMessage);
            }

        } else {
            postStruckOutListItemsDeletionFailed("No struck out ListItems found.");
        }
    }


    private void postStruckOutListItemsDeleted(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onStruckOutListItemsDeleted(successMessage);
            }
        });
    }

    private void postStruckOutListItemsDeletionFailed(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onStruckOutListItemsDeletionFailed(errorMessage);
            }
        });
    }
}
