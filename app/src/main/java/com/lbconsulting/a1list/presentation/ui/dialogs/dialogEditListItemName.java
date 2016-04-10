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
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository_Impl;
import com.lbconsulting.a1list.utils.MyEvents;
import com.lbconsulting.a1list.utils.MySettings;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

/**
 * A dialog where the user edits the ListItem's name
 */
public class dialogEditListItemName extends DialogFragment {
    public static final String DEFAULT_LIST_ITEM_NAME = "*#*#NewListItemName*#*#";
    private static final String ARG_LIST_ITEM_JSON = "argListItemJson";

    private EditText txtListItemName;
    private TextInputLayout txtListItemName_input_layout;

    private ListItem mListItem;
    private AlertDialog mEditListItemNameDialog;
    private ListItemRepository_Impl mListItemRepository;

    public dialogEditListItemName() {
        // Empty constructor required for DialogFragment
    }


    public static dialogEditListItemName newInstance(String listItemJson) {
        Timber.i("newInstance()");
        dialogEditListItemName frag = new dialogEditListItemName();
        Bundle args = new Bundle();
        args.putString(ARG_LIST_ITEM_JSON, listItemJson);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate()");
        Bundle args = getArguments();
        String listItemUuid = MySettings.NOT_AVAILABLE;
        mListItemRepository = AndroidApplication.getListItemRepository();
//        int mode = ListItemActivity.EDIT_EXISTING_LIST_THEME;

        if (args.containsKey(ARG_LIST_ITEM_JSON)) {
            String listItemJson = args.getString(ARG_LIST_ITEM_JSON);
            Gson gson = new Gson();
            mListItem = gson.fromJson(listItemJson, ListItem.class);
            // make sure we have the latest ListItem
            mListItem = mListItemRepository.retrieveListItemByUuid(mListItem.getUuid());
            if (mListItem == null) {
                Timber.e("onCreate(): FAILED to retrieve ListItem");
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.i("onActivityCreated()");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mEditListItemNameDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button saveButton = mEditListItemNameDialog.getButton(Dialog.BUTTON_POSITIVE);
                saveButton.setTextSize(17);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        String listItemProposedName = txtListItemName.getText().toString().trim();
                        if (okToReviseListItemName(listItemProposedName)) {
                            mListItem.setName(listItemProposedName);
                            mListItemRepository.update(mListItem);
                            EventBus.getDefault().post(new MyEvents.updateFragListItemsUI(mListItem.retrieveListTitle().getUuid()));
                            dismiss();
                        }
                    }
                });

                Button cancelButton = mEditListItemNameDialog.getButton(Dialog.BUTTON_NEGATIVE);
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

    private boolean okToReviseListItemName(String listItemProposedName) {
        boolean result = false;

        if (listItemProposedName.isEmpty()) {
            String errorMsg = getActivity().getString(R.string.listItemProposedName_isEmpty_error);
            txtListItemName_input_layout.setError(errorMsg);

        } else if (listItemProposedName.equals(DEFAULT_LIST_ITEM_NAME)) {
            String errorMsg = String.format(getActivity()
                            .getString(R.string.listItemProposedName_isDefault_error),
                    DEFAULT_LIST_ITEM_NAME);
            txtListItemName_input_layout.setError(errorMsg);

        } else if (!mListItemRepository.isValidListItemName(mListItem, listItemProposedName)) {
            txtListItemName.setText(mListItem.getName());
            String errorMsg = String.format(getActivity()
                            .getString(R.string.listItemProposedName_invalidName_error),
                    listItemProposedName);
            txtListItemName_input_layout.setError(errorMsg);

        } else {
            // ok to revise ListItem name.
            mListItem.setName(listItemProposedName);
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
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_single_edit_text, null);

        // find the dialog's views
        txtListItemName = (EditText) view.findViewById(R.id.txtName);

        String listItemName = mListItem.getName();
        if (listItemName.equals(DEFAULT_LIST_ITEM_NAME)) {
            listItemName = "";
        }
        txtListItemName.setText(listItemName);
        txtListItemName_input_layout = (TextInputLayout) view.findViewById(R.id.txtName_input_layout);
        txtListItemName_input_layout.setHint(getActivity().getString(R.string.txtListItemName_hint));
        txtListItemName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtListItemName_input_layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // build the dialog
        mEditListItemNameDialog = new AlertDialog.Builder(getActivity())
                .setTitle(" ")
                .setView(view)
                .setPositiveButton(R.string.btnSave_title, null)
                .setNegativeButton(R.string.btnCancel_title, null)
                .create();

        return mEditListItemNameDialog;
    }

}
