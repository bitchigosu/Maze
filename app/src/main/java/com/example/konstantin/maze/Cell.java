package com.example.konstantin.maze;

import java.io.Serializable;

import static com.example.konstantin.maze.MazeTestView.N;

class Cell implements Serializable {
    boolean
            topWall = true,
            leftWall = true,
            bottomWall = true,
            rightWall = true,
            visited = false; //for creating maze
    boolean toVisit; //if cell was visited by a player

    boolean[] pathVisited = new boolean[N]; //for searchPath
    boolean[] mVisited = new boolean[N];

    int col, row;

    Cell(int col, int row) {
        this.col = col;
        this.row = row;
    }
}
