package com.lbconsulting.a1list.domain.repositories;

import com.lbconsulting.a1list.domain.model.ListTheme;

import java.util.List;

/**
 * Repository interface for ListTheme
 */
public interface ListThemeRepository {

    boolean insert(ListTheme listTheme);

    boolean insertIntoSQLiteDb(ListTheme listTheme);

    ListTheme getListThemeByUuid(String uuid);

    List<ListTheme> retrieveAllListThemes(boolean isMarkedForDeletion);

    List<ListTheme> retrieveDirtyListThemes();

    ListTheme retrieveDefaultListTheme();

    List<ListTheme> retrieveStruckOutListThemes();

    int getNumberOfStruckOutListThemes();

//    boolean update(ListTheme listTheme, ContentValues contentValues);

    boolean update(ListTheme listTheme);

    int toggle(ListTheme listTheme, String fieldName);

    void clearDefaultFlag();

    int applyTextSizeAndMarginsToAllListThemes(ListTheme listTheme);

    int delete(ListTheme listTheme);


}
