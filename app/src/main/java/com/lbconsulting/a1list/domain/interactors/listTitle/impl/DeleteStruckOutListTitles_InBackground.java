package com.lbconsulting.a1list.domain.interactors.listTitle.impl;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.DeleteStruckOutListTitles_Interactor;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_interface;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * An interactor that deletes struck out ListTitles
 */
public class DeleteStruckOutListTitles_InBackground extends AbstractInteractor implements DeleteStruckOutListTitles_Interactor {


    private final Callback mCallback;
    private final ListTitleRepository_interface mListTitleRepository;


    public DeleteStruckOutListTitles_InBackground(Executor threadExecutor, MainThread mainThread,
                                                  Callback callback, ListTitleRepository_interface listTitleRepository) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListTitleRepository = listTitleRepository;
    }


    @Override
    public void run() {

//         get all struck out ListTitles
        List<ListTitle> struckOutListTitles = mListTitleRepository.retrieveStruckOutListTitles();
        if (struckOutListTitles.size() > 0) {
            int numberOfListTitlesDeleted = 0;
            int numberOfListTitlesDeletedFromBackendless = 0;
//
            if (CommonMethods.isNetworkAvailable()) {
                for (ListTitle listTitle : struckOutListTitles) {
                    // TODO: In the ListItems Table, delete any ListItems associated with the ListTitle being deleted.

                    // delete the ListTitle from the local SQLite db
                    numberOfListTitlesDeleted += mListTitleRepository.delete(listTitle);
                    // Delete ListTitle from Backendless
                    try {
                        long timestamp = Backendless.Data.of(ListTitle.class).remove(listTitle);
                        String msg = "\"" + listTitle.getName() + "\" removed from Backendless at " + new Date(timestamp).toString();
                        Timber.i("run(): %s", msg);
                    } catch (BackendlessException e) {
                        Timber.e("DeleteStruckOutListTitles_InBackground(): BackendlessException: %s.", e.getMessage());
                    }
                    // TODO: Send ListTitle delete messages to other devices
                }

            } else {
                // network not available
                for (ListTitle listTitle : struckOutListTitles) {
                    // TODO: In the ListItems Table, mark for deletion any ListItems associated with the ListTitle being deleted.
                    numberOfListTitlesDeleted += mListTitleRepository.markDeleted(listTitle);
                }
            }

            if (struckOutListTitles.size() == numberOfListTitlesDeleted) {
                // Success
                String successMessage = String.format("All %d struck out ListTitles deleted.", numberOfListTitlesDeleted);
                postStruckOutListTitlesDeleted(successMessage);
            } else {
                String errorMessage = String.format("Only %d of %d struck out ListTitles deleted.",
                        numberOfListTitlesDeleted, struckOutListTitles.size());
                notifyError(errorMessage);
            }

        } else {
            notifyError("No struck out ListTitles found.");
        }
    }


    private void postStruckOutListTitlesDeleted(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onStruckOutListTitlesDeleted(successMessage);
            }
        });
    }

    private void notifyError(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onStruckOutListTitlesDeletionFailed(errorMessage);
            }
        });
    }
}
