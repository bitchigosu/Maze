package com.example.konstantin.maze;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maze);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new GraphFragment()).commit();

    }
}
