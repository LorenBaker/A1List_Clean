package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface ToggleListThemeBooleanField_Interactor extends Interactor {

    interface Callback {
        // interactor callback methods
        void onListThemeBooleanFieldToggled(int toggleValue);
    }

}
