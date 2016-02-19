package com.lbconsulting.a1list.domain.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;

import java.util.List;


public interface AllListThemeInteractor extends Interactor {

    interface Callback {
        // interactor callback methods
        void onAllListThemesRetrieved(List<ListTheme> listThemes);

        void onRetrievalFailed(String error);
    }

    // TODO: Add interactor methods here
}
