package com.chrissetiana.petsapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.chrissetiana.petsapp.data.PetContract;

public class PetsAdapter extends CursorAdapter {

    public PetsAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.activity_layout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView pet_name = view.findViewById(R.id.pet_name);
        TextView pet_summary = view.findViewById(R.id.pet_summary);

        int nameColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME);
        int breedColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED);

        String petName = cursor.getString(nameColumnIndex);
        String petSummary = cursor.getString(breedColumnIndex);

        pet_name.setText(petName);
        pet_summary.setText(petSummary);
    }
}