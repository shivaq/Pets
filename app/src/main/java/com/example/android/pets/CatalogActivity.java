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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;


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

        //1. define projection
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_WEIGHT,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_BREED
        };

        //2.Perform a query on the provider using the ContentResolver.
        Cursor cursor = getContentResolver().query(
                PetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        //3.Find ListView to populate
        ListView petListView = (ListView) findViewById(R.id.list_view_pet);
        //4.Set up CursorAdapter
        PetCursorAdapter petAdapter = new PetCursorAdapter(this, cursor);
        //5.Attach CursorAdapter to the ListView
        petListView.setAdapter(petAdapter);

        //TODO check if cursor need to be closed here
/*        try{
        } finally {
            cursor.close();
        }*/
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

        //1.Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, dummyName);
        values.put(PetEntry.COLUMN_PET_BREED, dummyBreed);
        values.put(PetEntry.COLUMN_PET_GENDER, dummyGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, dummyWeight);

        //Do activity version insert for a new row
        Uri mNewUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
        int newRowId = (int)ContentUris.parseId(mNewUri);

        Toast.makeText(this, getResources().getString(R.string.insert_dummy)
                + newRowId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
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
