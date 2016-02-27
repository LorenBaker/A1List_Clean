package com.lbconsulting.a1list.domain.interactors.interfaces;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;


public interface RetrieveListTheme_Interactor extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListThemeRetrieved(ListTheme listTheme);

        void onListThemeRetrievalFailed(String error);

    }

    // TODO: Add interactor methods here
}
