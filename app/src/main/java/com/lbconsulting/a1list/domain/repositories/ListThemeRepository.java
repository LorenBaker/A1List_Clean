package com.lbconsulting.a1list.domain.repositories;

import com.lbconsulting.a1list.domain.model.ListTheme;

import java.util.List;

/**
 * Repository interface for ListTheme
 */
public interface ListThemeRepository {

    //region Insert ListTheme
    List<ListTheme> insertIntoStorage(List<ListTheme> listThemes);

    boolean insertIntoStorage(ListTheme listTheme);

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
    void updateStorage(List<ListTheme> listThemes);

    void updateStorage(ListTheme listTheme);

    List<ListTheme> updateInLocalStorage(List<ListTheme> listThemes);

    int updateInLocalStorage(ListTheme listTheme);

    void updateInCloud(List<ListTheme> listThemes, boolean isNew);

    void updateInCloud(ListTheme listTheme, boolean isNew);

    int applyTextSizeAndMarginsToAllListThemes(ListTheme listTheme);
    //endregion

    //region Delete ListTheme
    int deleteFromStorage(List<ListTheme> listThemes);

    int deleteFromStorage(ListTheme listTheme);

    List<ListTheme> setDeleteFlagInLocalStorage(List<ListTheme> listTheme);

    int setDeleteFlagInLocalStorage(ListTheme listTheme,ListTheme defaultListTheme);

    void deleteFromCloud(List<ListTheme> listThemes);

    void deleteFromCloud(ListTheme listTheme,ListTheme defaultListTheme);

    List<ListTheme> deleteFromLocalStorage(List<ListTheme> listThemes);

    int deleteFromLocalStorage(ListTheme listTheme);
    //endregion

    int clearLocalStorageDirtyFlag(ListTheme listTheme);

    int clearAllData();
}
