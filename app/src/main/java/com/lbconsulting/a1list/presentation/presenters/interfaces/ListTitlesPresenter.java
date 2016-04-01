package com.lbconsulting.a1list.presentation.presenters.interfaces;

import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.presentation.presenters.base.BasePresenter;
import com.lbconsulting.a1list.presentation.ui.BaseView;

import java.util.List;


public interface ListTitlesPresenter extends BasePresenter {

    interface ListTitleView extends BaseView {

        // Add your view methods
        void onPresenterAllListTitlesRetrieved(List<ListTitle> allListTitles);
    }
}
