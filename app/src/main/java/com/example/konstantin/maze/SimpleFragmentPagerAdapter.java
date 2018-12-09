package com.example.konstantin.maze;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {
    SimpleFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    private String[] titles=new String[] {"Maze","Graphs"};

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return new MazeFragment();
        else
            return new GraphFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }


}
