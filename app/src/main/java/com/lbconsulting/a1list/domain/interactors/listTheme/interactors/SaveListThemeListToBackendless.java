package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;

import java.util.List;


public interface SaveListThemeListToBackendless extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListThemeListSavedToBackendless(String successMessage, List<ListTheme> successfullySavedListThemes);

        void onListThemeListSaveToBackendlessFailed(String errorMessage, List<ListTheme> successfullySavedListThemes);

    }

}
