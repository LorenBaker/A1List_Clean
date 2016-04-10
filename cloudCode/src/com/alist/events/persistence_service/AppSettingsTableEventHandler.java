package com.alist.events.persistence_service;

import com.alist.models.AppSettings;
import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.servercode.ExecutionResult;
import com.backendless.servercode.RunnerContext;
import com.backendless.servercode.annotation.Asset;
import org.w3c.dom.events.EventException;

import java.util.Date;
import java.util.Iterator;

/**
 * AppSettingsTableEventHandler handles events for all entities. This is accomplished
 * with the @Asset( "AppSettings" ) annotation.
 * The methods in the class correspond to the events selected in Backendless
 * Console.
 */

@Asset("AppSettings")
public class AppSettingsTableEventHandler extends com.backendless.servercode.extension.PersistenceExtender<AppSettings> {
    @Override
    public void beforeCreate(RunnerContext context, AppSettings clientAppSettings) throws Exception {
        validateClientAppSettings(clientAppSettings);
    }

    @Override
    public void afterCreate(RunnerContext context, AppSettings appSettings, ExecutionResult<AppSettings> result) throws Exception {
        // add your code here
    }

    @Override
    public void beforeUpdate(RunnerContext context, AppSettings clientAppSettings) throws Exception {
        validateClientAppSettings(clientAppSettings);
    }

    @Override
    public void afterUpdate(RunnerContext context, AppSettings appSettings, ExecutionResult<AppSettings> result) throws Exception {
        // add your code here
    }

    private AppSettings getAppSettingsByUuid(String uuid) {

        AppSettings appSettings = null;

        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        String whereClause = String.format("uuid = '%s'", uuid);
        dataQuery.setWhereClause(whereClause);

        BackendlessCollection<AppSettings> appSettingss = Backendless.Data.of(AppSettings.class).find(dataQuery);
        Iterator<AppSettings> iterator = appSettingss.getCurrentPage().iterator();
        if (iterator.hasNext()) {
            appSettings = iterator.next();
        }

        return appSettings;
    }

    private void validateClientAppSettings(AppSettings clientAppSettings) {
        AppSettings cloudAppSettings = getAppSettingsByUuid(clientAppSettings.getUuid());
        if (cloudAppSettings != null) {
            Date clientAppSettingsDate = clientAppSettings.getUpdated();
            if (clientAppSettingsDate == null) {
                clientAppSettingsDate = clientAppSettings.getCreated();
            }

            Date cloudAppSettingsDate = cloudAppSettings.getUpdated();
            if (cloudAppSettingsDate == null) {
                cloudAppSettingsDate = cloudAppSettings.getCreated();
            }

            if (clientAppSettingsDate.after(cloudAppSettingsDate)) {
                if (clientAppSettings.getObjectId() == null) {
                    clientAppSettings.setObjectId(cloudAppSettings.getObjectId());
                }
            } else {
                updateClient(cloudAppSettings);
                // Throw an exception and should stop further execution of the API request
                throw new EventException((short) 0, "Attempted to overwrite a newer AppSettings");
            }
        }
    }

    private void updateClient(AppSettings cloudAppSettings) {
        // TODO: Send the newer cloudAppSettings to the client that sent the older clientAppSettings
    }
}
        