package com.lbconsulting.a1list.domain.interactors.listTheme.impl;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.DeleteStruckOutListThemes_Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_interface;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_interface;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * An interactor that retrieves all ListThemes
 */
public class DeleteStruckOutListThemes_InBackground extends AbstractInteractor implements DeleteStruckOutListThemes_Interactor {


    private final Callback mCallback;
    private final ListThemeRepository_interface mListThemeRepository;
    private final ListTitleRepository_interface mListTitleRepository;


    public DeleteStruckOutListThemes_InBackground(Executor threadExecutor, MainThread mainThread,
                                                  Callback callback,
                                                  ListThemeRepository_interface listThemeRepository,
                                                  ListTitleRepository_interface listTitleRepository) {
        super(threadExecutor, mainThread);

        mCallback = callback;
        mListThemeRepository = listThemeRepository;
        mListTitleRepository = listTitleRepository;
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
                            String msg = "\"" + listTheme.getName() + "\" removed at " + new Date(timestamp).toString();
                        } catch (BackendlessException e) {
                            Timber.e("DeleteStruckOutListThemes_InBackground(): BackendlessException: %s.", e.getMessage());
                        }
                        // Replace any ListTheme being deleted with the default ListTheme.
                        mListTitleRepository.replaceListTheme(listTheme, defaultListTheme, true);
                        // TODO: Send ListTheme delete and ListTitle update messages to other devices

                    } else {
                        Timber.e("DeleteStruckOutListThemes_InBackground: Aborted deleting the default ListTheme.");
                    }
                }

            } else {
                for (ListTheme listTheme : struckOutListThemes) {
                    if (!listTheme.isDefaultTheme()) {
                        numberOfListThemesDeleted += mListThemeRepository.markDeleted(listTheme);
                        // Replace any ListTheme being deleted with the default ListTheme.
                        mListTitleRepository.replaceListTheme(listTheme, defaultListTheme, false);
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
