package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface SaveListThemeToCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListThemeSavedToCloud(String successMessage);

        void onListThemeSaveToCloudFailed(String errorMessage);

    }

}
