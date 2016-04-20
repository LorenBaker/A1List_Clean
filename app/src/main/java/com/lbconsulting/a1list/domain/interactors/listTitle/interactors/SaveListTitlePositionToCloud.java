package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface SaveListTitlePositionToCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListTitlePositionSavedToCloud(String successMessage);

        void onListTitlePositionSaveToCloudFailed(String errorMessage);

    }

}
