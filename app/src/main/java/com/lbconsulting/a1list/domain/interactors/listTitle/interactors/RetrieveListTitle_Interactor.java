package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTitle;


public interface RetrieveListTitle_Interactor extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListTitleRetrieved(ListTitle listTitle);

        void onListTitleRetrievalFailed(String error);

    }

}
