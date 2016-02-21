package com.lbconsulting.a1list.domain.interactors.impl;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.ToggleStrikeoutInteractor;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repository.ListThemeRepository;
import com.lbconsulting.a1list.domain.storage.ListThemeSqlTable;

import java.util.List;

/**
 * This is an interactor toggles a ListTheme's strikeout attribute.
 * <p/>
 */
public class ToggleStrikeout_Impl extends AbstractInteractor implements ToggleStrikeoutInteractor {

    private final ToggleStrikeoutInteractor.Callback mCallback;
    private final ListThemeRepository mListThemeRepository;
    private ListTheme mListTheme;

    public ToggleStrikeout_Impl(Executor threadExecutor,
                                MainThread mainThread,
                                Callback callback, ListThemeRepository listThemeRepository,
                                ListTheme listTheme) {
        super(threadExecutor, mainThread);
        mCallback = callback;
        mListThemeRepository = listThemeRepository;
        mListTheme = listTheme;
    }

    @Override
    public void run() {
        // TODO: Implement this with your business logic
        mListThemeRepository.toggle(mListTheme, ListThemeSqlTable.COL_STRUCK_OUT, true);
        final List<ListTheme> allListThemes = mListThemeRepository.getAllListThemes(false);
        // check if we have failed to retrieve any ListThemes
        if (allListThemes == null || allListThemes.size() == 0) {
            // notify the failure on the main thread
            notifyError();
        } else {
            // we have retrieved all ListThemes. Notify the UI on the main thread.
            postAllListThemes(allListThemes);
        }
    }


    private void postAllListThemes(final List<ListTheme> allListThemes) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onStrikeout(allListThemes);
            }
        });
    }

    private void notifyError() {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onToggleStrikeoutFailed("No Themes retrieved!");
            }
        });
    }
}