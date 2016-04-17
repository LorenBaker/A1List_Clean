package com.alist.models;

import java.util.Date;

public class AppSettings {
    private long SQLiteId;
    private String uuid;
    private String objectId;
    private String deviceUuid;
    private String messageChannel;

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

    //region Getters and Setters
    public long getSQLiteId() {
        return SQLiteId;
    }

    public void setSQLiteId(long SQLiteId) {
        this.SQLiteId = SQLiteId;
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

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public String getMessageChannel() {
        return messageChannel;
    }

    public void setMessageChannel(String messageChannel) {
        this.messageChannel = messageChannel;
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