package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.pets.data.PetContract.PetEntry;


/**
 * Created by Yasuaki on 2016/09/26.
 */

/*Database helper for Pets app. Manages database creation and version management*/
public class PetDbHelper extends SQLiteOpenHelper {

    //If you change DB schema, you must increment the DB version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "pets.db";

    /**
     * Constructs a new instance of PetDbHelper
     *
     * @param context of the app
     */
    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Create String for SQL statement Data types
        final String TEXT_TYPE = " TEXT";
        final String INTEGER_TYPE = " INTEGER";
        final String COMMA_SEP = ",";

        //Create a String for SQL statement to create pets table
        final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + PetEntry.TABLE_NAME + " (" +
                        PetEntry._ID + " INTEGER PRIMARY KEY," +
                        PetEntry.COLUMN_PET_NAME + TEXT_TYPE + COMMA_SEP +
                        PetEntry.COLUMN_PET_BREED + TEXT_TYPE + COMMA_SEP +
                        PetEntry.COLUMN_PET_GENDER + INTEGER_TYPE + COMMA_SEP +
                        PetEntry.COLUMN_PET_WEIGHT + INTEGER_TYPE +
                        " )";

        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(PetContract.SQL_DELETE_ENTRIES);
    }
}
