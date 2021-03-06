package com.lbconsulting.a1list.presentation.ui;

/**
 * <p>
 * This interface represents a basic view. All views should implement these common methods.
 * </p>
 */
public interface BaseView {

    /**
     * This is a general method used for showing some kind of progress during a background task. For example, this
     * method should show a progress bar and/or disable buttons before some background work starts.
     */
    void showProgress(String waitMessage);

    /**
     * This is a general method used for hiding progress information after a background task finishes.
     * *
     * @param message The error message to be displayed.
     */
    void hideProgress(String message);

    /**
     * This method is used for showing error messages on the UI.
     *
     * @param errorMessage The error message to be displayed.
     */
    void showError(String errorMessage);
}
