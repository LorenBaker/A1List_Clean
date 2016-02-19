package com.lbconsulting.a1list.domain.model;

import com.lbconsulting.a1list.utils.MySettings;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;


/**
 * Java object for an A1List Theme.
 */

public class ListTheme {

    private String objectId;
    private long Id;
    private String name;
    private int startColor; // int
    private int endColor;
    private int textColor; // int
    private float textSize; //float
    private float horizontalPaddingInDp; //float dp. Need to convert to float px
    private float verticalPaddingInDp; //float dp. Need to convert to float px
    private boolean themeDirty;
    private boolean bold;
    private boolean checked;
    private boolean defaultTheme;
    private boolean markedForDeletion;
    private boolean transparent;
    private String uuid;
    private Date updated;
    private Date created;


    public ListTheme() {
        // A default constructor is required.
    }

    //region Getters and Setters

    public static ListTheme newInstance(String newThemeName,
                                        int startColor, int endColor,
                                        int textColor, float textSize,
                                        float horizontalPaddingInDp, float verticalPaddingInDp,
                                        boolean isBold, boolean isTransparent, boolean isDefaultTheme) {

        ListTheme newTheme = new ListTheme();
        newTheme.setName(newThemeName);
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

        return newTheme;
    }

    public int getEndColor() {
        return endColor;
    }

    public void setEndColor(int endColor) {
        setThemeDirty(true);
        this.endColor = endColor;
    }

    public float getHorizontalPaddingInDp() {
        return horizontalPaddingInDp;
    }

//    public int getHorizontalPaddingPx() {
//        return CommonMethods.convertDpToPixel(getInt(HORIZONTAL_PADDING_DP));
//    }
//
//    private int getHorizontalPaddingDp() {
//        return getInt(HORIZONTAL_PADDING_DP);
//    }

    public void setHorizontalPaddingInDp(float horizontalPaddingInDp) {
        setThemeDirty(true);
        this.horizontalPaddingInDp = horizontalPaddingInDp;
    }

    public float getVerticalPaddingInDp() {
        return verticalPaddingInDp;
    }

    public void setVerticalPaddingInDp(float verticalPaddingInDp) {
        setThemeDirty(true);
        this.verticalPaddingInDp = verticalPaddingInDp;
    }

    public boolean isThemeDirty() {
        return themeDirty;
    }

    public void setThemeDirty(boolean themeDirty) {
        this.themeDirty = themeDirty;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean isBold) {
        setThemeDirty(true);
        this.bold = isBold;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean isChecked) {
        setThemeDirty(true);
        this.checked = isChecked;
    }

    public boolean isDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(boolean isDefaultTheme) {
        setThemeDirty(true);
        this.defaultTheme = isDefaultTheme;
    }

    public boolean isMarkedForDeletion() {
        return markedForDeletion;
    }

    public void setMarkedForDeletion(boolean isMarkedForDeletion) {
        setThemeDirty(true);
        this.markedForDeletion = isMarkedForDeletion;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean isTransparent) {
        setThemeDirty(true);
        this.transparent = isTransparent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        setThemeDirty(true);
        this.name = name;
    }

    public String getObjectId() {
        if (objectId == null || objectId.isEmpty()) {
            objectId = MySettings.NOT_AVAILABLE;
        }

        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public int getStartColor() {
        return startColor;
    }

    public void setStartColor(int startColor) {
        setThemeDirty(true);
        this.startColor = startColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        setThemeDirty(true);
        this.textColor = textColor;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        setThemeDirty(true);
        this.textSize = textSize;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        setThemeDirty(true);
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


    //endregion

    @Override
    public String toString() {
        DateFormat mDateFormat = DateFormat.getDateTimeInstance();
        String result = "Name: " + getName();
/*        String result = "Name: " + getName() + "\n"
                + "ObjectId = " + getObjectId() + "\n"
                + "Uuid = " + getUuid() + "\n"
                + "Start Color = " + getStartColor() + "\n"
                + "End Color = " + getEndColor() + "\n"
                + "Text Color = " + getTextColor() + "\n"
                + "Text Size = " + getTextSize() + "\n"
                + "horizontalPaddingInDp = " + getHorizontalPaddingInDp() + "\n"
                + "verticalPaddingInDp = " + getVerticalPaddingInDp() + "\n"
                + "dirty = " + isThemeDirty() + "\n"
                + "bold = " + isBold() + "\n"
                + "checked = " + isChecked() + "\n"
                + "defaultTheme = " + isDefaultTheme() + "\n"
                + "markedForDeletion = " + isMarkedForDeletion() + "\n"
                + "transparent = " + isTransparent() + "\n"
                + "date/time modified = " + mDateFormat.format(getUpdated()) + "\n\n";*/

        return result;
    }

}
