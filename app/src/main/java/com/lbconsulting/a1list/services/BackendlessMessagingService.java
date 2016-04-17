package com.lbconsulting.a1list.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import com.backendless.Backendless;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.backendlessMessaging.AppSettingsMessage;
import com.lbconsulting.a1list.backendlessMessaging.ListItemMessage;
import com.lbconsulting.a1list.backendlessMessaging.ListThemeMessage;
import com.lbconsulting.a1list.backendlessMessaging.ListTitleMessage;
import com.lbconsulting.a1list.backendlessMessaging.Messaging;
import com.lbconsulting.a1list.domain.model.AppSettings;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.utils.MyEvents;
import com.lbconsulting.a1list.utils.MySettings;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import timber.log.Timber;

/**
 * This service listens for Backendless messages.
 * Once a message is received, it updates the local SQLite database storage.
 */
public class BackendlessMessagingService extends Service {

    private static final int INTERVAL_FAST = 30000; // 30 seconds
    private static final int INTERVAL_SLOW = 15 * 60000; // 15 minutes
    private static final int MAX_NUMBER_OF_FAST_SLEEP_INTERVALS = 60;

    private final String APP_SETTINGS_MESSAGE_PREFIX = "\"appSettings\":{";
    private final String LIST_THEME_MESSAGE_PREFIX = "\"listTheme\":{";
    private final String LIST_TITLE_MESSAGE_PREFIX = "\"listTitle\":{";
    private final String LIST_ITEM_MESSAGE_PREFIX = "\"listItem\":{";

    private ServiceHandler mServiceHandler;

    private volatile boolean mSeekingNetwork = true;
    private volatile boolean mStarted = false;
    private volatile int mInterval;
//    private volatile boolean mStoppedSelf;

    private volatile int mNumberOfSleepIntervals;
    private Subscription mSubscription;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Timber.i("onBind()");
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.i("onCreate()");
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.i("onDestroy()");
        if (mSubscription != null) {
            mSubscription.cancelSubscription();
        }
//        if (mStoppedSelf) {
//            MyLog.i("UploadDirtyObjectsService", "onDestroy: Stopped self.");
//        } else {
//            MyLog.i("UploadDirtyObjectsService", "onDestroy: Stopped by the Operating System.");
//        }
        mSeekingNetwork = false;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.i("onStartCommand()");
        try {
            if (!mStarted) {
                mInterval = INTERVAL_FAST;
                mStarted = true;

                // For each start request, send a message to start a job and deliver the
                // start ID so we know which request we're stopping when we finish the job
                Message msg = mServiceHandler.obtainMessage();
                msg.arg1 = startId;
                mServiceHandler.sendMessage(msg);
            }

        } catch (Exception e) {
            Timber.e("onStartCommand(): Exception: %s.", e.getMessage());
        }

        // TODO: Dow we want to START_STICKY ?
        // If we get killed, after returning from here, restart
        return Service.START_STICKY;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            int startID = msg.arg1;
            Timber.i("handleMessage(): Started background thread with StartID = %d.", startID);
            while (mSeekingNetwork) {
                synchronized (this) {
                    while (mSeekingNetwork) {
                        if (isNetworkAvailable()) {
                            mInterval = INTERVAL_FAST;
                            mNumberOfSleepIntervals = 0;
                            mSeekingNetwork = false;
                            subscribeToBackendlessMessaging();
                        } else {
                            try {
                                mNumberOfSleepIntervals++;
                                if (mNumberOfSleepIntervals > MAX_NUMBER_OF_FAST_SLEEP_INTERVALS) {
                                    // The network has not been available for a long time ... increase the sleep interval
                                    mInterval = INTERVAL_SLOW;
                                }
                                Timber.i("handleMessage(): %d SLEEPING %d seconds.", mNumberOfSleepIntervals, mInterval / 1000);
                                Thread.sleep(mInterval);
                            } catch (InterruptedException e) {
                                Timber.e("handleMessage(): InterruptedException: %s.", e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        private void subscribeToBackendlessMessaging() {

            final String messageChannel = MySettings.getActiveUserID();
            Backendless.Messaging.subscribe(messageChannel,
                    new AsyncCallback<List<com.backendless.messaging.Message>>() {
                        @Override
                        public void handleResponse(List<com.backendless.messaging.Message> response) {
                            for (com.backendless.messaging.Message message : response) {
                                String messageJasonString = message.getData().toString();

                                if (messageJasonString.contains(LIST_ITEM_MESSAGE_PREFIX)) {
                                    ListItemMessage listItemMessage = ListItemMessage.fromJason(messageJasonString);
                                    if (!listItemMessage.getListItem().getDeviceUuid().equals(MySettings.getDeviceUuid())) {
                                        // The message is not from this device ... so update local storage
                                        Timber.i("handleResponse(): Processing ListItem message with id = %s", message.getMessageId());
                                        ListItem listItem = listItemMessage.getListItem();
                                        switch (listItemMessage.getAction()) {
                                            case Messaging.ACTION_CREATE:
                                                AndroidApplication.getListItemRepository().insertIntoLocalStorage(listItem);
                                                break;
                                            case Messaging.ACTION_UPDATE:
                                                AndroidApplication.getListItemRepository().updateInLocalStorage(listItem);
                                                break;
                                            case Messaging.ACTION_DELETE:
                                                AndroidApplication.getListItemRepository().delete(listItem);
                                                break;
                                        }
                                        EventBus.getDefault().post(new MyEvents.updateFragListItemsUI(listItem.getListTitleUuid()));
                                    } else {
                                        Timber.i("handleResponse(): Received ListItem message with id = %s. Taking NO ACTION because it was sourced from this device.", message.getMessageId());
                                    }

                                } else if (messageJasonString.contains(LIST_TITLE_MESSAGE_PREFIX)) {
                                    ListTitleMessage listTitleMessage = ListTitleMessage.fromJason(messageJasonString);
                                    if (!listTitleMessage.getListTitle().getDeviceUuid().equals(MySettings.getDeviceUuid())) {
                                        // The message is not from this device ... so update local storage
                                        Timber.i("handleResponse(): Processing ListTitle message with id = %s", message.getMessageId());
                                        ListTitle listTitle = listTitleMessage.getListTitle();
                                        switch (listTitleMessage.getAction()) {
                                            case Messaging.ACTION_CREATE:
                                                AndroidApplication.getListTitleRepository().insertIntoLocalStorage(listTitle);
                                                break;
                                            case Messaging.ACTION_UPDATE:
                                                AndroidApplication.getListTitleRepository().updateInLocalStorage(listTitle);
                                                break;
                                            case Messaging.ACTION_DELETE:
                                                AndroidApplication.getListTitleRepository().delete(listTitle);
                                                break;
                                        }
                                        // TODO: Figure out what ui to update with a ListTitle change
//                                        EventBus.getDefault().post(new MyEvents.mainActivityPresenterResume());
                                    } else {
                                        Timber.i("handleResponse(): Received ListTitle message with id = %s. Taking NO ACTION because it was sourced from this device.", message.getMessageId());
                                    }

                                } else if (messageJasonString.contains(LIST_THEME_MESSAGE_PREFIX)) {
                                    ListThemeMessage listThemeMessage = ListThemeMessage.fromJason(messageJasonString);
                                    if (!listThemeMessage.getListTheme().getDeviceUuid().equals(MySettings.getDeviceUuid())) {
                                        // The message is not from this device ... so update local storage
                                        Timber.i("handleResponse(): Processing ListTheme message with id = %s", message.getMessageId());
                                        ListTheme listTheme = listThemeMessage.getListTheme();
                                        switch (listThemeMessage.getAction()) {
                                            case Messaging.ACTION_CREATE:
                                                AndroidApplication.getListThemeRepository().insertIntoLocalStorage(listTheme);
                                                break;
                                            case Messaging.ACTION_UPDATE:
                                                AndroidApplication.getListThemeRepository().updateInLocalStorage(listTheme);
                                                break;
                                            case Messaging.ACTION_DELETE:
                                                AndroidApplication.getListThemeRepository().delete(listTheme);
                                                break;
                                        }
                                        // TODO: Figure out what ui to update with a ListTheme change
//                                        EventBus.getDefault().post(new MyEvents.mainActivityPresenterResume());
                                    } else {
                                        Timber.i("handleResponse(): Received ListTheme message with id = %s. Taking NO ACTION because it was sourced from this device.", message.getMessageId());
                                    }

                                } else if (messageJasonString.contains(APP_SETTINGS_MESSAGE_PREFIX)) {
                                    AppSettingsMessage appSettingsMessage = AppSettingsMessage.fromJason(messageJasonString);
                                    if (!appSettingsMessage.getAppSettings().getDeviceUuid().equals(MySettings.getDeviceUuid())) {
                                        // The message is not from this device ... so update local storage
                                        Timber.i("handleResponse(): Processing AppSettings message with id = %s", message.getMessageId());
                                        AppSettings appSettings = appSettingsMessage.getAppSettings();
                                        switch (appSettingsMessage.getAction()) {
                                            case Messaging.ACTION_CREATE:
                                                AndroidApplication.getAppSettingsRepository().insertIntoLocalStorage(appSettings);
                                                break;
                                            case Messaging.ACTION_UPDATE:
                                                AndroidApplication.getAppSettingsRepository().updateInLocalStorage(appSettings);
                                                break;
                                            case Messaging.ACTION_DELETE:
                                                Timber.e("handleResponse(): Cannot delete AppSettings!");
//                                                AndroidApplication.getAppSettingsRepository().delete(appSettings);
                                                break;
                                        }
                                        // TODO: Figure out what ui to update with a AppSettings change
//                                        EventBus.getDefault().post(new MyEvents.updateFragAppSettingssUI(appSettings.getListTitleUuid()));
                                    } else {
                                        Timber.i("handleResponse(): Received AppSettings message with id = %s. Taking NO ACTION because it was sourced from this device.", message.getMessageId());
                                    }
                                } else {
                                    Timber.e("handleResponse(): Unknown message type. Json = %s", messageJasonString);
                                }

                            }
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Timber.e("handleFault(): BackendlessFault: %s.", fault.getMessage());
                        }
                    }, new AsyncCallback<Subscription>() {

                        @Override
                        public void handleResponse(Subscription response) {
                            mSubscription = response;
                            Timber.i("handleResponse(): Successfully subscribed to messageChannel = %s", messageChannel);
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Timber.e("handleFault(): BackendlessFault: %s.", fault.getMessage());
                        }
                    }
            );

        }


        private boolean isNetworkAvailable() {

            boolean networkAvailable = false;
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();

            if ((ni != null) && (ni.isConnected())) {
                // We have a network connection
                networkAvailable = true;
            }
            return networkAvailable;
        }
    }
}
