package com.lbconsulting.a1list.domain.interactors.listItem.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListItem;

import java.util.List;


public interface RetrieveAllListItems extends Interactor {

    interface Callback {

        // interactor callback methods
        void onAllListItemsRetrieved(List<ListItem> listItems);

        void onAllListItemsRetrievalFailed(String errorMessage);

    }
}
