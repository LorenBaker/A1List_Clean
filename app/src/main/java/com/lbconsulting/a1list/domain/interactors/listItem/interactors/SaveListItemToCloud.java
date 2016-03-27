package com.lbconsulting.a1list.domain.interactors.listItem.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface SaveListItemToCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListItemSavedToBackendless(String successMessage);

        void onListItemSaveToBackendlessFailed(String errorMessage);

    }

}
