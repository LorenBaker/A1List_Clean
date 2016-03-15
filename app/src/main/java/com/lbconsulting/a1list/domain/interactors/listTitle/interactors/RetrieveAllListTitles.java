package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;
import com.lbconsulting.a1list.domain.model.ListTitle;

import java.util.List;


public interface RetrieveAllListTitles extends Interactor {

    interface Callback {

        // interactor callback methods
        void onAllListTitlesRetrieved(List<ListTitle> listTitles);
        void onAllListTitlesRetrievalFailed(String errorMessage);

    }

    // TODO: Add interactor methods here
}
