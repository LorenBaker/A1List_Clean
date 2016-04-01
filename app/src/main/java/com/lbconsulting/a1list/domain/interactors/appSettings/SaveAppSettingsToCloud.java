package com.lbconsulting.a1list.domain.interactors.appSettings;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface SaveAppSettingsToCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onAppSettingsSavedToCloud(String successMessage);

        void onAppSettingsSaveToCloudFailed(String errorMessage);

    }

}
