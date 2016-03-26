package com.lbconsulting.a1list.domain.interactors.listItem.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface DeleteListItemsFromBackendless extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListItemsDeletedFromBackendless(String successMessage);

        void onListItemsDeleteFromBackendlessFailed(String errorMessage);

    }

}
