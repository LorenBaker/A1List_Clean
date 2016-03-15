package com.lbconsulting.a1list.domain.interactors.listTitle.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface ToggleListTitleBooleanField extends Interactor {

    interface Callback {
        // interactor callback methods
        void onListTitleBooleanFieldToggled(int toggleValue);
    }

}
