package com.lbconsulting.a1list.domain.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;

import java.util.List;


public interface AllListThemeInteractor extends Interactor {

    public final String NONE = "none";
    public final String TOGGLE_STRIKEOUT = "toggle_strikeout";

    interface Callback {

        // interactor callback methods
        void onAllListThemesRetrieved(List<ListTheme> listThemes);
        void onRetrievalFailed(String error);

    }
//    void setAction(ListTheme listTheme, String action);

    // TODO: Add interactor methods here
}
