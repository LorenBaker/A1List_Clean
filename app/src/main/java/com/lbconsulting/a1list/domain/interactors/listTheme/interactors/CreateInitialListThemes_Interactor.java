package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface CreateInitialListThemes_Interactor extends Interactor {

    interface Callback {
        // interactor callback methods
        void onInitialListThemesCreated( String message);

        void onListThemesCreationFailed(String error);
    }

    // TODO: Add interactor methods here
}
