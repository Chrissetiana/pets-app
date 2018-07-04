package com.chrissetiana.petsapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.chrissetiana.petsapp.data.PetContract.PetEntry;

public class PetProvider extends ContentProvider {

    private static final String LOG_TAG = PetProvider.class.getSimpleName();
    private static final int PETS = 100;
    private static final int PET_ID = 101;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_NAME, PETS);
        uriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_NAME + "/#", PET_ID);
    }

    private PetDbHelper helper;

    @Override
    public boolean onCreate() {
        helper = new PetDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor;

        int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                cursor = db.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri" + uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        dataValidation(values);

        SQLiteDatabase db = helper.getWritableDatabase();

        int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                long id = db.insert(PetEntry.TABLE_NAME, null, values);
                if (id == -1) {
                    Log.e(LOG_TAG, "Failed to insert row for " + uri);
                    return null;
                }
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        dataValidation(values);

        SQLiteDatabase database = helper.getWritableDatabase();

        int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                long id = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
                if (id == -1) {
                    Log.e(LOG_TAG, "Failed to update table");
                } else {
                    Log.v(LOG_TAG, "Table successfully updated");
                }
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                id = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
                if (id == -1) {
                    Log.e(LOG_TAG, "Failed to update row for " + uri);
                } else {
                    Log.v(LOG_TAG, "Row " + id + " successfully updated");
                }
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    private void dataValidation(ContentValues values) {
        if (values.size() > 0) {
            if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
                String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
                if (name == null) {
                    throw new IllegalArgumentException("Pet requires a name");
                }
            }
            if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
                Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
                if (gender == null || !PetEntry.isValidGender(gender)) {
                    throw new IllegalArgumentException("Pet requires valid gender");
                }
            }
            if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
                Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
                if (weight != null && weight < 0) {
                    throw new IllegalArgumentException("Pet requires valid weight");
                }
            }
        }
    }
}