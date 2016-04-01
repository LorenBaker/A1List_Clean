package com.lbconsulting.a1list.domain.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import timber.log.Timber;

public class AppSettingsSqlTable {


    public static final long SYNC_ALWAYS = 0;
    public static final long SYNC_NEVER = -1;
    public static final long SYNC_ONE_HOUR = 3600000;
    public static final long SYNC_TWO_HOURS = 7200000;
    public static final long SYNC_FOUR_HOURS = 14400000;
    public static final long SYNC_SIX_HOURS = 21600000;
    public static final long SYNC_TWENTY_FOUR_HOURS = 172800000;

    // Version 1
    public static final String TABLE_APP_SETTINGS = "tblAppSettings";
    public static final String COL_ID = "_id";
    public static final String COL_UUID = "uuid";
    public static final String COL_OBJECT_ID = "objectId";
    public static final String COL_NAME = "name";
    public static final String COL_APP_SETTINGS_DIRTY = "appSettingsDirty";
    public static final String COL_TIME_BETWEEN_SYNCHRONIZATIONS = "timeBetweenSynchronizations";
    public static final String COL_LIST_TITLE_LAST_SORT_KEY = "listTitleLastSortKey";
    public static final String COL_LIST_TITLES_SORTED_ALPHABETICALLY = "listTitlesSortedAlphabetically";
    public static final String COL_LAST_LIST_TITLE_VIEWED_UUID = "lastListTitleViewedUuid";
    public static final String COL_UPDATED = "updated";

    //region Projections and Content Path
    public static final String[] PROJECTION_ALL = {COL_ID, COL_UUID, COL_OBJECT_ID, COL_NAME,
            COL_APP_SETTINGS_DIRTY, COL_TIME_BETWEEN_SYNCHRONIZATIONS, COL_LIST_TITLE_LAST_SORT_KEY,
            COL_LIST_TITLES_SORTED_ALPHABETICALLY, COL_LAST_LIST_TITLE_VIEWED_UUID, COL_UPDATED};

    public static final String CONTENT_PATH = "appSettings";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
            + CONTENT_PATH;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
            + CONTENT_PATH;
    public static final Uri CONTENT_URI = Uri.parse("content://" + A1List_ContentProvider.AUTHORITY + "/"
            + CONTENT_PATH);

    //endregion

    // Database creation SQL statements
    private static final String DATA_TABLE_CREATE =
            "create table " + TABLE_APP_SETTINGS
                    + " ("
                    + COL_ID + " integer primary key autoincrement, "
                    + COL_UUID + " text default '', "
                    + COL_OBJECT_ID + " text default '', "
                    + COL_NAME + " text collate nocase default '', "
                    + COL_APP_SETTINGS_DIRTY + " integer default 0, "
                    + COL_TIME_BETWEEN_SYNCHRONIZATIONS + " integer default 0, "
                    + COL_LIST_TITLE_LAST_SORT_KEY + " integer default 0, "
                    + COL_LIST_TITLES_SORTED_ALPHABETICALLY + " integer default 1, "
                    + COL_LAST_LIST_TITLE_VIEWED_UUID + " text default '', "
                    + COL_UPDATED + " integer default 0"
                    + ");";

    public static void onCreate(SQLiteDatabase database, Context context) {
        database.execSQL(DATA_TABLE_CREATE);
        Timber.i("onCreate(): %s created.", TABLE_APP_SETTINGS);

    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion, Context context) {
        Timber.w("onUpgrade(): %s: Upgrading database from version %d to version %d.", TABLE_APP_SETTINGS, oldVersion, newVersion);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_SETTINGS);
        onCreate(database, context);
    }


}
