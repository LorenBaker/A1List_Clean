package com.lbconsulting.a1list.domain.interactors.listTheme.interactors;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface ApplyTextSizeAndMarginsToAllListThemes extends Interactor {

    interface Callback {
        // interactor callback methods
        void onTextSizeAndMarginsApplied(String successMessage);

        void onApplyTextSizeAndMarginsFailure(String errorMessage);
    }

}
