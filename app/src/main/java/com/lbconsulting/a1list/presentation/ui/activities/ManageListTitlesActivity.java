package com.lbconsulting.a1list.presentation.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.DeleteStruckOutListTitles_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.ToggleListTitleBooleanField_InBackground;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.AppSettingsRepository;
import com.lbconsulting.a1list.domain.repositories.AppSettingsRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_Impl;
import com.lbconsulting.a1list.presentation.presenters.impl.ListTitlesPresenter_Impl;
import com.lbconsulting.a1list.presentation.presenters.interfaces.ListTitlesPresenter;
import com.lbconsulting.a1list.presentation.ui.adapters.ListTitleArrayAdapter;
import com.lbconsulting.a1list.presentation.ui.dialogs.dialogEditListTitleName;
import com.lbconsulting.a1list.threading.MainThreadImpl;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class ManageListTitlesActivity extends AppCompatActivity implements ListTitlesPresenter.ListTitleView,
        DeleteStruckOutListTitles_InBackground.Callback, ToggleListTitleBooleanField_InBackground.Callback {

    private static ListTitlesPresenter_Impl mPresenter;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.lvListTitles)
    ListView lvListTitles;
    @Bind(R.id.manageListTitlesActivityContent)
    View manageListTitlesActivityContent;
    @Bind(R.id.activityProgressBar)
    View manageListTitlesActivityProgressBar;
    @Bind(R.id.tvActivityProgressBarMessage)
    TextView tvProgressBarMessage;
    private ListTitleArrayAdapter mListTitleAdapter;
    private DeleteStruckOutListTitles_InBackground mDeleteStruckOutListTitles;

    private AppSettingsRepository_Impl mAppSettingsRepository;
    private ListThemeRepository_Impl mListThemeRepository;
    private ListTitleRepository_Impl mListTitleRepository;
    private int mNumberOfStruckOutListTitles;

    // Note: these Toggle Methods run on the UI thread
    // The update one field on one record in one SQLite table (ListTitlesSqlTable)
    // After the toggle method complete, querying the SQLite ListTitlesSqlTable for all ListTitles
    // is executed on a background thread. Upon its completion, the ListTitleArray adapter is notified
    // of a data change.
    //
    // The app seemed more responsive running on the UI thread as opposed to running both the Toggle
    // Methods and ListTitlesSqlTable query on background threads.

    //region Toggle Methods
    private DialogInterface.OnClickListener deleteListTitlesDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    mDeleteStruckOutListTitles.execute();
                    if (CommonMethods.isNetworkAvailable()) {
                        String deletingThemeMessage = getResources().getQuantityString(
                                R.plurals.deletingListTitles, mNumberOfStruckOutListTitles, mNumberOfStruckOutListTitles);
                        showProgress(deletingThemeMessage);
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

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
        setContentView(R.layout.activity_manage_list_titles);
        ButterKnife.bind(this);

        mListTitleAdapter = new ListTitleArrayAdapter(this, lvListTitles, true, mFab);
        lvListTitles.setAdapter(mListTitleAdapter);

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

        mAppSettingsRepository = new AppSettingsRepository_Impl(this);
        mListThemeRepository = new ListThemeRepository_Impl(this);
        mListTitleRepository = new ListTitleRepository_Impl(this,mAppSettingsRepository ,mListThemeRepository);

        // TODO: Get isListTitlesSortedAlphabetically from AppSettings
        boolean isListTitlesSortedAlphabetically = true;

        mPresenter = new ListTitlesPresenter_Impl(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, mListTitleRepository,isListTitlesSortedAlphabetically);

        mDeleteStruckOutListTitles = new DeleteStruckOutListTitles_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, mListTitleRepository);

        mNumberOfStruckOutListTitles = mListTitleRepository.getNumberOfStruckOutListTitles();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Lists");
        }

    }

    @OnClick(R.id.fab)
    public void fab() {
        ListTheme defaultListTheme = mListThemeRepository.retrieveDefaultListTheme();
        AppSettingsRepository appSettingsRepository = new AppSettingsRepository_Impl(this);
        ListTitle newListTitle = ListTitle.newInstance(
                dialogEditListTitleName.DEFAULT_LIST_TITLE_NAME, defaultListTheme, appSettingsRepository);
        startListTitleActivity(newListTitle);
    }

    private void startListTitleActivity(ListTitle newListTitle) {
        Gson gson = new Gson();
        String listTitleJson = gson.toJson(newListTitle);
        Intent listTitleActivityIntent = new Intent(this, ListTitleActivity.class);
        listTitleActivityIntent.putExtra(ListTitleActivity.ARG_LIST_TITLE_JSON, listTitleJson);
        listTitleActivityIntent.putExtra(ListTitleActivity.ARG_MODE, ListTitleActivity.CREATE_NEW_LIST_TITLE);
        startActivity(listTitleActivityIntent);
    }

    @Override
    public void showProgress(String waitMessage) {
        Timber.i("showProgress()");
        manageListTitlesActivityProgressBar.setVisibility(View.VISIBLE);
        tvProgressBarMessage.setText(String.format("Please wait ...\n%s", waitMessage));
        manageListTitlesActivityContent.setVisibility(View.GONE);
    }

    @Override
    public void hideProgress(String message) {
        Timber.i("hideProgress()");
        manageListTitlesActivityProgressBar.setVisibility(View.GONE);
        manageListTitlesActivityContent.setVisibility(View.VISIBLE);
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
    public void displayAllListTitles(List<ListTitle> allListTitles) {
        // mPresenter's results
        mListTitleAdapter.setData(allListTitles);
        mListTitleAdapter.notifyDataSetChanged();
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
        getMenuInflater().inflate(R.menu.menu_activity_manage_list_titles, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_deleteListTitleStrikeouts) {
//            Toast.makeText(this, "deleteStruckOutListTitles Clicked", Toast.LENGTH_SHORT).show();
            if (mNumberOfStruckOutListTitles == 0) {
                String title = "";
                String msg = "No Lists selected for deletion.";
                CommonMethods.showOkDialog(this, title, msg);

            } else {
                String title = getResources().getQuantityString(R.plurals.deleteListTitlesYesNoDialogTitle,
                        mNumberOfStruckOutListTitles, mNumberOfStruckOutListTitles);
                String msg = getResources().getQuantityString(R.plurals.deleteListTitlesYesNoDialogMessage,
                        mNumberOfStruckOutListTitles, mNumberOfStruckOutListTitles);
                showDeleteListTitlesYesNoDialog(title, msg);
            }

            return true;
        } else if (id == R.id.action_listTitleSorting) {
            Toast.makeText(this, "action_listTitleSorting Clicked.", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteListTitlesYesNoDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", deleteListTitlesDialogClickListener)
                .setNegativeButton("No", deleteListTitlesDialogClickListener)
                .show();
    }


//    private void showProgressBar() {
//        Timber.i("showProgressBar()");
//        mainActivityProgressBar.setVisibility(View.VISIBLE);
//        tvProgressBarMessage.setText(String.format("Please wait ...\n%s", waitMessage));
//        tvProgressBarMessage.setVisibility(View.VISIBLE);
//        manageListTitlesActivityContent.setVisibility(View.GONE);
//
//
//
//        mainActivityProgressBar.setVisibility(View.VISIBLE);
//        tvProgressBarMessage.setVisibility(View.VISIBLE);
//        lvThemes.setVisibility(View.GONE);
//        // TODO: hide menus
//    }
//
//    private void hideProgressBar() {
//        Timber.i("hideProgressBar()");
//        mainActivityProgressBar.setVisibility(View.GONE);
//        tvProgressBarMessage.setVisibility(View.GONE);
//        lvThemes.setVisibility(View.VISIBLE);
//        // TODO: show menus
//    }

//    private void startLoginActivity() {
//        Intent intent = new Intent(this, BackendlessLoginActivity.class);
//        startActivity(intent);
//        finish();
//    }

    @Override
    public void onStruckOutListTitlesDeleted(String successMessage) {
        Timber.i("onStruckOutListTitlesDeleted(): %s.", successMessage);
        mNumberOfStruckOutListTitles = 0;
        mPresenter.resume();
        hideProgress("");
    }

    @Override
    public void onStruckOutListTitlesDeletionFailed(String errorMessage) {
        Timber.e("onStruckOutListTitlesDeleted(): %s.", errorMessage);
        mNumberOfStruckOutListTitles = mListTitleRepository.getNumberOfStruckOutListTitles();
        mPresenter.resume();
        hideProgress("");
    }

    @Override
    public void onListTitleBooleanFieldToggled(int toggleValue) {
        mNumberOfStruckOutListTitles += toggleValue;
//        Timber.i("onListTitleBooleanFieldToggled(): Number of struck out ListTitles = %d.", mNumberOfStruckOutListTitles);
    }

//    private class MessagePayload {
//        private String mCreationTime;
//        private String mAction;
//        private String mTableName;
//        private String mObjectUuid;
//
//        public MessagePayload(String action, String tableName, String objectUuid) {
//            this.mAction = action;
//            this.mTableName = tableName;
//            this.mObjectUuid = objectUuid;
//            mCreationTime = String.valueOf(System.currentTimeMillis());
//        }
//
//        public MessagePayload(String csvDataString) {
//            ArrayList<ArrayList<String>> records = CsvParser.CreateRecordAndFieldLists(csvDataString);
//            if (records.size() > 0) {
//                // load the first (and only) record.
//                ArrayList<String> record = records.get(0);
//                this.mAction = record.get(0);
//                this.mTableName = record.get(1);
//                this.mObjectUuid = record.get(2);
//                mCreationTime = record.get(3);
//            } else {
//                Timber.e("MessagePayload(): Unable to create MessagePayload. No data records found!");
//            }
//        }
//
//        @Override
//        public String toString() {
//            return String.valueOf(mAction + ": " + mTableName + ": " + mObjectUuid + "\n>> " + mCreationTime);
//        }
//
//        public String toCsvString() {
//            ArrayList<String> payload = new ArrayList<>();
//            payload.add(mAction);
//            payload.add(mTableName);
//            payload.add(mObjectUuid);
//            payload.add(mCreationTime);
//            return CsvParser.toCSVString(payload);
//        }
//    }
}
