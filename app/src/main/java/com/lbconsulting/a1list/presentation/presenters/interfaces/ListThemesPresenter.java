package com.lbconsulting.a1list.presentation.presenters.interfaces;

import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.presentation.presenters.base.BasePresenter;
import com.lbconsulting.a1list.presentation.ui.BaseView;

import java.util.List;


public interface ListThemesPresenter extends BasePresenter {

    interface ListThemeView extends BaseView {

        // Add your view methods
        void displayAllListThemes(List<ListTheme> allListThemes);
    }
    // TODO: Add your presenter methods


}
