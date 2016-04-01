package com.lbconsulting.a1list.domain.repositories;

import com.lbconsulting.a1list.domain.model.ListTheme;

import java.util.List;

/**
 * Repository interface for ListTheme
 */
public interface ListThemeRepository {

    //region Insert ListTheme
    List<ListTheme> insert(List<ListTheme> listThemes);

    boolean insert(ListTheme listTheme);

    List<ListTheme> insertIntoLocalStorage(List<ListTheme> listThemes);

    boolean insertIntoLocalStorage(ListTheme listTheme);

    void insertInCloud(List<ListTheme> listThemes);

    void insertInCloud(ListTheme listTheme);
    //endregion


    //region Retrieve ListTheme
    ListTheme retrieveListThemeByUuid(String uuid);

    List<ListTheme> retrieveAllListThemes(boolean isMarkedForDeletion);

    List<ListTheme> retrieveDirtyListThemes();

    ListTheme retrieveDefaultListTheme();

    List<ListTheme> retrieveStruckOutListThemes();

    int retrieveNumberOfStruckOutListThemes();
    //endregion

    //region Update ListTheme
    void update(List<ListTheme> listThemes);

    void update(ListTheme listTheme);

    List<ListTheme> updateInLocalStorage(List<ListTheme> listThemes);

    int updateInLocalStorage(ListTheme listTheme);

    void updateInCloud(List<ListTheme> listThemes);

    void updateInCloud(ListTheme listTheme);

    int toggle(ListTheme listTheme, String fieldName);

    void clearDefaultFlag();

    int applyTextSizeAndMarginsToAllListThemes(ListTheme listTheme);
    //endregion

    //region Delete ListTheme
    int delete(List<ListTheme> listThemes);

    int delete(ListTheme listTheme);

    List<ListTheme> deleteFromLocalStorage(List<ListTheme> listThemes);

    int deleteFromLocalStorage(ListTheme listTheme, ListTheme defaultListTheme);

    void deleteFromCloud(List<ListTheme> listThemes);

    void deleteFromCloud(ListTheme listTheme);


}
