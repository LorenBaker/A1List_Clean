package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.CreateNewListTitle_Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_interface;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_interface;

/**
 * An interactor that creates a new ListTitle
 */
public class CreateNewListTitle_InBackground extends AbstractInteractor implements CreateNewListTitle_Interactor {

    private final Callback mCallback;
    private final ListTitleRepository_interface mListTitleRepository;
    private final ListThemeRepository_interface mListThemeRepository;
    private final String mNewListTitleName;
    private final boolean mHideProgressBar;

    public CreateNewListTitle_InBackground(Executor threadExecutor, MainThread mainThread,
                                           Callback callback, String newListTitleName,
                                           ListTitleRepository_interface listTitleRepository,
                                           ListThemeRepository_interface listThemeRepository,
                                           boolean hideProgressBar) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mNewListTitleName = newListTitleName;
        mListTitleRepository = listTitleRepository;
        mListThemeRepository = listThemeRepository;
        mHideProgressBar = hideProgressBar;
    }


    @Override
    public void run() {

        if (mNewListTitleName == null || mNewListTitleName.isEmpty()) {
            notifyError("FAILED to create ListTitle. No ListTitle name provided!");
            return;
        }

        ListTheme defaultListTheme = mListThemeRepository.retrieveDefaultListTheme();
        if (defaultListTheme == null) {
            notifyError(String.format("FAILED to create ListTitle \"%s\". Failed to retrieve default ListTheme!", mNewListTitleName));
            return;
        }
        ListTitle proposedListTitle = ListTitle.newInstance(mNewListTitleName,defaultListTheme);

        // insert the new ListTitle in the SQLite db and to Backendless.
        ListTitle newListTitle = mListTitleRepository.insert(proposedListTitle);
        if (newListTitle != null) {
            postListTitleCreated(newListTitle,mHideProgressBar);
        } else {
            notifyError(String.format("FAILED to create ListTitle \"%s\".", mNewListTitleName));
        }
    }

    private void postListTitleCreated(final ListTitle newListTitle, final boolean hideProgressBar) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleCreated(newListTitle,hideProgressBar);
            }
        });
    }

    private void notifyError(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleCreationFailed(errorMessage);
            }
        });
    }

}
