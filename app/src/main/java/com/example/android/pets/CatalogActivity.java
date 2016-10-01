/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetProvider;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        Log.i(LOG_TAG,"onCreateしましたよー");
    }

    //To display DB info when user back from other activity
    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {

        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_WEIGHT,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_BREED
        };

        //Note!!! This is a bad behavior to contact with database directly.
        //Instead, do contact with Content Provider!!!

        //Perform a query on the provider using the ContentResolver.
        Cursor cursor = getContentResolver().query(
                PetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        TextView displayView = (TextView) findViewById(R.id.text_view_pet);

        try {
            //Create header in the text view that looks like this:
            //_id - name - breed - gender - weight
            displayView.setText("The pets table contains " + cursor.getCount() + "pets.\n\n");
            displayView.append(PetEntry._ID + " - " + PetEntry.COLUMN_PET_NAME +
                    " - " + PetEntry.COLUMN_PET_BREED + " - " + PetEntry.COLUMN_PET_GENDER +
                    " - " + PetEntry.COLUMN_PET_WEIGHT + "\n");

            //1.Get the index of each column
            int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            //2.Iterate throw all the returned rows in the Cursor to display its contents
            while (cursor.moveToNext()) {
                //2-1.Retrieve each value with column index
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentBreed = cursor.getString(breedColumnIndex);
                int currentGender = cursor.getInt(genderColumnIndex);
                int currentWeight = cursor.getInt(weightColumnIndex);

                //2-2. display this rows values
                displayView.append("\n" + currentID + " - " + currentName + " - " +
                        currentBreed + " - " + currentGender + " - " + currentWeight);
            }

        } finally {
            //Always close cursor when you're done reading from it.
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    private void insertPet() {

        //Define dummy values
        String dummyName = "Toto";
        String dummyBreed = "Terrier";
        int dummyGender = PetEntry.GENDER_MALE;
        int dummyWeight = 7;

        //Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, dummyName);
        values.put(PetEntry.COLUMN_PET_BREED, dummyBreed);
        values.put(PetEntry.COLUMN_PET_GENDER, dummyGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, dummyWeight);

        //Insert the new row, returning the primary key value of the new row
//        long newRowId = mProvider.insert(PetEntry.CONTENT_URI, values);//このプライマリーキわず
        Log.v(LOG_TAG, "just before getContentResolver().insert()");

        Uri mNewUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

        Log.v(LOG_TAG, "New Uri " + mNewUri.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(LOG_TAG, "itemSelected");

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                Log.v(LOG_TAG, "just before insertPet() excuted");
                insertPet();
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
