package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTitle;

import java.util.List;


public interface SaveListTitlesToCloud extends Interactor {


    interface Callback {

        // interactor callback methods
        void onListTitlesListSavedToBackendless(String successMessage, List<ListTitle> successfullySavedListTitles);

        void onListTitlesListSaveToBackendlessFailed(String errorMessage, List<ListTitle> successfullySavedListTitles);

    }

}
