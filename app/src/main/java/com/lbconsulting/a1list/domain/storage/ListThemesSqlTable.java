package com.lbconsulting.a1list.domain.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import timber.log.Timber;


public class ListThemesSqlTable {

    // Version 1
    public static final String TABLE_LIST_THEMES = "tblListThemes";
    public static final String COL_ID = "_id";
    public static final String COL_OBJECT_ID = "objectId";
    public static final String COL_NAME = "name";
    public static final String COL_START_COLOR = "startColor";
    public static final String COL_END_COLOR = "endColor";
    public static final String COL_TEXT_COLOR = "textColor";
    public static final String COL_TEXT_SIZE = "textSize";
    public static final String COL_HORIZONTAL_PADDING_IN_DP = "horizontalPaddingInDp";
    public static final String COL_VERTICAL_PADDING_IN_DP = "verticalPaddingInDp";
    public static final String COL_THEME_DIRTY = "themeDirty";
    public static final String COL_BOLD = "bold";
    public static final String COL_CHECKED = "checked";
    public static final String COL_DEFAULT_THEME = "defaultTheme";
    public static final String COL_MARKED_FOR_DELETION = "markedForDeletion";
    public static final String COL_STRUCK_OUT = "struckOut";
    public static final String COL_TRANSPARENT = "transparent";
    public static final String COL_UUID = "uuid";
    public static final String COL_UPDATED = "updated";

    //region Projections and Content Path
    public static final String[] PROJECTION_ALL = {COL_ID, COL_OBJECT_ID, COL_NAME,
            COL_START_COLOR, COL_END_COLOR, COL_TEXT_COLOR, COL_TEXT_SIZE,
            COL_HORIZONTAL_PADDING_IN_DP, COL_VERTICAL_PADDING_IN_DP,
            COL_THEME_DIRTY, COL_BOLD, COL_CHECKED, COL_DEFAULT_THEME,
            COL_MARKED_FOR_DELETION, COL_STRUCK_OUT, COL_TRANSPARENT, COL_UUID, COL_UPDATED};

    public static final String CONTENT_PATH = "listTheme";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
            + CONTENT_PATH;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
            + CONTENT_PATH;
    public static final Uri CONTENT_URI = Uri.parse("content://" + A1List_ContentProvider.AUTHORITY + "/"
            + CONTENT_PATH);

    public static final String SORT_ORDER_NAME_ASC = COL_NAME + " ASC";
    //endregion

    // Database creation SQL statements
    private static final String DATA_TABLE_CREATE =
            "create table " + TABLE_LIST_THEMES
                    + " ("
                    + COL_ID + " integer primary key autoincrement, "
                    + COL_OBJECT_ID + " text default '', "
                    + COL_NAME + " text collate nocase default '', "
                    + COL_START_COLOR + " integer default 0, "
                    + COL_END_COLOR + " integer default 0, "
                    + COL_TEXT_COLOR + " integer default 0, "
                    + COL_TEXT_SIZE + " real default 0, "
                    + COL_HORIZONTAL_PADDING_IN_DP + " real default 0, "
                    + COL_VERTICAL_PADDING_IN_DP + " real default 0, "
                    + COL_THEME_DIRTY + " integer default 0, "
                    + COL_BOLD + " integer default 0, "
                    + COL_CHECKED + " integer default 0, "
                    + COL_DEFAULT_THEME + " integer default 0, "
                    + COL_MARKED_FOR_DELETION + " integer default 0, "
                    + COL_STRUCK_OUT + " integer default 0, "
                    + COL_TRANSPARENT + " integer default 0, "
                    + COL_UUID + " text default '', "
                    + COL_UPDATED + " integer default 0"
                    + ");";

    public static void onCreate(SQLiteDatabase database, Context context) {
        database.execSQL(DATA_TABLE_CREATE);
        Timber.i("onCreate(): %s created.", TABLE_LIST_THEMES);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion, Context context) {
        Timber.w("onUpgrade(): %s: Upgrading database from version %d to version %d.", TABLE_LIST_THEMES, oldVersion, newVersion);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST_THEMES);
        onCreate(database, context);
    }

}
