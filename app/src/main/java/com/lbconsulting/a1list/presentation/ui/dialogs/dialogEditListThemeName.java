package com.lbconsulting.a1list.presentation.ui.dialogs;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_Impl;
import com.lbconsulting.a1list.presentation.ui.activities.ListThemeActivity;
import com.lbconsulting.a1list.utils.MyEvents;
import com.lbconsulting.a1list.utils.MySettings;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

/**
 * A dialog where the user edits the ListTheme's name
 */
public class dialogEditListThemeName extends DialogFragment {

    private static final String ARG_LIST_THEME_NAME = "argListThemeName";
    private static final String ARG_ORIGINAL_LIST_THEME_UUID = "argOriginalListThemeUuid";
    private static final String ARG_MODE = "argMode";

    private EditText txtListThemeName;
    private TextInputLayout txtListThemeName_input_layout;
    private ListTheme mListTheme;
    private String mListThemeName;
    private AlertDialog mEditListThemeNameDialog;
    private ListThemeRepository_Impl mListThemeRepository;
    private String mDialogTitle;

    public dialogEditListThemeName() {
        // Empty constructor required for DialogFragment
    }


    public static dialogEditListThemeName newInstance(String listThemeName, String originalListThemeUuid, int mode) {
        Timber.i("newInstance()");
        dialogEditListThemeName frag = new dialogEditListThemeName();
        Bundle args = new Bundle();
        args.putString(ARG_LIST_THEME_NAME, listThemeName);
        args.putString(ARG_ORIGINAL_LIST_THEME_UUID, originalListThemeUuid);
        args.putInt(ARG_MODE, mode);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate()");
        Bundle args = getArguments();
        String listThemeUuid = MySettings.NOT_AVAILABLE;
        mListThemeRepository = new ListThemeRepository_Impl(getActivity());
        int mode = ListThemeActivity.EDIT_EXISTING_LIST_THEME;

        if (args.containsKey(ARG_LIST_THEME_NAME)) {
            mListThemeName = args.getString(ARG_LIST_THEME_NAME);
            listThemeUuid = args.getString(ARG_ORIGINAL_LIST_THEME_UUID);
            mListTheme = mListThemeRepository.getListThemeByUuid(listThemeUuid);
            mode = args.getInt(ARG_MODE);
            switch (mode) {
                case ListThemeActivity.CREATE_NEW_LIST_THEME:
                    mDialogTitle = getActivity().getResources().getString(R.string.createListThemeNameDialog_title);
                    break;

                case ListThemeActivity.EDIT_EXISTING_LIST_THEME:
                    mDialogTitle = getActivity().getResources().getString(R.string.editListThemeNameDialog_title);
                    break;
            }
        }
        if (mListThemeName == null || (mListThemeName.isEmpty() && mode != ListThemeActivity.CREATE_NEW_LIST_THEME)) {
            Timber.e("onCreate(): ListTheme name not provided!");
        }

        if (mListTheme == null) {
            Timber.e("onCreate(): ListTheme with UUID = %s could not found!", listThemeUuid);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.i("onActivityCreated()");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mEditListThemeNameDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button saveButton = mEditListThemeNameDialog.getButton(Dialog.BUTTON_POSITIVE);
                saveButton.setTextSize(17);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        String themeProposedName = txtListThemeName.getText().toString().trim();

                        if (themeProposedName.isEmpty()) {
                            String errorMsg = getActivity().getString(R.string.themeProposedName_isEmpty_error);
                            txtListThemeName_input_layout.setError(errorMsg);

                        } else if (!mListThemeRepository.isValidThemeName(mListTheme, themeProposedName)) {
                            txtListThemeName.setText(mListTheme.getName());
                            String errorMsg = String.format(getActivity()
                                            .getString(R.string.themeProposedName_invalidName_error),
                                    themeProposedName);
                            txtListThemeName_input_layout.setError(errorMsg);

                        } else {
                            EventBus.getDefault().post(new MyEvents.setListThemeName(themeProposedName));
                            dismiss();
                        }
                    }
                });

                Button cancelButton = mEditListThemeNameDialog.getButton(Dialog.BUTTON_NEGATIVE);
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
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_single_edit_text, null);

        // find the dialog's views
        txtListThemeName = (EditText) view.findViewById(R.id.txtName);
        if (mListTheme != null) {
            txtListThemeName.setText(mListTheme.getName());
        }
        txtListThemeName_input_layout = (TextInputLayout) view.findViewById(R.id.txtName_input_layout);
        txtListThemeName_input_layout.setHint(getActivity().getString(R.string.txtListThemeName_hint));
        txtListThemeName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtListThemeName_input_layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // build the dialog
        mEditListThemeNameDialog = new AlertDialog.Builder(getActivity())
                .setTitle(mDialogTitle)
                .setView(view)
                .setPositiveButton(R.string.btnSave_title, null)
                .setNegativeButton(R.string.btnCancel_title, null)
                .create();

        return mEditListThemeNameDialog;
    }

}
