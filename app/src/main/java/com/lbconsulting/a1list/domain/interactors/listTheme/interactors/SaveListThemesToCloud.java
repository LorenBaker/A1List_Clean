package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;

import java.util.List;


public interface SaveListThemesToCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListThemesSavedToCloud(String successMessage, List<ListTheme> successfullySavedListThemes);

        void onListThemesSaveToCloudFailed(String errorMessage, List<ListTheme> successfullySavedListThemes);

    }

}
