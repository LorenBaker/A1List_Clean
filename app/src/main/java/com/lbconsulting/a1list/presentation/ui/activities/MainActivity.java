package com.lbconsulting.a1list.presentation.ui.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.Message;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.listTheme.impl.CreateInitialListThemes_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.CreateInitialListThemes_Interactor;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.CreateNewListTitle_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.CreateNewListTitle_Interactor;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_Impl;
import com.lbconsulting.a1list.presentation.ui.activities.backendless.BackendlessLoginActivity;
import com.lbconsulting.a1list.presentation.ui.dialogs.dialogNewListTitle;
import com.lbconsulting.a1list.threading.MainThreadImpl;
import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.CsvParser;
import com.lbconsulting.a1list.utils.MyEvents;
import com.lbconsulting.a1list.utils.MySettings;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements CreateInitialListThemes_Interactor.Callback,
        CreateNewListTitle_Interactor.Callback {
    @Bind(R.id.fab)
    FloatingActionButton mFab;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.tvMainActivity)
    TextView tvMainActivity;

    @Bind(R.id.mainActivityContent)
    View mainActivityContent;

    @Bind(R.id.activityProgressBar)
    ProgressBar mProgressBar;

    @Bind(R.id.tvActivityProgressBarMessage)
    TextView tvProgressBarMessage;

    private String MESSAGE_CHANNEL = "";
    private Subscription mSubscription;

    private ListThemeRepository_Impl mListThemeRepository;
    private ListTitleRepository_Impl mListTitleRepository;

//    private CreateInitialListThemes_InBackground mCreateInitialListThemesInBackground;

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
        EventBus.getDefault().register(this);


//        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


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
                        Timber.e("Backendless.Messaging.subscribe()MessageCallback: BackendlessFault: %s", fault.getMessage());
                    }
                }, new AsyncCallback<Subscription>() {

                    @Override
                    public void handleResponse(Subscription response) {
                        Timber.i("Backendless. Successful messaging.subscribe()SubscriptionCallback");
                        mSubscription = response;
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Timber.e("Backendless.FAIL messaging.subscribe()SubscriptionCallback: BackendlessFault: %s", fault.getMessage());

                    }
                }
        );
        //endregion

        mListThemeRepository = new ListThemeRepository_Impl(this);
        mListTitleRepository = new ListTitleRepository_Impl(this, mListThemeRepository);
        showActiveUser();
    }

    @Subscribe
    public void onEvent(MyEvents.createNewListTitle event) {
        if (event.showProgress()) {
            showProgressBar("Creating new List.");
        }

        new CreateNewListTitle_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(),  this, event.getName(),
                mListTitleRepository, mListThemeRepository, event.showProgress()).execute();
    }


    private void updateUI(boolean hideProgressBar) {
        // TODO: implement updateUI()
        if (hideProgressBar) {
            hideProgressBar();
        }
    }


    @Override
    public void onListTitleCreated(ListTitle newListTitle, boolean hideProgressBar) {
        Timber.i("onListTitleCreated(): %s.", newListTitle.getName());
        updateUI(hideProgressBar);
    }

    @Override
    public void onListTitleCreationFailed(String errorMessage) {
        Timber.e("onListTitleCreationFailed(): %s.", errorMessage);

    }

    @OnClick(R.id.fab)
    public void fab() {
        String message = "Create new ListItem clicked.";
        CommonMethods.showSnackbar(mFab, message, Snackbar.LENGTH_LONG);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Timber.i("onResume()");

        if (MySettings.isStartedFromRegistrationActivity()) {
            Timber.i("onResume(): Show welcome dialog");
            initializeApp();
        } else {

            // TODO: Figure out where to hide the progress bar
            hideProgressBar();
//            mMainActivityPresenter.resume();

        }

//        initializeApp();
    }

    private void initializeApp() {
        Timber.i("initializeApp()");
        showProgressBar("Loading initial Themes.");
        new CreateInitialListThemes_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, mListThemeRepository, this).execute();
    }

    @Override
    public void onInitialListThemesCreated(String message) {
        Timber.i("onInitialListThemesCreated():\n%s", message);
        hideProgressBar();
        CommonMethods.showSnackbar(mFab, message, Snackbar.LENGTH_LONG);
        // TODO: show welcome dialog
    }

    @Override
    public void onListThemesCreationFailed(String errorMessage) {
        Timber.e("onListThemesCreationFailed(): %s.", errorMessage);
        hideProgressBar();
    }

    private void showProgressBar(String waitMessage) {
        mProgressBar.setVisibility(View.VISIBLE);
        tvProgressBarMessage.setText(String.format("Please wait ...\n%s", waitMessage));
        tvProgressBarMessage.setVisibility(View.VISIBLE);
        mainActivityContent.setVisibility(View.GONE);
        // TODO: hide menus
    }

    private void hideProgressBar() {
        Timber.i("hideProgressBar()");
        mProgressBar.setVisibility(View.GONE);
        tvProgressBarMessage.setVisibility(View.GONE);
        mainActivityContent.setVisibility(View.VISIBLE);
        // TODO: show menus
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.i("onPause()");
        MySettings.setStartedFromRegistrationActivity(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.i("onDestroy()");
        EventBus.getDefault().unregister(this);

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

        if (id == R.id.action_deleteItemsStruckOut) {
            deleteStruckOutItems();
            return true;
        } else if (id == R.id.action_showFavorites) {
            showFavorites();
            return true;
        } else if (id == R.id.action_newList) {
            createNewList();
            return true;

        } else if (id == R.id.action_listSorting) {
            showListSortingDialogue();
            return true;

        } else if (id == R.id.action_editListTheme) {
            editListTheme();
            return true;

        } else if (id == R.id.action_manageLists) {
            showManageListActivity();
            return true;

        } else if (id == R.id.action_manageThemes) {
            showManageListThemesActivity();
            return true;

        } else if (id == R.id.action_refresh) {
            refresh();
            return true;

        } else if (id == R.id.action_who_is_logged_in) {
            showActiveUser();
            return true;

        } else if (id == R.id.action_change_my_password) {
            changePassword();
            final String activeUserEmail = MySettings.getActiveUserEmail();
            CommonMethods.changePasswordRequest(this, activeUserEmail);
            return true;

        } else if (id == R.id.action_logout) {
            logoutUser();
            return true;

        } else if (id == R.id.action_settings) {
            showPreferencesActivity();
            Toast.makeText(this, "action_settings clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteStruckOutItems() {
        Toast.makeText(this, "deleteStruckOutItems clicked", Toast.LENGTH_SHORT).show();

    }

    private void showFavorites() {
        Toast.makeText(this, "showFavorites clicked", Toast.LENGTH_SHORT).show();

    }

    private void createNewList() {
        FragmentManager fm = getSupportFragmentManager();
        dialogNewListTitle dialog = dialogNewListTitle.newInstance();
        dialog.show(fm, "dialogNewListTitle");
//        Toast.makeText(this, "createNewList clicked", Toast.LENGTH_SHORT).show();

    }

    private void showListSortingDialogue() {
        Toast.makeText(this, "showListSortingDialogue clicked", Toast.LENGTH_SHORT).show();

    }

    private void editListTheme() {
        Toast.makeText(this, "editListTheme clicked", Toast.LENGTH_SHORT).show();

    }

    private void showManageListActivity() {
        Toast.makeText(this, "showManageListActivity clicked", Toast.LENGTH_SHORT).show();

    }

    private void showManageListThemesActivity() {
//        Toast.makeText(this, "showManageListThemesActivity clicked", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ManageListThemesActivity.class);
        startActivity(intent);
    }

    private void refresh() {
        Toast.makeText(this, "refresh clicked", Toast.LENGTH_SHORT).show();

    }

    private void showActiveUser() {
        String activeUserNameAndEmail = MySettings.getActiveUserNameAndEmail();
        String msg = "User: " + activeUserNameAndEmail;
        if (activeUserNameAndEmail.isEmpty()) {
            msg = "ERROR: user not available!";
        }
        CommonMethods.showSnackbar(mFab, msg, Snackbar.LENGTH_LONG);
    }

    private void changePassword() {
        Toast.makeText(this, "deleteStruckOutItems clicked", Toast.LENGTH_SHORT).show();

    }

    private void logoutUser() {
// TODO: Figure out how to logoutUser if they uninstall the app
        if (CommonMethods.isNetworkAvailable()) {
            Backendless.UserService.logout(new AsyncCallback<Void>() {
                public void handleResponse(Void response) {
                    // user has been logged out.
                    String msg = "User logged out.";
                    Timber.i("logoutUser(): %s", msg);
                    CommonMethods.showSnackbar(mFab, msg, Snackbar.LENGTH_LONG);
                    MySettings.resetActiveUserAndEmail();
                    MESSAGE_CHANNEL = MySettings.NOT_AVAILABLE;
                    startLoginActivity();
                }

                public void handleFault(BackendlessFault e) {
                    // something went wrong and logout failed, to get the error code call fault.getCode()
                    Timber.e("logoutUser(): BackendlessFault: %s", e.getMessage());
                }
            });
        } else {
            String msg = "Unable to log user. Network is not available";
            Timber.i("logoutUser(): %s", msg);
            CommonMethods.showSnackbar(mFab, msg, Snackbar.LENGTH_LONG);
        }
    }

    private void showPreferencesActivity() {
        Toast.makeText(this, "deleteStruckOutItems clicked", Toast.LENGTH_SHORT).show();

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
