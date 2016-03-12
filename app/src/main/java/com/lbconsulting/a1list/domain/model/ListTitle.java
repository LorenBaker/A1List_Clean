package com.lbconsulting.a1list.domain.model;

import com.lbconsulting.a1list.domain.repositories.AppSettingsRepository;

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
    private boolean markedForDeletion;
    private boolean sortListItemsAlphabetically;
    private boolean struckOut;
    private long manualSortKey;
    private String listLockString;
    private boolean listLocked;
    private boolean listPrivateToThisDevice;
    private long listItemLastSortKey;

    private int firstVisiblePosition;
    private int listViewTop;

    private Date updated;
    private Date created;

    public ListTitle() {
        // A default constructor.
    }

    public static ListTitle newInstance(String name, ListTheme defaultListTheme, AppSettingsRepository appSettings){
        ListTitle newListTitle = new ListTitle();

        String newUuid = UUID.randomUUID().toString();
        // replace uuid "-" with "_" to distinguish it from Backendless objectId
        newUuid = newUuid.replace("-", "_");
        newListTitle.setUuid(newUuid);

        newListTitle.setName(name);
        newListTitle.setListTheme(defaultListTheme);
        newListTitle.setChecked(false);
        newListTitle.setForceViewInflation(false);
        newListTitle.setMarkedForDeletion(false);
        newListTitle.setManualSortKey(appSettings.retrieveListTitleNextSortKey());
        newListTitle.setSortListItemsAlphabetically(true);
        newListTitle.setListLockString(LIST_NOT_LOCK);
        newListTitle.setListPrivateToThisDevice(false);
        newListTitle.setListItemLastSortKey(0);
        newListTitle.setFirstVisiblePosition(-1);
        newListTitle.setListViewTop(0);

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

    public boolean isStruckOut() {
        return struckOut;
    }

    public void setStruckOut(boolean struckOut) {
        this.struckOut = struckOut;
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

    public boolean isListLocked() {
        return listLocked;
    }

    public void setListLocked(boolean listLocked) {
        this.listLocked = listLocked;
    }

    public long getListItemLastSortKey() {
        return listItemLastSortKey;
    }

    public void setListItemLastSortKey(long listItemLastSortKey) {
        this.listItemLastSortKey = listItemLastSortKey;
    }

    public boolean isListPrivateToThisDevice() {
        return listPrivateToThisDevice;
    }

    public void setListPrivateToThisDevice(boolean listPrivateToThisDevice) {
        this.listPrivateToThisDevice = listPrivateToThisDevice;
    }

    public int getFirstVisiblePosition() {
        return firstVisiblePosition;
    }

    public void setFirstVisiblePosition(int firstVisiblePosition) {
        this.firstVisiblePosition = firstVisiblePosition;
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
    //endregion

    @Override
    public String toString() {
        return getName();
    }
}
