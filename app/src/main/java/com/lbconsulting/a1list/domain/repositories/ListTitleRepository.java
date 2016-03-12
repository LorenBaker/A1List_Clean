package com.lbconsulting.a1list.domain.repositories;

import android.content.ContentValues;

import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;

import java.util.List;

/**
 * Repository interface for ListTitle
 */
public interface ListTitleRepository {

    //region Create
    ListTitle insert(ListTitle listTitle);
    //endregion

    //region Read
    ListTitle getListTitleByUuid(String uuid);

    List<ListTitle> retrieveAllListTitles(boolean isMarkedForDeletion);

    List<ListTitle> retrieveStruckOutListTitles();

    int getNumberOfStruckOutListTitles();

    long retrieveListItemNextSortKey();

    void setListItemLastSortKey(ListTitle listTitle, long sortKey);
    //endregion


    //region Update
    boolean update(ListTitle listTitle, ContentValues contentValues);

    boolean update(ListTitle listTitle);

    int toggle(ListTitle listTitle, String fieldName);

    void replaceListTheme(ListTheme listTheme, ListTheme defaultListTheme);

    //endregion
    //region Delete
    int delete(ListTitle listTitle);

    int markDeleted(ListTitle listTitle);
    //endregion
}
