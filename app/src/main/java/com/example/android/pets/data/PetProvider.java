package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.pets.data.PetContract.PetEntry;

import static android.R.attr.id;
import static android.R.attr.key;
import static android.R.attr.name;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    //declare here and make this global
    private PetDbHelper mDbHelper;

    /**
     * URI matcher code for the content URI for the pets table
     */
    private static final int PETS = 100;

    /**
     * URI matcher code for the content URI for a single pet in the pets table
     */
    private static final int PET_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    //Static initializer は、このクラスから何がコールされようが、最初に走るcode
    static {

        //Add all content URI patterns to UriMatcher.
        // Provider should recognize them.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {

        mDbHelper = new PetDbHelper(getContext());//Activity のコンテキストをゲットして渡す
        return true;
    }

    /**
     * Perform the query for the given URI.
     * Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get access to database with read mode
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                //Use arguments for selection, selectionArgs and sortOrder.
                // No need to specify where clause here. Do request for entire table.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PET_ID:

                //the PET_ID code uri pattern requests for specific row.
                // So construct a where clause.
                selection = PetEntry._ID + "=?";
                //valueOf: see the popup for this method.
                //ContentUris.parseId(uri): converts last path segment to long.
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                //This cursor is for return value
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //Set notification URI on the Cursor.
        // So we know what content URI the Cursor was created for.

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    //We separate insert() to two shorter method. insert and insertPet.
    //We choose not they remains longer method.
    /**
     * Insert a pet into the database with the given content values.
     * Return the new content URI for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {

        //Get name from ContentValues and do sanity check
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null) {//null は駄目。空欄はよし。となる。入力しなくてもOK 状態。
            throw new IllegalArgumentException("Pet requires a name");
        }

        // No need to check the breed, any value is valid (including null).

        //isValidGender is defined in Contract
        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if(gender == null || !PetEntry.isValidGender(gender)){
            throw new IllegalArgumentException("Pet requires valid gender");
        }

        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }


        //1.Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();//データレポジトリ取得。書き込みモード

        //2.Do provider version insert.
        long id = db.insert(PetEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //Insert() triggers notification of data change of this uri for this uri for all listeners
        //$2: notify 対象。nullだと、CursorAdapter がデフォルトで通知を受ける
        getContext().getContentResolver().notifyChange(uri, null);

        //3.Return the new URI with the ID appended to the end of it.
        //           New row ID is automatically retrieved.
        return ContentUris.withAppendedId(uri, id);
    }


    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    //this is ContentProviders update() method, differ from SQLDatabases update() method in its parameters.
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:

                //row ID was passed from Activity to resolver to Provider.
                selection = PetEntry._ID + "=?";

                //extracting out the ID with ContentUris.parseId(uri)
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values.
     * Apply the changes to the rows specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     *
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        //1.Do sanity check
        //Update time name check: No need to check "name". In update time, its OK if name is null.

        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {//I thought no keys, no null check. Key exists, no null check.
            // But keys presence and values presence is separated case... Of course you need to do it!
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        //Insert/Update time breed check: No need to check the "breed", any value is valid (including null).

        //Check if ContentValue contains gender for update target.
        if(values.containsKey(PetEntry.COLUMN_PET_GENDER)){
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if(gender == null || !PetEntry.isValidGender(gender)){
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        //Check if ContentValue contains weight for update target.
        if(values.containsKey(PetEntry.COLUMN_PET_WEIGHT)){
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        //2.get db obj
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //3. do update
        //4. get the number of updated rows
        //Perform the update on the database and get the number of rows affected
        int rowsUpdated = db.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);

        if(rowsUpdated != 0){
            //Notify all listeners that the data at the given uri has changed
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    //Returns the number of deleted rows.
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        //Track the number of rows that were deleted
        int rowsDeleted;

        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);

                if(rowsDeleted != 0){
                    //Notify all listeners that the data at the given uri has changed
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsDeleted;
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);

                if(rowsDeleted != 0){
                    //Notify all listeners that the data at the given uri has changed
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}