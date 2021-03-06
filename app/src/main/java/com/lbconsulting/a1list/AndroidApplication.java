package com.lbconsulting.a1list;

import android.app.Application;
import android.content.Context;

import com.backendless.Backendless;
import com.lbconsulting.a1list.domain.repositories.AppSettingsRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_Impl;
import com.lbconsulting.a1list.utils.MySettings;

import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class AndroidApplication extends Application {

    private static Context mContext;
    private static AppSettingsRepository_Impl mAppSettingsRepository;
    private static ListThemeRepository_Impl mListThemeRepository;
    private static ListTitleRepository_Impl mListTitleRepository;
    private static ListItemRepository_Impl mListItemRepository;

    private final String APP_ID = "D86918B7-C363-1B67-FF92-697761C42400";
    private final String ANDROID_SECRET_KEY = "D6553747-6D5B-F593-FF11-50D86C14DD00";
    private final String APP_VERSION = "v1";

    public static Context getContext() {
        return mContext;
    }

    public static AppSettingsRepository_Impl getAppSettingsRepository() {
        return mAppSettingsRepository;
    }

    public static ListThemeRepository_Impl getListThemeRepository() {
        return mListThemeRepository;
    }

    public static ListTitleRepository_Impl getListTitleRepository() {
        return mListTitleRepository;
    }

    public static ListItemRepository_Impl getListItemRepository() {
        return mListItemRepository;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        Backendless.initApp(this, APP_ID, ANDROID_SECRET_KEY, APP_VERSION);
        Timber.i("onCreate(): Backendless initialized");

        MySettings.setContext(mContext);

        // initiate Timber
        Timber.plant(new DebugTree() {
                         @Override
                         protected String createStackElementTag(StackTraceElement element) {
                             return super.createStackElementTag(element) + ":" + element.getLineNumber();
                         }
                     }
        );

        // TODO: Create release tree. See https://caster.io/episodes/episode-14-logging-with-timber/

        // Note: these repositories must be created in the following order
        mAppSettingsRepository = new AppSettingsRepository_Impl(mContext);
        mListThemeRepository = new ListThemeRepository_Impl(mContext);
        mListTitleRepository = new ListTitleRepository_Impl(mContext);
        mListItemRepository = new ListItemRepository_Impl(mContext);
    }
}
