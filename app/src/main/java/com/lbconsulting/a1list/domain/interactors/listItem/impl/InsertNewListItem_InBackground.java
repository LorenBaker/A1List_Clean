package com.lbconsulting.a1list.domain.interactors.listItem.impl;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.InsertNewListItem;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository;

/**
 * An interactor that creates a new ListItem
 */
public class InsertNewListItem_InBackground extends AbstractInteractor implements InsertNewListItem{

    private final Callback mCallback;
    private final ListItemRepository mListItemRepository;
    private final ListItem mNewListItem;

    public InsertNewListItem_InBackground(Executor threadExecutor, MainThread mainThread,
                                          Callback callback, ListItem newListItem) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mNewListItem = newListItem;
        mListItemRepository = AndroidApplication.getListItemRepository();
    }


    @Override
    public void run() {
        // insert the new ListItem in the SQLite db
        if(mListItemRepository.insert(mNewListItem)){
            String successMessage = String.format("Successfully inserted \"%s\" into SQLite db.",mNewListItem.getName());
            postListItemCreated(successMessage);
        }else{
            String errorMessage = String.format("FAILED to insert \"%s\" into SQLite db.",mNewListItem.getName());
            postListItemCreated(errorMessage);
        }
    }

    private void postListItemCreated(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemInsertedIntoSQLiteDb(successMessage);
            }
        });
    }

    private void notifyError(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListItemInsertionIntoSQLiteDbFailed(errorMessage);
            }
        });
    }

}
