package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface DeleteListThemesFromCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListThemesDeletedFromCloud(String successMessage);

        void onListThemesDeleteFromCloudFailed(String errorMessage);

    }

}
