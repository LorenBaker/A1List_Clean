package com.lbconsulting.a1list.domain.interactors.impl;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.CreateInitialListThemesInteractor;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repository.ListThemeRepository;

import java.util.List;

import timber.log.Timber;

/**
 * An interactor that retrieves all ListThemes
 */
public class CreateInitialListThemesInteractor_Imp extends AbstractInteractor implements CreateInitialListThemesInteractor {

    private final Callback mCallback;
    private final ListThemeRepository mListThemeRepository;
    private final Context mContext;

    public CreateInitialListThemesInteractor_Imp(Executor threadExecutor, MainThread mainThread,
                                                 Callback callback, ListThemeRepository listThemeRepository,
                                                 Context context) {
        super(threadExecutor, mainThread);
        mCallback = callback;
        mListThemeRepository = listThemeRepository;
        mContext = context;
    }

    @Override
    public void run() {
        try {
            // create initial ListThemes
            int requestedListThemeCount = 0;
            int newBackendlessListThemeCount = 0;

            ListTheme newListTheme;

            newListTheme = ListTheme.newInstance("Genoa",
                    Color.parseColor("#4c898e"), Color.parseColor("#125156"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, true);
            requestedListThemeCount++;
            ListTheme response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Opal",
                    Color.parseColor("#cbdcd4"), Color.parseColor("#91a69d"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Shades of Blue",
                    -5777934, -10841921,
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Off White",
                    ContextCompat.getColor(mContext, R.color.white), -2436147,
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, true, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Whiskey",
                    Color.parseColor("#e9ac6d"), Color.parseColor("#ad7940"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Shakespeare",
                    Color.parseColor("#73c5d3"), Color.parseColor("#308d9e"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Sorbus",
                    Color.parseColor("#f0725b"), Color.parseColor("#bc3c21"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Dark Khaki",
                    Color.parseColor("#ced285"), Color.parseColor("#9b9f55"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Lemon Chiffon",
                    Color.parseColor("#fdfcdd"), Color.parseColor("#e3e2ac"),
                    ContextCompat.getColor(mContext, R.color.black),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Paprika",
                    Color.parseColor("#994552"), Color.parseColor("#5f0c16"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }

            newListTheme = ListTheme.newInstance("Medium Wood",
                    Color.parseColor("#bfaa75"), Color.parseColor("#8a7246"),
                    ContextCompat.getColor(mContext, R.color.white),
                    17f, 10f, 10f, false, false, false);
            requestedListThemeCount++;
            response = mListThemeRepository.insert(newListTheme);
            if (response != null) {
                newBackendlessListThemeCount++;
            }



            final List<ListTheme> allListThemes = mListThemeRepository.getAllListThemes(false);
            String createdListThemeMessage;
            // check if we have failed to retrieve any ListThemes
            if (allListThemes == null) {
                // notify the failure on the main thread
                notifyError("No ListThemes created!");

            } else {
                // we have created ListThemes. Notify the UI on the main thread.
                if (allListThemes.size() != requestedListThemeCount) {
                    createdListThemeMessage = String.format("Only %d out of %d requested ListThemes created in the SQLite db.", allListThemes.size(), requestedListThemeCount);

                } else {
                    createdListThemeMessage = String.format("All %d ListThemes created in the SQLite db.", requestedListThemeCount);
                }

                if(allListThemes.size() != newBackendlessListThemeCount){
                    createdListThemeMessage = createdListThemeMessage + "\n" +
                            String.format("Only %d out of %d requested ListThemes saved to Backendless.",  newBackendlessListThemeCount,requestedListThemeCount);
                }else {
                    createdListThemeMessage = createdListThemeMessage + "\n" +
                            String.format("All %d ListThemes saved to Backendless.", requestedListThemeCount);
                }

                postInitialListThemesCreated(allListThemes, createdListThemeMessage);

            }
        } catch (Exception e) {
            Timber.e("run(): Exception: %s.", e.getMessage());
        }
    }

    private void postInitialListThemesCreated(final List<ListTheme> allListThemes, final String message) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onInitialListThemesCreated(allListThemes, message);
            }
        });
    }

    private void notifyError(String message) {

        final String msg = message;
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onListThemesCreationFailed(msg);
            }
        });
    }
}
