package com.lbconsulting.a1list.domain.interactors.appSettings;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.utils.SyncStats;


public interface SyncObjectsFromCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onSyncObjectsFromCloudSuccess(String successMessage, SyncStats syncStats);

        void onSyncObjectsFromCloudFailed(String errorMessage, SyncStats syncStats);

    }

}
