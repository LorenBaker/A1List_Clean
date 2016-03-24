package com.lbconsulting.a1list.domain.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import timber.log.Timber;

public class ListTitlesSqlTable {

    // Version 1
    public static final String TABLE_LIST_TITLES = "tblListTitles";
    public static final String COL_ID = "_id";
    public static final String COL_OBJECT_ID = "objectId";
    public static final String COL_NAME = "name";
    public static final String COL_LIST_THEME_UUID = "listThemeUuid";
    public static final String COL_MANUAL_SORT_KEY = "manualSortKey";
    public static final String COL_LIST_ITEM_LAST_SORT_KEY = "listItemLastSortKey";
    public static final String COL_CHECKED = "checked";
    public static final String COL_LIST_TITLE_DIRTY = "listTitleDirty";
    public static final String COL_FORCED_VIEW_INFLATION = "forceViewInflation";
    public static final String COL_MARKED_FOR_DELETION = "markedForDeletion";
    public static final String COL_STRUCK_OUT = "struckOut";
    public static final String COL_SORT_ALPHABETICALLY = "sortAlphabetically";
    public static final String COL_LIST_LOCKED = "listLocked";
    public static final String COL_LIST_LOCKED_STRING = "listLockString";
    public static final String COL_UUID = "uuid";
    public static final String COL_FIRST_VISIBLE_POSITION = "firstVisiblePosition";
    public static final String COL_LIST_VIEW_TOP = "listViewTop";
    public static final String COL_LIST_PRIVATE_TO_THIS_DEVICE = "listPrivateToThisDevice";
    public static final String COL_UPDATED = "updated";

    //region Projections and Content Path
    public static final String[] PROJECTION_ALL = {COL_ID, COL_OBJECT_ID, COL_NAME,
            COL_LIST_THEME_UUID, COL_MANUAL_SORT_KEY, COL_LIST_ITEM_LAST_SORT_KEY, COL_CHECKED,
            COL_LIST_TITLE_DIRTY, COL_FORCED_VIEW_INFLATION, COL_MARKED_FOR_DELETION, COL_STRUCK_OUT,
            COL_SORT_ALPHABETICALLY, COL_LIST_LOCKED, COL_LIST_LOCKED_STRING, COL_UUID,
            COL_FIRST_VISIBLE_POSITION, COL_LIST_PRIVATE_TO_THIS_DEVICE, COL_LIST_VIEW_TOP, COL_UPDATED};

    public static final String CONTENT_PATH = "listTitles";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
            + CONTENT_PATH;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
            + CONTENT_PATH;
    public static final Uri CONTENT_URI = Uri.parse("content://" + A1List_ContentProvider.AUTHORITY + "/"
            + CONTENT_PATH);

    public static final String SORT_ORDER_NAME_ASC = COL_NAME + " ASC";
    public static final String SORT_MANUALLY_ASC = COL_MANUAL_SORT_KEY + " ASC";
    //endregion

    // Database creation SQL statements
    private static final String DATA_TABLE_CREATE =
            "create table " + TABLE_LIST_TITLES
                    + " ("
                    + COL_ID + " integer primary key autoincrement, "
                    + COL_OBJECT_ID + " text default '', "
                    + COL_NAME + " text collate nocase default '', "
                    + COL_LIST_THEME_UUID + " text default '', "
                    + COL_MANUAL_SORT_KEY + " integer default 0, "
                    + COL_LIST_ITEM_LAST_SORT_KEY + " integer default 0, "
                    + COL_CHECKED + " integer default 0, "
                    + COL_LIST_TITLE_DIRTY + " integer default 0, "
                    + COL_FORCED_VIEW_INFLATION + " integer default 0, "
                    + COL_MARKED_FOR_DELETION + " integer default 0, "
                    + COL_STRUCK_OUT + " integer default 0, "
                    + COL_SORT_ALPHABETICALLY + " integer default 1, "
                    + COL_LIST_LOCKED + " integer default 0, "
                    + COL_LIST_LOCKED_STRING + " text default '', "
                    + COL_UUID + " text default '', "
                    + COL_FIRST_VISIBLE_POSITION + " integer default -1, "
                    + COL_LIST_PRIVATE_TO_THIS_DEVICE + " integer default 0, "
                    + COL_LIST_VIEW_TOP + " integer default 0, "
                    + COL_UPDATED + " integer default 0"
                    + ");";

    public static void onCreate(SQLiteDatabase database, Context context) {
        database.execSQL(DATA_TABLE_CREATE);
        Timber.i("onCreate(): %s created.", TABLE_LIST_TITLES);

    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion, Context context) {
        Timber.w("onUpgrade(): %s: Upgrading database from version %d to version %d.", TABLE_LIST_TITLES, oldVersion, newVersion);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST_TITLES);
        onCreate(database, context);
    }


}
