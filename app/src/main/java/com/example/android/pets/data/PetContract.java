package com.example.android.pets.data;

import android.provider.BaseColumns;

/**
 * Created by Yasuaki on 2016/09/26.
 */

public final class PetContract {

    public static final class Pets implements BaseColumns {

        public static final String TABLE_NAME = "pets";

        public static final String COLUMN_ID ="_id";
        public static final String COLUMN_PET_NAME ="name";
        public static final String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_GENDER = "gender";
        public static final String COLUMN_PET_WEIGHT = "weight";

        public static final int MALE = 1;
        public static final int FEMALE = 2;
        public static final int UNKNOWN = 0;
    }

}
