package com.lbconsulting.a1list.domain.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import timber.log.Timber;


public class A1List_DatabaseHelper extends SQLiteOpenHelper {

    private static Context mContext;

    private static final String DATABASE_NAME = "A1List.db";
    private static final int DATABASE_VERSION = 1;

    private static SQLiteDatabase dBase;

    public A1List_DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        A1List_DatabaseHelper.dBase = database;
        Timber.i("onCreate()");
        ListThemeSqlTable.onCreate(database, mContext);

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Timber.i("onUpgrade()");
        ListItemsSqlTable.onUpgrade(database, oldVersion, newVersion, mContext);

    }

    public static SQLiteDatabase getDatabase() {
        return dBase;
    }

}