package com.chrissetiana.petsapp;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.chrissetiana.petsapp.data.PetContract.PetEntry;
import com.chrissetiana.petsapp.data.PetDbHelper;

import org.w3c.dom.Text;

public class EditorActivity extends AppCompatActivity {

    private EditText nameText, breedText, weightText;
    private Spinner gender;
    private int genderType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        nameText = findViewById(R.id.edit_pet_name);
        breedText = findViewById(R.id.edit_pet_breed);
        weightText = findViewById(R.id.edit_pet_weight);
        gender = findViewById(R.id.spinner_gender);

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
                genderType = 0;
            }
        });
    }

    private void insertPet() {
        String name = nameText.getText().toString().trim();
        String breed = breedText.getText().toString().trim();
        int weight = Integer.parseInt(weightText.getText().toString().trim());

        PetDbHelper helper = new PetDbHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, name);
        values.put(PetEntry.COLUMN_PET_BREED, breed);
        values.put(PetEntry.COLUMN_PET_GENDER, genderType);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        long newRow = db.insert(PetEntry.TABLE_NAME, null, values);
        
        if(newRow == -1) {
            Toast.makeText(this, "Error with saving pet.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Pet saved with id " + newRow, Toast.LENGTH_SHORT).show();
        }
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
                insertPet();
                finish();
                return true;
            case R.id.action_delete:
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 
