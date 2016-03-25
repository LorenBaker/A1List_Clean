package com.lbconsulting.a1list.presentation.ui.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.gson.Gson;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.appSettings.SaveDirtyObjectsToBackendless_InBackground;
import com.lbconsulting.a1list.domain.interactors.listItem.impl.DeleteStruckOutListItems_InBackground;
import com.lbconsulting.a1list.domain.interactors.listItem.impl.UpdateListItem_InBackground;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.DeleteStruckOutListItems;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.InsertNewListItem;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.SaveListItemListToBackendless;
import com.lbconsulting.a1list.domain.interactors.listItem.interactors.UpdateListItem;
import com.lbconsulting.a1list.domain.interactors.listTheme.impl.CreateInitialListThemes_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.CreateInitialListThemes;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.SaveListTitleListToBackendless_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.InsertNewListTitle;
import com.lbconsulting.a1list.domain.interactors.listTitle.interactors.SaveListTitleListToBackendless;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.AppSettingsRepository;
import com.lbconsulting.a1list.domain.repositories.AppSettingsRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_Impl;
import com.lbconsulting.a1list.presentation.presenters.impl.ListTitlesPresenter_Impl;
import com.lbconsulting.a1list.presentation.presenters.interfaces.ListTitlesPresenter;
import com.lbconsulting.a1list.presentation.ui.activities.backendless.BackendlessLoginActivity;
import com.lbconsulting.a1list.presentation.ui.adapters.SectionsPagerAdapter;
import com.lbconsulting.a1list.presentation.ui.dialogs.dialogEditListItemName;
import com.lbconsulting.a1list.presentation.ui.dialogs.dialogEditListTitleName;
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

public class MainActivity extends AppCompatActivity implements ListTitlesPresenter.ListTitleView,
        CreateInitialListThemes.Callback, InsertNewListTitle.Callback, InsertNewListItem.Callback,
        SaveListTitleListToBackendless.Callback,
        SaveListItemListToBackendless.Callback, DeleteStruckOutListItems.Callback, UpdateListItem.Callback {
    private static ListTitlesPresenter_Impl mMainActivityPresenter;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.listItemsViewPager)
    ViewPager mViewPager;
    @Bind(R.id.tabs)
    TabLayout mTabLayout;
    @Bind(R.id.activityProgressBar)
    View mainActivityProgressBar;
    @Bind(R.id.tvActivityProgressBarMessage)
    TextView tvProgressBarMessage;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private String MESSAGE_CHANNEL = "";
    private Subscription mSubscription;

    private AppSettingsRepository_Impl mAppSettingsRepository;
    private ListThemeRepository_Impl mListThemeRepository;
    private ListTitleRepository_Impl mListTitleRepository;
    private ListItemRepository_Impl mListItemRepository;
    private int mPosition;
    private ListTitle mActiveListTitle;

    // TODO: Add share from groupon
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

        setSupportActionBar(mToolbar);
        mAppSettingsRepository = AndroidApplication.getAppSettingsRepository();
        mListThemeRepository = AndroidApplication.getListThemeRepository();
        mListTitleRepository = AndroidApplication.getListTitleRepository();
        mListItemRepository = AndroidApplication.getListItemRepository();

        boolean isListTitlesSortedAlphabetically = true;
        AppSettings appSettings = mAppSettingsRepository.retrieveAppSettings();
        if (appSettings != null) {
            isListTitlesSortedAlphabetically = appSettings.isListTitlesSortedAlphabetically();
        }

        mMainActivityPresenter = new ListTitlesPresenter_Impl(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, isListTitlesSortedAlphabetically);


        //region Messaging
//        MESSAGE_CHANNEL = MySettings.getActiveUserID();
//        Backendless.Messaging.subscribe(MESSAGE_CHANNEL,
//                new AsyncCallback<List<Message>>() {
//                    @Override
//                    public void handleResponse(List<Message> response) {
//                        for (Message message : response) {
//                            // TODO: ignore messages initiated from this device
//                            // TODO: design message payload. Make changes to SQLite db.
//                            String csvDataString = message.getData().toString();
//                            MessagePayload payload = new MessagePayload(csvDataString);
////                            tvMessagesReceived.setText(tvMessagesReceived.getText() + "\n\n" + payload.toString());
//                        }
//                    }
//
//                    @Override
//                    public void handleFault(BackendlessFault fault) {
//                        Timber.e("Backendless.Messaging.subscribe()MessageCallback: BackendlessFault: %s", fault.getMessage());
//                    }
//                }, new AsyncCallback<Subscription>() {
//
//                    @Override
//                    public void handleResponse(Subscription response) {
//                        Timber.i("Backendless. Successful messaging.subscribe()SubscriptionCallback");
//                        mSubscription = response;
//                    }
//
//                    @Override
//                    public void handleFault(BackendlessFault fault) {
//                        Timber.e("Backendless.FAIL messaging.subscribe()SubscriptionCallback: BackendlessFault: %s", fault.getMessage());
//
//                    }
//                }
//        );
        //endregion


        showActiveUser();
    }

    @Subscribe
    public void onEvent(MyEvents.replaceListTitle event) {
        mSectionsPagerAdapter.replaceListTitle(event.getPosition(), event.getListTitle());
    }

    @Subscribe
    public void onEvent(MyEvents.updateListItem event) {
        new UpdateListItem_InBackground(ThreadExecutor.getInstance(), MainThreadImpl.getInstance(),
                this, event.getListItem()).execute();
    }

    @Subscribe
    public void onEvent(MyEvents.showEditListItemDialog event) {
        Gson gson = new Gson();
        String listItemJson = gson.toJson(event.getListItem());
        dialogEditListItemName dialog = dialogEditListItemName.newInstance(listItemJson);
        dialog.show(getSupportFragmentManager(), "dialogEditListItemName");
    }

    @Override
    public void onListItemUpdated(String successMessage) {
        Timber.i("onListItemUpdated(): %s.", successMessage);
        EventBus.getDefault().post(new MyEvents.updateFragListItemsUI(mActiveListTitle.getUuid()));
    }

    @Override
    public void onListItemUpdateFailed(String errorMessage) {
        Timber.e("onListItemUpdated(): %s.", errorMessage);
        EventBus.getDefault().post(new MyEvents.updateFragListItemsUI(mActiveListTitle.getUuid()));
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
            initializeApp();
        } else {
            mMainActivityPresenter.resume();
        }

    }

    private void initializeApp() {
        Timber.i("initializeApp()");
        showProgress("Loading initial Themes.");
        new CreateInitialListThemes_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this).execute();
    }

    @Override
    public void onInitialListThemesCreated(String message) {
        Timber.i("onInitialListThemesCreated(): %s", message);
        mMainActivityPresenter.resume();
        CommonMethods.showSnackbar(mFab, message, Snackbar.LENGTH_LONG);
        // TODO: show welcome dialog
    }

    @Override
    public void onListThemesCreationFailed(String errorMessage) {
        Timber.e("onListThemesCreationFailed(): %s.", errorMessage);
        hideProgress("");
    }

    @Override
    public void displayAllListTitles(List<ListTitle> listTitles) {
        // Create the adapter that will return a fragListItems fragments
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), listTitles);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // Update Active ListTitle;
                mPosition = position;
                mActiveListTitle = mSectionsPagerAdapter.getListTitle(position);
                if (mActiveListTitle != null) {
                    mToolbar.setTitle(mActiveListTitle.getName());
                    Timber.i("onPageSelected(): position = %d: %s", position, mActiveListTitle.getName());
                } else {
                    Timber.e("onPageSelected(): Unable to find ListTitle at position = %d", position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabLayout.setupWithViewPager(mViewPager);

        AppSettings appSettings = mAppSettingsRepository.retrieveAppSettings();
        String lastListTitleViewedUuid = appSettings.getLastListTitleViewedUuid();
        int position = mSectionsPagerAdapter.getPosition(lastListTitleViewedUuid);
        mViewPager.setCurrentItem(position);

    }

    @Override
    public void showProgress(String waitMessage) {
        mainActivityProgressBar.setVisibility(View.VISIBLE);
        tvProgressBarMessage.setText(String.format("Please wait ...\n%s", waitMessage));
        mViewPager.setVisibility(View.GONE);
        // TODO: hide menus
    }

    @Override
    public void hideProgress(String message) {
        mainActivityProgressBar.setVisibility(View.GONE);
        mViewPager.setVisibility(View.VISIBLE);
        // TODO: show menus
    }

    @Override
    public void showError(String message) {
        Timber.e("showError(): %s.", message);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Timber.i("onPause()");
        MySettings.setStartedFromRegistrationActivity(false);
        if (mSectionsPagerAdapter != null) {
            ListTitle listTitle = mSectionsPagerAdapter.getListTitle(mPosition);
            AppSettings appSettings = mAppSettingsRepository.retrieveAppSettings();
            appSettings.setLastListTitleViewedUuid(listTitle.getUuid());
            mAppSettingsRepository.setLastListTitleViewedUuid(appSettings, listTitle.getUuid());
        }
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

        } else if (id == R.id.action_addTestLists) {
            addTestLists();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //region Add Test Lists with Test Items
    // TODO: Remove addTestLists
    private void addTestLists() {
        int numberOfLists = 10;
        List<ListTitle> listTitles = new ArrayList<>();

        ListTheme defaultListTheme;

        ListTitleRepository_Impl listTitleRepository = AndroidApplication.getListTitleRepository();
        ListTitle newListTitle;

        Timber.i("addTestLists(): Starting to add test lists.");
        for (int i = 1; i < numberOfLists + 1; i++) {
            defaultListTheme = mListThemeRepository.retrieveDefaultListTheme();
            String listNumber = String.valueOf(i);
            if (i < 10) {
                listNumber = "0" + listNumber;
            }
            String listName = "List " + listNumber;
            Timber.i("addTestLists(): adding \"%s\"", listName);
            newListTitle = ListTitle.newInstance(listName, defaultListTheme, AndroidApplication.getAppSettingsRepository());
            listTitles.add(newListTitle);
        }
        mListTitleRepository.insertIntoSQLiteDb(listTitles);

        List<ListTitle> allListTitles = listTitleRepository.retrieveAllListTitles(false, true);
        new SaveListTitleListToBackendless_InBackground(ThreadExecutor.getInstance(), MainThreadImpl.getInstance(), this, allListTitles).execute();
    }

    @Override
    public void onListTitleListSavedToBackendless(String successMessage, List<ListTitle> successfullySavedListTitles) {
        Timber.i("onListTitleListSavedToBackendless(): %s.", successMessage);
        Timber.i("addTestLists(): Starting to add test items to %d ListTitles.", successfullySavedListTitles.size());
        addItemsToListTitles(successfullySavedListTitles);
    }

    @Override
    public void onListTitleListSaveToBackendlessFailed(String errorMessage, List<ListTitle> successfullySavedListTitles) {
        Timber.e("onListTitleListSavedToBackendless(): %s.", errorMessage);
        Timber.i("addTestLists(): Starting to add test items to %d ListTitles.", successfullySavedListTitles.size());
        addItemsToListTitles(successfullySavedListTitles);
    }

    private void addItemsToListTitles(List<ListTitle> listTitles) {
        int numberOfItemsPerList = 5;
        ListItem newListItem;
        List<ListItem> listItems = new ArrayList<>();
        String listTitleName;
        for (ListTitle listTitle : listTitles) {
            numberOfItemsPerList++;
            listTitleName = listTitle.getName();
            Timber.i("addItemsToListTitles(): adding items to \"%s\"", listTitle.getName());
            for (int i = 1; i < numberOfItemsPerList + 1; i++) {
                String itemNumber = String.valueOf(i);
                if (i < 10) {
                    itemNumber = "0" + itemNumber;
                }
                String itemName = listTitleName + ": Item " + itemNumber;
                Timber.i("addTestLists(): adding to \"%s\"", itemName);
                newListItem = ListItem.newInstance(itemName, listTitle, mListTitleRepository, false);
                listItems.add(newListItem);
            }
        }
        mListItemRepository.insertIntoSQLiteDb(listItems);
        Timber.i("addItemsToListTitles(): Starting to save dirty objects to Backendless.");
        new SaveDirtyObjectsToBackendless_InBackground(ThreadExecutor.getInstance(), MainThreadImpl.getInstance()).execute();
//        new SaveListItemListToBackendless_InBackground(ThreadExecutor.getInstance(),
//                MainThreadImpl.getInstance(), listItems, this).execute();
    }

    @Override
    public void onListItemListSavedToBackendless(String successMessage, List<ListItem> successfullySavedListItems) {
        Timber.e("onListItemListSavedToBackendless(): %s.", successMessage);
        mMainActivityPresenter.resume();
    }

    @Override
    public void onListItemListSaveToBackendlessFailed(String errorMessage, List<ListItem> successfullySavedListItems) {
        Timber.e("onListItemListSaveToBackendlessFailed(): %s.", errorMessage);
        mMainActivityPresenter.resume();
    }

    @Override
    public void onListTitleInsertedIntoSQLiteDb(String successMessage) {
        Timber.i("onListTitleInsertedIntoSQLiteDb(): %s.", successMessage);
    }

    @Override
    public void onListTitleInsertionIntoSQLiteDbFailed(String errorMessage) {
        Timber.e("onListTitleInsertionIntoSQLiteDbFailed(): %s.", errorMessage);
    }

    @Override
    public void onListItemInsertedIntoSQLiteDb(String successMessage) {
        Timber.i("onListItemInsertedIntoSQLiteDb(): %s.", successMessage);
    }

    @Override
    public void onListItemInsertionIntoSQLiteDbFailed(String errorMessage) {
        Timber.e("onListItemInsertionIntoSQLiteDbFailed(): %s.", errorMessage);
    }
    //endregion

    private void deleteStruckOutItems() {
        new DeleteStruckOutListItems_InBackground(ThreadExecutor.getInstance(), MainThreadImpl.getInstance(),
                this, mListItemRepository, mActiveListTitle).execute();
    }

    @Override
    public void onStruckOutListItemsDeleted(String successMessage) {
        Timber.i("onStruckOutListItemsDeleted(): %s.", successMessage);
        EventBus.getDefault().post(new MyEvents.updateFragListItemsUI(mActiveListTitle.getUuid()));
    }

    @Override
    public void onStruckOutListItemsDeletionFailed(String errorMessage) {
        Timber.e("onStruckOutListItemsDeleted(): %s.", errorMessage);
        EventBus.getDefault().post(new MyEvents.updateFragListItemsUI(mActiveListTitle.getUuid()));
    }

    private void showFavorites() {
        Toast.makeText(this, "showFavorites clicked", Toast.LENGTH_SHORT).show();
    }

    private void createNewList() {
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

    private void showListSortingDialogue() {
        Toast.makeText(this, "showListSortingDialogue clicked", Toast.LENGTH_SHORT).show();
    }

    private void editListTheme() {
        Toast.makeText(this, "editListTheme clicked", Toast.LENGTH_SHORT).show();
    }

    private void showManageListActivity() {
        Intent intent = new Intent(this, ManageListTitlesActivity.class);
        startActivity(intent);
    }

    private void showManageListThemesActivity() {
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
