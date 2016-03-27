package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface DeleteListTitlesFromCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListTitlesDeletedFromBackendless(String successMessage);

        void onListTitlesDeleteFromBackendlessFailed(String errorMessage);

    }

}
