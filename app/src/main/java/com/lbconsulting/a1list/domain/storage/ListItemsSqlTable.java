package com.lbconsulting.a1list.domain.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import timber.log.Timber;

public class ListItemsSqlTable {

    // Version 1
    public static final String TABLE_LIST_ITEMS = "tblListItems";
    public static final String COL_ID = "_id";
    public static final String COL_OBJECT_ID = "objectId";
    public static final String COL_NAME = "name";
    public static final String COL_LIST_TITLE_UUID = "listTitleUuid";
//    public static final String COL_LIST_ATTRIBUTES_ID = "listAttributesID";
    public static final String COL_MANUAL_SORT_KEY = "manualSortKey";
    public static final String COL_CHECKED = "checked";
    public static final String COL_FAVORITE = "favorite";
    public static final String COL_LIST_ITEM_DIRTY = "listItemDirty";
    public static final String COL_MARKED_FOR_DELETION = "markedForDeletion";
    public static final String COL_STRUCK_OUT = "struckOut";
    public static final String COL_UUID = "uuid";
    public static final String COL_UPDATED = "updated";

    public static final String[] PROJECTION_ALL = {COL_ID, COL_OBJECT_ID, COL_NAME,
            COL_LIST_TITLE_UUID,  COL_MANUAL_SORT_KEY, COL_CHECKED, COL_FAVORITE,
            COL_LIST_ITEM_DIRTY, COL_MARKED_FOR_DELETION, COL_STRUCK_OUT, COL_UUID, COL_UPDATED};

    public static final String CONTENT_PATH = "listItems";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
            + CONTENT_PATH;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
            + CONTENT_PATH;
    public static final Uri CONTENT_URI = Uri.parse("content://" + A1List_ContentProvider.AUTHORITY + "/"
            + CONTENT_PATH);

    public static final String SORT_ORDER_NAME_ASC = COL_NAME + " ASC";
    public static final String SORT_ORDER_MANUAL_ASC = COL_MANUAL_SORT_KEY + " ASC";

    // Database creation SQL statements
    private static final String DATA_TABLE_CREATE =
            "create table " + TABLE_LIST_ITEMS
                    + " ("
                    + COL_ID + " integer primary key autoincrement, "
                    + COL_OBJECT_ID + " text default '', "
                    + COL_NAME + " text collate nocase default '', "
                    + COL_LIST_TITLE_UUID + " text default '', "
                    + COL_MANUAL_SORT_KEY + " integer default 0, "
                    + COL_CHECKED + " integer default 0, "
                    + COL_FAVORITE + " integer default 0, "
                    + COL_LIST_ITEM_DIRTY + " integer default 0, "
                    + COL_MARKED_FOR_DELETION + " integer default 0, "
                    + COL_STRUCK_OUT + " integer default 0, "
                    + COL_UUID + " text default '', "
                    + COL_UPDATED + " integer default 0"
                    + ");";

    public static void onCreate(SQLiteDatabase database, Context context) {
        database.execSQL(DATA_TABLE_CREATE);
        Timber.i("onCreate(): %s created.", TABLE_LIST_ITEMS);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion, Context context) {
        Timber.w("onUpgrade(): %s: Upgrading database from version %d to version %d.", TABLE_LIST_ITEMS, oldVersion, newVersion);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST_ITEMS);
        onCreate(database, context);
    }

}
