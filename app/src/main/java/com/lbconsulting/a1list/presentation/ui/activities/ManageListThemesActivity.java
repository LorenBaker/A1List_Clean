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

import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repository.ListThemeRepository_Impl;
import com.lbconsulting.a1list.presentation.presenters.impl.ManageListThemesActivityPresenter_Impl;
import com.lbconsulting.a1list.presentation.presenters.interfaces.ManageListThemesActvityPresenter;
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

public class ManageListThemesActivity extends AppCompatActivity implements ManageListThemesActvityPresenter.ListThemeView{
    private static ManageListThemesActivityPresenter_Impl mPresenter;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.lvThemes)
    ListView lvThemes;

    private ListThemeArrayAdapter mListThemeAdapter;

    //region Toggle Methods

    // Note: these Toggle Methods run on the UI thread
    // The update one field on one record in one SQLite table (ListThemeSqlTable)
    // After the toggle method complete, querying the SQLite ListThemeSqlTable for all ListThemes
    // is executed on a background thread. Upon its completion, the ListThemeArray adapter is notified
    // of a data change.
    //
    // The app seemed more responsive running on the UI thread as opposed to running both the Toggle
    // Methods and ListThemeSqlTable query on background threads.

    public static void toggleBold(ListTheme listTheme) {
        mPresenter.toggleBold(listTheme);
    }

    public static void toggleChecked(ListTheme listTheme) {
        mPresenter.toggleChecked(listTheme);
    }

    public static void toggleDefaultTheme(ListTheme listTheme) {
        mPresenter.toggleDefaultTheme(listTheme);
    }

    public static void toggleMarkedForDeletion(ListTheme listTheme) {
        mPresenter.toggleMarkedForDeletion(listTheme);
    }

    public static void toggleStrikeout(ListTheme listTheme) {
        mPresenter.toggleStrikeout(listTheme);
    }

    public static void toggleTransparent(ListTheme listTheme) {
        mPresenter.toggleTransparent(listTheme);
    }

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

        mPresenter = new ManageListThemesActivityPresenter_Impl(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, new ListThemeRepository_Impl(this));

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
        mPresenter.resume();
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
            deleteListThemeStrikeouts();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteListThemeStrikeouts() {
        Toast.makeText(this, "deleteListThemeStrikeouts Clicked", Toast.LENGTH_SHORT).show();
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
