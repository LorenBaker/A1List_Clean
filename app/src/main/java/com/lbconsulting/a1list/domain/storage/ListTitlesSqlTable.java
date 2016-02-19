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
    public static final String COL_LIST_ATTRIBUTES_ID = "listAttributesID";
    public static final String COL_MANUAL_SORT_KEY = "manualSortKey";
    public static final String COL_CHECKED = "checked";
    public static final String COL_LIST_TITLE_DIRTY = "listTitleDirty";
    public static final String COL_FORCED_VIEW_INFLATION = "forceViewInflation";
    public static final String COL_MARKED_FOR_DELETION = "markedForDeletion";
    public static final String COL_SORT_ALPHABETICALLY = "sortAlphabetically";
    public static final String COL_LIST_LOCKED = "listLocked";
    public static final String COL_LIST_LOCKED_STRING = "listLockString";
    public static final String COL_UUID = "uuid";
    public static final String COL_UPDATED = "updated";

    //region Projections and Content Path
    public static final String[] PROJECTION_ALL = {COL_ID, COL_OBJECT_ID, COL_NAME,
            COL_LIST_ATTRIBUTES_ID, COL_MANUAL_SORT_KEY, COL_CHECKED, COL_LIST_TITLE_DIRTY, COL_FORCED_VIEW_INFLATION,
            COL_MARKED_FOR_DELETION, COL_SORT_ALPHABETICALLY, COL_LIST_LOCKED, COL_LIST_LOCKED_STRING, COL_UUID, COL_UPDATED};

    public static final String CONTENT_PATH = "listTitles";
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
            "create table " + TABLE_LIST_TITLES
                    + " ("
                    + COL_ID + " integer primary key autoincrement, "
                    + COL_OBJECT_ID + " text default '', "
                    + COL_NAME + " text collate nocase default '', "
                    + COL_LIST_ATTRIBUTES_ID + " text default '', "
                    + COL_MANUAL_SORT_KEY + " integer default 0, "
                    + COL_CHECKED + " integer default 0, "
                    + COL_LIST_TITLE_DIRTY + " integer default 0, "
                    + COL_FORCED_VIEW_INFLATION + " integer default 0, "
                    + COL_MARKED_FOR_DELETION + " integer default 0, "
                    + COL_SORT_ALPHABETICALLY + " integer default 1, "
                    + COL_LIST_LOCKED + " integer default 0, "
                    + COL_LIST_LOCKED_STRING + " text default '', "
                    + COL_UUID + " text default '', "
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

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Create Methods
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////

//    public static long CreateMeet(Context context, int meetTypeID, String meetTitle) {
//        long newMeetID = -1;
//        meetTitle = meetTitle.trim();
//        Cursor meetCursor = null;
//        if (!TextUtils.isEmpty(meetTitle)) {
//            // check to see if the meet is already in the database
//            meetCursor = getMeetCursor(context, meetTypeID, meetTitle);
//            if (meetCursor != null && meetCursor.getCount() > 0) {
//                // the MeetEvent already exists in the database
//                meetCursor.moveToFirst();
//                newMeetID = meetCursor.getLong(meetCursor.getColumnIndexOrThrow(COL_MEET_ID));
//                meetCursor.close();
//                return newMeetID;
//            }
//
//            // the meet is NOT in the database ... so create it.
//            Uri uri = CONTENT_URI;
//            ContentResolver cr = context.getContentResolver();
//            ContentValues cv = new ContentValues();
//            cv.put(COL_MEET_TYPE_ID, meetTypeID);
//            cv.put(COL_TITLE, meetTitle);
//            Uri newMeetEventUri = cr.insert(uri, cv);
//            if (newMeetEventUri != null) {
//                newMeetID = Long.parseLong(newMeetEventUri.getLastPathSegment());
//            }
//        }
//        if (meetCursor != null) {
//            meetCursor.close();
//        }
//        return newMeetID;
//    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Read Methods
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////

//    public static Cursor getMeetCursor(Context context, int meetTypeID, String meetTitle) {
//        Uri uri = CONTENT_URI;
//        String[] projection = PROJECTION_ALL;
//        String selection = COL_MEET_TYPE_ID + " = ? AND " + COL_TITLE + " = ? ";
//        String selectionArgs[] = new String[]{String.valueOf(meetTypeID), meetTitle};
//        String sortOrder = null;
//
//        ContentResolver cr = context.getContentResolver();
//        Cursor cursor = null;
//        try {
//            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
//        } catch (Exception e) {
//            MyLog.e("MeetsTable", "Exception error in getMeetCursor:");
//            e.printStackTrace();
//        }
//        return cursor;
//    }
//
//    public static Cursor getMeetCursor(Context context, long meetID) {
//        Uri uri = CONTENT_URI;
//        String[] projection = PROJECTION_ALL;
//        String selection = COL_MEET_ID + " = ?";
//        String selectionArgs[] = new String[]{String.valueOf(meetID)};
//        String sortOrder = null;
//
//        ContentResolver cr = context.getContentResolver();
//        Cursor cursor = null;
//        try {
//            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
//        } catch (Exception e) {
//            MyLog.e("MeetsTable", "Exception error in getMeetCursor:");
//            e.printStackTrace();
//        }
//        return cursor;
//    }
//
//    public static Cursor getAllMeetsCursor(Context context, int meetTypeID, boolean selected, String sortOrder) {
//        int selectedValue = 0;
//        if (selected) {
//            selectedValue = 1;
//        }
//        Uri uri = CONTENT_URI;
//        String[] projection = PROJECTION_ALL;
//        String selection = COL_MEET_TYPE_ID + " = ? AND " + COL_SELECTED + " = ? ";
//        String selectionArgs[] = new String[]{String.valueOf(meetTypeID), String.valueOf(selectedValue)};
//
//        ContentResolver cr = context.getContentResolver();
//        Cursor cursor = null;
//        try {
//            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
//        } catch (Exception e) {
//            MyLog.e("MeetsTable", "Exception error in getAllMeetsCursor:");
//            e.printStackTrace();
//        }
//        return cursor;
//    }
//
//    public static Cursor getAllCheckedMeetsCursor(Context context, int meetType) {
//        Uri uri = CONTENT_URI;
//        String[] projection = {COL_MEET_ID, COL_TITLE};
//        String selection = COL_MEET_TYPE_ID + " = ? AND " + COL_CHECKED + " = ? ";
//        String selectionArgs[] = new String[]{String.valueOf(meetType), String.valueOf(1)};
//
//        ContentResolver cr = context.getContentResolver();
//        String sortOrder = SORT_ORDER_MEET_TITLE;
//        Cursor cursor = null;
//        try {
//            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
//        } catch (Exception e) {
//            MyLog.e("MeetsTable", "Exception error in getAllCheckedMeetsCursor:");
//            e.printStackTrace();
//        }
//        return cursor;
//    }
//
//    public static CursorLoader getAllMeetsExcludingDefault(Context context, int meetType, String sortOrder) {
//        Uri uri = CONTENT_URI;
//        String[] projection = PROJECTION_ALL;
//        String selection = COL_MEET_TYPE_ID + " = ? AND " + COL_MEET_ID + " > ?";
//        String selectionArgs[] = new String[]{String.valueOf(meetType), String.valueOf(1)};
//        CursorLoader cursorLoader = null;
//        try {
//            cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
//        } catch (Exception e) {
//            MyLog.e("MeetsTable", "Exception error in getAllMeetsExcludingDefault:");
//            e.printStackTrace();
//        }
//        return cursorLoader;
//    }
//
//    public static CursorLoader getAllMeets(Context context, int meetTypeID, boolean selected, String sortOrder) {
//        int selectedValue = 0;
//        if (selected) {
//            selectedValue = 1;
//        }
//        Uri uri = CONTENT_URI;
//        String[] projection = PROJECTION_ALL;
//        String selection = "(" + COL_MEET_TYPE_ID + " = ? AND " + COL_SELECTED + " = ? ) OR " + COL_MEET_ID + " = ?";
//        String selectionArgs[] = new String[]{String.valueOf(meetTypeID), String.valueOf(selectedValue),
//                String.valueOf(1)};
//        CursorLoader cursorLoader = null;
//        try {
//            cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
//        } catch (Exception e) {
//            MyLog.e("MeetsTable", "Exception error in getAllMeets:");
//            e.printStackTrace();
//        }
//        return cursorLoader;
//    }
//
//    public static boolean isMeetTitleSelected(Context context, long meetID) {
//        boolean result = false;
//        Cursor cursor = getMeetCursor(context, meetID);
//        if (cursor != null && cursor.getCount() > 0) {
//            cursor.moveToFirst();
//            int selectedValue = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SELECTED));
//            result = selectedValue > 0;
//        }
//
//        if (cursor != null) {
//            cursor.close();
//        }
//        return result;
//    }
//
//    public static boolean isMeetTitleChecked(Context context, long meetID) {
//        boolean result = false;
//        Cursor cursor = getMeetCursor(context, meetID);
//        if (cursor != null && cursor.getCount() > 0) {
//            cursor.moveToFirst();
//            int checkedValue = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CHECKED));
//            result = checkedValue > 0;
//        }
//
//        if (cursor != null) {
//            cursor.close();
//        }
//        return result;
//    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Update Methods
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////

//    public static int UpdateMeetFieldValues(Context context, long meetID, ContentValues newFieldValues) {
//        int numberOfUpdatedRecords = -1;
//        if (meetID > 1) {
//            ContentResolver cr = context.getContentResolver();
//            Uri uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(meetID));
//            String selection = null;
//            String[] selectionArgs = null;
//            numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
//        }
//        return numberOfUpdatedRecords;
//    }
//
//    public static int CheckAllMeetTitles(Context context, boolean checked) {
//        int numberOfUpdatedRecords = -1;
//
//        ContentResolver cr = context.getContentResolver();
//        Uri uri = CONTENT_URI;
//        String selection = COL_CHECKED + " = ?";
//
//        ContentValues newFieldValues = new ContentValues();
//        String selectionArgs[] = new String[]{String.valueOf(0)};
//        if (checked) {
//            newFieldValues.put(COL_CHECKED, 1);
//
//        } else {
//            selectionArgs = new String[]{String.valueOf(1)};
//            newFieldValues.put(COL_CHECKED, 0);
//        }
//
//        numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
//        return numberOfUpdatedRecords;
//    }
//
//    public static int SelectAllMeetTitles(Context context, boolean selected) {
//        int numberOfUpdatedRecords = -1;
//
//        ContentResolver cr = context.getContentResolver();
//        Uri uri = CONTENT_URI;
//        String selection = COL_SELECTED + " = ?";
//
//        ContentValues newFieldValues = new ContentValues();
//        String selectionArgs[] = new String[]{String.valueOf(0)};
//        if (selected) {
//            newFieldValues.put(COL_SELECTED, 1);
//
//        } else {
//            selectionArgs = new String[]{String.valueOf(1)};
//            newFieldValues.put(COL_SELECTED, 0);
//        }
//
//        numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
//        return numberOfUpdatedRecords;
//    }
//
//    public static int setMeetTitleCheckbox(Context context, long meetID, boolean checked) {
//        int numberOfUpdatedRecords = -1;
//
//        ContentResolver cr = context.getContentResolver();
//        Uri uri = CONTENT_URI;
//        String selection = COL_MEET_ID + " = ?";
//        String selectionArgs[] = new String[]{String.valueOf(meetID)};
//
//        ContentValues newFieldValues = new ContentValues();
//
//        if (checked) {
//            newFieldValues.put(COL_CHECKED, 1);
//
//        } else {
//            newFieldValues.put(COL_CHECKED, 0);
//        }
//
//        numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
//        return numberOfUpdatedRecords;
//    }
//
//    public static int ToggleMeetTitleCheckbox(Context context, long meetID) {
//        int numberOfUpdatedRecords = -1;
//
//        ContentResolver cr = context.getContentResolver();
//        Uri uri = CONTENT_URI;
//        String selection = COL_MEET_ID + " = ?";
//        String selectionArgs[] = new String[]{String.valueOf(meetID)};
//
//        ContentValues newFieldValues = new ContentValues();
//
//        if (isMeetTitleChecked(context, meetID)) {
//            newFieldValues.put(COL_CHECKED, 0);
//
//        } else {
//            newFieldValues.put(COL_CHECKED, 1);
//        }
//
//        numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
//        return numberOfUpdatedRecords;
//    }
//
//    public static int ToggleMeetTitleSelectedBox(Context context, long meetID) {
//        int numberOfUpdatedRecords = -1;
//
//        ContentResolver cr = context.getContentResolver();
//        Uri uri = CONTENT_URI;
//        String selection = COL_MEET_ID + " = ?";
//        String selectionArgs[] = new String[]{String.valueOf(meetID)};
//
//        ContentValues newFieldValues = new ContentValues();
//
//        if (isMeetTitleSelected(context, meetID)) {
//            newFieldValues.put(COL_SELECTED, 0);
//
//        } else {
//            newFieldValues.put(COL_SELECTED, 1);
//        }
//
//        numberOfUpdatedRecords = cr.update(uri, newFieldValues, selection, selectionArgs);
//        return numberOfUpdatedRecords;
//    }
//
//    public static CharSequence getMeetTitle(Context context, long meetID) {
//        String meetTitle = "";
//        Cursor cursor = getMeetCursor(context, meetID);
//        if (cursor != null && cursor.getCount() > 0) {
//            cursor.moveToFirst();
//            meetTitle = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
//        }
//
//        if (cursor != null) {
//            cursor.close();
//        }
//        return meetTitle;
//    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Delete Methods
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////

//    public static int DeleteMeet(Context context, long meetID) {
//        int numberOfDeletedRecords = 0;
//        if (meetID > 1) {
//
//            // Races Table: iteratively delete all Races that contain the Meet
//            Cursor racesCursor = RacesTable.getAllRacesCursorWithMeet(context, meetID);
//            if (racesCursor != null && racesCursor.getCount() > 0) {
//                long raceID = -1;
//                while (racesCursor.moveToNext()) {
//                    raceID = racesCursor.getLong(racesCursor
//                            .getColumnIndexOrThrow(RacesTable.COL_RACE_ID));
//                    numberOfDeletedRecords += RacesTable.deleteRace(context, raceID);
//                }
//            }
//
//            if (racesCursor != null) {
//                racesCursor.close();
//            }
//
//            ContentResolver cr = context.getContentResolver();
//            Uri channelUri = CONTENT_URI;
//            String where = COL_MEET_ID + " = ?";
//            String selectionArgs[] = new String[]{String.valueOf(meetID)};
//            numberOfDeletedRecords += cr.delete(channelUri, where, selectionArgs);
//        }
//        return numberOfDeletedRecords;
//
//    }
//
//    public static int DeleteAllCheckedMeets(Context context, int meetType) {
//        int numberOfDeletedRecords = 0;
//        Cursor checkedMeetsCursor = getAllCheckedMeetsCursor(context, meetType);
//        if (checkedMeetsCursor != null) {
//            while (checkedMeetsCursor.moveToNext()) {
//                numberOfDeletedRecords += DeleteMeet(context,
//                        checkedMeetsCursor.getLong(checkedMeetsCursor.getColumnIndexOrThrow(COL_MEET_ID)));
//            }
//            checkedMeetsCursor.close();
//        }
//        return numberOfDeletedRecords;
//    }

}
