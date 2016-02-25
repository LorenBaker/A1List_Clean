package com.lbconsulting.a1list.domain.model;

import java.util.Date;
import java.util.UUID;

/**
 * Java object for an A1List ListTitle.
 */
public class ListTitle {

    private static final String LIST_NOT_LOCK = "listNotLocked";

    private String objectId;
    private long Id;
    private String uuid;

    private String name;
    private ListTheme listTheme;
    private boolean checked;
    private boolean forceViewInflation;
    private boolean listTitleDirty;
    private boolean markedForDeletion;
    private boolean sortListItemsAlphabetically;
    private long manualSortKey;
    private String listLockString;
    private boolean listPrivateToThisDevice;

    private Date updated;
    private Date created;

    public ListTitle() {
        // A default constructor.
    }

    public static ListTitle newInstance(String name, ListTheme defaultListTheme){
        ListTitle newListTitle = new ListTitle();
        newListTitle.setName(name);
        newListTitle.setListTheme(defaultListTheme);
        newListTitle.setChecked(false);
        newListTitle.setForceViewInflation(false);
        newListTitle.setListTitleDirty(true);
        newListTitle.setMarkedForDeletion(false);
        newListTitle.setManualSortKey(-1l);
        newListTitle.setSortListItemsAlphabetically(true);
        String newUuid = UUID.randomUUID().toString();
        // replace uuid "-" with "_" to distinguish it from Backendless objectId
        newUuid = newUuid.replace("-", "_");
        newListTitle.setUuid(newUuid);
        newListTitle.setListLockString(LIST_NOT_LOCK);
        newListTitle.setListPrivateToThisDevice(false);

        return newListTitle;
    }

    //region Getters and Setters
    public String getObjectId() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ListTheme getListTheme() {
        return listTheme;
    }

    public void setListTheme(ListTheme listTheme) {
        this.listTheme = listTheme;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isForceViewInflation() {
        return forceViewInflation;
    }

    public void setForceViewInflation(boolean forceViewInflation) {
        this.forceViewInflation = forceViewInflation;
    }

    public boolean isListTitleDirty() {
        return listTitleDirty;
    }

    public void setListTitleDirty(boolean listTitleDirty) {
        this.listTitleDirty = listTitleDirty;
    }

    public boolean isMarkedForDeletion() {
        return markedForDeletion;
    }

    public void setMarkedForDeletion(boolean markedForDeletion) {
        this.markedForDeletion = markedForDeletion;
    }

    public long getManualSortKey() {
        return manualSortKey;
    }

    public void setManualSortKey(long manualSortKey) {
        this.manualSortKey = manualSortKey;
    }

    public boolean isSortListItemsAlphabetically() {
        return sortListItemsAlphabetically;
    }

    public void setSortListItemsAlphabetically(boolean sortListItemsAlphabetically) {
        this.sortListItemsAlphabetically = sortListItemsAlphabetically;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getListLockString() {
        return listLockString;
    }

    public void setListLockString(String listLockString) {
        this.listLockString = listLockString;
    }

    public boolean isListPrivateToThisDevice() {
        return listPrivateToThisDevice;
    }

    public void setListPrivateToThisDevice(boolean listPrivateToThisDevice) {
        this.listPrivateToThisDevice = listPrivateToThisDevice;
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
        return getName();
    }
}
