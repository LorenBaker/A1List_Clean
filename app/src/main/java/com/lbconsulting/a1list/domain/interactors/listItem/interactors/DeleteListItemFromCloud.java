package com.lbconsulting.a1list.domain.interactors.listItem.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface DeleteListItemFromCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListItemDeletedFromCloud(String successMessage);

        void onListItemDeleteFromCloudFailed(String errorMessage);

    }

}
