package com.lbconsulting.a1list.domain.interactors.listItem.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface InsertNewListItem extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListItemInsertedIntoSQLiteDb(String successMessage);

        void onListItemInsertionIntoSQLiteDbFailed(String errorMessage);

    }

}
