package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface DeleteListTitleFromCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListTitleDeletedFromBackendless(String successMessage);

        void onListTitleDeleteFromBackendlessFailed(String errorMessage);

    }

}
