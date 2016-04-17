package com.alist.models;

import com.backendless.Backendless;

import java.util.Date;

public class ListItem {
    private long SQLiteId;
    private String uuid;
    private String objectId;

    private String name;
    private String listTitleUuid;
    private String deviceUuid;
    private String messageChannel;
    private long manualSortKey;
    private boolean checked;
    private boolean favorite;
    private boolean markedForDeletion;
    private boolean struckOut;

    private Date updated;
    private Date created;


    public ListItem() {
// A default constructor.
    }

    public static ListItem findById(String id) {
        return Backendless.Data.of(ListItem.class).findById(id);
    }

    public static ListItem findFirst() {
        return Backendless.Data.of(ListItem.class).findFirst();
    }

    public static ListItem findLast() {
        return Backendless.Data.of(ListItem.class).findLast();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getListTitleUuid() {
        return listTitleUuid;
    }

    public void setListTitleUuid(String listTitleUuid) {
        this.listTitleUuid = listTitleUuid;
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

    public long getManualSortKey() {
        return manualSortKey;
    }

    public void setManualSortKey(long manualSortKey) {
        this.manualSortKey = manualSortKey;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isMarkedForDeletion() {
        return markedForDeletion;
    }

    public void setMarkedForDeletion(boolean markedForDeletion) {
        this.markedForDeletion = markedForDeletion;
    }

    public boolean isStruckOut() {
        return struckOut;
    }

    public void setStruckOut(boolean struckOut) {
        this.struckOut = struckOut;
    }

    public Date getUpdated() {
        return updated;
    }
    //endregion

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

//  public void setObjectId( String objectId )
//  {
//    this.objectId = objectId;
//  }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return getName();
    }

    public ListItem save() {
        return Backendless.Data.of(ListItem.class).save(this);
    }

    public Long remove() {
        return Backendless.Data.of(ListItem.class).remove(this);
    }
}