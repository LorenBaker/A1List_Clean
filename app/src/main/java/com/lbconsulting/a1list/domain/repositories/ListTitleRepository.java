package com.lbconsulting.a1list.domain.repositories;

import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;

import java.util.List;

/**
 * Repository interface for ListTitle
 */
public interface ListTitleRepository {

    //region Insert ListTitle
    List<ListTitle> insert(List<ListTitle> listTitles);

    boolean insert(ListTitle listTitle);

    List<ListTitle> insertIntoLocalStorage(List<ListTitle> listTitles);

    boolean insertIntoLocalStorage(ListTitle listTitle);

    void insertInCloud(List<ListTitle> listTitles);

    void insertInCloud(ListTitle listTitle);

    //endregion


    //region Retrieve ListTitle
    ListTitle retrieveListTitleByUuid(String uuid);

    List<ListTitle> retrieveAllListTitles(boolean isMarkedForDeletion, boolean isListsSortedAlphabetically);

    List<ListTitle> retrieveDirtyListTitles();

    List<ListTitle> retrieveStruckOutListTitles();

    int retrieveNumberOfStruckOutListTitles();

    long retrieveListItemNextSortKey(ListTitle listTitle,boolean saveToBackendless);

    //endregion


    //region Update ListTitle
    void update(List<ListTitle> listTitles);

    void update(ListTitle listTitle);

    List<ListTitle> updateInLocalStorage(List<ListTitle> listTitles);

    int updateInLocalStorage(ListTitle listTitle);

    void updateInCloud(List<ListTitle> listTitles);

    void updateInCloud(ListTitle listTitle);

    int toggle(ListTitle listTitle, String fieldName);

    void replaceListTheme(ListTheme listTheme, ListTheme defaultListTheme);
    //endregion

    //region Delete ListTitle
    int delete(List<ListTitle> listTitles);

    int delete(ListTitle listTitle);

    List<ListTitle> deleteFromLocalStorage(List<ListTitle> listTitles);

    int deleteFromLocalStorage(ListTitle listTitle);

    void deleteFromCloud(List<ListTitle> listTitles);

    void deleteFromCloud(ListTitle listTitle);
    //endregion

}
