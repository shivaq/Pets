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
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

/*
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
*/

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // This is the Adapter being used to display the list's data
    PetCursorAdapter mPetAdapter;

    // Identifies a particular Loader being used in this component
    private static final int PET_LOADER = 0;

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


        // Find the ListView which will be populated with the pet data
        ListView petListView = (ListView) findViewById(R.id.list_view_pet);

        // Find and set empty view on the ListView,
        // so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // 4. Set up empty CursorAdapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mPetAdapter = new PetCursorAdapter(this, null);

        //5.Attach CursorAdapter to the ListView
        petListView.setAdapter(mPetAdapter);

        //Set up onItemClickListener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create Intent from here to EditorActivity
                Intent toEditIntent = new Intent(CatalogActivity.this, EditorActivity.class);

                //Create Uri for selected item
                Uri uriToEdit = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
//                toEditIntent.putExtra("URI_TO_EDIT", uriToEdit);

                // Set the URI on the data field of the intent
                toEditIntent.setData(uriToEdit);
                startActivity(toEditIntent);
            }
        });

        //Initializes the CursorLoader.
        //The PET_LOADER value is eventually passed to onCreateLoader();
        getLoaderManager().initLoader(PET_LOADER, null, this);

/*
どうも、support.v4 を使う場合、getSupportLoaderManager() を使う必要があるらしい。
さらに、getSupportLoaderManager() は、ListActivity に対しては使えないらしい。
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
*/


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
        int newRowId = (int) ContentUris.parseId(mNewUri);

        Toast.makeText(this, getResources().getString(R.string.insert_dummy)
                + newRowId, Toast.LENGTH_SHORT).show();
    }

    private void deletePet(){
        int deletedRows = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);

        if (deletedRows == 0) {
            Toast.makeText(this, getResources().getString(R.string.error_delete), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_em_all);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a new Loader needs to be created
     *
     * @param loaderID contains the ID value passed to the initLoader() call.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        //1. define projection
        //Note: _ID must be included. CursorAdapter assumes the Cursor contains a column called _ID
        String[] mProjection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED
        };

        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,                 // Parent activity context
                PetEntry.CONTENT_URI,        // Provider content URI to query
                mProjection,     // Columns to include in the resulting Cursor
                null,            // No selection clause
                null,            // No selection arguments
                null             // Default sort order
        );
    }

    //Called when a previously created loader has finished loading
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        //mPetAdapters cursor is swapped from null to loaded cursor.
        //The framework will take care of closing the old cursor once we return
        mPetAdapter.swapCursor(cursor);
    }

    //Called when the last Cursor provided to onLoadFinished() above is about to be closed.
    //We need to make sure we are no longer use it.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPetAdapter.swapCursor(null);
    }
}
