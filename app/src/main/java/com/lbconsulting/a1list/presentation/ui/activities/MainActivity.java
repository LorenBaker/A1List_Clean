package com.lbconsulting.a1list.presentation.ui.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
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
import com.lbconsulting.a1list.domain.interactors.appSettings.SyncObjectsFromCloud;
import com.lbconsulting.a1list.domain.interactors.appSettings.SyncObjectsFromCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTheme.impl.CreateInitialListThemes_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.CreateInitialListThemes;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.model.ListTitleAndPosition;
import com.lbconsulting.a1list.domain.model.ListTitlePosition;
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
import com.lbconsulting.a1list.presentation.ui.dialogs.dialogNewListItem;
import com.lbconsulting.a1list.presentation.ui.dialogs.dialogSelectFavorites;
import com.lbconsulting.a1list.services.BackendlessMessagingService;
import com.lbconsulting.a1list.threading.MainThreadImpl;
import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.CsvParser;
import com.lbconsulting.a1list.utils.MyEvents;
import com.lbconsulting.a1list.utils.MySettings;
import com.lbconsulting.a1list.utils.SyncStats;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements ListTitlesPresenter.ListTitleView,
        CreateInitialListThemes.Callback,
        SyncObjectsFromCloud.Callback {
    private static final int NOTIFICATION_DOWNLOAD_ID = 33;
    private static ListTitlesPresenter_Impl mMainActivityPresenter;
    private static boolean mLoggingOut;
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
    private Subscription mSubscription;
    private AppSettingsRepository_Impl mAppSettingsRepository;
    private ListThemeRepository_Impl mListThemeRepository;
    private ListTitleRepository_Impl mListTitleRepository;
    private ListItemRepository_Impl mListItemRepository;
    private int mPosition;
    private ListTitle mActiveListTitle;
    private boolean mDownloadNotificationRunning;

    public static boolean isLoggingOut() {
        return mLoggingOut;
    }

    // TODO: Add share from Groupon
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
        mDownloadNotificationRunning = false;
        mLoggingOut = false;

        if (MySettings.getDeviceUuid().equals(MySettings.NOT_AVAILABLE)) {
            String newUuid = UUID.randomUUID().toString();
            // replace uuid "-" with "_" to distinguish it from Backendless objectId
            newUuid = newUuid.replace("-", "_");
            MySettings.setDeviceUuid(newUuid);
        }

        boolean isListTitlesSortedAlphabetically = true;
        AppSettings appSettings = mAppSettingsRepository.retrieveAppSettings();
        if (appSettings != null) {
            isListTitlesSortedAlphabetically = appSettings.isListTitlesSortedAlphabetically();
        }

        mMainActivityPresenter = new ListTitlesPresenter_Impl(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, isListTitlesSortedAlphabetically);

        Intent backendlessMessagingService = new Intent(this, BackendlessMessagingService.class);
        startService(backendlessMessagingService);

        showActiveUser();

    }

    @Subscribe
    public void onEvent(MyEvents.replaceListTitle event) {
        mSectionsPagerAdapter.replaceListTitle(event.getPosition(), event.getListTitle());
    }


    @Subscribe
    public void onEvent(MyEvents.showEditListItemDialog event) {
        Gson gson = new Gson();
        ListItem listItem = event.getListItem();
        String listItemJson = gson.toJson(listItem);
        dialogEditListItemName dialog = dialogEditListItemName.newInstance(listItemJson);
        dialog.show(getSupportFragmentManager(), "dialogEditListItemName");
    }

    @Subscribe
    public void onEvent(MyEvents.mainActivityPresenterResume event) {
        Timber.d("onEvent(): mainActivityPresenterResume");
        mainActivityPresenterResume();
    }

    @OnClick(R.id.fab)
    public void fab() {
        if (mActiveListTitle != null) {
            // Check if the ListTitle has been saved in Backendless
            String listTitleObjectID = mActiveListTitle.getObjectId();
            if (listTitleObjectID == null || listTitleObjectID.isEmpty()) {
                // The mActiveListTitle has not been saved to Backendless ... so
                // Retrieve the ListTitle from local storage.
                mActiveListTitle = mListTitleRepository.retrieveListTitleByUuid(mActiveListTitle.getUuid());
            }

            showNewListItemDialog(mActiveListTitle);
        } else {
            Timber.e("fab()onClick: Unable add ListItem because there is no ListTitle!");
        }

    }

    private void showNewListItemDialog(ListTitle listTitle) {
        FragmentManager fm = getSupportFragmentManager();
        Gson gson = new Gson();
        String listTitleJson = gson.toJson(listTitle);
        dialogNewListItem dialog = dialogNewListItem.newInstance(listTitleJson);
        dialog.show(fm, "dialogNewListItem");
    }


    @Override
    protected void onResume() {
        super.onResume();
        Timber.i("onResume()");

        if (MySettings.isStartedFromRegistrationActivity()) {
            initializeApp();
        } else {
            mainActivityPresenterResume();

            if (requiresSyncing()) {
                if (CommonMethods.isNetworkAvailable()) {
                    Timber.i("onResume(): Starting syncing objects from the Cloud.");
                    showDownLoadNotification();
                    new SyncObjectsFromCloud_InBackground(ThreadExecutor.getInstance(),
                            MainThreadImpl.getInstance(), this).execute();
                } else {
                    Timber.i("onResume(): Syncing objects from the Cloud required, but the network not available.");
                    // TODO: Implement a service to sync when the network becomes available.
                }
            } else {
                Timber.i("onResume(): Syncing from Cloud not required.");
            }
        }

    }

    private void mainActivityPresenterResume() {
        mMainActivityPresenter.resume();
    }

    private void showDownLoadNotification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this,
                MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
        notificationBuilder.setContentTitle("A1List");
        notificationBuilder.setContentText("Downloading data from the cloud.");
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        notificationBuilder.setTicker("Syncing A1List");
        notificationManager
                .notify(NOTIFICATION_DOWNLOAD_ID, notificationBuilder.build());
        mDownloadNotificationRunning = true;
    }

    private void cancelDownLoadNotification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this,
                MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
        notificationBuilder.setContentTitle("A1List");
        notificationBuilder.setContentText("Downloading data from the cloud.");
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        notificationBuilder.setTicker("Syncing A1List");
        notificationManager.cancelAll();
        mDownloadNotificationRunning = false;
    }

    private boolean requiresSyncing() {
        boolean result = false;
        long duration = System.currentTimeMillis() - MySettings.getLastTimeSynced();
        long requiredDuration = mAppSettingsRepository.retrieveTimeBetweenSynchronizations();
        if (requiredDuration > -1 && duration > requiredDuration) {
            result = true;
        }
        return result;
    }

    private void initializeApp() {
        Timber.i("initializeApp()");
//        showProgress("Loading initial Themes.");
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
//        hideProgress("");
    }

    @Override
    public void onPresenterAllListTitlesRetrieved(List<ListTitle> listTitles) {
        // Create the adapter that will return a fragListItems fragments
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), listTitles);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
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
        int position = 0;
        AppSettings appSettings = mAppSettingsRepository.retrieveAppSettings();
        if (appSettings != null) {
            String lastListTitleViewedUuid = appSettings.getLastListTitleViewedUuid();
            position = mSectionsPagerAdapter.getPosition(lastListTitleViewedUuid);
        }
        mViewPager.setCurrentItem(position);
        mActiveListTitle = mSectionsPagerAdapter.getListTitle(position);
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
        // TODO: If not logging out, start service that saves any dirty objects to Backendless
        super.onPause();
        Timber.i("onPause()");
        MySettings.setStartedFromRegistrationActivity(false);
        if (mDownloadNotificationRunning) {
            cancelDownLoadNotification();
        }
        if (mSectionsPagerAdapter != null) {
            ListTitle listTitle = mSectionsPagerAdapter.getListTitle(mPosition);
            if (listTitle != null) {
                AppSettings appSettings = mAppSettingsRepository.retrieveAppSettings();
                if (appSettings != null) {
                    appSettings.setLastListTitleViewedUuid(listTitle.getUuid());
                    mAppSettingsRepository.updateInStorage(appSettings);
                }
            }
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
//            Toast.makeText(this, "action_settings clicked", Toast.LENGTH_SHORT).show();
            return true;

        } else if (id == R.id.action_addTestLists) {
            // TODO: Remove action_addTestLists
            addTestLists();
            return true;

        } else if (id == R.id.action_addTestItems) {
            // TODO: Remove action_addTestItems
            List<ListTitle> listTitles = mListTitleRepository.retrieveAllListTitles(false, true);
            addItemsToListTitles(listTitles);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //region Add Test Lists with Test Items
    // TODO: Remove addTestLists
    private void addTestLists() {
        int numberOfLists = 5;
        List<ListTitle> listTitles = new ArrayList<>();
        List<ListTitleAndPosition> listTitleAndPositions = new ArrayList<>();

        ListTheme defaultListTheme;

        ListTitle newListTitle;

        Timber.i("addTestLists(): Starting to add test lists.");
        for (int i = 1; i < numberOfLists + 1; i++) {
            defaultListTheme = mListThemeRepository.retrieveDefaultListTheme();
            String listNumber = String.valueOf(i);
            if (i < 10) {
                listNumber = "0" + listNumber;
            }
            String listName = "List " + listNumber;
            newListTitle = ListTitle.newInstance(listName, defaultListTheme);
            ListTitlePosition newListTitlePosition = ListTitlePosition.newInstance(newListTitle.getUuid());
            newListTitle.setListTitlePositionUuid(newListTitlePosition.getUuid());
            listTitles.add(newListTitle);
            ListTitleAndPosition newPosition = new ListTitleAndPosition(newListTitle, newListTitlePosition);
            listTitleAndPositions.add(newPosition);
        }
        mListTitleRepository.insertIntoStorage(listTitles);
        mListTitleRepository.insertListTitlePositions(listTitleAndPositions);
        AppSettings dirtyAppSettings = mAppSettingsRepository.retrieveDirtyAppSettings();
        if (dirtyAppSettings != null) {
            mAppSettingsRepository.updateInCloud(dirtyAppSettings, false);
        }
    }


    private void addItemsToListTitles(List<ListTitle> listTitles) {
        // TODO: Remove addItemsToListTitles
        Timber.i("addItemsToListTitles(): Starting to add test ListItems.");
        int numberOfItemsPerList = 5;
        ListItem newListItem;
        List<ListItem> listItems = new ArrayList<>();
        String listTitleName;
        for (ListTitle listTitle : listTitles) {
            listTitleName = listTitle.getName();
            Timber.i("addItemsToListTitles(): adding items to \"%s\"", listTitle.getName());
            for (int i = 1; i < numberOfItemsPerList + 1; i++) {
                String itemNumber = String.valueOf(i);
                if (i < 10) {
                    itemNumber = "0" + itemNumber;
                }
                String itemName = listTitleName + ": Item " + itemNumber;
                newListItem = ListItem.newInstance(itemName, listTitle, false);
                listItems.add(newListItem);
            }
            numberOfItemsPerList++;
        }
        mListItemRepository.insertIntoStorage(listItems);

        mMainActivityPresenter.resume();

    }


    //endregion

    private void deleteStruckOutItems() {
        List<ListItem> struckOutListItems = mListItemRepository.retrieveStruckOutListItems(mActiveListTitle);
        Timber.i("deleteStruckOutItems(): Found %d struck out ListItems to deleteFromStorage.", struckOutListItems.size());
        if (struckOutListItems.size() > 0) {
            mListItemRepository.deleteFromStorage(struckOutListItems);
            EventBus.getDefault().post(new MyEvents.updateFragListItemsUI(mActiveListTitle.getUuid()));
        }
    }


    private void showFavorites() {
        FragmentManager fm = getSupportFragmentManager();
        Gson gson = new Gson();
        String listTitleJson = gson.toJson(mActiveListTitle);
        dialogSelectFavorites dialog = dialogSelectFavorites.newInstance(listTitleJson);
        dialog.show(fm, "dialogSelectFavorites");
    }

    private void createNewList() {
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
//        Toast.makeText(this, "refresh clicked", Toast.LENGTH_SHORT).show();
        showDownLoadNotification();
        new SyncObjectsFromCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this).execute();
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
                    String msg = String.format("User \"%s\" logged out.", MySettings.getActiveUserName());
                    Timber.i("logoutUser(): %s", msg);
                    CommonMethods.showSnackbar(mFab, msg, Snackbar.LENGTH_LONG);
                    MySettings.resetActiveUserAndEmail();
                    clearAllDatabaseTables();
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

    private void clearAllDatabaseTables() {
        AndroidApplication.getAppSettingsRepository().clearAllData();
        AndroidApplication.getListThemeRepository().clearAllData();
        AndroidApplication.getListTitleRepository().clearAllData();
        AndroidApplication.getListItemRepository().clearAllData();
        mLoggingOut = true;
    }

    private void showPreferencesActivity() {
        Toast.makeText(this, "action_settings clicked", Toast.LENGTH_SHORT).show();

    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, BackendlessLoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSyncObjectsFromCloudSuccess(String successMessage, SyncStats syncStats) {
//        CommonMethods.showOkDialog(this, "Sync Stats", successMessage);
        EventBus.getDefault().post(new MyEvents.updateFragListItemsUI(null));
        CommonMethods.showSnackbar(mFab, syncStats.getSnackBarMessage(), Snackbar.LENGTH_LONG);
        cancelDownLoadNotification();
        mMainActivityPresenter.resume();
    }

    @Override
    public void onSyncObjectsFromCloudFailed(String errorMessage, SyncStats syncStats) {
        CommonMethods.showOkDialog(this, "Sync Stats", errorMessage);
        cancelDownLoadNotification();
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
