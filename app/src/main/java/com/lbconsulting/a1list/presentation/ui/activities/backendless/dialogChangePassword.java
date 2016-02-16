package com.lbconsulting.a1list.presentation.ui.activities.backendless;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.utils.CommonMethods;

import timber.log.Timber;


/**
 * A dialog where the user creates a new ListTitle
 */
public class dialogChangePassword extends DialogFragment {

    private EditText txtEmail;
    private TextInputLayout txtEmail_input_layout;

    private AlertDialog mChangePasswordDialog;

    public dialogChangePassword() {
        // Empty constructor required for DialogFragment
    }


    public static dialogChangePassword newInstance() {
        Timber.i("newInstance()");
        return new dialogChangePassword();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate()");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.i("onActivityCreated()");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mChangePasswordDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = mChangePasswordDialog.getButton(Dialog.BUTTON_POSITIVE);
                positiveButton.setTextSize(17);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        String email = txtEmail.getText().toString().trim();
                        attemptChangePasswordRequest(email);
                    }
                });

                Button negativeButton = mChangePasswordDialog.getButton(Dialog.BUTTON_NEGATIVE);
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

    private void attemptChangePasswordRequest(String email) {
        boolean cancel = false;
        if (TextUtils.isEmpty(email)) {
            txtEmail.setError(getString(R.string.error_field_required));
            cancel = true;
        } else if (!CommonMethods.isEmailValid(email)) {
            txtEmail.setError(getString(R.string.error_invalid_email));
            cancel = true;
        }

        if(cancel){
            // There was an error; don't attempt change password request
            // focus the first form on txtEmail field.
            txtEmail.requestFocus();
        }else{
            CommonMethods.changePasswordRequest(getActivity(), email);
            dismiss();
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.i("onCreateDialog()");

        // inflate the xml layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_single_edit_text, null, false);

        // find the dialog's views
        txtEmail = (EditText) view.findViewById(R.id.txtName);
        txtEmail_input_layout = (TextInputLayout) view.findViewById(R.id.txtName_input_layout);
        txtEmail_input_layout.setHint(getActivity().getString(R.string.prompt_email));
        txtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtEmail_input_layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // build the dialog
        mChangePasswordDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Request Password Change")
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        return mChangePasswordDialog;
    }

}
