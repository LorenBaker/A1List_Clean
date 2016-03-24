package com.lbconsulting.a1list.presentation.presenters.interfaces;

import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.presentation.presenters.base.BasePresenter;
import com.lbconsulting.a1list.presentation.ui.BaseView;

import java.util.List;


public interface ListItemsPresenter extends BasePresenter {

    interface ListItemView extends BaseView {

        // Add your view methods
        void displayListItems(List<ListItem> allListItems);
    }
}
