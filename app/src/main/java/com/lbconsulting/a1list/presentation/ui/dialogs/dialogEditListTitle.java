package com.lbconsulting.a1list.presentation.ui.dialogs;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import com.lbconsulting.a1list.domain.model.ListTitle;

import timber.log.Timber;


/**
 * A dialog where the user creates a new ListTitle
 */
public class dialogEditListTitle extends DialogFragment {

    private static final String ARG_LIST_TITLE_UUID = "argListTitleUuid";

    private EditText txtListTitleName;
    private TextInputLayout txtListTitleName_input_layout;

    private AlertDialog mNewListTitleDialog;
    private ListTitle mListTitle;

    public dialogEditListTitle() {
        // Empty constructor required for DialogFragment
    }


    public static dialogEditListTitle newInstance(String listTitleUuid) {
        Timber.i("newInstance()");
        dialogEditListTitle frag = new dialogEditListTitle();
        Bundle args = new Bundle();
        args.putString(ARG_LIST_TITLE_UUID, listTitleUuid);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate()");

        Bundle args = getArguments();
        if (args.containsKey(ARG_LIST_TITLE_UUID)) {
            String listTitleID = args.getString(ARG_LIST_TITLE_UUID);
//            mListTitle = ListTitle.getListTitle(listTitleID);
            // TODO: get ListTile from repositories
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.i("onActivityCreated()");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mNewListTitleDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = mNewListTitleDialog.getButton(Dialog.BUTTON_POSITIVE);
                positiveButton.setTextSize(17);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        if (reviseListName(txtListTitleName.getText().toString().trim())) {
//                            EventBus.getDefault().post(new MyEvents.updateListTitleUI());
                            // TODO: implement updateListTileUI
                            dismiss();
                        }
                    }
                });

                Button negativeButton = mNewListTitleDialog.getButton(Dialog.BUTTON_NEGATIVE);
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

    private boolean reviseListName(String newListName) {
        boolean result = false;
        // TODO: implement reviseListName
//        if (newListName.isEmpty()) {
//            String errorMsg = getActivity().getString(R.string.reviseListName_isEmpty_error);
//            txtListTitleName_input_layout.setError(errorMsg);
//
//        } else if (ListTitle.listExists(newListName)) {
//            boolean isSameObject = ListTitle.getIsSameObject(mListTitle, newListName);
//            if (isSameObject) {
//                mListTitle.setName(newListName);
//                result = true;
//            } else {
//                String errorMsg = String.format(getActivity()
//                        .getString(R.string.reviseListName_listExists_error), newListName);
//                txtListTitleName_input_layout.setError(errorMsg);
//            }
//
//        } else {
//            // ok to create list
//            mListTitle.setName(newListName);
//            result = true;
//        }
        return result;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.i("onCreateDialog()");

        // inflate the xml layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_single_edit_text, null, false);

        // find the dialog's views
        txtListTitleName = (EditText) view.findViewById(R.id.txtName);
        txtListTitleName.setText(mListTitle.getName());
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
        mNewListTitleDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.editListTitleDialog_title)
                .setView(view)
                .setPositiveButton(R.string.btnSave_title, null)
                .setNegativeButton(R.string.btnCancel_title, null)
                .create();

        return mNewListTitleDialog;
    }

}
