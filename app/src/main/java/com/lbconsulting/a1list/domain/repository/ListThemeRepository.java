package com.lbconsulting.a1list.domain.repository;

import android.content.ContentValues;

import com.lbconsulting.a1list.domain.model.ListTheme;

import java.util.List;

/**
 * Repository interface for ListTheme
 */
public interface ListThemeRepository {

    //region Create
    ListTheme insert(ListTheme listTheme);
    //endregion

    //region Read
    ListTheme getListThemeByUuid(String uuid);

    List<ListTheme> getAllListThemes(boolean isMarkedForDeletion);
    //endregion


    //region Update
    void update(ListTheme listTheme, ContentValues contentValues, String selection, String[] selectionArgs, boolean updateBackendless);

    void toggle(ListTheme listTheme, String fieldName, boolean updateBackendless);
    //endregion

    //region Delete
    void delete(String selection, String[] selectionArgs);
    //endregion
}
