package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.CreateNewListTitle_Interactor;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository;

/**
 * An interactor that creates a new ListTitle
 */
public class CreateNewListTitle_InBackground extends AbstractInteractor implements CreateNewListTitle_Interactor {

    private final Callback mCallback;
//    private final AppSettingsRepository mAppSettingsRepository;
    private final ListTitleRepository mListTitleRepository;
//    private final ListThemeRepository mListThemeRepository;
    private final ListTitle mNewListTitle;
//    private final boolean mHideProgressBar;

    public CreateNewListTitle_InBackground(Executor threadExecutor, MainThread mainThread,
                                           Callback callback, ListTitle newListTitle,
                                           ListTitleRepository listTitleRepository) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mNewListTitle = newListTitle;
//        mAppSettingsRepository = appSettingsRepository;
        mListTitleRepository = listTitleRepository;
//        mListThemeRepository = listThemeRepository;
//        mHideProgressBar = hideProgressBar;
    }


    @Override
    public void run() {

        // insert the new ListTitle in the SQLite db and to Backendless.
        ListTitle newListTitle = mListTitleRepository.insert(mNewListTitle);
        if (newListTitle != null) {
            postListTitleCreated(newListTitle);
        } else {
            notifyError(String.format("FAILED to create ListTitle \"%s\".", mNewListTitle));
        }
    }

    private void postListTitleCreated(final ListTitle newListTitle) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListTitleCreated(newListTitle);
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
