package com.example.konstantin.maze;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.design.widget.TabLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.konstantin.maze.MazeTestView.COLS;
import static com.example.konstantin.maze.MazeTestView.N;
import static com.example.konstantin.maze.MazeTestView.ROWS;

public class MainActivity extends AppCompatActivity {

    Intent intent;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);

        ViewPager viewPager = findViewById(R.id.viewpager);
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.slidingTabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void saveMaze() {
        Cell[][] cells = MazeTestView.cells;
        for (int i = 0; i < COLS; i++)
            for (int j = 0; j < ROWS; j++) {
                cells[i][j].toVisit = false;
                for (int k = 0; k < N; k++) {
                    cells[i][j].mVisited[k] = false;
                }
            }
        try {
            File file = new File (this.getFilesDir(), "" + Calendar.getInstance().getTimeInMillis() + ".txt");
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file,false);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
            outputStream.writeObject(cells);
            outputStream.close();
            fileOutputStream.close();
            Toast.makeText(this, "Maze saved successful!", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error with saving!", Toast.LENGTH_LONG).show();
        }
    }

    private void loadMaze() {
        List<String> fileList = new ArrayList<>();
        File dir = getFilesDir();
        File[] subFiles = dir.listFiles();

        if (subFiles != null) {
            for (File file : subFiles) {
                fileList.add(file.getAbsolutePath());
            }
            ArrayAdapter<String> directoryList = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
            listView.setAdapter(directoryList);
            listView.setVisibility(View.VISIBLE);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String path = (String) parent.getItemAtPosition(position);
                    try {
                        ViewPager viewPager = findViewById(R.id.viewpager);
                        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
                        viewPager.setAdapter(adapter);

                        TabLayout tabLayout = findViewById(R.id.slidingTabs);
                        tabLayout.setupWithViewPager(viewPager);

                        FileInputStream fileInputStream = new FileInputStream(path);
                        ObjectInputStream in = new ObjectInputStream(fileInputStream);
                        MazeTestView.setCells((Cell[][]) in.readObject());
                        in.close();
                        fileInputStream.close();
                        listView.setVisibility(View.GONE);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_restart:
                ViewPager viewPager = findViewById(R.id.viewpager);
                SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
                viewPager.setAdapter(adapter);

                TabLayout tabLayout = findViewById(R.id.slidingTabs);
                tabLayout.setupWithViewPager(viewPager);
                break;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_save:
                saveMaze();
                break;
            case R.id.action_load:
                loadMaze();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}
