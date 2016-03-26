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

import com.google.gson.Gson;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_Impl;
import com.lbconsulting.a1list.utils.MyEvents;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;


/**
 * A dialog where the user edits an existing ListTitle Name
 */
public class dialogEditListTitleName extends DialogFragment {
    public static final String DEFAULT_LIST_TITLE_NAME = "*#*#NewList*#*#";
    private static final String ARG_LIST_TITLE_JSON = "argListTitleJson";

    private EditText txtListTitleName;
    private TextInputLayout txtListTitleName_input_layout;

    private AlertDialog mEditListTitleDialog;
    private ListTitleRepository_Impl mListTitleRepository;
    private ListTitle mListTitle;

    public dialogEditListTitleName() {
        // Empty constructor required for DialogFragment
    }


    public static dialogEditListTitleName newInstance(String listTitleJson) {
        Timber.i("newInstance()");
        dialogEditListTitleName frag = new dialogEditListTitleName();
        Bundle args = new Bundle();
        args.putString(ARG_LIST_TITLE_JSON, listTitleJson);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate()");

        mListTitleRepository = AndroidApplication.getListTitleRepository();
        Bundle args = getArguments();
        if (args.containsKey(ARG_LIST_TITLE_JSON)) {
            String listTitleJson = args.getString(ARG_LIST_TITLE_JSON);
            Gson gson = new Gson();
            mListTitle = gson.fromJson(listTitleJson, ListTitle.class);
            if (mListTitle == null) {
                Timber.e("onCreate(): FAILED to retrieve ListTitle");
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.i("onActivityCreated()");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mEditListTitleDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = mEditListTitleDialog.getButton(Dialog.BUTTON_POSITIVE);
                positiveButton.setTextSize(17);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        String proposedListTitleName = txtListTitleName.getText().toString().trim();
                        if (okToReviseListName(proposedListTitleName)) {
                            EventBus.getDefault().post(new MyEvents.setListTitleName(proposedListTitleName));
                            dismiss();
                        }
                    }
                });

                Button negativeButton = mEditListTitleDialog.getButton(Dialog.BUTTON_NEGATIVE);
                negativeButton.setTextSize(17);
                negativeButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Cancel
                        dismiss();
                    }
                });

            }
        });
    }

    private boolean okToReviseListName(String proposedListTitleName) {
        boolean result = false;
        if (proposedListTitleName.isEmpty()) {
            String errorMsg = getActivity().getString(R.string.newListTitleName_isEmpty_error);
            txtListTitleName_input_layout.setError(errorMsg);

        } else if(proposedListTitleName.equals(DEFAULT_LIST_TITLE_NAME)){
            String errorMsg = String.format(getActivity()
                            .getString(R.string.newListTitleName_isDefault_error),
                    DEFAULT_LIST_TITLE_NAME);
            txtListTitleName_input_layout.setError(errorMsg);

        } else if (!mListTitleRepository.isValidListTitleName(mListTitle, proposedListTitleName)) {
            String errorMsg = String.format(getActivity()
                    .getString(R.string.newListTitleName_listExists_error), proposedListTitleName);
            txtListTitleName_input_layout.setError(errorMsg);

        } else {
            // ok to revise ListTitle name.
            mListTitle.setName(proposedListTitleName);
            result = true;
        }
        return result;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.i("onCreateDialog()");

        // inflate the xml layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_single_edit_text, null, false);

        // find the dialog's views
        txtListTitleName = (EditText) view.findViewById(R.id.txtName);

        String listTitleName = mListTitle.getName();
        if (listTitleName.equals(DEFAULT_LIST_TITLE_NAME)) {
            listTitleName = "";
        }
        txtListTitleName.setText(listTitleName);
        txtListTitleName_input_layout = (TextInputLayout) view.findViewById(R.id.txtName_input_layout);
        txtListTitleName_input_layout.setHint(getActivity().getString(R.string.txtListTitleName_hint));
        txtListTitleName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtListTitleName_input_layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // build the dialog
        mEditListTitleDialog = new AlertDialog.Builder(getActivity())
                .setTitle(" ")
                .setView(view)
                .setPositiveButton(R.string.btnEnter_title, null)
                .setNegativeButton(R.string.btnCancel_title, null)
                .create();

        return mEditListTitleDialog;
    }

}
