package com.example.konstantin.maze;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MazeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maze);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new MazeFragment()).commit();
    }
}
