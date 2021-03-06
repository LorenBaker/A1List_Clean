package com.lbconsulting.a1list.domain.model;

import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.MySettings;

import java.util.Date;
import java.util.UUID;

/**
 * Java object for AppSettings.
 */
public class AppSettings {

    private long Id;
    private String uuid;
    private String objectId;

    private String name;
    private long timeBetweenSynchronizations;
    private long listTitleLastSortKey;
    private boolean listTitlesSortedAlphabetically;
    private String lastListTitleViewedUuid;

    private Date updated;
    private Date created;

    public AppSettings() {
        // A default constructor.
    }

    public static AppSettings newInstance() {
        AppSettings newAppSettings = new AppSettings();

        String newUuid = UUID.randomUUID().toString();
        // replace uuid "-" with "_" to distinguish it from Backendless objectId
        newUuid = newUuid.replace("-", "_");
        newAppSettings.setUuid(newUuid);
        newAppSettings.setName(MySettings.getActiveUserName());
        newAppSettings.setTimeBetweenSynchronizations(0);
        newAppSettings.setListTitleLastSortKey(0);
        newAppSettings.setListTitlesSortedAlphabetically(true);
        newAppSettings.setLastListTitleViewedUuid(CommonMethods.NOT_AVAILABLE);

        return newAppSettings;
    }

    //region Getters and Setters
    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastListTitleViewedUuid() {
        return lastListTitleViewedUuid;
    }

    public void setLastListTitleViewedUuid(String lastListTitleViewedUuid) {
        this.lastListTitleViewedUuid = lastListTitleViewedUuid;
    }

    public long getTimeBetweenSynchronizations() {
        return timeBetweenSynchronizations;
    }

    public void setTimeBetweenSynchronizations(long timeBetweenSynchronizations) {
        this.timeBetweenSynchronizations = timeBetweenSynchronizations;
    }

    public long getListTitleLastSortKey() {
        return listTitleLastSortKey;
    }

    public void setListTitleLastSortKey(long listTitleLastSortKey) {
        this.listTitleLastSortKey = listTitleLastSortKey;
    }

    public boolean isListTitlesSortedAlphabetically() {
        return listTitlesSortedAlphabetically;
    }

    public void setListTitlesSortedAlphabetically(boolean listTitlesSortedAlphabetically) {
        this.listTitlesSortedAlphabetically = listTitlesSortedAlphabetically;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
    //endregion
}
