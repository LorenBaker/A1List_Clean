package com.lbconsulting.a1list.domain.model;

import com.lbconsulting.a1list.utils.MySettings;

import java.util.Date;
import java.util.UUID;


/**
 * Java object for an A1List Theme.
 */

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



    public static ListTheme newInstance(String newThemeName,
                                        int startColor, int endColor,
                                        int textColor, float textSize,
                                        float horizontalPaddingInDp, float verticalPaddingInDp,
                                        boolean isBold, boolean isTransparent, boolean isDefaultTheme) {

        ListTheme newTheme = new ListTheme();
        newTheme.setName(newThemeName);
        newTheme.setDeviceUuid(MySettings.getDeviceUuid());
        newTheme.setStartColor(startColor);
        newTheme.setEndColor(endColor);
        newTheme.setTextColor(textColor);
        newTheme.setTextSize(textSize);
        newTheme.setHorizontalPaddingInDp(horizontalPaddingInDp);
        newTheme.setVerticalPaddingInDp(verticalPaddingInDp);
        newTheme.setBold(isBold);
        newTheme.setChecked(false);
        newTheme.setDefaultTheme(isDefaultTheme);
        newTheme.setMarkedForDeletion(false);
        newTheme.setTransparent(isTransparent);
        String newUuid = UUID.randomUUID().toString();
        // replace uuid "-" with "_" to distinguish it from Backendless objectId
        newUuid = newUuid.replace("-", "_");
        newTheme.setUuid(newUuid);
        newTheme.setMessageChannel(MySettings.getActiveUserID());

        return newTheme;
    }

    public static ListTheme newInstance(ListTheme defaultListTheme) {
        ListTheme newTheme = new ListTheme();
        newTheme.setName("");
        newTheme.setDeviceUuid(MySettings.getDeviceUuid());
        newTheme.setStartColor(defaultListTheme.getStartColor());
        newTheme.setEndColor(defaultListTheme.getEndColor());
        newTheme.setTextColor(defaultListTheme.getTextColor());
        newTheme.setTextSize(defaultListTheme.getTextSize());
        newTheme.setHorizontalPaddingInDp(defaultListTheme.getHorizontalPaddingInDp());
        newTheme.setVerticalPaddingInDp(defaultListTheme.getVerticalPaddingInDp());
        newTheme.setBold(defaultListTheme.isBold());
        newTheme.setChecked(false);
        newTheme.setDefaultTheme(false);
        newTheme.setMarkedForDeletion(false);
        newTheme.setTransparent(defaultListTheme.isTransparent());
        String newUuid = UUID.randomUUID().toString();
        // replace uuid "-" with "_" to distinguish it from Backendless objectId
        newUuid = newUuid.replace("-", "_");
        newTheme.setUuid(newUuid);

        return newTheme;
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

    @Override
    public String toString() {
        return getName();
    }


}
