package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface SaveListTitleToBackendless extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListTitleSavedToBackendless(String successMessage);

        void onListTitleSaveToBackendlessFailed(String errorMessage);

    }

}