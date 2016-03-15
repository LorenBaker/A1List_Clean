package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface DeleteListTitleFromBackendless extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListTitleDeletedFromBackendless(String successMessage);

        void onListTitleDeleteFromBackendlessFailed(String errorMessage);

    }

}
