package com.chrissetiana.petsapp;

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
import android.widget.TextView;

import com.chrissetiana.petsapp.data.PetContract.PetEntry;

public class CatalogActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    private void displayDatabaseInfo() {
        String[] projection = {PetEntry._ID, PetEntry.COLUMN_PET_NAME, PetEntry.COLUMN_PET_BREED, PetEntry.COLUMN_PET_GENDER, PetEntry.COLUMN_PET_WEIGHT};

        Cursor cursor = getContentResolver().query(PetEntry.CONTENT_URI, projection, null, null, null);

        TextView text = findViewById(R.id.text_view_pet);

        try {
            text.setText("Number of rows in pets database table: " + cursor.getCount() + "\n\n");
            text.append(PetEntry._ID + " - " + PetEntry.COLUMN_PET_NAME + " - " + PetEntry.COLUMN_PET_BREED + " - " + PetEntry.COLUMN_PET_GENDER + " - " + PetEntry.COLUMN_PET_WEIGHT + "\n");

            int id_columnIndex = cursor.getColumnIndex(PetEntry._ID);
            int name_columnIndex = cursor.getColumnIndex((PetEntry.COLUMN_PET_NAME));
            int breed_columnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int gender_columnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weight_columnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            while ((cursor.moveToNext())) {
                int currId = cursor.getInt(id_columnIndex);
                String currName = cursor.getString(name_columnIndex);
                String currBreed = cursor.getString(breed_columnIndex);
                String currGender = cursor.getString(gender_columnIndex);
                int currWeight = cursor.getInt(weight_columnIndex);

                text.append("\n" + currId + " - " + currName + " - " + currBreed + " - " + currGender + " - " + currWeight);
            }
        } finally {
            cursor.close();
        }
    }

    private void insertPet() {
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertPet();
                displayDatabaseInfo();
                return true;
            case R.id.action_delete_all_entries:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
