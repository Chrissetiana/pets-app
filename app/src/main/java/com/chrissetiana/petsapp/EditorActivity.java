package com.chrissetiana.petsapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.chrissetiana.petsapp.data.PetContract.PetEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 0;
    private Uri uri;
    private EditText nameText, breedText, weightText;
    private Spinner gender;
    private int genderType = PetEntry.GENDER_UNKNOWN;
    private boolean hasChanged = false;
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            hasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        uri = intent.getData();

        if (uri == null) {
            setTitle(getString(R.string.editor_activity_title_new_pet));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_pet));
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }

        nameText = findViewById(R.id.edit_pet_name);
        breedText = findViewById(R.id.edit_pet_breed);
        weightText = findViewById(R.id.edit_pet_weight);
        gender = findViewById(R.id.spinner_gender);

        nameText.setOnTouchListener(touchListener);
        breedText.setOnTouchListener(touchListener);
        weightText.setOnTouchListener(touchListener);
        gender.setOnTouchListener(touchListener);

        setupSpinner();
    }

    private void setupSpinner() {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.array_gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        gender.setAdapter(adapter);

        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        genderType = PetEntry.GENDER_MALE;
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        genderType = PetEntry.GENDER_FEMALE;
                    } else {
                        genderType = PetEntry.GENDER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                genderType = PetEntry.GENDER_UNKNOWN;
            }
        });
    }

    private void savePet() {
        String name = nameText.getText().toString().trim();
        String breed = breedText.getText().toString().trim();
        String weightStr = weightText.getText().toString().trim();
        int weight = 0;

        if (uri == null && TextUtils.isEmpty(name) && TextUtils.isEmpty(breed) && TextUtils.isEmpty(weightStr) && genderType == PetEntry.GENDER_UNKNOWN) {
            return;
        }

        if (!TextUtils.isEmpty(weightStr)) {
            weight = Integer.parseInt(weightStr);
        }

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, name);
        values.put(PetEntry.COLUMN_PET_BREED, breed);
        values.put(PetEntry.COLUMN_PET_GENDER, genderType);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        if (uri == null) {
            Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_pet_successful), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rows = getContentResolver().update(PetEntry.CONTENT_URI, values, null, null);
            if (rows == 0) {
                Toast.makeText(this, getString(R.string.editor_update_pet_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_pet_successful), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deletePet() {
        if (uri != null) {
            int rows = getContentResolver().delete(uri, null, null);
            if (rows == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (uri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                savePet();
                finish();
                return true;
            case R.id.action_delete:
                confirmOnDeleteDialog();
                return true;
            case android.R.id.home:
                if (!hasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                confirmOnExitDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT};
        return new CursorLoader(
                this,
                uri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(breedColumnIndex);
            int Gender = cursor.getInt(genderColumnIndex);
            int weight = cursor.getInt(weightColumnIndex);

            nameText.setText(name);
            breedText.setText(breed);
            weightText.setText(Integer.toString(weight));

            switch (Gender) {
                case PetEntry.GENDER_MALE:
                    gender.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    gender.setSelection(2);
                    break;
                default:
                    gender.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameText.setText("");
        breedText.setText("");
        weightText.setText("");
        gender.setSelection(0);
    }

    @Override
    public void onBackPressed() {
        if (!hasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        confirmOnExitDialog(discardButtonClickListener);
    }

    private void confirmOnExitDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void confirmOnDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
