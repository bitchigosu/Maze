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
import static com.example.konstantin.maze.MazeTestView.setN;

public class MainActivity extends AppCompatActivity {

    Intent intent;
    ListView listView, listQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        listQuantity = findViewById(R.id.listQuantity);

        restartView();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void saveMaze() {
        Cell[][] cells = MazeTestView.cells;
        try {
            File file = new File (this.getFilesDir(), "" + Calendar.getInstance().getTimeInMillis() + ".txt");
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file,false);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
            outputStream.writeObject(cells);
            outputStream.close();
            fileOutputStream.close();
            Toast.makeText(this, "Maze saved successfully!", Toast.LENGTH_SHORT).show();

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
                        restartView();

                        FileInputStream fileInputStream = new FileInputStream(path);
                        ObjectInputStream in = new ObjectInputStream(fileInputStream);
                        MazeTestView.setCells((Cell[][]) in.readObject());
                        in.close();
                        fileInputStream.close();

                        listView.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Maze loaded successfully!", Toast.LENGTH_SHORT).show();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Maze load was failed!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void loadLastMaze() {
        List<String> fileList = new ArrayList<>();
        File dir = getFilesDir();
        File[] subFiles = dir.listFiles();

        if (subFiles != null) {
            for (File file : subFiles) {
                fileList.add(file.getAbsolutePath());
            }
            ArrayAdapter<String> directoryList = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
            String path = (String) fileList.get(fileList.size() - 1);
            try {
                restartView();
                FileInputStream fileInputStream = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileInputStream);
                MazeTestView.setCells((Cell[][]) in.readObject());
                in.close();
                fileInputStream.close();
                Toast.makeText(this, "Quantity of players was changed", Toast.LENGTH_SHORT).show();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Quantity of players wasn't changed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void restartView () {
        ViewPager viewPager = findViewById(R.id.viewpager);
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.slidingTabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void showPlayersQuantity() {
        ArrayAdapter<?> quantityList = ArrayAdapter.createFromResource(this, R.array.playersNum,android.R.layout.simple_list_item_1);
        listQuantity.setAdapter(quantityList);
        listQuantity.setVisibility(View.VISIBLE);
        listQuantity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                saveMaze();
                setN(position + 1);
                loadLastMaze();
                listQuantity.setVisibility(View.GONE);
            }
        });
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
                restartView();
                break;
            case R.id.action_save:
                saveMaze();
                break;
            case R.id.action_load:
                loadMaze();
                break;
            case R.id.players_number:
                showPlayersQuantity();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}
