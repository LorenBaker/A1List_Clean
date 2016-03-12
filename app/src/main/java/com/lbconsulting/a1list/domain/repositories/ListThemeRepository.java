package com.lbconsulting.a1list.domain.repositories;

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

    List<ListTheme> retrieveAllListThemes(boolean isMarkedForDeletion);

    ListTheme retrieveDefaultListTheme();
    List<ListTheme> retrieveStruckOutListThemes();
    int getNumberOfStruckOutListThemes();
    //endregion


    //region Update
    boolean update(ListTheme listTheme, ContentValues contentValues);
    boolean update(ListTheme listTheme);
    int toggle(ListTheme listTheme, String fieldName);
    void clearDefaultFlag();
    int applyTextSizeAndMarginsToAllListThemes(ListTheme listTheme);

    //endregion

    //region Delete
    int delete(ListTheme listTheme);

    int markDeleted(ListTheme listTheme);



    //endregion
}
