package com.lbconsulting.a1list.domain.interactors.interfaces;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;


public interface CloneListTheme_Interactor extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListThemeCloned( ListTheme listTheme);

        void onListThemeCloneFailed(String error);

    }
//    void setAction(ListTheme listTheme, String action);

    // TODO: Add interactor methods here
}
