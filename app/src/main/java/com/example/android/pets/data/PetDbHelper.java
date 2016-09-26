package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Yasuaki on 2016/09/26.
 */

/*Database helper for Pets app. Manages database creation and version management*/
public class PetDbHelper extends SQLiteOpenHelper{

    //If you change DB schema, you must increment the DB version
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "pets.db";

    /**
     * Constructs a new instance of PetDbHelper
     *
     * @param context of the app
     */
    public PetDbHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


        sqLiteDatabase.execSQL(PetContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(PetContract.SQL_DELETE_ENTRIES);
    }
}
