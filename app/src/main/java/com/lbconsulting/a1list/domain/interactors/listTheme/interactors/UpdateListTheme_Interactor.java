package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface UpdateListTheme_Interactor extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListThemeUpdated(String message);

        void onListThemeUpdateFailed(String errorMessage);

    }

    // TODO: Add interactor methods here
}
