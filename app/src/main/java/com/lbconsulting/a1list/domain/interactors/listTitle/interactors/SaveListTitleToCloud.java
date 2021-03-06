package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface SaveListTitleToCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListTitleSavedToCloud(String successMessage);

        void onListTitleSaveToCloudFailed(String errorMessage);

    }

}
