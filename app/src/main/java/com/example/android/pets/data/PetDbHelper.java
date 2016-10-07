package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//constant は inner class にいるので、PetEntry まで import する
import com.example.android.pets.data.PetContract.PetEntry;

/*Database helper for Pets app. Manages database creation and version management*/
public class PetDbHelper extends SQLiteOpenHelper {

    //If you change DB schema, you must increment the DB version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "shelter.db";

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
    public void onCreate(SQLiteDatabase db) {//レクチャーによると、デフォが長いからdb にリネームしてるって。

        //Create a String for SQL statement to create pets table
        final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + PetEntry.TABLE_NAME + " ("
                        + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
                        + PetEntry.COLUMN_PET_BREED + " TEXT, "
                        + PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
                        + PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

        //execSQL は static ではないので、インスタンスメソッドなので、インスタンス経由で参照
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + PetEntry.TABLE_NAME;
        db.execSQL(SQL_DELETE_ENTRIES);
    }
}
