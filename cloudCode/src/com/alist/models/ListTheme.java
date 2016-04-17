package com.alist.models;

import java.util.Date;

public class ListTheme {
    private String objectId;
    private long SQLiteId;
    private String name;
    private String deviceUuid;
    private String messageChannel;
    private int startColor; // int
    private int endColor;
    private int textColor; // int
    private float textSize; //float
    private float horizontalPaddingInDp; //float dp. Need to convert to float px
    private float verticalPaddingInDp; //float dp. Need to convert to float px
    private boolean bold;
    private boolean checked;
    private boolean defaultTheme;
    private boolean markedForDeletion;
    private boolean struckOut;
    private boolean transparent;
    private String uuid;
    private Date updated;
    private Date created;


    public ListTheme() {
        // A default constructor.
    }

    //region Getters and Setters
    public int getEndColor() {
        return endColor;
    }

    public void setEndColor(int endColor) {
        this.endColor = endColor;
    }

    public float getHorizontalPaddingInDp() {
        return horizontalPaddingInDp;
    }

    public void setHorizontalPaddingInDp(float horizontalPaddingInDp) {
        this.horizontalPaddingInDp = horizontalPaddingInDp;
    }

    public float getVerticalPaddingInDp() {
        return verticalPaddingInDp;
    }

    public void setVerticalPaddingInDp(float verticalPaddingInDp) {
        this.verticalPaddingInDp = verticalPaddingInDp;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean isBold) {
        this.bold = isBold;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean isChecked) {
        this.checked = isChecked;
    }

    public boolean isDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(boolean isDefaultTheme) {
        this.defaultTheme = isDefaultTheme;
    }

    public boolean isMarkedForDeletion() {
        return markedForDeletion;
    }

    public void setMarkedForDeletion(boolean isMarkedForDeletion) {
        this.markedForDeletion = isMarkedForDeletion;
    }

    public boolean isStruckOut() {
        return struckOut;
    }

    public void setStruckOut(boolean struckOut) {
        this.struckOut = struckOut;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean isTransparent) {
        this.transparent = isTransparent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public long getSQLiteId() {
        return SQLiteId;
    }

    public void setSQLiteId(long SQLiteId) {
        this.SQLiteId = SQLiteId;
    }

    public int getStartColor() {
        return startColor;
    }

    public void setStartColor(int startColor) {
        this.startColor = startColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getMessageChannel() {
        return messageChannel;
    }

    public void setMessageChannel(String messageChannel) {
        this.messageChannel = messageChannel;
    }

//endregion
}