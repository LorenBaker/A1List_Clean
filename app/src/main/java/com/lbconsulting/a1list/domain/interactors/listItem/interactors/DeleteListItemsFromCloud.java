package com.lbconsulting.a1list.domain.interactors.listItem.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface DeleteListItemsFromCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListItemsDeletedFromCloud(String successMessage);

        void onListItemsDeleteFromCloudFailed(String errorMessage);

    }

}
