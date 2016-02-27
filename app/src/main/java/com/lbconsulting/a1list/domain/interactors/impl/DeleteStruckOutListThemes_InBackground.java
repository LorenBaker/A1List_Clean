package com.lbconsulting.a1list.domain.interactors.impl;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.interfaces.DeleteStruckOutListThemes_Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repository.ListThemeRepository;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * An interactor that retrieves all ListThemes
 */
public class DeleteStruckOutListThemes_InBackground extends AbstractInteractor implements DeleteStruckOutListThemes_Interactor {


    private final Callback mCallback;
    private final ListThemeRepository mListThemeRepository;


    public DeleteStruckOutListThemes_InBackground(Executor threadExecutor, MainThread mainThread,
                                                  Callback callback, ListThemeRepository listThemeRepository) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListThemeRepository = listThemeRepository;
    }


    @Override
    public void run() {

        // get all struck out ListThemes
        List<ListTheme> struckOutListThemes = mListThemeRepository.retrieveStruckOutListThemes();
        if (struckOutListThemes.size() > 0) {
            ListTheme defaultListTheme = mListThemeRepository.retrieveDefaultListTheme();
            if (defaultListTheme == null) {
                Timber.e("DeleteStruckOutListThemes_InBackground: Failed to retrieve default ListTheme!");
            }
            int numberOfListThemesDeleted = 0;
            int numberOfListThemesDeletedFromBackendless = 0;

            if (CommonMethods.isNetworkAvailable()) {
                for (ListTheme listTheme : struckOutListThemes) {
                    if (!listTheme.isDefaultTheme()) {
                        numberOfListThemesDeleted += mListThemeRepository.delete(listTheme);
                        // Delete ListThemes from Backendless
                        try {
                            long timestamp = Backendless.Data.of(ListTheme.class).remove(listTheme);
                            String msg = "\"" + listTheme.getName() +"\" removed at " + new Date(timestamp).toString();
                        } catch (BackendlessException e) {
                            Timber.e("DeleteStruckOutListThemes_InBackground(): BackendlessException: %s.", e.getMessage());
                        }
                        // TODO: In the ListTitles Table, replace any ListTheme being deleted with the default ListTheme.
                        // TODO: Update changed ListTitles in Backendless
                        // TODO: Send ListTheme delete and ListTitle update messages to other devices

                    } else {
                        Timber.e("DeleteStruckOutListThemes_InBackground: Aborted deleting the default ListTheme.");
                    }
                }

            } else {
                for (ListTheme listTheme : struckOutListThemes) {
                    if (!listTheme.isDefaultTheme()) {
                        numberOfListThemesDeleted += mListThemeRepository.markDeleted(listTheme);
                        // TODO: In the ListTitles Table, replace any ListTheme being deleted with the default ListTheme. Mark them dirty.
                    } else {
                        Timber.e("DeleteStruckOutListThemes_InBackground: Aborted deleting the default ListTheme.");
                    }
                }
            }

            if (struckOutListThemes.size() == numberOfListThemesDeleted) {
                // Success
                String successMessage = String.format("All %d struck out ListThemes deleted.", numberOfListThemesDeleted);
                postListThemesDeleted(successMessage);
            } else {
                String errorMessage = String.format("Only %d of %d struck out ListThemes deleted.",
                        numberOfListThemesDeleted, struckOutListThemes.size());
                notifyError(errorMessage);
            }

        } else {
            notifyError("No struck out ListThemes found.");
        }
    }


    private void postListThemesDeleted(final String successMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onStruckOutListThemesDeleted(successMessage);
            }
        });
    }

    private void notifyError(final String errorMessage) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onStruckOutListThemesDeletionFailed(errorMessage);
            }
        });
    }
}
