package com.lbconsulting.a1list.presentation.ui.activities.backendless;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.presentation.ui.activities.MainActivity;
import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.MySettings;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A registration screen that offers registration via txtEmail/password.
 */
public class BackendlessRegistrationActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the registration task to ensure we can cancel it if requested.
     */
    private UserRegistrationTask mAuthTask = null;

    // UI references.
    private EditText txtFirstName;
    private EditText txtLastName;
    private AutoCompleteTextView txtEmail;
    private EditText txtPassword;
    private EditText txtReenteredPassword;

    private View mProgressView;
    private View mRegistrationFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate()");
        setContentView(R.layout.activity_registration);

        // Set up the registration form.
        txtFirstName = (EditText) findViewById(R.id.txtFirstName);
        txtLastName = (EditText) findViewById(R.id.txtLastName);

        txtEmail = (AutoCompleteTextView) findViewById(R.id.txtEmail);
        populateAutoComplete();

        txtPassword = (EditText) findViewById(R.id.txtPassword);


        txtReenteredPassword = (EditText) findViewById(R.id.txtReenteredPassword);
        txtReenteredPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptRegistration();
                    handled = true;
                }
                return handled;
            }
        });

        Bundle extras = getIntent().getExtras();
        txtEmail.setText(extras.getString("email"));
        txtPassword.setText(extras.getString("password"));

        Button mRegistrationButton = (Button) findViewById(R.id.email_register_button);
        mRegistrationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegistration();
            }
        });

        mRegistrationFormView = findViewById(R.id.registration_form);
        mProgressView = findViewById(R.id.registration_progress);
    }


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(txtEmail, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the registration form.
     * If there are form errors (invalid txtEmail, missing fields, etc.), the
     * errors are presented and no actual registration attempt is made.
     */
    private void attemptRegistration() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.

        txtFirstName.setError(null);
        txtLastName.setError(null);
        txtEmail.setError(null);
        txtPassword.setError(null);
        txtReenteredPassword.setError(null);

        // Store values at the time of the registration attempt.
        String firstName = txtFirstName.getText().toString().trim();
        String lastName = txtLastName.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        String reenteredPassword = txtReenteredPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid first name
        if (TextUtils.isEmpty(firstName)) {
            txtFirstName.setError(getString(R.string.error_field_required));
            focusView = txtFirstName;
            cancel = true;
        }

        // Check for a valid last name
        if (TextUtils.isEmpty(lastName)) {
            txtLastName.setError(getString(R.string.error_field_required));
            focusView = txtLastName;
            cancel = true;
        }

        // Check for a valid Email address.
        if (TextUtils.isEmpty(email)) {
            txtEmail.setError(getString(R.string.error_field_required));
            focusView = txtEmail;
            cancel = true;
        } else if (!CommonMethods.isEmailValid(email)) {
            txtEmail.setError(getString(R.string.error_invalid_email));
            focusView = txtEmail;
            cancel = true;
        }

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            txtPassword.setError(getString(R.string.error_field_required));
            focusView = txtPassword;
            cancel = true;
        } else if (!CommonMethods.isPasswordValid(password)) {
            txtPassword.setError(getString(R.string.error_invalid_password));
            focusView = txtPassword;
            cancel = true;
        } else if (!password.equals(reenteredPassword)) {
            txtReenteredPassword.setError(getString(R.string.error_passwords_do_not_match));
            cancel = true;
        }

        // Check for a valid reentered password
        if (TextUtils.isEmpty(reenteredPassword)) {
            txtReenteredPassword.setError(getString(R.string.error_field_required));
            focusView = txtReenteredPassword;
            cancel = true;
        } else if (!CommonMethods.isPasswordValid(reenteredPassword)) {
            txtReenteredPassword.setError(getString(R.string.error_invalid_password));
            focusView = txtReenteredPassword;
            cancel = true;
        } else if (!password.equals(reenteredPassword)) {
            txtReenteredPassword.setError(getString(R.string.error_passwords_do_not_match));
            focusView = txtReenteredPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt registration and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user registration attempt.
            showProgress(true);
            mAuthTask = new UserRegistrationTask(firstName, lastName, email, password);
            mAuthTask.execute((Void) null);
        }
    }


    /**
     * Shows the progress UI and hides the registration form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegistrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegistrationFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegistrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegistrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only txtEmail addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary txtEmail addresses first. Note that there won't be
                // a primary txtEmail address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(BackendlessRegistrationActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        txtEmail.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous registration/registration task used to authenticate
     * the user.
     */
    public class UserRegistrationTask extends AsyncTask<Void, Void, Boolean> {

        private final String mFirstName;
        private final String mLastName;
        private final String mEmail;
        private final String mPassword;
        private String mRegistrationFailMessage = "";

        UserRegistrationTask(String firstName, String lastName, String email, String password) {
            mFirstName = firstName;
            mLastName = lastName;
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Timber.i("onPreExecute()");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean isRegistrationSuccess = false;
            try {
                BackendlessUser user = new BackendlessUser();
                user.setEmail(mEmail);
                user.setPassword(mPassword);
                user.setProperty("firstName", mFirstName);
                user.setProperty("lastName", mLastName);
                user = Backendless.UserService.register(user);

                try {
                    Backendless.UserService.login(mEmail, mPassword, true);
                    isRegistrationSuccess = Backendless.UserService.isValidLogin();
                    if (isRegistrationSuccess) {
                        MySettings.setActiveUserAndEmail(
                                user.getUserId(),
                                user.getProperty("firstName").toString(),
                                user.getProperty("lastName").toString(),
                                user.getEmail());
                    }

                } catch (BackendlessException e) {
                    String failMessage = "Login failed. " + e.getMessage();
                    Timber.e("UserRegistrationTask: doInBackground(): BackendlessException: %s", failMessage);

                }

                // login the new user

            } catch (BackendlessException e) {
                mRegistrationFailMessage = e.getMessage();
                String failMessage = "Registration failed. " + e.getMessage();
                Timber.e("UserRegistrationTask: doInBackground(): BackendlessException: %s", failMessage);
            }

            // TODO: register the new account here.
            return isRegistrationSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Timber.i("UserRegistrationTask: onPostExecute(): Registration success: %s", mEmail);
                startMainActivity();
            } else {
                Timber.i("UserRegistrationTask: onPostExecute(): Registration Failed. %s", mRegistrationFailMessage);
                CommonMethods.showOkDialog(BackendlessRegistrationActivity.this, "Registration Failed" ,mRegistrationFailMessage);
                CommonMethods.showSnackbar(mRegistrationFormView, mRegistrationFailMessage, Snackbar.LENGTH_LONG);
//                Toast.makeText(BackendlessRegistrationActivity.this, mRegistrationFailMessage, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        // reset the backstack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}

