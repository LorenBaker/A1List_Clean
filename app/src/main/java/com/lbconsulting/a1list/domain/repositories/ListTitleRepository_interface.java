package com.lbconsulting.a1list.domain.repositories;

import android.content.ContentValues;

import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;

import java.util.List;

/**
 * Repository interface for ListTitle
 */
public interface ListTitleRepository_interface {

    //region Create
    ListTitle insert(ListTitle listTitle);
    //endregion

    //region Read
    ListTitle getListTitleByUuid(String uuid);
    List<ListTitle> retrieveAllListTitles(boolean isMarkedForDeletion);
    List<ListTitle> retrieveStruckOutListTitles();
    int getNumberOfStruckOutListTitles();
    //endregion


    //region Update
    boolean update(ListTitle listTitle, ContentValues contentValues, boolean updateBackendless);
    boolean update(ListTitle listTitle, boolean updateBackendless);
    int toggle(ListTitle listTitle, String fieldName, boolean updateBackendless);
    void replaceListTheme(ListTheme listTheme, ListTheme defaultListTheme,boolean updateBackendless);

    //endregion
    //region Delete
    int delete(ListTitle listTitle);

    int markDeleted(ListTitle listTitle);
    //endregion
}
