package com.lbconsulting.a1list.domain.repositories;

import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;

import java.util.List;

/**
 * Repository interface for ListTitle
 */
public interface ListTitleRepository {

    boolean insert(ListTitle listTitle);

    boolean insertIntoSQLiteDb(ListTitle listTitle);

    void insertIntoSQLiteDb(List<ListTitle> listTitles);

    ListTitle getListTitleByUuid(String uuid);

    List<ListTitle> retrieveAllListTitles(boolean isMarkedForDeletion, boolean isListsSortedAlphabetically);

    List<ListTitle> retrieveDirtyListTitles();

    List<ListTitle> retrieveStruckOutListTitles();

    int getNumberOfStruckOutListTitles();

    long retrieveListItemNextSortKey(String listTitleUuid);

    void setListItemLastSortKey(ListTitle listTitle, long sortKey);

    boolean update(ListTitle listTitle);

    int toggle(ListTitle listTitle, String fieldName);

    void replaceListTheme(ListTheme listTheme, ListTheme defaultListTheme);

    int delete(ListTitle listTitle);

    long retrieveListItemNextSortKeyNoBackendless(String uuid, boolean saveToBackendless);
}
