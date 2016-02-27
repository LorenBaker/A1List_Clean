package com.lbconsulting.a1list.domain.interactors.interfaces;


import com.lbconsulting.a1list.domain.interactors.base.Interactor;


public interface ApplyTextSizeAndMarginsToAllListThemes_Interactor extends Interactor {

    interface Callback {
        // interactor callback methods
        void onTextSizeAndMarginsApplied(String successMessage);

        void onApplyTextSizeAndMarginsFailure(String errorMessage);
    }

}
