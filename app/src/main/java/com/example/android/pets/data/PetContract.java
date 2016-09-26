package com.example.android.pets.data;

import android.provider.BaseColumns;

/**
 * Created by Yasuaki on 2016/09/26.
 */

public final class PetContract {

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PetEntry.TABLE_NAME + " (" +
                    PetEntry._ID + " INTEGER PRIMARY KEY," +
                    PetEntry.COLUMN_PET_NAME + TEXT_TYPE + COMMA_SEP +
                    PetEntry.COLUMN_PET_BREED + TEXT_TYPE + COMMA_SEP +
                    PetEntry.COLUMN_PET_GENDER + INTEGER_TYPE + COMMA_SEP +
                    PetEntry.COLUMN_PET_WEIGHT + INTEGER_TYPE +
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PetEntry.TABLE_NAME;

    public static final class PetEntry implements BaseColumns {

        public static final String TABLE_NAME = "pets";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "name";
        public static final String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_GENDER = "gender";
        public static final String COLUMN_PET_WEIGHT = "weight";

        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
        public static final int GENDER_UNKNOWN = 0;
    }

}
