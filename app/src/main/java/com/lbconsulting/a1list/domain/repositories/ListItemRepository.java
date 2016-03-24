package com.lbconsulting.a1list.domain.repositories;

import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTitle;

import java.util.List;

/**
 * Repository interface for ListItem
 */
public interface ListItemRepository {

    boolean insert(ListItem listItem);

    boolean insertIntoSQLiteDb(ListItem listItem);

    void insertIntoSQLiteDb(List<ListItem> listItems);

    ListItem retrieveListItemByUuid(String uuid);

    List<ListItem> retrieveAllListItems(ListTitle listTitle, boolean isMarkedForDeletion);

    List<ListItem> retrieveDirtyListItems();

    List<ListItem> retrieveStruckOutListItems(ListTitle listTitle);

    int getNumberOfStruckOutListItems(ListTitle listTitle);

    long retrieveListItemNextSortKey(ListTitle listTitle);

    boolean update(ListItem listItem);

    int toggle(ListItem listItem, String fieldName);

    int delete(ListItem listItem);
}
