package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface DeleteListThemeFromCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListThemeDeletedFromCloud(String successMessage);

        void onListThemeDeleteFromCloudFailed(String errorMessage);

    }

}
