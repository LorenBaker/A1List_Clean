package com.lbconsulting.a1list.domain.interactors.listItem.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface UpdateListItem extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListItemUpdated(String successMessage);

        void onListItemUpdateFailed(String errorMessage);

    }

}
