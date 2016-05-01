package com.lbconsulting.a1list.domain.repositories;

import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.model.ListTitlePosition;

import java.util.List;

/**
 * Repository interface for ListTitle
 */
public interface ListTitleRepository {

    //region Insert ListTitle
    List<ListTitle> insertIntoStorage(List<ListTitle> listTitles);

    boolean insertIntoStorage(ListTitle listTitle);

    List<ListTitle> insertIntoLocalStorage(List<ListTitle> listTitles);

    boolean insertIntoLocalStorage(ListTitle listTitle);

    void insertIntoCloudStorage(List<ListTitle> listTitles);

    void insertIntoCloudStorage(ListTitle listTitle);

    //endregion


    //region Retrieve ListTitle
    ListTitle retrieveListTitleByUuid(String uuid);

    List<ListTitle> retrieveAllListTitles(boolean isMarkedForDeletion, boolean isListsSortedAlphabetically);

    List<ListTitle> retrieveDirtyListTitles();

    List<ListTitle> retrieveStruckOutListTitles();

    ListTitlePosition retrieveListTitlePosition(ListTitle listTitle);

    int retrieveNumberOfStruckOutListTitles();

    long retrieveListItemNextSortKey(ListTitle listTitle, boolean saveToBackendless);

    //endregion


    //region Update ListTitle
    void updateStorage(List<ListTitle> listTitles);

    void updateStorage(ListTitle listTitle);

    void updateListTitlePosition(ListTitle listTitle, int firstVisiblePosition, int top);

    List<ListTitle> updateInLocalStorage(List<ListTitle> listTitles);

    int updateInLocalStorage(ListTitle listTitle);

    void updateInCloud(List<ListTitle> listTitles, boolean isNew);

    void updateInCloud(ListTitle listTitle, boolean isNew);

    void replaceListTheme(ListTheme listTheme, ListTheme defaultListTheme);
    //endregion

    //region Delete ListTitle

    int deleteFromStorage(ListTitle listTitle);

    List<ListTitle> setDeleteFlagInLocalStorage(List<ListTitle> listTitles);

    int setDeleteFlagInLocalStorage(ListTitle listTitle);

    void deleteFromCloudStorage(List<ListTitle> listTitles);

    void deleteFromCloudStorage(ListTitle listTitle);

    List<ListTitle> deleteFromLocalStorage(List<ListTitle> listTitles);

    int deleteFromLocalStorage(ListTitle listTitle);

    int deleteFromLocalStorage(ListTitle listTitle,ListTitlePosition listTitlePosition);

    //endregion

    int clearLocalStorageDirtyFlag(ListTitle listTitle);
}
