package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface InsertNewListTitle extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListTitleInsertedIntoSQLiteDb(String successMessage);

        void onListTitleInsertionIntoSQLiteDbFailed(String errorMessage);

    }

}
