package com.lbconsulting.a1list.domain.interactors.listItem.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface DeleteStruckOutListItems extends Interactor {

    interface Callback {
        // interactor callback methods
        void onStruckOutListItemsDeleted(String successMessage);

        void onStruckOutListItemsDeletionFailed(String errorMessage);
    }

}
