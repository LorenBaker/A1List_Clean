package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface UpdateListTitle_Interactor extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListTitleUpdated(String successMessage);

        void onListTitleUpdateFailed(String errorMessage);

    }

}
