package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;

import java.util.List;


public interface RetrieveAllListThemes_Interactor extends Interactor {

    interface Callback {

        // interactor callback methods
        void onAllListThemesRetrieved(List<ListTheme> listThemes);
        void onRetrievalFailed(String errorMessage);

    }

}
