package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface DeleteListThemeFromBackendless extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListThemeDeletedFromBackendless(String successMessage);

        void onListThemeDeleteFromBackendlessFailed(String errorMessage);

    }

}