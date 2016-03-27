package com.lbconsulting.a1list.domain.model;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository;

import java.util.Date;
import java.util.UUID;

/**
 * Java object for an A1List ListItem.
 */
public class ListItem {

    private long Id;
    private String uuid;
    private String objectId;

    private String name;
    private ListTitle listTitle;
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

    public static ListItem newInstance(String name, ListTitle listTitle, boolean saveNextSortKeyToBackendless) {
        ListTitleRepository listTitleRepository = AndroidApplication.getListTitleRepository();
        ListItem newListItem = new ListItem();

        String newUuid = UUID.randomUUID().toString();
        // replace uuid "-" with "_" to distinguish it from Backendless objectId
        newUuid = newUuid.replace("-", "_");
        newListItem.setUuid(newUuid);

        newListItem.setName(name);
        newListItem.setListTitle(listTitle);
        newListItem.setManualSortKey(listTitleRepository.retrieveListItemNextSortKey(listTitle, saveNextSortKeyToBackendless));
        newListItem.setChecked(false);
        newListItem.setFavorite(false);
        newListItem.setMarkedForDeletion(false);
        newListItem.setStruckOut(false);

        return newListItem;
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

    public ListTitle getListTitle() {
        return listTitle;
    }

    public void setListTitle(ListTitle listTitle) {
        this.listTitle = listTitle;
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
