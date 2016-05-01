package com.lbconsulting.a1list.domain.repositories;

import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTitle;

import java.util.List;

/**
 * Repository interface for ListItem
 */
public interface ListItemRepository {

    //region Insert ListItem
    List<ListItem> insertIntoStorage(List<ListItem> listItems);

    boolean insertIntoStorage(ListItem listItem);

    List<ListItem> insertIntoLocalStorage(List<ListItem> listItems);

    boolean insertIntoLocalStorage(ListItem listItem);

    void insertIntoCloudStorage(List<ListItem> listItems);

    void insertIntoCloudStorage(ListItem listItem);
    //endregion

    //region Retrieve ListItem
    ListItem retrieveListItemByUuid(String uuid);

    List<ListItem> retrieveAllListItems(ListTitle listTitle, boolean isMarkedForDeletion);

    List<ListItem> retrieveListItems(ListTitle listTitle);

    List<ListItem> retrieveDirtyListItems();

    List<ListItem> retrieveFavoriteListItems(ListTitle listTitle);

    List<ListItem> retrieveStruckOutListItems(ListTitle listTitle);

//    int retrieveNumberOfStruckOutListItems(ListTitle listTitle);

    //endregion

    //region Update ListItem
    void updateStorage(List<ListItem> listItems);

    void updateStorage(ListItem listItem);

    List<ListItem> updateInLocalStorage(List<ListItem> listItems);

    int updateInLocalStorage(ListItem listItem);

    void updateInCloudStorage(List<ListItem> listItems, boolean isNew);

    void updateInCloudStorage(ListItem listItem, boolean isNew);

    //endregion

    //region Delete ListItem
    int deleteFromStorage(List<ListItem> listItems);

    int deleteFromStorage(ListItem listItem);

    List<ListItem> setDeleteFlagInLocalStorage(List<ListItem> listItems);

    int setDeleteFlagInLocalStorage(ListItem listItem);

    void deleteFromCloudStorage(List<ListItem> listItems);

    void deleteFromCloudStorage(ListItem listItem);

    List<ListItem> deleteFromLocalStorage(List<ListItem> listItems);

    int deleteFromLocalStorage(ListItem listItem);

    //endregion

    int clearLocalStorageDirtyFlag(ListItem listItem);
}
