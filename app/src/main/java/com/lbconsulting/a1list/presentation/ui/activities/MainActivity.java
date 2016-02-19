package com.lbconsulting.a1list.presentation.ui.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.Message;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.CreateInitialListThemesInteractor;
import com.lbconsulting.a1list.domain.interactors.impl.CreateInitialListThemesInteractor_Imp;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repository.ListThemeRepository_Impl;
import com.lbconsulting.a1list.presentation.presenters.ListThemesPresenter;
import com.lbconsulting.a1list.presentation.presenters.impl.ListThemePresenter_Impl;
import com.lbconsulting.a1list.presentation.ui.activities.backendless.BackendlessLoginActivity;
import com.lbconsulting.a1list.presentation.ui.adapters.ListThemeArrayAdapter;
import com.lbconsulting.a1list.threading.MainThreadImpl;
import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.CsvParser;
import com.lbconsulting.a1list.utils.MySettings;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements ListThemesPresenter.ListThemeView,
        CreateInitialListThemesInteractor.Callback {
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.lvThemes)
    ListView lvThemes;
    private boolean mIsStartedFromRegistrationActivity;
    private String MESSAGE_CHANNEL = "";
    private Subscription mSubscription;
    private ListThemeArrayAdapter mListThemeAdapter;
    private ListThemePresenter_Impl mPresenter;
    private CreateInitialListThemesInteractor_Imp mListThemeCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate()");
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mIsStartedFromRegistrationActivity = extras.getBoolean(MySettings.EXTRA_IS_STARTED_FROM_REGISTRATION_ACTIVITY);
        }

//        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mListThemeAdapter = new ListThemeArrayAdapter(this, lvThemes, true, mFab);
        lvThemes.setAdapter(mListThemeAdapter);

        //region Messaging
        MESSAGE_CHANNEL = MySettings.getActiveUserID();
        Backendless.Messaging.subscribe(MESSAGE_CHANNEL,
                new AsyncCallback<List<Message>>() {
                    @Override
                    public void handleResponse(List<Message> response) {
                        for (Message message : response) {
                            // TODO: ignore messages initiated from this device
                            // TODO: design message payload. Make changes to SQLite db.
                            String csvDataString = message.getData().toString();
                            MessagePayload payload = new MessagePayload(csvDataString);
//                            tvMessagesReceived.setText(tvMessagesReceived.getText() + "\n\n" + payload.toString());
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Timber.e("Backendless.Messaging.subscribe()MessageCallback: BackendlessFault: ", fault.getMessage());
                    }
                }, new AsyncCallback<Subscription>() {

                    @Override
                    public void handleResponse(Subscription response) {
                        Timber.i("Backendless. Successful messaging.subscribe()SubscriptionCallback");
                        mSubscription = response;
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Timber.e("Backendless.FAIL messaging.subscribe()SubscriptionCallback: BackendlessFault: ", fault.getMessage());

                    }
                }
        );
        //endregion

        mPresenter = new ListThemePresenter_Impl(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, new ListThemeRepository_Impl(this));

        mListThemeCreator = new CreateInitialListThemesInteractor_Imp(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, new ListThemeRepository_Impl(this), this);

        showActiveUser();
    }


    @OnClick(R.id.fab)
    public void fab() {
        String message = "Floating action button clicked.";
        CommonMethods.showSnackbar(mFab, message, Snackbar.LENGTH_LONG);
    }

    @Override
    public void showProgress() {
        Timber.i("showProgress()");
    }

    @Override
    public void hideProgress() {
        Timber.i("hideProgress()");
    }

    @Override
    public void showError(String message) {
        Timber.e("showError(): %s", message);
    }


    @Override
    public void displayAllListThemes(List<ListTheme> allListThemes) {
        mListThemeAdapter.setData(allListThemes);
        mListThemeAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.i("onResume()");
        if (mIsStartedFromRegistrationActivity) {
            Timber.i("onResume(): Show welcome dialog");
            // TODO: show welcome dialog
            initializeApp();
        } else {
            // present all ListThemes
            Timber.i("onResume(): starting  ListThemePresenter_Impl");
            mPresenter.resume();
        }

//        initializeApp();
    }


    private void initializeApp() {
        Timber.i("initializeApp()");
        mListThemeCreator.execute();
    }


    @Override
    public void onInitialListThemesCreated(List<ListTheme> listThemes, String message) {
        Timber.i("onInitialListThemesCreated():\n%s", message);
        mPresenter.resume();
    }

    @Override
    public void onListThemesCreationFailed(String errorMessage) {
        Timber.e("onListThemesCreationFailed(): %s.", errorMessage);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.i("onPause()");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.i("onDestroy()");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_actvity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_who_is_logged_in) {
            showActiveUser();
            return true;

        } else if (id == R.id.action_change_my_password) {
            final String activeUserEmail = MySettings.getActiveUserEmail();
            CommonMethods.changePasswordRequest(this, activeUserEmail);
            return true;

        } else if (id == R.id.action_logout) {
            logoutUser();
            return true;

        } else if (id == R.id.action_settings) {
            Toast.makeText(this, "action_settings clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showActiveUser() {
        String activeUserNameAndEmail = MySettings.getActiveUserNameAndEmail();
        String msg = "User: " + activeUserNameAndEmail;
        if (activeUserNameAndEmail.isEmpty()) {
            msg = "ERROR: user not available!";
        }
        CommonMethods.showSnackbar(mFab, msg, Snackbar.LENGTH_LONG);
    }

    private void logoutUser() {
// TODO: Figure out how to logoutUser if they uninstall the app
        if (CommonMethods.isNetworkAvailable()) {
            Backendless.UserService.logout(new AsyncCallback<Void>() {
                public void handleResponse(Void response) {
                    // user has been logged out.
                    String msg = "User logged out.";
                    Timber.i("logoutUser(): ", msg);
                    CommonMethods.showSnackbar(mFab, msg, Snackbar.LENGTH_LONG);
                    MySettings.resetActiveUserAndEmail();
                    MESSAGE_CHANNEL = MySettings.NOT_AVAILABLE;
                    startLoginActivity();
                }

                public void handleFault(BackendlessFault e) {
                    // something went wrong and logout failed, to get the error code call fault.getCode()
                    Timber.e("logoutUser(): BackendlessFault: ", e.getMessage());
                }
            });
        } else {
            String msg = "Unable to log user. Network is not available";
            Timber.i("logoutUser(): ", msg);
            CommonMethods.showSnackbar(mFab, msg, Snackbar.LENGTH_LONG);
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, BackendlessLoginActivity.class);
        startActivity(intent);
        finish();
    }



    private class MessagePayload {
        private String mCreationTime;
        private String mAction;
        private String mTableName;
        private String mObjectUuid;

        public MessagePayload(String action, String tableName, String objectUuid) {
            this.mAction = action;
            this.mTableName = tableName;
            this.mObjectUuid = objectUuid;
            mCreationTime = String.valueOf(System.currentTimeMillis());
        }

        public MessagePayload(String csvDataString) {
            ArrayList<ArrayList<String>> records = CsvParser.CreateRecordAndFieldLists(csvDataString);
            if (records.size() > 0) {
                // load the first (and only) record.
                ArrayList<String> record = records.get(0);
                this.mAction = record.get(0);
                this.mTableName = record.get(1);
                this.mObjectUuid = record.get(2);
                mCreationTime = record.get(3);
            } else {
                Timber.e("MessagePayload(): Unable to create MessagePayload. No data records found!");
            }
        }

        @Override
        public String toString() {
            return String.valueOf(mAction + ": " + mTableName + ": " + mObjectUuid + "\n>> " + mCreationTime);
        }

        public String toCsvString() {
            ArrayList<String> payload = new ArrayList<>();
            payload.add(mAction);
            payload.add(mTableName);
            payload.add(mObjectUuid);
            payload.add(mCreationTime);
            return CsvParser.toCSVString(payload);
        }
    }
}
