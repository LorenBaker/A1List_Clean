package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTitle;


public interface CreateNewListTitle_Interactor extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListTitleCreated(ListTitle newListTitle);

        void onListTitleCreationFailed(String errorMessage);

    }

}
