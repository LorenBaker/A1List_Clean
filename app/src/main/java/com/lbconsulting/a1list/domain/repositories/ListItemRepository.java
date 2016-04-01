package com.lbconsulting.a1list.domain.repositories;

import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTitle;

import java.util.List;

/**
 * Repository interface for ListItem
 */
public interface ListItemRepository {

    //region Insert ListItem
    List<ListItem> insert(List<ListItem> listItems);

    boolean insert(ListItem listItem);

    List<ListItem> insertIntoLocalStorage(List<ListItem> listItems);

    boolean insertIntoLocalStorage(ListItem listItem);

    void insertInCloud(List<ListItem> listItems);

    void insertInCloud(ListItem listItem);
    //endregion

    //region Retrieve ListItem
    ListItem retrieveListItemByUuid(String uuid);

    List<ListItem> retrieveAllListItems(ListTitle listTitle, boolean isMarkedForDeletion);

    List<ListItem> retrieveListItems(ListTitle listTitle);

    List<ListItem> retrieveDirtyListItems();

    List<ListItem> retrieveFavoriteListItems(ListTitle listTitle);

    List<ListItem> retrieveStruckOutListItems(ListTitle listTitle);

    int retrieveNumberOfStruckOutListItems(ListTitle listTitle);

//    long retrieveListItemNextSortKey(ListTitle listTitle);
    //endregion

    //region Update ListItem
    void update(List<ListItem> listItems);

    void update(ListItem listItem);

    List<ListItem> updateInLocalStorage(List<ListItem> listItems);

    int updateInLocalStorage(ListItem listItem);

    void updateInCloud(List<ListItem> listItems, boolean isNew);

    void updateInCloud(ListItem listItem, boolean isNew);

    int toggle(ListItem listItem, String fieldName);
    //endregion

    //region Delete ListItem
    int delete(List<ListItem> listItems);

    int delete(ListItem listItem);

    List<ListItem> deleteFromLocalStorage(List<ListItem> listItems);

    int deleteFromLocalStorage(ListItem listItem);

    void deleteFromCloud(List<ListItem> listItems);

    void deleteFromCloud(ListItem listItem);

    //endregion
}
