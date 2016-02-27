package com.lbconsulting.a1list.presentation.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.impl.DeleteStruckOutListThemes_InBackground;
import com.lbconsulting.a1list.domain.interactors.impl.ToggleListThemeBooleanField_InBackground;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repository.ListThemeRepository_Impl;
import com.lbconsulting.a1list.presentation.presenters.impl.ListThemesPresenter_Impl;
import com.lbconsulting.a1list.presentation.presenters.interfaces.ListThemesPresenter;
import com.lbconsulting.a1list.presentation.ui.activities.backendless.BackendlessLoginActivity;
import com.lbconsulting.a1list.presentation.ui.adapters.ListThemeArrayAdapter;
import com.lbconsulting.a1list.threading.MainThreadImpl;
import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.CsvParser;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class ManageListThemesActivity extends AppCompatActivity implements ListThemesPresenter.ListThemeView,
        DeleteStruckOutListThemes_InBackground.Callback, ToggleListThemeBooleanField_InBackground.Callback {
    private static ListThemesPresenter_Impl mPresenter;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.lvThemes)
    ListView lvThemes;

    @Bind(R.id.manageListThemesProgressBar)
    ProgressBar mProgressBar;

    @Bind(R.id.manageListThemesProgressBarMessage)
    TextView tvProgressBarMessage;

    private ListThemeArrayAdapter mListThemeAdapter;
    private DeleteStruckOutListThemes_InBackground mDeleteStruckOutListThemes;
    private ListThemeRepository_Impl mListThemeRepository;

    private DialogInterface.OnClickListener deleteListThemesDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    mDeleteStruckOutListThemes.execute();
                    if (CommonMethods.isNetworkAvailable()) {
                        showProgressBar();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    //region Toggle Methods

    // Note: these Toggle Methods run on the UI thread
    // The update one field on one record in one SQLite table (ListThemeSqlTable)
    // After the toggle method complete, querying the SQLite ListThemeSqlTable for all ListThemes
    // is executed on a background thread. Upon its completion, the ListThemeArray adapter is notified
    // of a data change.
    //
    // The app seemed more responsive running on the UI thread as opposed to running both the Toggle
    // Methods and ListThemeSqlTable query on background threads.
    private int mNumberOfStruckOutListThemes;

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
        setContentView(R.layout.activity_manage_list_themes);

        ButterKnife.bind(this);

        mListThemeAdapter = new ListThemeArrayAdapter(this, lvThemes, true, mFab);
        lvThemes.setAdapter(mListThemeAdapter);

        //region Messaging
/*        MESSAGE_CHANNEL = MySettings.getActiveUserID();
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
        );*/
        //endregion

        mListThemeRepository = new ListThemeRepository_Impl(this);

        mPresenter = new ListThemesPresenter_Impl(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, mListThemeRepository);

        mDeleteStruckOutListThemes = new DeleteStruckOutListThemes_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, mListThemeRepository);

        mNumberOfStruckOutListThemes = mListThemeRepository.getNumberOfStruckOutListThemes();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @OnClick(R.id.fab)
    public void fab() {
        String message = "Create New ListTheme action button clicked.";
        CommonMethods.showSnackbar(mFab, message, Snackbar.LENGTH_LONG);
    }

    @Override
    public void showProgress(String waitMessage) {
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
    protected void onResume() {
        super.onResume();
        Timber.i("onResume()");
        mPresenter.resume();
    }

    @Override
    public void displayAllListThemes(List<ListTheme> allListThemes) {
        // mPresenter's results
        mListThemeAdapter.setData(allListThemes);
        mListThemeAdapter.notifyDataSetChanged();
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
        getMenuInflater().inflate(R.menu.menu_manage_list_theme_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_deleteListThemeStrikeouts) {
//            Toast.makeText(this, "deleteStruckOutListThemes Clicked", Toast.LENGTH_SHORT).show();
            if (mNumberOfStruckOutListThemes == 0) {
                String title = "";
                String msg = "No Themes selected for deletion.";
                CommonMethods.showOkDialog(this, title, msg);

            } else if (mNumberOfStruckOutListThemes == 1) {
                String title = "Delete Theme";
                String msg = String.format("Do you want to permanently delete %d Theme?", mNumberOfStruckOutListThemes);
                showDeleteListThemesYesNoDialog(title, msg);

            } else {
                String title = "Delete Themes";
                String msg = String.format("Do you want to permanently delete %d Themes.", mNumberOfStruckOutListThemes);
                showDeleteListThemesYesNoDialog(title, msg);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteListThemesYesNoDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", deleteListThemesDialogClickListener)
                .setNegativeButton("No", deleteListThemesDialogClickListener)
                .show();
    }


    private void showProgressBar() {
        Timber.i("showProgressBar()");
        mProgressBar.setVisibility(View.VISIBLE);
        tvProgressBarMessage.setVisibility(View.VISIBLE);
        lvThemes.setVisibility(View.GONE);
        // TODO: hide menus
    }

    private void hideProgressBar() {
        Timber.i("hideProgressBar()");
        mProgressBar.setVisibility(View.GONE);
        tvProgressBarMessage.setVisibility(View.GONE);
        lvThemes.setVisibility(View.VISIBLE);
        // TODO: show menus
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, BackendlessLoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStruckOutListThemesDeleted(String successMessage) {
        Timber.i("onStruckOutListThemesDeleted(): %s.", successMessage);
        mNumberOfStruckOutListThemes=0;
        mPresenter.resume();
        hideProgressBar();
    }

    @Override
    public void onStruckOutListThemesDeletionFailed(String errorMessage) {
        Timber.e("onStruckOutListThemesDeleted(): %s.", errorMessage);
        mNumberOfStruckOutListThemes = mListThemeRepository.getNumberOfStruckOutListThemes();
        mPresenter.resume();
        hideProgressBar();
    }

    @Override
    public void onListThemeBooleanFieldToggled(int toggleValue) {
        mNumberOfStruckOutListThemes += toggleValue;
//        Timber.i("onListThemeBooleanFieldToggled(): Number of struck out ListThemes = %d.", mNumberOfStruckOutListThemes);
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
