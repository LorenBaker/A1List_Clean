package com.lbconsulting.a1list.domain.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;

import java.util.List;


public interface CreateInitialListThemesInteractor extends Interactor {

    interface Callback {
        // interactor callback methods
        void onInitialListThemesCreated(List<ListTheme> listThemes, String message);

        void onListThemesCreationFailed(String error);
    }

    // TODO: Add interactor methods here
}
