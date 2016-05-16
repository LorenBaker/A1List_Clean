package com.lbconsulting.a1list.domain.storage;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import timber.log.Timber;


public class A1List_ContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.lbconsulting.a1list";
    // UriMatcher switch constants
    private static final int APP_SETTINGS_MULTI_ROWS = 10;
    private static final int APP_SETTINGS_SINGLE_ROW = 11;
    private static final int LIST_ITEMS_MULTI_ROWS = 20;
    private static final int LIST_ITEMS_SINGLE_ROW = 21;
    private static final int LIST_TITLES_MULTI_ROWS = 30;
    private static final int LIST_TITLES_SINGLE_ROW = 31;
    private static final int LIST_TITLE_POSITIONS_MULTI_ROWS = 40;
    private static final int LIST_TITLE_POSITIONS_SINGLE_ROW = 41;
    private static final int LIST_THEMES_MULTI_ROWS = 50;
    private static final int LIST_THEMES_SINGLE_ROW = 51;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, AppSettingsSqlTable.CONTENT_PATH, APP_SETTINGS_MULTI_ROWS);
        sURIMatcher.addURI(AUTHORITY, AppSettingsSqlTable.CONTENT_PATH + "/#", APP_SETTINGS_SINGLE_ROW);

        sURIMatcher.addURI(AUTHORITY, ListItemsSqlTable.CONTENT_PATH, LIST_ITEMS_MULTI_ROWS);
        sURIMatcher.addURI(AUTHORITY, ListItemsSqlTable.CONTENT_PATH + "/#", LIST_ITEMS_SINGLE_ROW);

        sURIMatcher.addURI(AUTHORITY, ListTitlesSqlTable.CONTENT_PATH, LIST_TITLES_MULTI_ROWS);
        sURIMatcher.addURI(AUTHORITY, ListTitlesSqlTable.CONTENT_PATH + "/#", LIST_TITLES_SINGLE_ROW);

        sURIMatcher.addURI(AUTHORITY, ListTitlePositionsSqlTable.CONTENT_PATH, LIST_TITLE_POSITIONS_MULTI_ROWS);
        sURIMatcher.addURI(AUTHORITY, ListTitlePositionsSqlTable.CONTENT_PATH + "/#", LIST_TITLE_POSITIONS_SINGLE_ROW);

        sURIMatcher.addURI(AUTHORITY, ListThemesSqlTable.CONTENT_PATH, LIST_THEMES_MULTI_ROWS);
        sURIMatcher.addURI(AUTHORITY, ListThemesSqlTable.CONTENT_PATH + "/#", LIST_THEMES_SINGLE_ROW);
    }

    private A1List_DatabaseHelper database = null;

    @Override
    public boolean onCreate() {
        Timber.i("onCreate()");

        // Construct the underlying database
        // Defer opening the database until you need to perform
        // a query or other transaction.
        database = new A1List_DatabaseHelper(getContext());
        return true;
    }

	/*	A content provider is created when its hosting process is created, and remains around for as long as the process
        does, so there is no need to close the database -- it will get closed as part of the kernel cleaning up the
		process's resources when the process is killed. 
	*/

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        String rowId;
        int deleteCount;

        // Open a WritableDatabase database to support the deleteFromStorage transaction
        SQLiteDatabase db = database.getWritableDatabase();

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {

            case APP_SETTINGS_MULTI_ROWS:
                // To return the number of deleted items you must specify a where clause.
                // To deleteFromStorage all rows and return a value pass in "1".
                if (selection == null) {
                    selection = "1";
                }
                // Perform the deletion
                deleteCount = db.delete(AppSettingsSqlTable.TABLE_APP_SETTINGS, selection, selectionArgs);
                break;

            case APP_SETTINGS_SINGLE_ROW:
                // Limit deletion to a single row
                rowId = uri.getLastPathSegment();
                selection = ListItemsSqlTable.COL_ID + "=" + rowId;
                // Perform the deletion
                deleteCount = db.delete(AppSettingsSqlTable.TABLE_APP_SETTINGS, selection, selectionArgs);
                break;

            case LIST_ITEMS_MULTI_ROWS:
                // To return the number of deleted items you must specify a where clause.
                // To deleteFromStorage all rows and return a value pass in "1".
                if (selection == null) {
                    selection = "1";
                }
                // Perform the deletion
                deleteCount = db.delete(ListItemsSqlTable.TABLE_LIST_ITEMS, selection, selectionArgs);
                break;

            case LIST_ITEMS_SINGLE_ROW:
                // Limit deletion to a single row
                rowId = uri.getLastPathSegment();
                selection = ListItemsSqlTable.COL_ID + "=" + rowId;
                // Perform the deletion
                deleteCount = db.delete(ListItemsSqlTable.TABLE_LIST_ITEMS, selection, selectionArgs);
                break;

            case LIST_TITLES_MULTI_ROWS:
                // To return the number of deleted items you must specify a where clause.
                // To deleteFromStorage all rows and return a value pass in "1".
                if (selection == null) {
                    selection = "1";
                }
                // Perform the deletion
                deleteCount = db.delete(ListTitlesSqlTable.TABLE_LIST_TITLES, selection, selectionArgs);
                break;

            case LIST_TITLES_SINGLE_ROW:
                // Limit deletion to a single row
                rowId = uri.getLastPathSegment();
                selection = ListTitlesSqlTable.COL_ID + "=" + rowId;
                // Perform the deletion
                deleteCount = db.delete(ListTitlesSqlTable.TABLE_LIST_TITLES, selection, selectionArgs);
                break;

            case LIST_TITLE_POSITIONS_MULTI_ROWS:
                // To return the number of deleted items you must specify a where clause.
                // To deleteFromStorage all rows and return a value pass in "1".
                if (selection == null) {
                    selection = "1";
                }
                // Perform the deletion
                deleteCount = db.delete(ListTitlePositionsSqlTable.TABLE_LIST_TITLE_POSITIONS, selection, selectionArgs);
                break;

            case LIST_TITLE_POSITIONS_SINGLE_ROW:
                // Limit deletion to a single row
                rowId = uri.getLastPathSegment();
                selection = ListTitlesSqlTable.COL_ID + "=" + rowId;
                // Perform the deletion
                deleteCount = db.delete(ListTitlePositionsSqlTable.TABLE_LIST_TITLE_POSITIONS, selection, selectionArgs);
                break;

            case LIST_THEMES_MULTI_ROWS:
                // To return the number of deleted items you must specify a where clause.
                // To deleteFromStorage all rows and return a value pass in "1".
                if (selection == null) {
                    selection = "1";
                }
                // Perform the deletion
                deleteCount = db.delete(ListThemesSqlTable.TABLE_LIST_THEMES, selection, selectionArgs);
                break;

            case LIST_THEMES_SINGLE_ROW:
                // Limit deletion to a single row
                rowId = uri.getLastPathSegment();
                selection = ListThemesSqlTable.COL_ID + "=" + rowId;
                // Perform the deletion
                deleteCount = db.delete(ListThemesSqlTable.TABLE_LIST_THEMES, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Method deleteFromStorage: Unknown URI: " + uri);
        }

        if (getContext() != null && getContext().getContentResolver() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deleteCount;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {

            case APP_SETTINGS_MULTI_ROWS:
                return AppSettingsSqlTable.CONTENT_TYPE;
            case APP_SETTINGS_SINGLE_ROW:
                return AppSettingsSqlTable.CONTENT_ITEM_TYPE;

            case LIST_ITEMS_MULTI_ROWS:
                return ListItemsSqlTable.CONTENT_TYPE;
            case LIST_ITEMS_SINGLE_ROW:
                return ListItemsSqlTable.CONTENT_ITEM_TYPE;

            case LIST_TITLES_MULTI_ROWS:
                return ListTitlesSqlTable.CONTENT_TYPE;
            case LIST_TITLES_SINGLE_ROW:
                return ListTitlesSqlTable.CONTENT_ITEM_TYPE;

            case LIST_TITLE_POSITIONS_MULTI_ROWS:
                return ListTitlePositionsSqlTable.CONTENT_TYPE;
            case LIST_TITLE_POSITIONS_SINGLE_ROW:
                return ListTitlePositionsSqlTable.CONTENT_ITEM_TYPE;

            case LIST_THEMES_MULTI_ROWS:
                return ListThemesSqlTable.CONTENT_TYPE;
            case LIST_THEMES_SINGLE_ROW:
                return ListThemesSqlTable.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Method getType. Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        SQLiteDatabase db = null;
        long newRowId;
        String nullColumnHack = null;

        // Open a WritableDatabase database to support the insertIntoStorage transaction
        db = database.getWritableDatabase();

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {

            case APP_SETTINGS_MULTI_ROWS:
                newRowId = db.insertOrThrow(AppSettingsSqlTable.TABLE_APP_SETTINGS, nullColumnHack, values);
                if (newRowId > 0) {
                    // Construct and return the URI of the newly inserted row.
                    Uri newRowUri = ContentUris.withAppendedId(AppSettingsSqlTable.CONTENT_URI, newRowId);

                    if (getContext() != null && getContext().getContentResolver() != null) {
                        getContext().getContentResolver().notifyChange(AppSettingsSqlTable.CONTENT_URI, null);
                    }

                    return newRowUri;
                }
                return null;

            case APP_SETTINGS_SINGLE_ROW:
                throw new IllegalArgumentException(
                        "Illegal URI: Cannot insertIntoStorage a new row with a single row URI. " + uri);

            case LIST_ITEMS_MULTI_ROWS:
                newRowId = db.insertOrThrow(ListItemsSqlTable.TABLE_LIST_ITEMS, nullColumnHack, values);
                if (newRowId > 0) {
                    // Construct and return the URI of the newly inserted row.
                    Uri newRowUri = ContentUris.withAppendedId(ListItemsSqlTable.CONTENT_URI, newRowId);

                    if (getContext() != null && getContext().getContentResolver() != null) {
                        getContext().getContentResolver().notifyChange(ListItemsSqlTable.CONTENT_URI, null);
                    }

                    return newRowUri;
                }
                return null;

            case LIST_ITEMS_SINGLE_ROW:
                throw new IllegalArgumentException(
                        "Illegal URI: Cannot insertIntoStorage a new row with a single row URI. " + uri);

            case LIST_TITLES_MULTI_ROWS:
                newRowId = db.insertOrThrow(ListTitlesSqlTable.TABLE_LIST_TITLES, nullColumnHack, values);
                if (newRowId > 0) {
                    // Construct and return the URI of the newly inserted row.
                    Uri newRowUri = ContentUris.withAppendedId(ListTitlesSqlTable.CONTENT_URI, newRowId);
                    if (getContext() != null && getContext().getContentResolver() != null) {
                        getContext().getContentResolver().notifyChange(ListTitlesSqlTable.CONTENT_URI, null);
                    }

                    return newRowUri;
                }
                return null;

            case LIST_TITLES_SINGLE_ROW:
                throw new IllegalArgumentException(
                        "Illegal URI: Cannot insertIntoStorage a new row with a single row URI. " + uri);

            case LIST_TITLE_POSITIONS_MULTI_ROWS:
                newRowId = db.insertOrThrow(ListTitlePositionsSqlTable.TABLE_LIST_TITLE_POSITIONS, nullColumnHack, values);
                if (newRowId > 0) {
                    // Construct and return the URI of the newly inserted row.
                    Uri newRowUri = ContentUris.withAppendedId(ListTitlePositionsSqlTable.CONTENT_URI, newRowId);
                    if (getContext() != null && getContext().getContentResolver() != null) {
                        getContext().getContentResolver().notifyChange(ListTitlePositionsSqlTable.CONTENT_URI, null);
                    }

                    return newRowUri;
                }
                return null;

            case LIST_TITLE_POSITIONS_SINGLE_ROW:
                throw new IllegalArgumentException(
                        "Illegal URI: Cannot insertIntoStorage a new row with a single row URI. " + uri);

            case LIST_THEMES_MULTI_ROWS:
                newRowId = db.insertOrThrow(ListThemesSqlTable.TABLE_LIST_THEMES, nullColumnHack, values);
                if (newRowId > 0) {
                    // Construct and return the URI of the newly inserted row.
                    Uri newRowUri = ContentUris.withAppendedId(ListThemesSqlTable.CONTENT_URI, newRowId);
                    if (getContext() != null && getContext().getContentResolver() != null) {
                        getContext().getContentResolver().notifyChange(ListThemesSqlTable.CONTENT_URI, null);
                    }

                    return newRowUri;
                }
                return null;

            case LIST_THEMES_SINGLE_ROW:
                throw new IllegalArgumentException(
                        "Illegal URI: Cannot insertIntoStorage a new row with a single row URI. " + uri);

            default:
                throw new IllegalArgumentException("Method insertIntoStorage: Unknown URI:" + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {

            case APP_SETTINGS_MULTI_ROWS:
                queryBuilder.setTables(AppSettingsSqlTable.TABLE_APP_SETTINGS);
                break;

            case APP_SETTINGS_SINGLE_ROW:
                queryBuilder.setTables(AppSettingsSqlTable.TABLE_APP_SETTINGS);
                queryBuilder.appendWhere(AppSettingsSqlTable.COL_ID + "=" + uri.getLastPathSegment());
                break;

            case LIST_ITEMS_MULTI_ROWS:
                queryBuilder.setTables(ListItemsSqlTable.TABLE_LIST_ITEMS);
                break;

            case LIST_ITEMS_SINGLE_ROW:
                queryBuilder.setTables(ListItemsSqlTable.TABLE_LIST_ITEMS);
                queryBuilder.appendWhere(ListItemsSqlTable.COL_ID + "=" + uri.getLastPathSegment());
                break;

            case LIST_TITLES_MULTI_ROWS:
                queryBuilder.setTables(ListTitlesSqlTable.TABLE_LIST_TITLES);
                break;

            case LIST_TITLES_SINGLE_ROW:
                queryBuilder.setTables(ListTitlesSqlTable.TABLE_LIST_TITLES);
                queryBuilder.appendWhere(ListTitlesSqlTable.COL_ID + "=" + uri.getLastPathSegment());
                break;

            case LIST_TITLE_POSITIONS_MULTI_ROWS:
                queryBuilder.setTables(ListTitlePositionsSqlTable.TABLE_LIST_TITLE_POSITIONS);
                break;

            case LIST_TITLE_POSITIONS_SINGLE_ROW:
                queryBuilder.setTables(ListTitlePositionsSqlTable.TABLE_LIST_TITLE_POSITIONS);
                queryBuilder.appendWhere(ListTitlePositionsSqlTable.COL_ID + "=" + uri.getLastPathSegment());
                break;

            case LIST_THEMES_MULTI_ROWS:
                queryBuilder.setTables(ListThemesSqlTable.TABLE_LIST_THEMES);
                break;

            case LIST_THEMES_SINGLE_ROW:
                queryBuilder.setTables(ListThemesSqlTable.TABLE_LIST_THEMES);
                queryBuilder.appendWhere(ListThemesSqlTable.COL_ID + "=" + uri.getLastPathSegment());
                break;


            default:
                throw new IllegalArgumentException("Method query. Unknown URI:" + uri);
        }

        // Execute the query on the database
        SQLiteDatabase db = null;
        try {
            db = database.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = database.getReadableDatabase();
        }

        if (null != db) {
            String groupBy = null;
            String having = null;
            Cursor cursor = null;
            try {
                cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
            } catch (Exception e) {
                Timber.e("query(): Exception: %s.", e.getMessage());
                e.printStackTrace();
            }

            if (null != cursor && getContext() != null && getContext().getContentResolver() != null) {
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
            }
            return cursor;
        }
        return null;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String rowID;
        int updateCount = 0;

        // Open a WritableDatabase database to support the updateStorage transaction
        SQLiteDatabase db = database.getWritableDatabase();

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {

            case APP_SETTINGS_MULTI_ROWS:
                updateCount = db.update(AppSettingsSqlTable.TABLE_APP_SETTINGS, values, selection, selectionArgs);
                break;

            case APP_SETTINGS_SINGLE_ROW:
                rowID = uri.getLastPathSegment();
                selection = AppSettingsSqlTable.COL_ID + "=" + rowID;
                updateCount = db.update(AppSettingsSqlTable.TABLE_APP_SETTINGS, values, selection, selectionArgs);
                break;

            case LIST_ITEMS_MULTI_ROWS:
                updateCount = db.update(ListItemsSqlTable.TABLE_LIST_ITEMS, values, selection, selectionArgs);
                break;

            case LIST_ITEMS_SINGLE_ROW:
                rowID = uri.getLastPathSegment();
                selection = ListItemsSqlTable.COL_ID + "=" + rowID;
                updateCount = db.update(ListItemsSqlTable.TABLE_LIST_ITEMS, values, selection, selectionArgs);
                break;

            case LIST_TITLES_MULTI_ROWS:
                updateCount = db.update(ListTitlesSqlTable.TABLE_LIST_TITLES, values, selection, selectionArgs);
                break;

            case LIST_TITLES_SINGLE_ROW:
                rowID = uri.getLastPathSegment();
                selection = ListTitlesSqlTable.COL_ID + "=" + rowID;
                updateCount = db.update(ListTitlesSqlTable.TABLE_LIST_TITLES, values, selection, selectionArgs);
                break;

            case LIST_TITLE_POSITIONS_MULTI_ROWS:
                updateCount = db.update(ListTitlePositionsSqlTable.TABLE_LIST_TITLE_POSITIONS, values, selection, selectionArgs);
                break;

            case LIST_TITLE_POSITIONS_SINGLE_ROW:
                rowID = uri.getLastPathSegment();
                selection = ListTitlePositionsSqlTable.COL_ID + "=" + rowID;
                updateCount = db.update(ListTitlePositionsSqlTable.TABLE_LIST_TITLE_POSITIONS, values, selection, selectionArgs);
                break;

            case LIST_THEMES_MULTI_ROWS:
                updateCount = db.update(ListThemesSqlTable.TABLE_LIST_THEMES, values, selection,
                        selectionArgs);
                break;

            case LIST_THEMES_SINGLE_ROW:
                rowID = uri.getLastPathSegment();
                selection = ListThemesSqlTable.COL_ID + "=" + rowID;
                updateCount = db.update(ListThemesSqlTable.TABLE_LIST_THEMES, values, selection,
                        selectionArgs);
                break;


            default:
                throw new IllegalArgumentException("Method updateStorage: Unknown URI: " + uri);
        }
        if (getContext() != null && getContext().getContentResolver() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updateCount;
    }

    /**
     * A test package can call this to get a handle to the database underlying HW311ContentProvider, so it can insertIntoStorage
     * test data into the database. The test case class is responsible for instantiating the provider in a test context;
     * {@link android.test.ProviderTestCase2} does this during the call to setUp()
     *
     * @return a handle to the database helper object for the provider's data.
     */
    public A1List_DatabaseHelper getOpenHelperForTest() {
        return database;
    }
}
