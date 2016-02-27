package com.lbconsulting.a1list.presentation.ui.dialogs;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_Impl;
import com.lbconsulting.a1list.presentation.presenters.impl.ListThemesPresenter_Impl;
import com.lbconsulting.a1list.presentation.presenters.interfaces.ListThemesPresenter;
import com.lbconsulting.a1list.presentation.ui.adapters.ListThemeDialogArrayAdapter;
import com.lbconsulting.a1list.threading.MainThreadImpl;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * A dialog where the user selects a ListTheme
 */
public class dialogSelectTheme extends DialogFragment implements  ListThemesPresenter.ListThemeView{

    @Bind(R.id.lvThemes)
    ListView lvThemes;
    private AlertDialog mDialog;
    private ListThemeDialogArrayAdapter mListThemeArrayAdapter;

    private static ListThemesPresenter_Impl mListThemesPresenter;

    public dialogSelectTheme() {
        // Empty constructor required for DialogFragment
    }

    public static dialogSelectTheme newInstance() {
        Timber.i("newInstance()");
        return new dialogSelectTheme();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate()");
        mListThemesPresenter = new ListThemesPresenter_Impl(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, new ListThemeRepository_Impl(getActivity()));
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.i("onActivityCreated()");
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {

                Button cancelButton = mDialog.getButton(Dialog.BUTTON_NEGATIVE);
                cancelButton.setTextSize(17);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Cancel
                        dismiss();
                    }
                });
            }
        });
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.i("onCreateDialog()");

        // inflate the xml layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_themes, null, false);

        ButterKnife.bind(this, view);


        lvThemes.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.whiteSmoke));
        mListThemeArrayAdapter = new ListThemeDialogArrayAdapter(getActivity(), lvThemes);
        lvThemes.setAdapter(mListThemeArrayAdapter);

        mListThemesPresenter.resume();

        // build the dialog
        mDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialogSelectTheme_title)
                .setView(view)
                .setNegativeButton(R.string.btnCancel_title, null)
                .create();

        return mDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.i("onDestroy()");
    }



    @Override
    public void displayAllListThemes(List<ListTheme> allListThemes) {

        mListThemeArrayAdapter.setData(allListThemes);
        mListThemeArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void showProgress(String waitMessage) {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void showError(String message) {

    }
}
