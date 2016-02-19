package com.lbconsulting.a1list.domain.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.storage.ListThemeSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.MySettings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;


/**
 * This class provided CRUD operations for ListTheme
 * NOTE: All CRUD operations should run on a background thread
 */
public class ListThemeRepository_Impl implements ListThemeRepository {

    private final int FALSE = 0;
    private final int TRUE = 1;
    private final Context mContext;
//    private final DateFormat mDateFormat;
//    private final SimpleDateFormat sdf;


    public ListThemeRepository_Impl(Context context) {
        // private constructor
        this.mContext = context;
//        mDateFormat = DateFormat.getDateTimeInstance();
//        sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS", Locale.US);

    }

    // CRUD operations

    //region Create
    @Override
    public ListTheme insert(ListTheme listTheme) {
        // insert new listTheme into SQLite db
        ListTheme backendlessResponse = null;
        long newThemeSqlId = -1;
        listTheme.setThemeDirty(true);

        Uri uri = ListThemeSqlTable.CONTENT_URI;
        ContentValues cv = new ContentValues();
        cv.put(ListThemeSqlTable.COL_OBJECT_ID, listTheme.getObjectId());
        cv.put(ListThemeSqlTable.COL_NAME, listTheme.getName());
        cv.put(ListThemeSqlTable.COL_START_COLOR, listTheme.getStartColor());
        cv.put(ListThemeSqlTable.COL_END_COLOR, listTheme.getEndColor());
        cv.put(ListThemeSqlTable.COL_TEXT_COLOR, listTheme.getTextColor());
        cv.put(ListThemeSqlTable.COL_TEXT_SIZE, listTheme.getTextSize());
        cv.put(ListThemeSqlTable.COL_HORIZONTAL_PADDING_IN_DP, listTheme.getHorizontalPaddingInDp());
        cv.put(ListThemeSqlTable.COL_VERTICAL_PADDING_IN_DP, listTheme.getVerticalPaddingInDp());

        cv.put(ListThemeSqlTable.COL_THEME_DIRTY, (listTheme.isThemeDirty()) ? TRUE : FALSE);
        cv.put(ListThemeSqlTable.COL_BOLD, (listTheme.isBold()) ? TRUE : FALSE);
        cv.put(ListThemeSqlTable.COL_CHECKED, (listTheme.isChecked()) ? TRUE : FALSE);
        cv.put(ListThemeSqlTable.COL_DEFAULT_THEME, (listTheme.isDefaultTheme()) ? TRUE : FALSE);
        cv.put(ListThemeSqlTable.COL_MARKED_FOR_DELETION, (listTheme.isMarkedForDeletion()) ? TRUE : FALSE);
        cv.put(ListThemeSqlTable.COL_TRANSPARENT, (listTheme.isTransparent()) ? TRUE : FALSE);
        cv.put(ListThemeSqlTable.COL_UUID, listTheme.getUuid());
        Date updatedDateTime = listTheme.getUpdated();
        if (updatedDateTime != null) {
            cv.put(ListThemeSqlTable.COL_UPDATED, updatedDateTime.getTime());
        }

        ContentResolver cr = mContext.getContentResolver();
        Uri newThemeUri = cr.insert(uri, cv);
        if (newThemeUri != null) {
            newThemeSqlId = Long.parseLong(newThemeUri.getLastPathSegment());
        }

        if (newThemeSqlId > -1) {
            // successfully saved new ListTheme to the SQLite db
            Timber.i("insert(): ListThemeRepository_Impl: Successfully inserted \"%s\" into the SQLite db.", listTheme.getName());

            // if the network is available ... save new listTheme to Backendless
            if (CommonMethods.isNetworkAvailable()) {
                backendlessResponse = saveListThemeToBackendless(listTheme);
                // TODO: send message to Backendless to notify other devices of the new ListTheme
            }

        } else {
            // failed to create listTheme in the SQLite db
            Timber.e("insert(): ListThemeRepository_Impl: FAILED to insert \"%s\" into the SQLite db.", listTheme.getName());
        }
//        if (backendlessResponse != null) {
//            return backendlessResponse;
//        } else {
//            return listTheme;
//        }
        return backendlessResponse;
    }


    private ListTheme saveListThemeToBackendless(final ListTheme listTheme) {
        // saveListThemeToBackendless object synchronously
        ListTheme response = null;
        String objectId = listTheme.getObjectId();
        final boolean isNew = objectId.equals(MySettings.NOT_AVAILABLE);
        try {
            response = Backendless.Data.of(ListTheme.class).save(listTheme);
            Timber.i("saveListThemeToBackendless(): successfully saved \"%s\" to Backendless.", response.getName());
            // If a new ListTheme, update SQLite db with objectID, dirty to false, and updated date and time
            ContentValues cv = new ContentValues();
            Date updatedDate = response.getUpdated();
            if (updatedDate == null) {
                updatedDate = response.getCreated();
            }
            if (updatedDate != null) {
                long updated = updatedDate.getTime();
                cv.put(ListThemeSqlTable.COL_UPDATED, updated);
            }
            cv.put(ListThemeSqlTable.COL_THEME_DIRTY, FALSE);

            if (isNew) {
                cv.put(ListThemeSqlTable.COL_OBJECT_ID, response.getObjectId());
            }
            update(response, cv, false);

        } catch (BackendlessException e) {
            listTheme.setThemeDirty(true);
            Timber.e("saveListThemeToBackendless(): FAILED to save \"%s\" to Backendless. BackendlessException: Code: %s; Message: %s.",
                    listTheme.getName(), e.getCode(), e.getMessage());
            // Set dirty flag to true in SQLite db
            setSQLiteDirtyFlag(listTheme, TRUE);

        }
        return response;
    }

    private void setSQLiteDirtyFlag(ListTheme listTheme, int dirtyFlag) {
        ContentValues cv = new ContentValues();
        cv.put(ListThemeSqlTable.COL_THEME_DIRTY, dirtyFlag);
        update(listTheme, cv, false);
    }

    private void setSQLiteMarkForDeletionFlag(ListTheme listTheme, int markedForDeletionFlag) {
        ContentValues cv = new ContentValues();
        cv.put(ListThemeSqlTable.COL_MARKED_FOR_DELETION, markedForDeletionFlag);
        update(listTheme, cv, false);
    }

    //endregion

    //region Read


    private ListTheme ListThemeFromCursor(Cursor cursor) {
        ListTheme listTheme = new ListTheme();
        listTheme.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_ID)));
        listTheme.setObjectId(cursor.getString(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_OBJECT_ID)));
        listTheme.setName(cursor.getString(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_NAME)));
        listTheme.setStartColor(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_START_COLOR)));
        listTheme.setEndColor(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_END_COLOR)));
        listTheme.setTextColor(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_TEXT_COLOR)));
        listTheme.setTextSize(cursor.getFloat(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_TEXT_SIZE)));
        listTheme.setHorizontalPaddingInDp(cursor.getFloat(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_HORIZONTAL_PADDING_IN_DP)));
        listTheme.setVerticalPaddingInDp(cursor.getFloat(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_VERTICAL_PADDING_IN_DP)));
        listTheme.setBold(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_BOLD)) > 0);
        listTheme.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_CHECKED)) > 0);
        listTheme.setDefaultTheme(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_DEFAULT_THEME)) > 0);
        listTheme.setMarkedForDeletion(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_MARKED_FOR_DELETION)) > 0);
        listTheme.setTransparent(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_TRANSPARENT)) > 0);
        listTheme.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_UUID)));
        long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_UPDATED));
        Date updated = new Date(dateMillis);
        listTheme.setUpdated(updated);
        // since above set methods set themeDirty to true, this statement must be last set statement
        // so that the cursor's themeDirty prevails
        listTheme.setThemeDirty(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemeSqlTable.COL_THEME_DIRTY)) > 0);

        return listTheme;
    }


    private Cursor getThemeCursorByObjectId(Context context, String objectID) {
        Cursor cursor = null;
        Uri uri = ListThemeSqlTable.CONTENT_URI;
        String[] projection = ListThemeSqlTable.PROJECTION_ALL;
        String selection = ListThemeSqlTable.COL_OBJECT_ID + " = ? AND "
                + ListThemeSqlTable.COL_MARKED_FOR_DELETION + " = ?";
        String selectionArgs[] = new String[]{objectID, String.valueOf(FALSE)};
        String sortOrder = null;

        ContentResolver cr = context.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getThemeCursorByObjectId(): Exception: %s.", e.getMessage());
        }
        return cursor;
    }

    private Cursor getThemeCursorByUuid(String uuid) {
        Cursor cursor = null;
        Uri uri = ListThemeSqlTable.CONTENT_URI;
        String[] projection = ListThemeSqlTable.PROJECTION_ALL;
        String selection = ListThemeSqlTable.COL_UUID + " = ? AND "
                + ListThemeSqlTable.COL_MARKED_FOR_DELETION + " = ?";
        String selectionArgs[] = new String[]{uuid, String.valueOf(FALSE)};
        String sortOrder = null;

        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getThemeCursorByUuid(): Exception: %s.", e.getMessage());
        }
        return cursor;
    }


    @Override
    public ListTheme getListThemeByUuid(String uuid) {
        ListTheme foundListTheme = null;
        Cursor cursor = null;
        try {
            cursor = getThemeCursorByUuid(uuid);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                foundListTheme = ListThemeFromCursor(cursor);
            }
        } catch (Exception e) {
            Timber.e("getListThemeByUuid(): Exception: %s.", e.getMessage());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return foundListTheme;
    }

    @Override
    public List<ListTheme> getAllListThemes(boolean isMarkedForDeletion) {
        List<ListTheme> listThemes = new ArrayList<>();
        ListTheme listTheme;
        Cursor cursor = null;
        try {
            cursor = getAllThemesCursor(isMarkedForDeletion);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    listTheme = ListThemeFromCursor(cursor);
                    listThemes.add(listTheme);
                }
            }
        } catch (Exception e) {
            Timber.e("getAllListThemes(): Exception: %s.", e.getMessage());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return listThemes;
    }


    private Cursor getAllThemesCursor(boolean isMarkedForDeletion) {
        Cursor cursor = null;
        Uri uri = ListThemeSqlTable.CONTENT_URI;
        String[] projection = ListThemeSqlTable.PROJECTION_ALL;
        String selection = ListThemeSqlTable.COL_MARKED_FOR_DELETION + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(FALSE)};
        if (isMarkedForDeletion) {
            selectionArgs = new String[]{String.valueOf(TRUE)};
        }
        String sortOrder = ListThemeSqlTable.SORT_ORDER_NAME_ASC;

        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getAllThemesCursor(): Exception: %s.", e.getMessage());
        }
        return cursor;

    }

    public List<ListTheme> getListThemes(String selection, String selectionArgs[]) {
        List<ListTheme> listThemes = new ArrayList<>();
        ListTheme listTheme;
        Cursor cursor = null;
        try {
            cursor = getThemesCursor(selection, selectionArgs);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    listTheme = ListThemeFromCursor(cursor);
                    listThemes.add(listTheme);
                }
            }
        } catch (Exception e) {
            Timber.e("getListThemes(): Exception: %s.", e.getMessage());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return listThemes;
    }

    private Cursor getThemesCursor(String selection, String selectionArgs[]) {
        Cursor cursor = null;
        Uri uri = ListThemeSqlTable.CONTENT_URI;
        String[] projection = ListThemeSqlTable.PROJECTION_ALL;
        String sortOrder = ListThemeSqlTable.SORT_ORDER_NAME_ASC;

        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getThemesCursor(): Exception: %s.", e.getMessage());
        }
        return cursor;
    }

    //endregion

    //region Update
    @Override
    public void update(ListTheme listTheme, ContentValues contentValues,
                       String selection, String[] selectionArgs,
                       boolean updateBackendless) {
        try {
            Uri uri = ListThemeSqlTable.CONTENT_URI;
            ContentResolver cr = mContext.getContentResolver();

            if (contentValues.containsKey(ListThemeSqlTable.COL_THEME_DIRTY)) {
                // If contentValues more than one key/value pair,
                // make sure that the theme dirty field is set to true.
                if (contentValues.size() > 1) {
                    contentValues.remove(ListThemeSqlTable.COL_THEME_DIRTY);
                    contentValues.put(ListThemeSqlTable.COL_THEME_DIRTY, TRUE);
                }
                // If contentValues has only one key/value pair (e.g. the theme dirty field,
                // then don't change the theme dirty field.
            } else {
                // contentValues do not contain the theme dirty field ... so add it
                contentValues.put(ListThemeSqlTable.COL_THEME_DIRTY, TRUE);
            }

            int numberOfRecordsUpdated = cr.update(uri, contentValues, selection, selectionArgs);
            if (numberOfRecordsUpdated < 1) {
                Timber.e("update(): Error trying to update SQLite db: %s", contentValues.toString());
            } else if (updateBackendless && CommonMethods.isNetworkAvailable()) {
                saveListThemeToBackendless(listTheme);
                // TODO: Send update message to other devices
            }
        } catch (Exception e) {
            Timber.e("update(): Exception: %s.", e.getMessage());
        }
    }


    public void update(ListTheme listTheme, ContentValues contentValues, boolean updateBackendless) {
        String selection = ListThemeSqlTable.COL_UUID + " = '" + listTheme.getUuid() + "'";
        String[] selectionArgs = null;
        update(listTheme, contentValues, selection, selectionArgs, updateBackendless);
    }

    public void update(ListTheme listTheme, String FieldName, boolean value, boolean updateBackendless) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FieldName, value);
        update(listTheme, contentValues, updateBackendless);
    }

    public void update(ListTheme listTheme, String FieldName, float value, boolean updateBackendless) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FieldName, value);
        update(listTheme, contentValues, updateBackendless);
    }

    public void update(ListTheme listTheme, String FieldName, int value, boolean updateBackendless) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FieldName, value);
        update(listTheme, contentValues, updateBackendless);
    }

    public void update(ListTheme listTheme, String FieldName, long value, boolean updateBackendless) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FieldName, value);
        update(listTheme, contentValues, updateBackendless);
    }

    public void update(ListTheme listTheme, String FieldName, String value, boolean updateBackendless) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FieldName, value);
        update(listTheme, contentValues, updateBackendless);
    }

    public void toggle(ListTheme listTheme, String fieldName, boolean updateBackendless) {
        ListTheme currentListTheme = getListThemeByUuid(listTheme.getUuid());
        if (currentListTheme == null) {
            Timber.e("toggle(): Unable to toggle field \"%s\". Could not find ListTheme \"%s\".", fieldName, listTheme.getName());
            return;
        }

        boolean currentValue;
        ContentValues cv = new ContentValues();
        String selection = ListThemeSqlTable.COL_UUID + " = ?'";
        String[] selectionArgs = {listTheme.getUuid() + "'"};

        switch (fieldName) {
            case ListThemeSqlTable.COL_BOLD:
                currentValue = currentListTheme.isBold();
                cv.put(ListThemeSqlTable.COL_BOLD, !currentValue);
                update(listTheme, cv, selection, selectionArgs, updateBackendless);
                break;

            case ListThemeSqlTable.COL_CHECKED:
                currentValue = currentListTheme.isChecked();
                cv.put(ListThemeSqlTable.COL_CHECKED, !currentValue);
                update(listTheme, cv, selection, selectionArgs, updateBackendless);
                break;

            case ListThemeSqlTable.COL_DEFAULT_THEME:
                currentValue = currentListTheme.isDefaultTheme();
                cv.put(ListThemeSqlTable.COL_DEFAULT_THEME, !currentValue);
                update(listTheme, cv, selection, selectionArgs, updateBackendless);
                break;

            case ListThemeSqlTable.COL_THEME_DIRTY:
                currentValue = currentListTheme.isThemeDirty();
                cv.put(ListThemeSqlTable.COL_THEME_DIRTY, !currentValue);
                update(listTheme, cv, selection, selectionArgs, updateBackendless);
                break;

            case ListThemeSqlTable.COL_MARKED_FOR_DELETION:
                currentValue = currentListTheme.isMarkedForDeletion();
                cv.put(ListThemeSqlTable.COL_MARKED_FOR_DELETION, !currentValue);
                update(listTheme, cv, selection, selectionArgs, updateBackendless);
                break;

            case ListThemeSqlTable.COL_TRANSPARENT:
                currentValue = currentListTheme.isTransparent();
                cv.put(ListThemeSqlTable.COL_TRANSPARENT, !currentValue);
                update(listTheme, cv, selection, selectionArgs, updateBackendless);
                break;

            default:
                Timber.e("toggle(): Unknown Field Name! \"%s\"", fieldName);
                break;
        }
    }
    // endregion

    //region Delete
    @Override
    public void delete(String selection, String[] selectionArgs) {
        Uri uri = ListThemeSqlTable.CONTENT_URI;
        ContentResolver cr = mContext.getContentResolver();

        List<ListTheme> themesForDeletion = getListThemes(selection, selectionArgs);

        if (CommonMethods.isNetworkAvailable()) {
            // delete Themes from Backendless
            ArrayList<ListTheme> themesThatFailedDeletion = new ArrayList<>();
            if (themesForDeletion != null) {
                for (ListTheme listTheme : themesForDeletion) {
                    try {
                        // now delete the object
                        Long backendlessDeletionResult = Backendless.Persistence.of(ListTheme.class).remove(listTheme);
                        // TODO: send delete message to other devices
                    } catch (BackendlessException e) {
                        Timber.e("delete() FAILED for \"%s\". BackendlessException: %s.", listTheme.getName(), e.getMessage());
                        themesThatFailedDeletion.add(listTheme);
                    }
                }
            }

            if (themesThatFailedDeletion.size() == 0) {
                // delete Themes from SQLite db
                int numberOfRecordsDeleted = cr.delete(uri, selection, selectionArgs);
                if (numberOfRecordsDeleted < 1) {
                    Timber.e("delete(): Nothing deleted while trying to delete SQLite db object with selection = %s.", selection);
                }
            } else {
                // Some Themes were not deleted from backendless ... so
                // delete only those that were deleted and mark the ones that failed to delete.

                if (themesForDeletion != null) {
                    for (ListTheme listTheme : themesForDeletion) {
                        if (themesThatFailedDeletion.contains(listTheme)) {
                            // mark listTheme for deletion
                            setSQLiteMarkForDeletionFlag(listTheme, TRUE);

                        } else {
                            // delete Theme from SQLite db
                            String deleteSelection = ListThemeSqlTable.COL_UUID + " = '?";
                            String[] deleteSelectionArgs = {listTheme.getUuid() + "'"};
                            int numberOfRecordsDeleted = cr.delete(uri, deleteSelection, deleteSelectionArgs);
                            if (numberOfRecordsDeleted < 1) {
                                Timber.e("delete(): FAILED to delete \"%s\" from the SQLite db.", listTheme.getName());
                            }
                        }
                    }
                }
            }
        } else {
            // mark all listThemes for deletion
            for (ListTheme listTheme : themesForDeletion) {
                setSQLiteMarkForDeletionFlag(listTheme, TRUE);
            }
        }
    }


    public void delete(String uuid) {
        String selection = ListThemeSqlTable.COL_UUID + " = '?";
        String[] selectionArgs = new String[]{uuid + "'"};
        delete(selection, selectionArgs);
    }
    //endregion
}
