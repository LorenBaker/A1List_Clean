package com.lbconsulting.a1list.domain.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import timber.log.Timber;

public class ListTitlePositionsSqlTable {

    // Version 1
    public static final String TABLE_LIST_TITLE_POSITIONS = "tblListTitlePositions";
    public static final String COL_ID = "_id";
    public static final String COL_OBJECT_ID = "objectId";

    public static final String COL_LIST_TITLE_UUID = "listTitleUuid";
    public static final String COL_FIRST_POSITION = "listViewFirstPosition";
    public static final String COL_LIST_VIEW_TOP = "listViewTop";
    public static final String COL_LIST_TITLE_POSITION_DIRTY = "listTitlePositionDirty";

    public static final String COL_UUID = "uuid";
    public static final String COL_UPDATED = "updated";

    //region Projections and Content Path
    public static final String[] PROJECTION_ALL = {COL_ID, COL_OBJECT_ID, COL_LIST_TITLE_UUID,
            COL_FIRST_POSITION, COL_LIST_VIEW_TOP, COL_LIST_TITLE_POSITION_DIRTY, COL_UUID, COL_UPDATED};

    public static final String CONTENT_PATH = "listTitlePositions";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
            + CONTENT_PATH;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
            + CONTENT_PATH;
    public static final Uri CONTENT_URI = Uri.parse("content://" + A1List_ContentProvider.AUTHORITY + "/"
            + CONTENT_PATH);

    //endregion

    // Database creation SQL statements
    private static final String DATA_TABLE_CREATE =
            "create table " + TABLE_LIST_TITLE_POSITIONS
                    + " ("
                    + COL_ID + " integer primary key autoincrement, "
                    + COL_OBJECT_ID + " text default '', "
                    + COL_LIST_TITLE_UUID + " text default '', "
                    + COL_FIRST_POSITION + " integer default 0, "
                    + COL_LIST_VIEW_TOP + " integer default 0, "
                    + COL_LIST_TITLE_POSITION_DIRTY + " integer default 0, "
                    + COL_UUID + " text default '', "
                    + COL_UPDATED + " integer default 0"
                    + ");";

    public static void onCreate(SQLiteDatabase database, Context context) {
        database.execSQL(DATA_TABLE_CREATE);
        Timber.i("onCreate(): %s created.", TABLE_LIST_TITLE_POSITIONS);

    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion, Context context) {
        Timber.w("onUpgrade(): %s: Upgrading database from version %d to version %d.", TABLE_LIST_TITLE_POSITIONS, oldVersion, newVersion);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST_TITLE_POSITIONS);
        onCreate(database, context);
    }


}
