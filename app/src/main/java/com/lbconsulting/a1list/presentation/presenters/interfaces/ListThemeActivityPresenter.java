package com.lbconsulting.a1list.presentation.presenters.interfaces;

import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.presentation.presenters.base.BasePresenter;
import com.lbconsulting.a1list.presentation.ui.BaseView;


public interface ListThemeActivityPresenter extends BasePresenter {

    interface ListThemeActivityView extends BaseView {
        // TODO: Add your view methods
        void displayRetrievedListTheme(ListTheme listTheme);
    }

    // TODO: Add your presenter methods

}
