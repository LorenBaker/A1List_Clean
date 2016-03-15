package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface DeleteStruckOutListTitles extends Interactor {

    interface Callback {
        // interactor callback methods
        void onStruckOutListTitlesDeleted(String successMessage);
        void onStruckOutListTitlesDeletionFailed(String errorMessage);
    }

}
