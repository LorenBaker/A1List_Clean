package com.lbconsulting.a1list.domain.interactors.listItem.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface ToggleListItemBooleanField extends Interactor {

    interface Callback {
        // interactor callback methods
        void onListItemBooleanFieldToggled(int toggleValue);
    }

}
