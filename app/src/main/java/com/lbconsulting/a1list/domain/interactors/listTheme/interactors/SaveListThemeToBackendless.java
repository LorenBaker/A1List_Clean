package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface SaveListThemeToBackendless extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListThemeSavedToBackendless(String successMessage);

        void onListThemeSaveToBackendlessFailed(String errorMessage);

    }

}
