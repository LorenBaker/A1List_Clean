package com.lbconsulting.a1list.domain.interactors.listItem.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListItem;


public interface RetrieveListItem extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListItemRetrieved(ListItem listItem);

        void onListItemRetrievalFailed(String error);

    }

}
