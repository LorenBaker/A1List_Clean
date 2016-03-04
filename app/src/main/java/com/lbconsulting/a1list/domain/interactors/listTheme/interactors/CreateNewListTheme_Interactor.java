package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;


public interface CreateNewListTheme_Interactor extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListThemeCreated(ListTheme newListTheme);

        void onListThemeCreationFailed(String errorMessage);

    }

}
