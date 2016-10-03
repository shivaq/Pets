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

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;


/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifies a particular Loader being used in this component
    private static final int PET_LOADER = 0;

    // Content Uri for the existing pet (null for it's a new pet)
    private Uri mCurrentPetUri;

    //Check if field has some change
    private boolean mPetHasChanged = false;

    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;


    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // Set touch listener to check if they changed
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        //Examine the intent that was used to launch this Activity
        Intent fromCatalogIntent = getIntent();
//        Uri uriToEdit = Uri.parse(fromCatalogIntent.getStringExtra("URI_TO_EDIT"));
//        Uri uriToEdit = fromCatalogIntent.getParcelableExtra("URI_TO_EDIT");
        mCurrentPetUri = fromCatalogIntent.getData();

        //Change title depend on how this activity launched.
        //And here we set title in code, we don't need label definition of this Activity.
        //So delete label of this Activity in manifest
        if (mCurrentPetUri == null) {
            setTitle(getString(R.string.title_add_a_pet));
            //新規作成モードのときは、オプションメニュー内容を変える ※Delete の選択肢を表示させない
            //invalidateOptionsMenu が起動すると、onPrepareOptionsMenu() が起動する
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.title_edit_pet));
            //Initializes the CursorLoader.
            //The PET_LOADER value is eventually passed to onCreateLoader();
            getLoaderManager().initLoader(PET_LOADER, null, this);
        }

        Log.i(LOG_TAG, "I got Uri  →" + mCurrentPetUri);

    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner using default Adapter.
        // The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    /**
     * Get user input from edittext and save new pet into database
     */
    private void savePet() {

        //2.Convert editText input to each columns values
        String name = mNameEditText.getText().toString().trim();//trim() で前後の不要な空白を削除
        String breed = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();


        //Check if all the fields are empty.
        if (mCurrentPetUri == null && TextUtils.isEmpty(name) && TextUtils.isEmpty(breed) &&
                mGender == PetEntry.GENDER_UNKNOWN && TextUtils.isEmpty(weightString)) {
            //If all the fields are empty, no need to continue. Just leave this Activity
            return;
        }

        //2-2.Empty weightString cannot do parseInt. if its empty, set default value
        int weight = 0;
        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString);
        }

        //3.Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, name);
        values.put(PetEntry.COLUMN_PET_BREED, breed);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);


        if (mCurrentPetUri == null) {
            //4.get ContentResolver and make it insert data
            Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

            //※※ ユーザーに、登録の成否を伝えるのは This is a really critical app-building skill.
            //Toast if insert data was successful
            if (newUri == null) {
                Toast.makeText(this, getResources().getString(R.string.error_registering),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getResources().getString(R.string.pet_register_success),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            //4.get ContentResolver and make it update data
            //Pass in null for the selection and selectionArgs.
            //Because mCurrentPetUri already identify target row to modify.
            int mUpdatedRows = getContentResolver().update(
                    mCurrentPetUri,
                    values,
                    null,
                    null);

            //※※ ユーザーに、登録の成否を伝えるのは This is a really critical app-building skill.
            //Toast if insert data was successful
            if (mUpdatedRows == 0) {
                Toast.makeText(this, getResources().getString(R.string.update_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getResources().getString(R.string.update_succeed),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * メニューが押下されるたびに、表示の直前にコールされる。
     *
     イベントが発生し、メニューをアップデートするときは、
     invalidateOptionsMenu() を呼び出して、
     システムが onPrepareOptionsMenu() を呼び出すようリクエストする
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // 現在のActivity のモードが Add モードかどうかをチェックして、、、
        if (mCurrentPetUri == null) {
            //delete のメニューアイテム参照を取得し、、、
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            //見えなくさせる。
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet();
                finish();//exit EditActivity to CatalogActivity
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:

                // If the pet hasn't changed, continue with navigating up to parent activity
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        //1. define projection
        //Note: _ID must be included. CursorAdapter assumes the Cursor contains a column called _ID
        String[] mProjection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,                 // Parent activity context
                mCurrentPetUri,        // Provider content URI to query
                mProjection,     // Columns to include in the resulting Cursor
                null,            // No selection clause
                null,            // No selection arguments
                null             // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {


        //move cursor to 0th position
        cursor.moveToFirst();

        // Update the editor fields with the data for the current pet
        int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
        int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
        int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
        int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

        String petName = cursor.getString(nameColumnIndex);
        String petBreed = cursor.getString(breedColumnIndex);
        int petGender = cursor.getInt(genderColumnIndex);
        int petWeight = cursor.getInt(weightColumnIndex);


        mNameEditText.setText(petName);
        mBreedEditText.setText(petBreed);
        mGenderSpinner.setSelection(petGender);
        mWeightEditText.setText(String.valueOf(petWeight));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText(null);
        mBreedEditText.setText(null);
        mGenderSpinner.setSelection(PetEntry.GENDER_UNKNOWN);
        mWeightEditText.setText(null);
    }





    //Create touch listener for all fields
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        // Create an AlertDialog.Builder and ,click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set the message
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        // Set positive Button
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);

        // Set negative Button with dialog.dismiss function
        builder.setNegativeButton(
                R.string.keep_editing,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Keep editing" button,
                        // so dismiss the dialog and continue editing the pet.
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
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }


        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

}