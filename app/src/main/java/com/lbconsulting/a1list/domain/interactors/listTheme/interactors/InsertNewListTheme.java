package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface InsertNewListTheme extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListThemeInsertedIntoSQLiteDb(String successMessage);

        void onListThemeInsertionIntoSQLiteDbFailed(String errorMessage);

    }

}
