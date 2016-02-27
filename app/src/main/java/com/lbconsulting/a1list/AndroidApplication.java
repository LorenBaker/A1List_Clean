package com.lbconsulting.a1list;

import android.app.Application;
import android.content.Context;

import com.backendless.Backendless;
import com.lbconsulting.a1list.utils.MySettings;

import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class AndroidApplication extends Application {

    private static Context mContext;

    private final String APP_ID ="E3C25B04-0237-343F-FF8D-4ACDD2199C00";
    private final String ANDROID_SECRET_KEY ="D631E44F-7983-D11D-FFE5-6C5541A09000";
    private final String APP_VERSION="v1";

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
    }

    public static Context getContext(){
        return mContext;
    }
}
