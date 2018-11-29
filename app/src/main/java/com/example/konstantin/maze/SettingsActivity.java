package com.example.konstantin.maze;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SettingsActivity extends AppCompatActivity {

    private Spinner spinner;

    private int pQuantity;
    private MazeView maze;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        maze = new MazeView(this, null);
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(this, R.array.playersNum, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                String choose = parent.getItemAtPosition(selectedItemPosition).toString();
                pQuantity = Integer.parseInt(choose);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    public int getpQuantity() {
        return pQuantity;
    }
}

