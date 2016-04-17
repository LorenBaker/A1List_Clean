package com.alist.events.persistence_service;

import com.alist.backendlessMessaging.AppSettingsMessage;
import com.alist.backendlessMessaging.Messaging;
import com.alist.models.AppSettings;
import com.backendless.Backendless;
import com.backendless.commons.exception.ExceptionWrapper;
import com.backendless.servercode.ExecutionResult;
import com.backendless.servercode.RunnerContext;
import com.backendless.servercode.annotation.Asset;

/**
 * AppSettingsTableEventHandler handles events for all entities. This is accomplished
 * with the @Asset( "AppSettings" ) annotation.
 * The methods in the class correspond to the events selected in Backendless
 * Console.
 */

@Asset("AppSettings")
public class AppSettingsTableEventHandler extends com.backendless.servercode.extension.PersistenceExtender<AppSettings> {
    //    @Override
//    public void beforeCreate(RunnerContext context, AppSettings clientAppSettings) throws Exception {
//        validateClientAppSettings(clientAppSettings);
//    }
//
    @Override
    public void afterCreate(RunnerContext context, AppSettings appSettings, ExecutionResult<AppSettings> result) throws Exception {
        ExceptionWrapper exception = result.getException();
        if (exception == null) {
            AppSettings newAppSettings = result.getResult();
            if (newAppSettings != null) {
                sendAppSettingsUpdateMessage(newAppSettings, Messaging.ACTION_CREATE);
            }
        }
    }

    //
//    @Override
//    public void beforeUpdate(RunnerContext context, AppSettings clientAppSettings) throws Exception {
//        validateClientAppSettings(clientAppSettings);
//    }
//
    @Override
    public void afterUpdate(RunnerContext context, AppSettings appSettings, ExecutionResult<AppSettings> result) throws Exception {
        ExceptionWrapper exception = result.getException();
        if (exception == null) {
            AppSettings updatedAppSettings = result.getResult();
            if (updatedAppSettings != null) {
                sendAppSettingsUpdateMessage(updatedAppSettings, Messaging.ACTION_UPDATE);
            }
        }
    }

    private void sendAppSettingsUpdateMessage(AppSettings updatedAppSettings, int action) {
        String messageChannel = updatedAppSettings.getMessageChannel();
        int target = Messaging.TARGET_ALL_DEVICES;
        String appSettingsMessageJson = AppSettingsMessage.toJson(updatedAppSettings, action, target);
        Backendless.Messaging.publish(messageChannel, appSettingsMessageJson);
    }
//
//    private AppSettings getAppSettingsByUuid(String uuid) {
//
//        AppSettings appSettings = null;
//
//        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
//        String whereClause = String.format("uuid = '%s'", uuid);
//        dataQuery.setWhereClause(whereClause);
//
//        BackendlessCollection<AppSettings> cloudAppSettingsCollection = Backendless.Data.of(AppSettings.class).find(dataQuery);
//        Iterator<AppSettings> iterator = cloudAppSettingsCollection.getCurrentPage().iterator();
//        if (iterator.hasNext()) {
//            appSettings = iterator.next();
//        }
//
//        return appSettings;
//    }
//
//    private void validateClientAppSettings(AppSettings clientAppSettings) {
//        AppSettings cloudAppSettings = getAppSettingsByUuid(clientAppSettings.getUuid());
//        if (cloudAppSettings != null) {
//            Date clientAppSettingsDate = clientAppSettings.getUpdated();
//            if (clientAppSettingsDate == null) {
//                clientAppSettingsDate = clientAppSettings.getCreated();
//            }
//
//            Date cloudAppSettingsDate = cloudAppSettings.getUpdated();
//            if (cloudAppSettingsDate == null) {
//                cloudAppSettingsDate = cloudAppSettings.getCreated();
//            }
//
//            if (clientAppSettingsDate.after(cloudAppSettingsDate)) {
//                if (clientAppSettings.getObjectId() == null) {
//                    clientAppSettings.setObjectId(cloudAppSettings.getObjectId());
//                }
//            } else {
//                updateClient(cloudAppSettings);
//                // Throw an exception and should stop further execution of the API request
//                throw new EventException((short) 0, "Attempted to overwrite a newer AppSettings");
//            }
//        }
//    }
//
//    private void updateClient(AppSettings cloudAppSettings) {
//        // TODO: Send the newer cloudAppSettings to the client that sent the older clientAppSettings
//    }
}
        