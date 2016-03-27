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
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.ToggleListTitleBooleanField_InBackground;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
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
        ToggleListTitleBooleanField_InBackground.Callback {

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

    private AppSettingsRepository_Impl mAppSettingsRepository;
    private ListThemeRepository_Impl mListThemeRepository;
    private ListTitleRepository_Impl mListTitleRepository;
    private int mNumberOfStruckOutListTitles;


    //region Toggle Methods
    private DialogInterface.OnClickListener deleteListTitlesDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //The "Yes" button clicked
                    List<ListTitle> struckOutListTitles = mListTitleRepository.retrieveStruckOutListTitles();
                    if (struckOutListTitles.size() > 0) {
                        List<ListTitle> listTitlesMarkedForDeletion = mListTitleRepository
                                .deleteFromLocalStorage(struckOutListTitles);
                        mPresenter.resume();
                        hideProgress("");
                        if (listTitlesMarkedForDeletion.size() > 0) {
                            mListTitleRepository.deleteFromCloud(listTitlesMarkedForDeletion);
                        }
                    }

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //The "No" button clicked
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

        mListTitleAdapter = new ListTitleArrayAdapter(this, lvListTitles, true);
        lvListTitles.setAdapter(mListTitleAdapter);

        mAppSettingsRepository = AndroidApplication.getAppSettingsRepository();
        mListThemeRepository = AndroidApplication.getListThemeRepository();
        mListTitleRepository = AndroidApplication.getListTitleRepository();

        boolean isListTitlesSortedAlphabetically = true;
        AppSettings appSettings = mAppSettingsRepository.retrieveAppSettings();
        if (appSettings != null) {
            isListTitlesSortedAlphabetically = appSettings.isListTitlesSortedAlphabetically();
        }
        mPresenter = new ListTitlesPresenter_Impl(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, isListTitlesSortedAlphabetically);


        mNumberOfStruckOutListTitles = mListTitleRepository.retrieveNumberOfStruckOutListTitles();

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
        ListTitle newListTitle = ListTitle.newInstance(
                dialogEditListTitleName.DEFAULT_LIST_TITLE_NAME, defaultListTheme);
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


//    @Override
//    public void onStruckOutListTitlesDeleted(String successMessage) {
//        Timber.i("onStruckOutListTitlesDeleted(): %s.", successMessage);
//        mNumberOfStruckOutListTitles = 0;
//        mPresenter.resume();
//        hideProgress("");
//    }
//
//    @Override
//    public void onStruckOutListTitlesDeletionFailed(String errorMessage) {
//        Timber.e("onStruckOutListTitlesDeleted(): %s.", errorMessage);
//        mNumberOfStruckOutListTitles = mListTitleRepository.retrieveNumberOfStruckOutListTitles();
//        mPresenter.resume();
//        hideProgress("");
//    }

    @Override
    public void onListTitleBooleanFieldToggled(int toggleValue) {
        mNumberOfStruckOutListTitles += toggleValue;
    }

}
