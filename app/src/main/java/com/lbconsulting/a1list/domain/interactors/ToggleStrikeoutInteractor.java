package com.lbconsulting.a1list.domain.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;

import java.util.List;


public interface ToggleStrikeoutInteractor extends Interactor {

    interface Callback {
        // interactor callback methods
        void onStrikeout(List<ListTheme> listThemes);

        void onToggleStrikeoutFailed(String error);
    }

    // TODO: Add interactor methods here
}
