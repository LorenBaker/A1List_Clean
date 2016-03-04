package com.lbconsulting.a1list.domain.repositories;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.storage.ListThemesSqlTable;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import timber.log.Timber;


/**
 * This class provided CRUD operations for ListTheme
 * NOTE: All CRUD operations should run on a background thread
 */
public class ListThemeRepository_Impl implements ListThemeRepository_interface {

    private final int FALSE = 0;
    private final int TRUE = 1;
    private final Context mContext;

    public ListThemeRepository_Impl(Context context) {
        // private constructor
        this.mContext = context;

    }

    // CRUD operations

    //region Create
    @Override
    public ListTheme insert(ListTheme listTheme) {
        // insert new listTheme into SQLite db
        ListTheme backendlessResponse = null;
        long newThemeSqlId = -1;
        listTheme.setThemeDirty(true);

        Uri uri = ListThemesSqlTable.CONTENT_URI;
        ContentValues cv = new ContentValues();
        cv.put(ListThemesSqlTable.COL_NAME, listTheme.getName());
        cv.put(ListThemesSqlTable.COL_UUID, listTheme.getUuid());
        cv.put(ListThemesSqlTable.COL_OBJECT_ID, listTheme.getObjectId());
        cv.put(ListThemesSqlTable.COL_START_COLOR, listTheme.getStartColor());
        cv.put(ListThemesSqlTable.COL_END_COLOR, listTheme.getEndColor());
        cv.put(ListThemesSqlTable.COL_TEXT_COLOR, listTheme.getTextColor());
        cv.put(ListThemesSqlTable.COL_TEXT_SIZE, listTheme.getTextSize());
        cv.put(ListThemesSqlTable.COL_HORIZONTAL_PADDING_IN_DP, listTheme.getHorizontalPaddingInDp());
        cv.put(ListThemesSqlTable.COL_VERTICAL_PADDING_IN_DP, listTheme.getVerticalPaddingInDp());

        cv.put(ListThemesSqlTable.COL_THEME_DIRTY, (listTheme.isThemeDirty()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_BOLD, (listTheme.isBold()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_CHECKED, (listTheme.isChecked()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_DEFAULT_THEME, (listTheme.isDefaultTheme()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_MARKED_FOR_DELETION, (listTheme.isMarkedForDeletion()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_TRANSPARENT, (listTheme.isTransparent()) ? TRUE : FALSE);
        Date updatedDateTime = listTheme.getUpdated();
        if (updatedDateTime != null) {
            cv.put(ListThemesSqlTable.COL_UPDATED, updatedDateTime.getTime());
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
                // save listTheme to Backendless
                backendlessResponse = saveListThemeToBackendless(listTheme);
                // TODO: send message to Backendless to notify other devices of the new ListTheme
            }

        } else {
            // failed to create listTheme in the SQLite db
            Timber.e("insert(): ListThemeRepository_Impl: FAILED to insert \"%s\" into the SQLite db.", listTheme.getName());
        }

        return backendlessResponse;
    }


    private ListTheme saveListThemeToBackendless(final ListTheme listTheme) {
        // saveListThemeToBackendless object synchronously
        ListTheme response = null;
        String objectId = listTheme.getObjectId();
        final boolean isNew = objectId == null || objectId.isEmpty();
        try {
            response = Backendless.Data.of(ListTheme.class).save(listTheme);
            Timber.i("saveListThemeToBackendless(): successfully saved \"%s\" to Backendless.", response.getName());
            // Update the SQLite db: set dirty to false, and updated date and time
            ContentValues cv = new ContentValues();
            Date updatedDate = response.getUpdated();
            if (updatedDate == null) {
                updatedDate = response.getCreated();
            }
            if (updatedDate != null) {
                long updated = updatedDate.getTime();
                cv.put(ListThemesSqlTable.COL_UPDATED, updated);
            }
            cv.put(ListThemesSqlTable.COL_THEME_DIRTY, FALSE);

            // If a new ListTheme, update SQLite db with objectID
            if (isNew) {
                cv.put(ListThemesSqlTable.COL_OBJECT_ID, response.getObjectId());
            }
            // update the SQLite db ... but don't send changes to Backendless
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
        cv.put(ListThemesSqlTable.COL_THEME_DIRTY, dirtyFlag);
        update(listTheme, cv, false);
    }

    private void setSQLiteMarkForDeletionFlag(ListTheme listTheme, int markedForDeletionFlag) {
        ContentValues cv = new ContentValues();
        cv.put(ListThemesSqlTable.COL_MARKED_FOR_DELETION, markedForDeletionFlag);
        update(listTheme, cv, false);
    }

    //endregion

    //region Read

    private ListTheme listThemeFromCursor(Cursor cursor) {
        ListTheme listTheme = new ListTheme();
        listTheme.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_ID)));
        listTheme.setObjectId(cursor.getString(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_OBJECT_ID)));
        listTheme.setName(cursor.getString(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_NAME)));
        listTheme.setStartColor(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_START_COLOR)));
        listTheme.setEndColor(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_END_COLOR)));
        listTheme.setTextColor(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_TEXT_COLOR)));
        listTheme.setTextSize(cursor.getFloat(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_TEXT_SIZE)));
        listTheme.setHorizontalPaddingInDp(cursor.getFloat(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_HORIZONTAL_PADDING_IN_DP)));
        listTheme.setVerticalPaddingInDp(cursor.getFloat(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_VERTICAL_PADDING_IN_DP)));
        listTheme.setBold(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_BOLD)) > 0);
        listTheme.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_CHECKED)) > 0);
        listTheme.setDefaultTheme(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_DEFAULT_THEME)) > 0);
        listTheme.setMarkedForDeletion(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_MARKED_FOR_DELETION)) > 0);
        listTheme.setStruckOut(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_STRUCK_OUT)) > 0);
        listTheme.setTransparent(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_TRANSPARENT)) > 0);
        listTheme.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_UUID)));
        long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_UPDATED));
        Date updated = new Date(dateMillis);
        listTheme.setUpdated(updated);
        // since above set methods set themeDirty to true, this statement must be last set statement
        // so that the cursor's themeDirty prevails
        listTheme.setThemeDirty(cursor.getInt(cursor.getColumnIndexOrThrow(ListThemesSqlTable.COL_THEME_DIRTY)) > 0);

        return listTheme;
    }


    private Cursor getThemeCursorByObjectId(Context context, String objectID) {
        Cursor cursor = null;
        Uri uri = ListThemesSqlTable.CONTENT_URI;
        String[] projection = ListThemesSqlTable.PROJECTION_ALL;
        String selection = ListThemesSqlTable.COL_OBJECT_ID + " = ? AND "
                + ListThemesSqlTable.COL_MARKED_FOR_DELETION + " = ?";
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
        Uri uri = ListThemesSqlTable.CONTENT_URI;
        String[] projection = ListThemesSqlTable.PROJECTION_ALL;
        String selection = ListThemesSqlTable.COL_UUID + " = ? AND "
                + ListThemesSqlTable.COL_MARKED_FOR_DELETION + " = ?";
        String selectionArgs[] = new String[]{uuid, String.valueOf(FALSE)};
        String sortOrder = null;
        try {
            ContentResolver cr = mContext.getContentResolver();
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
                foundListTheme = listThemeFromCursor(cursor);
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
    public List<ListTheme> retrieveAllListThemes(boolean isMarkedForDeletion) {
        List<ListTheme> listThemes = new ArrayList<>();
        ListTheme listTheme;
        Cursor cursor = null;
        try {
            cursor = getAllThemesCursor(isMarkedForDeletion);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    listTheme = listThemeFromCursor(cursor);
                    listThemes.add(listTheme);
                }
            }
        } catch (Exception e) {
            Timber.e("retrieveAllListThemes(): Exception: %s.", e.getMessage());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return listThemes;
    }

    @Override
    public ListTheme retrieveDefaultListTheme() {
        ListTheme defaultListTheme = null;
        Cursor cursor = null;
        Uri uri = ListThemesSqlTable.CONTENT_URI;
        String[] projection = ListThemesSqlTable.PROJECTION_ALL;
        String selection = ListThemesSqlTable.COL_DEFAULT_THEME + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(TRUE)};
        String sortOrder = ListThemesSqlTable.SORT_ORDER_NAME_ASC;
        try {
            ContentResolver cr = mContext.getContentResolver();
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                defaultListTheme = listThemeFromCursor(cursor);
            } else {
                // No Theme has it's default flag set
                // so randomly pick a ListTheme
                List<ListTheme> listThemes = retrieveAllListThemes(false);
                if (listThemes.size() > 0) {
                    Random r = new Random();
                    int listThemeIndex = r.nextInt(listThemes.size());
                    defaultListTheme = listThemes.get(listThemeIndex);
                }
            }

            if (defaultListTheme == null) {
                Timber.e("retrieveDefaultListTheme(): Did not retrieve the default ListTheme!");
            }

        } catch (Exception e) {
            Timber.e("retrieveDefaultListTheme(): Exception: %s.", e.getMessage());

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return defaultListTheme;
    }

    @Override
    public List<ListTheme> retrieveStruckOutListThemes() {
        List<ListTheme> struckOutListThemes = new ArrayList<>();
        Cursor cursor = null;
        Uri uri = ListThemesSqlTable.CONTENT_URI;
        String[] projection = ListThemesSqlTable.PROJECTION_ALL;
        String selection = ListThemesSqlTable.COL_STRUCK_OUT + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(TRUE)};
        String sortOrder = null;

        ContentResolver cr = mContext.getContentResolver();
        ListTheme struckOutListTheme;
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null & cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    struckOutListTheme = listThemeFromCursor(cursor);
                    struckOutListThemes.add(struckOutListTheme);
                }
            }
            cursor.close();

        } catch (Exception e) {
            Timber.e("retrieveStruckOutListThemes(): Exception: %s.", e.getMessage());
        }

        return struckOutListThemes;
    }

    @Override
    public int getNumberOfStruckOutListThemes() {
        int struckOutListThemes = 0;
        Cursor cursor = null;
        Uri uri = ListThemesSqlTable.CONTENT_URI;
        String[] projection = new String[]{ListThemesSqlTable.COL_ID};
        String selection = ListThemesSqlTable.COL_STRUCK_OUT + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(TRUE)};
        String sortOrder = null;
        try {
            ContentResolver cr = mContext.getContentResolver();
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                struckOutListThemes = cursor.getCount();
                cursor.close();
            }

        } catch (Exception e) {
            Timber.e("getNumberOfStruckOutListThemes(): Exception: %s.", e.getMessage());
        }

        return struckOutListThemes;
    }


    private Cursor getAllThemesCursor(boolean isMarkedForDeletion) {
        Cursor cursor = null;
        Uri uri = ListThemesSqlTable.CONTENT_URI;
        String[] projection = ListThemesSqlTable.PROJECTION_ALL;
        String selection = ListThemesSqlTable.COL_MARKED_FOR_DELETION + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(FALSE)};
        if (isMarkedForDeletion) {
            selectionArgs = new String[]{String.valueOf(TRUE)};
        }
        String sortOrder = ListThemesSqlTable.SORT_ORDER_NAME_ASC;

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
                    listTheme = listThemeFromCursor(cursor);
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
        Uri uri = ListThemesSqlTable.CONTENT_URI;
        String[] projection = ListThemesSqlTable.PROJECTION_ALL;
        String sortOrder = ListThemesSqlTable.SORT_ORDER_NAME_ASC;

        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getThemesCursor(): Exception: %s.", e.getMessage());
        }
        return cursor;
    }

    private ListTheme getListTheme(String listThemeName) {
        ListTheme result = null;
        Cursor cursor = null;
        Uri uri = ListThemesSqlTable.CONTENT_URI;
        String selection = ListThemesSqlTable.COL_NAME + " = ?";
        String[] selectionArgs = new String[]{listThemeName};
        String[] projection = ListThemesSqlTable.PROJECTION_ALL;
        String sortOrder = null;

        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    result = listThemeFromCursor(cursor);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Timber.e("getListTheme(): Exception: %s.", e.getMessage());
        }

        return result;
    }

    public boolean isValidThemeName(ListTheme originalListTheme, String proposedListThemeName) {
        boolean isValidName = false;
        ListTheme listThemeFromName = getListTheme(proposedListThemeName);
        if (listThemeFromName == null) {
            // The proposed ListTheme name is not in the SQLite db.
            isValidName = true;
        } else {
            // A ListTheme with the proposed name exists in the SQLite db ...
            // so, check its Uuid with the original ListTheme
            if (originalListTheme.getUuid().equals(listThemeFromName.getUuid())) {
                // both the original and existing ListTheme are the same object
                isValidName = true;
            }
        }

        return isValidName;
    }
    //endregion

    //region Update
    @Override
    public boolean update(ListTheme listTheme, ContentValues contentValues, boolean updateBackendless) {
        boolean result = false;
        try {
            Uri uri = ListThemesSqlTable.CONTENT_URI;
            String selection = ListThemesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTheme.getUuid()};
            ContentResolver cr = mContext.getContentResolver();

            if (contentValues.containsKey(ListThemesSqlTable.COL_THEME_DIRTY)) {
                // If contentValues more than one key/value pair,
                // make sure that the theme dirty field is set to true.
                if (contentValues.size() > 1) {
                    contentValues.remove(ListThemesSqlTable.COL_THEME_DIRTY);
                    contentValues.put(ListThemesSqlTable.COL_THEME_DIRTY, TRUE);
                }
                // If contentValues has only one key/value pair (e.g. the theme dirty field,
                // then don't change the theme dirty field.
            } else {
                // contentValues do not contain the theme dirty field ... so add it
                contentValues.put(ListThemesSqlTable.COL_THEME_DIRTY, TRUE);
            }

            int numberOfRecordsUpdated = cr.update(uri, contentValues, selection, selectionArgs);
            if (numberOfRecordsUpdated < 1) {
                Timber.e("update(): Error trying to update SQLite db: %s", listTheme.toString());
            } else if (updateBackendless && CommonMethods.isNetworkAvailable()) {
                result = true;
                saveListThemeToBackendless(listTheme);
                // TODO: Send update message to other devices
            }
        } catch (Exception e) {
            Timber.e("update(): Exception: %s.", e.getMessage());
        }

        return result;
    }

    @Override
    public boolean update(ListTheme listTheme, boolean updateBackendless) {
        ContentValues cv = new ContentValues();

        cv.put(ListThemesSqlTable.COL_NAME, listTheme.getName());
        cv.put(ListThemesSqlTable.COL_START_COLOR, listTheme.getStartColor());
        cv.put(ListThemesSqlTable.COL_END_COLOR, listTheme.getEndColor());
        cv.put(ListThemesSqlTable.COL_TEXT_COLOR, listTheme.getTextColor());
        cv.put(ListThemesSqlTable.COL_TEXT_SIZE, listTheme.getTextSize());
        cv.put(ListThemesSqlTable.COL_HORIZONTAL_PADDING_IN_DP, listTheme.getHorizontalPaddingInDp());
        cv.put(ListThemesSqlTable.COL_VERTICAL_PADDING_IN_DP, listTheme.getVerticalPaddingInDp());
        cv.put(ListThemesSqlTable.COL_BOLD, (listTheme.isBold()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_CHECKED, (listTheme.isChecked()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_DEFAULT_THEME, (listTheme.isDefaultTheme()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_MARKED_FOR_DELETION, (listTheme.isMarkedForDeletion()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_STRUCK_OUT, (listTheme.isStruckOut()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_TRANSPARENT, (listTheme.isTransparent()) ? TRUE : FALSE);

        return update(listTheme, cv, updateBackendless);
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

    public int toggle(ListTheme listTheme, String fieldName, boolean updateBackendless) {
        int result = 0;
        ListTheme currentListTheme = getListThemeByUuid(listTheme.getUuid());
        if (currentListTheme == null) {
            Timber.e("toggle(): Unable to toggle field \"%s\". Could not find ListTheme \"%s\".", fieldName, listTheme.getName());
            return 0;
        }

        boolean newValue;
        ContentValues cv = new ContentValues();
        switch (fieldName) {
            case ListThemesSqlTable.COL_BOLD:
                newValue = !currentListTheme.isBold();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListThemesSqlTable.COL_BOLD, newValue ? TRUE : FALSE);
                update(listTheme, cv, updateBackendless);
                break;

            case ListThemesSqlTable.COL_CHECKED:
                newValue = !currentListTheme.isChecked();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListThemesSqlTable.COL_CHECKED, newValue ? TRUE : FALSE);
                update(listTheme, cv, updateBackendless);
                break;

            case ListThemesSqlTable.COL_DEFAULT_THEME:
                newValue = !currentListTheme.isDefaultTheme();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListThemesSqlTable.COL_DEFAULT_THEME, newValue ? TRUE : FALSE);
                update(listTheme, cv, updateBackendless);
                break;

            case ListThemesSqlTable.COL_THEME_DIRTY:
                newValue = !currentListTheme.isThemeDirty();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListThemesSqlTable.COL_THEME_DIRTY, newValue ? TRUE : FALSE);
                update(listTheme, cv, updateBackendless);
                break;

            case ListThemesSqlTable.COL_MARKED_FOR_DELETION:
                newValue = !currentListTheme.isMarkedForDeletion();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListThemesSqlTable.COL_MARKED_FOR_DELETION, newValue ? TRUE : FALSE);
                update(listTheme, cv, updateBackendless);
                break;

            case ListThemesSqlTable.COL_TRANSPARENT:
                newValue = !currentListTheme.isTransparent();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListThemesSqlTable.COL_TRANSPARENT, newValue ? TRUE : FALSE);
                update(listTheme, cv, updateBackendless);
                break;

            case ListThemesSqlTable.COL_STRUCK_OUT:
                newValue = !currentListTheme.isStruckOut();
                if (newValue) {
                    result++;
                } else {
                    result--;
                }
                cv.put(ListThemesSqlTable.COL_STRUCK_OUT, newValue ? TRUE : FALSE);
                update(listTheme, cv, updateBackendless);
                break;

            default:
                Timber.e("toggle(): Unknown Field Name! \"%s\"", fieldName);
                break;
        }
        return result;
    }

    public void clearDefaultFlag() {
        try {
            Uri uri = ListThemesSqlTable.CONTENT_URI;
            String selection = ListThemesSqlTable.COL_DEFAULT_THEME + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(TRUE)};
            ContentResolver cr = mContext.getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(ListThemesSqlTable.COL_DEFAULT_THEME, FALSE);
            int numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);
            if (numberOfRecordsUpdated < 1) {
                Timber.e("clearDefaultFlag(): No default ListTheme found in the SQLite db.");
            }
        } catch (Exception e) {
            Timber.e("clearDefaultFlag(): Exception: %s.", e.getMessage());
        }
    }

    @Override
    public int applyTextSizeAndMarginsToAllListThemes(ListTheme sourceListTheme, boolean updateBackendless) {
        int numberOfUpdatedListThemes = 0;
        // retrieve all ListThemes
        List<ListTheme> allListThemes = retrieveAllListThemes(false);
        for (ListTheme listTheme : allListThemes) {
            if (!sourceListTheme.getUuid().equals(listTheme.getUuid())) {
                numberOfUpdatedListThemes += updateTextSizeAndMargins(sourceListTheme, listTheme);
            }
        }

        return numberOfUpdatedListThemes;
    }

    private int updateTextSizeAndMargins(ListTheme sourceListTheme, ListTheme listTheme) {
        int result = 0;
        ContentValues cv = new ContentValues();
        cv.put(ListThemesSqlTable.COL_TEXT_SIZE, sourceListTheme.getTextSize());
        cv.put(ListThemesSqlTable.COL_HORIZONTAL_PADDING_IN_DP, sourceListTheme.getHorizontalPaddingInDp());
        cv.put(ListThemesSqlTable.COL_VERTICAL_PADDING_IN_DP, sourceListTheme.getVerticalPaddingInDp());

        listTheme.setTextSize(sourceListTheme.getTextSize());
        listTheme.setHorizontalPaddingInDp(sourceListTheme.getHorizontalPaddingInDp());
        listTheme.setVerticalPaddingInDp(sourceListTheme.getVerticalPaddingInDp());

        if (update(listTheme, cv, true)) {
            result++;
        }
        return result;
    }

    // endregion

    //region Delete

    @Override
    public int delete(ListTheme listTheme) {
        int numberOfDeletedListThemes = 0;
        try {
            Uri uri = ListThemesSqlTable.CONTENT_URI;
            String selection = ListThemesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTheme.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            numberOfDeletedListThemes = cr.delete(uri, selection, selectionArgs);
        } catch (Exception e) {
            Timber.e("delete(): Exception: %s.", e.getMessage());
        }

        return numberOfDeletedListThemes;
    }

    @Override
    public int markDeleted(ListTheme listTheme) {
        int numberOfDeletedListThemes = 0;
        try {
            Uri uri = ListThemesSqlTable.CONTENT_URI;
            String selection = ListThemesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTheme.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(ListThemesSqlTable.COL_MARKED_FOR_DELETION, String.valueOf(TRUE));
            numberOfDeletedListThemes = cr.update(uri, cv, selection, selectionArgs);
        } catch (Exception e) {
            Timber.e("markDeleted(): Exception: %s.", e.getMessage());
        }

        return numberOfDeletedListThemes;
    }

    //endregion
}
