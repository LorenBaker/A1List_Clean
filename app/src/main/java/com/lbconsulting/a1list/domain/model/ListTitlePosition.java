package com.lbconsulting.a1list.domain.model;

import com.lbconsulting.a1list.utils.MySettings;

import java.util.Date;
import java.util.UUID;

/**
 * This class holds the ListTitle's listView position
 */
public class ListTitlePosition {

    private String uuid;
    private long SQLiteId;
    private String objectId;
    private String deviceUuid;

    private String listTitleUuid;
    private int listViewFirstVisiblePosition;
    private int listViewTop;

    private Date updated;
    private Date created;

    public ListTitlePosition() {

    }

    public static ListTitlePosition newInstance(String listTitleUuid) {
        ListTitlePosition newListTitlePosition = new ListTitlePosition();

        String newUuid = UUID.randomUUID().toString();
        // replace uuid "-" with "_" to distinguish it from Backendless objectId
        newUuid = newUuid.replace("-", "_");
        newListTitlePosition.setUuid(newUuid);
        newListTitlePosition.setDeviceUuid(MySettings.getDeviceUuid());

        newListTitlePosition.setListTitleUuid(listTitleUuid);
        newListTitlePosition.setListViewFirstVisiblePosition(0);
        newListTitlePosition.setListViewTop(0);
        newListTitlePosition.setDeviceUuid(MySettings.getDeviceUuid());

        return newListTitlePosition;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getSQLiteId() {
        return SQLiteId;
    }

    public void setSQLiteId(long SQLiteId) {
        this.SQLiteId = SQLiteId;
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

    public String getListTitleUuid() {
        return listTitleUuid;
    }

    public void setListTitleUuid(String listTitleUuid) {
        this.listTitleUuid = listTitleUuid;
    }

    public int getListViewFirstVisiblePosition() {
        return listViewFirstVisiblePosition;
    }

    public void setListViewFirstVisiblePosition(int listViewFirstVisiblePosition) {
        this.listViewFirstVisiblePosition = listViewFirstVisiblePosition;
    }

    public int getListViewTop() {
        return listViewTop;
    }

    public void setListViewTop(int listViewTop) {
        this.listViewTop = listViewTop;
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
}
