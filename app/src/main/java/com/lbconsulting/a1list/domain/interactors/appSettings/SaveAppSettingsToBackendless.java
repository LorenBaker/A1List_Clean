package com.lbconsulting.a1list.domain.interactors.appSettings;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface SaveAppSettingsToBackendless extends Interactor {


    interface Callback {

        // interactor callback methods
        void onAppSettingsSavedToBackendless(String successMessage);

        void onAppSettingsSaveToBackendlessFailed(String errorMessage);

    }

}
