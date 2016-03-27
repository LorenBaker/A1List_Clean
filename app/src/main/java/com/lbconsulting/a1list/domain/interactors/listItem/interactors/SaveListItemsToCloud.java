package com.lbconsulting.a1list.domain.interactors.listItem.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListItem;

import java.util.List;


public interface SaveListItemsToCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListItemListSavedToBackendless(String successMessage, List<ListItem> successfullySavedListItems);

        void onListItemListSaveToBackendlessFailed(String errorMessage, List<ListItem> successfullySavedListItems);

    }

}
