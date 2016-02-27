package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface DeleteStruckOutListThemes_Interactor extends Interactor {

    interface Callback {
        // interactor callback methods
        void onStruckOutListThemesDeleted(String successMessage);
        void onStruckOutListThemesDeletionFailed(String errorMessage);
    }

}
