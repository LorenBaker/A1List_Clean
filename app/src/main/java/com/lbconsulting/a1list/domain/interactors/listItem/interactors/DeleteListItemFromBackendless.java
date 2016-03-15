package com.lbconsulting.a1list.domain.interactors.listItem.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface DeleteListItemFromBackendless extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListItemDeletedFromBackendless(String successMessage);

        void onListItemDeleteFromBackendlessFailed(String errorMessage);

    }

}
