package com.lbconsulting.a1list.domain.repositories;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.listTheme.impl.DeleteListThemeFromCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTheme.impl.DeleteListThemesFromCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTheme.impl.SaveListThemeToCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTheme.impl.SaveListThemesToCloud_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.DeleteListThemeFromCloud;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.DeleteListThemesFromCloud;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.SaveListThemeToCloud;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.SaveListThemesToCloud;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.storage.ListThemesSqlTable;
import com.lbconsulting.a1list.threading.MainThreadImpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import timber.log.Timber;


/**
 * This class provided CRUD operations for ListTheme
 * NOTE: All CRUD operations should run on a background thread
 */
public class ListThemeRepository_Impl implements ListThemeRepository,
        SaveListThemesToCloud.Callback,
        SaveListThemeToCloud.Callback,
        DeleteListThemeFromCloud.Callback,
        DeleteListThemesFromCloud.Callback {

    private static final int FALSE = 0;
    private static final int TRUE = 1;
    private final Context mContext;

    public ListThemeRepository_Impl(Context context) {
        // private constructor
        this.mContext = context;
    }

    // CRUD operations

    //region Insert ListTheme
    @Override
    public List<ListTheme> insert(List<ListTheme> listThemes) {
        List<ListTheme> successfullyInsertedListThemes = insertIntoLocalStorage(listThemes);
        if (successfullyInsertedListThemes.size() > 0) {
            insertInCloud(successfullyInsertedListThemes);
        }
        return successfullyInsertedListThemes;
    }

    //region Create
    @Override
    public boolean insert(ListTheme listTheme) {
        boolean successfullySavedIntoLocalStorage = insertIntoLocalStorage(listTheme);
        if (successfullySavedIntoLocalStorage) {
            insertInCloud(listTheme);
        }
        return successfullySavedIntoLocalStorage;
    }

    @Override
    public List<ListTheme> insertIntoLocalStorage(List<ListTheme> listThemes) {
        List<ListTheme> successfullyInsertedListThemes = new ArrayList<>();
        for (ListTheme listTheme : listThemes) {
            if (insertIntoLocalStorage(listTheme)) {
                successfullyInsertedListThemes.add(listTheme);
            }
        }

        if (successfullyInsertedListThemes.size() == listThemes.size()) {
            Timber.i("insertIntoLocalStorage(): Successfully inserted all %d ListThemes into SQLiteDb.", listThemes.size());
        } else {
            Timber.e("insertIntoLocalStorage(): Only inserted %d out of %d ListThemes into SQLiteDb.",
                    successfullyInsertedListThemes.size(), listThemes.size());
        }
        return successfullyInsertedListThemes;
    }

    @Override
    public boolean insertIntoLocalStorage(ListTheme listTheme) {
        boolean result = false;
        long newListThemeSqlId = -1;

        Uri uri = ListThemesSqlTable.CONTENT_URI;
        listTheme.setUpdated(Calendar.getInstance().getTime());
        ContentValues cv = makeListThemeContentValues(listTheme);
        ContentResolver cr = mContext.getContentResolver();
        Uri newListThemeUri = cr.insert(uri, cv);

        if (newListThemeUri != null) {
            newListThemeSqlId = Long.parseLong(newListThemeUri.getLastPathSegment());
        }

        if (newListThemeSqlId > -1) {
            // successfully saved new ListTheme to the SQLite db
            result = true;
            Timber.i("insertIntoLocalStorage(): ListThemeRepository_Impl: Successfully inserted \"%s\" into the SQLite db.", listTheme.getName());
        } else {
            // failed to create listTheme in the SQLite db
            Timber.e("insertIntoLocalStorage(): ListThemeRepository_Impl: FAILED to insert \"%s\" into the SQLite db.", listTheme.getName());
        }

        return result;
    }

    public static ContentValues makeListThemeContentValues(ListTheme listTheme) {

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

        cv.put(ListThemesSqlTable.COL_THEME_DIRTY, TRUE);
        cv.put(ListThemesSqlTable.COL_BOLD, (listTheme.isBold()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_CHECKED, (listTheme.isChecked()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_DEFAULT_THEME, (listTheme.isDefaultTheme()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_MARKED_FOR_DELETION, (listTheme.isMarkedForDeletion()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_STRUCK_OUT, (listTheme.isStruckOut()) ? TRUE : FALSE);
        cv.put(ListThemesSqlTable.COL_TRANSPARENT, (listTheme.isTransparent()) ? TRUE : FALSE);
        Date updatedDateTime = listTheme.getUpdated();
        if (updatedDateTime != null) {
            cv.put(ListThemesSqlTable.COL_UPDATED, updatedDateTime.getTime());
        }
        return cv;
    }

    @Override
    public void insertInCloud(List<ListTheme> listThemes) {
        updateInCloud(listThemes, true);
    }

    @Override
    public void insertInCloud(ListTheme listTheme) {
        updateInCloud(listTheme, true);
    }
    //endregion

//
//    private void saveListThemeToBackendless(final ListTheme listTheme) {
//        new SaveListThemeToCloud_InBackground(ThreadExecutor.getInstance(),
//                MainThreadImpl.getInstance(), this, listTheme).execute();
//    }
//    
//    private int updateSQLiteDb(ListTheme listTheme, ContentValues cv) {
//        int numberOfRecordsUpdated = 0;
//        try {
//            Uri uri = ListThemesSqlTable.CONTENT_URI;
//            ContentResolver cr = mContext.getContentResolver();
//            String selection = ListThemesSqlTable.COL_UUID + " = ?";
//            String[] selectionArgs = new String[]{listTheme.getUuid()};
//            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);
//
//        } catch (Exception e) {
//            Timber.e("updateInLocalStorage(): Exception: %s.", e.getMessage());
//        }
//        if (numberOfRecordsUpdated != 1) {
//            Timber.e("updateInLocalStorage(): Error updating AppSettings with uuid = %s", listTheme.getUuid());
//        }
//        return numberOfRecordsUpdated;
//    }


    //endregion

    //region Read ListTheme


    @Override
    public ListTheme retrieveListThemeByUuid(String uuid) {
        ListTheme foundListTheme = null;
        Cursor cursor = null;
        try {
            cursor = getThemeCursorByUuid(uuid);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                foundListTheme = listThemeFromCursor(cursor);
            }
        } catch (Exception e) {
            Timber.e("retrieveListThemeByUuid(): Exception: %s.", e.getMessage());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return foundListTheme;
    }

    public static ListTheme listThemeFromCursor(Cursor cursor) {
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

        return listTheme;
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

    @Override
    public List<ListTheme> retrieveDirtyListThemes() {
        List<ListTheme> dirtyListThemes = new ArrayList<>();
        Cursor cursor = getDirtyListThemesCursor();
        ListTheme listTheme;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                listTheme = listThemeFromCursor(cursor);
                dirtyListThemes.add(listTheme);
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        return dirtyListThemes;
    }


    private Cursor getDirtyListThemesCursor() {
        Cursor cursor = null;
        Uri uri = ListThemesSqlTable.CONTENT_URI;
        String[] projection = ListThemesSqlTable.PROJECTION_ALL;
        String selection = ListThemesSqlTable.COL_THEME_DIRTY + " = ?";
        String selectionArgs[] = new String[]{String.valueOf(TRUE)};
        String sortOrder = null;
        ContentResolver cr = mContext.getContentResolver();
        try {
            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Timber.e("getDirtyListThemesCursor(): Exception: %s.", e.getMessage());
        }
        return cursor;
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
    public int retrieveNumberOfStruckOutListThemes() {
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
            Timber.e("retrieveNumberOfStruckOutListThemes(): Exception: %s.", e.getMessage());
        }

        return struckOutListThemes;
    }


//    public List<ListTheme> getListThemes(String selection, String selectionArgs[]) {
//        List<ListTheme> listThemes = new ArrayList<>();
//        ListTheme listTheme;
//        Cursor cursor = null;
//        try {
//            cursor = getThemesCursor(selection, selectionArgs);
//            if (cursor != null && cursor.getCount() > 0) {
//                while (cursor.moveToNext()) {
//                    listTheme = listThemeFromCursor(cursor);
//                    listThemes.add(listTheme);
//                }
//            }
//        } catch (Exception e) {
//            Timber.e("getListThemes(): Exception: %s.", e.getMessage());
//
//        } finally {
//            if (cursor != null && !cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//
//        return listThemes;
//    }

//    private Cursor getThemesCursor(String selection, String selectionArgs[]) {
//        Cursor cursor = null;
//        Uri uri = ListThemesSqlTable.CONTENT_URI;
//        String[] projection = ListThemesSqlTable.PROJECTION_ALL;
//        String sortOrder = ListThemesSqlTable.SORT_ORDER_NAME_ASC;
//
//        ContentResolver cr = mContext.getContentResolver();
//        try {
//            cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
//        } catch (Exception e) {
//            Timber.e("getThemesCursor(): Exception: %s.", e.getMessage());
//        }
//        return cursor;
//    }

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

    public boolean listThemeNameExists(String listThemeName) {
        boolean itemExists = false;
        ListTheme listItemFromName = getListTheme(listThemeName);
        if (listItemFromName != null) {
            // The proposed ListTheme name is in the SQLite db.
            itemExists = true;
        }
        return itemExists;
    }

    //endregion

    //region Update
    @Override
    public void update(List<ListTheme> listThemes) {
        List<ListTheme> successfullyUpdatedListThemesInLocalStorage = updateInLocalStorage(listThemes);
        if (successfullyUpdatedListThemesInLocalStorage.size() > 0) {
            updateInCloud(successfullyUpdatedListThemesInLocalStorage, false);
        }
    }

    @Override
    public void update(ListTheme listTheme) {
        if (updateInLocalStorage(listTheme) == 1) {
            updateInCloud(listTheme, false);
        }
    }

    @Override
    public List<ListTheme> updateInLocalStorage(List<ListTheme> listThemes) {
        List<ListTheme> successfullyUpdatedListThemes = new ArrayList<>();
        for (ListTheme listTheme : listThemes) {
            if (updateInLocalStorage(listTheme) == 1) {
                successfullyUpdatedListThemes.add(listTheme);
            }
        }

        if (successfullyUpdatedListThemes.size() == listThemes.size()) {
            Timber.i("updateInLocalStorage(): All %d ListThemes updated.", listThemes.size());
        } else {
            Timber.e("updateInLocalStorage(): Only %d of %d ListThemes updated.",
                    successfullyUpdatedListThemes.size(), listThemes.size());
        }
        return successfullyUpdatedListThemes;
    }

    @Override
    public int updateInLocalStorage(ListTheme listTheme) {
        listTheme.setUpdated(Calendar.getInstance().getTime());
        ContentValues cv = makeListThemeContentValues(listTheme);
        int numberOfRecordsUpdated = updateInLocalStorage(listTheme, cv);
        if (numberOfRecordsUpdated == 1) {
            Timber.i("updateInLocalStorage(): Successfully updated \"%s\" in the SQLiteDb.", listTheme.getName());
        } else {
            Timber.e("updateInLocalStorage(): FAILED to update \"%s\" in the SQLiteDb.", listTheme.getName());
        }
        return numberOfRecordsUpdated;
    }

    private int updateInLocalStorage(ListTheme listTheme, ContentValues cv) {
        int numberOfRecordsUpdated = 0;
        try {
            Uri uri = ListThemesSqlTable.CONTENT_URI;
            String selection = ListThemesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTheme.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            numberOfRecordsUpdated = cr.update(uri, cv, selection, selectionArgs);

        } catch (Exception e) {
            Timber.e("updateInLocalStorage(): Exception: %s.", e.getMessage());
        }

        return numberOfRecordsUpdated;

    }

//    public int toggle(ListTheme listTheme, String fieldName) {
//        int result = 0;
//        ListTheme currentListTheme = retrieveListThemeByUuid(listTheme.getUuid());
//        if (currentListTheme == null) {
//            Timber.e("toggle(): Unable to toggle field \"%s\". Could not find ListTheme \"%s\".", fieldName, listTheme.getName());
//            return 0;
//        }
//
//        boolean newValue;
//        ContentValues cv = new ContentValues();
//        switch (fieldName) {
//            case ListThemesSqlTable.COL_BOLD:
//                newValue = !currentListTheme.isBold();
//                if (newValue) {
//                    result++;
//                } else {
//                    result--;
//                }
//                cv.put(ListThemesSqlTable.COL_BOLD, newValue ? TRUE : FALSE);
//                cv.put(ListThemesSqlTable.COL_THEME_DIRTY, TRUE);
//                updateInLocalStorage(listTheme, cv);
//                break;
//
//            case ListThemesSqlTable.COL_CHECKED:
//                newValue = !currentListTheme.isChecked();
//                if (newValue) {
//                    result++;
//                } else {
//                    result--;
//                }
//                cv.put(ListThemesSqlTable.COL_CHECKED, newValue ? TRUE : FALSE);
//                cv.put(ListThemesSqlTable.COL_THEME_DIRTY, TRUE);
//                updateInLocalStorage(listTheme, cv);
//                break;
//
//            case ListThemesSqlTable.COL_DEFAULT_THEME:
//                newValue = !currentListTheme.isDefaultTheme();
//                if (newValue) {
//                    result++;
//                } else {
//                    result--;
//                }
//                cv.put(ListThemesSqlTable.COL_DEFAULT_THEME, newValue ? TRUE : FALSE);
//                cv.put(ListThemesSqlTable.COL_THEME_DIRTY, TRUE);
//                updateInLocalStorage(listTheme, cv);
//                break;
//
//            case ListThemesSqlTable.COL_MARKED_FOR_DELETION:
//                newValue = !currentListTheme.isMarkedForDeletion();
//                if (newValue) {
//                    result++;
//                } else {
//                    result--;
//                }
//                cv.put(ListThemesSqlTable.COL_MARKED_FOR_DELETION, newValue ? TRUE : FALSE);
//                cv.put(ListThemesSqlTable.COL_THEME_DIRTY, TRUE);
//                updateInLocalStorage(listTheme, cv);
//                break;
//
//            case ListThemesSqlTable.COL_TRANSPARENT:
//                newValue = !currentListTheme.isTransparent();
//                if (newValue) {
//                    result++;
//                } else {
//                    result--;
//                }
//                cv.put(ListThemesSqlTable.COL_TRANSPARENT, newValue ? TRUE : FALSE);
//                cv.put(ListThemesSqlTable.COL_THEME_DIRTY, TRUE);
//                updateInLocalStorage(listTheme, cv);
//                break;
//
//            case ListThemesSqlTable.COL_STRUCK_OUT:
//                newValue = !currentListTheme.isStruckOut();
//                if (newValue) {
//                    result++;
//                } else {
//                    result--;
//                }
//                cv.put(ListThemesSqlTable.COL_STRUCK_OUT, newValue ? TRUE : FALSE);
//                cv.put(ListThemesSqlTable.COL_THEME_DIRTY, TRUE);
//                updateInLocalStorage(listTheme, cv);
//                break;
//
//            default:
//                Timber.e("toggle(): Unknown Field Name! \"%s\"", fieldName);
//                break;
//        }
//        return result;
//    }

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
    public int applyTextSizeAndMarginsToAllListThemes(ListTheme sourceListTheme) {
        List<ListTheme> listThemesUpdatedInLocalStorage = new ArrayList<>();
        // retrieve all ListThemes
        List<ListTheme> allListThemes = retrieveAllListThemes(false);
        for (ListTheme listTheme : allListThemes) {
            if (!sourceListTheme.getUuid().equals(listTheme.getUuid())) {
                listTheme.setTextSize(sourceListTheme.getTextSize());
                listTheme.setHorizontalPaddingInDp(sourceListTheme.getHorizontalPaddingInDp());
                listTheme.setVerticalPaddingInDp(sourceListTheme.getVerticalPaddingInDp());

                if (updateTextSizeAndMarginsInLocalStorage(sourceListTheme, listTheme)) {
                    listThemesUpdatedInLocalStorage.add(listTheme);
                }
            }
        }

        if (listThemesUpdatedInLocalStorage.size() > 0) {
            updateInCloud(listThemesUpdatedInLocalStorage, false);
        }

        return listThemesUpdatedInLocalStorage.size();
    }

    private boolean updateTextSizeAndMarginsInLocalStorage(ListTheme sourceListTheme, ListTheme listTheme) {
        boolean result = false;
        ContentValues cv = new ContentValues();
        cv.put(ListThemesSqlTable.COL_TEXT_SIZE, sourceListTheme.getTextSize());
        cv.put(ListThemesSqlTable.COL_HORIZONTAL_PADDING_IN_DP, sourceListTheme.getHorizontalPaddingInDp());
        cv.put(ListThemesSqlTable.COL_VERTICAL_PADDING_IN_DP, sourceListTheme.getVerticalPaddingInDp());
        cv.put(ListThemesSqlTable.COL_THEME_DIRTY, TRUE);

        if (updateInLocalStorage(listTheme, cv) == 1) {
            result = true;
        }
        return result;
    }

    @Override
    public void updateInCloud(List<ListTheme> listThemes, boolean isNew) {
        List<ListTheme> listThemesThatDoNotHaveObjectIds = new ArrayList<>();
        List<ListTheme> listThemesThatHaveObjectIds = new ArrayList<>();
        if (!isNew) {
            // If the listTheme is not new ... make sure that it has a Backendless objectId.
            for (ListTheme listTheme : listThemes) {
                if (listTheme.getObjectId() == null || listTheme.getObjectId().isEmpty()) {
                    ListTheme existingListTheme = retrieveListThemeByUuid(listTheme.getUuid());
                    if (existingListTheme.getObjectId() == null || existingListTheme.getObjectId().isEmpty()) {
                        listThemesThatDoNotHaveObjectIds.add(listTheme);
                    } else {
                        listTheme.setObjectId(existingListTheme.getObjectId());
                        listThemesThatHaveObjectIds.add(listTheme);
                    }
                } else {
                    listThemesThatHaveObjectIds.add(listTheme);
                }
            }

            new SaveListThemesToCloud_InBackground(ThreadExecutor.getInstance(),
                    MainThreadImpl.getInstance(), this, listThemesThatHaveObjectIds).execute();

            for (ListTheme listTheme : listThemesThatDoNotHaveObjectIds) {
                Timber.e("updateInCloud(): Unable to update \"%s\" in the Cloud. No Backendless objectId available!",
                        listTheme.getName());
            }

        } else {
            new SaveListThemesToCloud_InBackground(ThreadExecutor.getInstance(),
                    MainThreadImpl.getInstance(), this, listThemes).execute();
        }
    }

    @Override
    public void onListThemesSavedToCloud(String successMessage, List<ListTheme> successfullySavedListThemes) {
        Timber.i("onListThemesSavedToCloud(): %s.", successMessage);
    }

    @Override
    public void onListThemesSaveToCloudFailed(String errorMessage, List<ListTheme> successfullySavedListThemes) {
        Timber.e("onListThemesSaveToCloudFailed(): %s.", errorMessage);
    }

    @Override
    public void updateInCloud(ListTheme listTheme, boolean isNew) {
        // If the listTheme is not new ... make sure that it has a Backendless objectId.
        if (!isNew) {
            if (listTheme.getObjectId() == null || listTheme.getObjectId().isEmpty()) {
                ListTheme existingListTheme = retrieveListThemeByUuid(listTheme.getUuid());
                listTheme.setObjectId(existingListTheme.getObjectId());
            }
            if (listTheme.getObjectId() == null || listTheme.getObjectId().isEmpty()) {
                // The listTheme is not new AND there is no Backendless objectId available ... so,
                // Unable to update the listTheme in Backendless
                Timber.e("updateInCloud(): Unable to update \"%s\" in the Cloud. No Backendless objectId available!",
                        listTheme.getName());
                return;
            }
        }

        new SaveListThemeToCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, listTheme).execute();
    }


    @Override
    public void onListThemeSavedToCloud(String successMessage) {
        Timber.i("onListThemeSavedToCloud(): %s", successMessage);
    }

    @Override
    public void onListThemeSaveToCloudFailed(String errorMessage) {
        Timber.e("onListThemeSaveToCloudFailed(): %s", errorMessage);
    }

    // endregion


    //region Delete

    // TODO: Make sure that there is at least one ListTheme ... can't delete default ListTheme
    @Override
    public int delete(List<ListTheme> listThemes) {
        List<ListTheme> successfullyMarkedForDeletionListThemes = deleteFromLocalStorage(listThemes);
        if (successfullyMarkedForDeletionListThemes.size() > 0) {
            deleteFromCloud(successfullyMarkedForDeletionListThemes);
        }

        return successfullyMarkedForDeletionListThemes.size();
    }

    @Override
    public int delete(ListTheme listTheme) {
        ListTheme defaultListTheme = retrieveDefaultListTheme();
        int numberOfDeletedListThemes = deleteFromLocalStorage(listTheme, defaultListTheme);
        if (numberOfDeletedListThemes == 1) {
            deleteFromCloud(listTheme);
        }

        return numberOfDeletedListThemes;
    }

    @Override
    public List<ListTheme> deleteFromLocalStorage(List<ListTheme> listThemes) {
        ListTheme defaultListTheme = retrieveDefaultListTheme();
        List<ListTheme> successfullyMarkedForDeletionListThemes = new ArrayList<>();
        for (ListTheme listTheme : listThemes) {
            if (deleteFromLocalStorage(listTheme, defaultListTheme) == 1) {
                successfullyMarkedForDeletionListThemes.add(listTheme);
            }
        }

        if (successfullyMarkedForDeletionListThemes.size() == listThemes.size()) {
            Timber.i("deleteFromLocalStorage(): Successfully marked all %d ListThemes for deletion.",
                    successfullyMarkedForDeletionListThemes.size());
        } else {
            Timber.e("deleteFromLocalStorage(): Only marked %d of of %d ListThemes for deletion.",
                    successfullyMarkedForDeletionListThemes.size(), listThemes.size());
        }

        return successfullyMarkedForDeletionListThemes;
    }

    @Override
    public int deleteFromLocalStorage(ListTheme listTheme, ListTheme defaultListTheme) {
        int numberOfDeletedListThemes = 0;
        try {
            Uri uri = ListThemesSqlTable.CONTENT_URI;
            String selection = ListThemesSqlTable.COL_UUID + " = ?";
            String[] selectionArgs = new String[]{listTheme.getUuid()};
            ContentResolver cr = mContext.getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(ListThemesSqlTable.COL_UPDATED, Calendar.getInstance().getTimeInMillis());
            cv.put(ListThemesSqlTable.COL_MARKED_FOR_DELETION, String.valueOf(TRUE));
            cv.put(ListThemesSqlTable.COL_STRUCK_OUT, String.valueOf(FALSE));
            numberOfDeletedListThemes = cr.update(uri, cv, selection, selectionArgs);
            if (numberOfDeletedListThemes == 1) {
                AndroidApplication.getListTitleRepository().replaceListTheme(listTheme, defaultListTheme);
                Timber.i("deleteFromLocalStorage(): Successfully marked \"%s\" for deletion.", listTheme.getName());
            } else {
                Timber.e("deleteFromLocalStorage(): FAILED to marked \"%s\" for deletion.", listTheme.getName());
            }

        } catch (Exception e) {
            Timber.e("delete(): Exception: %s.", e.getMessage());
        }

        return numberOfDeletedListThemes;
    }

    @Override
    public void deleteFromCloud(List<ListTheme> listThemes) {
        new DeleteListThemesFromCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, listThemes).execute();
    }

    @Override
    public void onListThemesDeletedFromCloud(String successMessage) {
        Timber.i("onListThemesDeletedFromCloud(): %s.", successMessage);
    }

    @Override
    public void onListThemesDeleteFromCloudFailed(String errorMessage) {
        Timber.e("onListThemesDeleteFromCloudFailed(): %s.", errorMessage);
    }

    @Override
    public void deleteFromCloud(ListTheme listTheme) {
        new DeleteListThemeFromCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, listTheme).execute();
    }

    @Override
    public void onListThemeDeletedFromCloud(String successMessage) {
        Timber.i("onListThemeDeletedFromCloud(): %s.", successMessage);
    }

    @Override
    public void onListThemeDeleteFromCloudFailed(String errorMessage) {
        Timber.e("onListThemeDeleteFromCloudFailed(): %s.", errorMessage);
    }
    //endregion
}
