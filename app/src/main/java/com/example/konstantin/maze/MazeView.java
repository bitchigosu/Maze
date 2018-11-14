package com.example.konstantin.maze;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static com.example.konstantin.maze.MainActivity.handler;
import static com.example.konstantin.maze.MainActivity.handler1;

public class MazeView extends View {

    enum Direction {UP, RIGHT,DOWN, LEFT};

    private final List<Integer> path = new ArrayList<Integer>();
    ArrayList<Cell> neighbours;

    private Cell[][] cells;
    public Cell player, exit, player1, player2;
    private Cell[] players;
    public static final int COLS = 15, ROWS = 15;
    private static final float WALL_THICKNESS = 4;
    private float cellSize, hMargin, vMargin;
    private Paint wallPaint, playerPaint, exitPaint, pathPaint, player1Paint, player2Paint;
    private Random random;
    private int delay = 500;
    private Runnable runnable;
    int i;
    boolean player1move = true;
    MyThread thread = null;


    public MazeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        playerPaint = new Paint();
        playerPaint.setColor(Color.RED);

        player1Paint = new Paint();
        player1Paint.setColor(Color.YELLOW);

        player2Paint = new Paint();
        player2Paint.setColor(Color.GRAY);

        exitPaint = new Paint();
        exitPaint.setColor(Color.BLUE);

        pathPaint = new Paint();
        pathPaint.setColor(Color.GREEN);

        random = new Random();
        createMaze();
    }

    private void moveByPath() {
        if (thread == null)
            thread = new MyThread(0);
        thread.start();
    }
    private boolean searchPath(Cell[][] maze, int x, int y
            , List<Integer> path) {

        int dx,dy;

        if (maze[x][y] == exit) {  //if path point equals exit point - we found the path
            path.add(x);
            path.add(y);
            return true;
        }

        if (!maze[x][y].pathVisited) {
            maze[x][y].pathVisited = true;

            if (!maze[x][y].leftWall) {
                dx = -1;
                dy = 0;
                if (searchPath(maze, x + dx, y + dy, path)) {
                    path.add(x);
                    path.add(y);
                    return true;
                }
            }

            if (!maze[x][y].rightWall) {
                dx = 1;
                dy = 0;
                if (searchPath(maze, x + dx, y + dy, path)) {
                    path.add(x);
                    path.add(y);
                    return true;
                }
            }

            if (!maze[x][y].topWall) {
                dx = 0;
                dy = -1;
                if (searchPath(maze, x + dx, y + dy, path)) {
                    path.add(x);
                    path.add(y);
                    return true;
                }
            }

            if (!maze[x][y].bottomWall) {
                dx = 0;
                dy = 1;
                if (searchPath(maze, x + dx, y + dy, path)) {
                    path.add(x);
                    path.add(y);
                    return true;
                }
            }
        }
        return false;
    }

    private void movement(final int n) throws IndexOutOfBoundsException {
        if (!players[n].bottomWall && !cells[players[n].col][players[n].row + 1].nextVisited) {
            cells[players[n].col][players[n].row + 1].nextVisited = true;
            if (players[n].row != 0 && !cells[players[n].col][players[n].row - 1].nextVisited && !players[n].topWall) {
                MyThread t = new MyThread(n+1);
                t.start();
                // players[n+1] = cells[players[n].col][players[n].row - 1];
            }
            else if (players[n].col != COLS - 1 && !cells[players[n].col + 1][players[n].row].nextVisited && !players[n].rightWall) {
                MyThread t = new MyThread(n+1);
                t.start();
               // players[n+1] = cells[players[n].col + 1][players[n].row];
            }
            else if (players[n].col != 0 && !cells[players[n].col - 1][players[n].row].nextVisited && !players[n].leftWall) {
                MyThread t = new MyThread(n+1);
                t.start();
               // players[n+1] = cells[players[n].col - 1][players[n].row];
            }
            players[n] = cells[players[n].col][players[n].row + 1];

        }

        else if (!players[n].rightWall && !cells[players[n].col + 1][players[n].row].nextVisited) {
            cells[players[n].col + 1][players[n].row].nextVisited = true;
            if (players[n].row != 0 && !cells[players[n].col][players[n].row - 1].nextVisited && !players[n].topWall) {
                MyThread t = new MyThread(n+1);
                t.start();
                players[n+1] = cells[players[n].col][players[n].row - 1];
            }
            else if (players[n].row != ROWS - 1 && !cells[players[n].col][players[n].row + 1].nextVisited && !players[n].bottomWall) {
                MyThread t = new MyThread(n+1);
                t.start();
                players[n+1] = cells[players[n].col][players[n].row + 1];
            }
            else if (players[n].col != 0 && !cells[players[n].col - 1][players[n].row].nextVisited && !players[n].leftWall) {
                MyThread t = new MyThread(n+1);
                t.start();
                players[n+1] = cells[players[n].col - 1][players[n].row];
            }
            players[n] = cells[players[n].col + 1][players[n].row];
        }

        else if (!players[n].topWall && !cells[players[n].col][players[n].row - 1].nextVisited) {
            cells[players[n].col][players[n].row - 1].nextVisited = true;
            if (players[n].row != ROWS - 1 && !cells[players[n].col][players[n].row + 1].nextVisited && !players[n].bottomWall) {
                MyThread t = new MyThread(n+1);
                t.start();
                players[n+1] = cells[players[n].col][players[n].row + 1];
            }
            else if (players[n].col != COLS - 1 && !cells[players[n].col + 1][players[n].row].nextVisited && !players[n].rightWall) {
                MyThread t = new MyThread(n+1);
                t.start();
                players[n+1] = cells[players[n].col + 1][players[n].row];
            }
            else if (players[n].col != 0 && !cells[players[n].col - 1][players[n].row].nextVisited && !players[n].leftWall) {
                MyThread t = new MyThread(n+1);
                t.start();
                players[n+1] = cells[players[n].col - 1][players[n].row];
            }
            players[n] = cells[players[n].col][players[n].row - 1];
        }

        else if (!players[n].leftWall && !cells[players[n].col - 1][players[n].row].nextVisited) {
            cells[players[n].col - 1][players[n].row].nextVisited = true;
            if (players[n].row != 0 && !cells[players[n].col][players[n].row - 1].nextVisited && !players[n].topWall) {
                MyThread t = new MyThread(n+1);
                t.start();
                players[n+1] = cells[players[n].col][players[n].row - 1];
            }
            else if (players[n].col != COLS - 1 && !cells[players[n].col + 1][players[n].row].nextVisited && !players[n].rightWall) {
                MyThread t = new MyThread(n+1);
                t.start();
                players[n+1] = cells[players[n].col + 1][players[n].row];
            }
            else if (players[n].row != ROWS - 1 && !cells[players[n].col][players[n].row + 1].nextVisited && !players[n].bottomWall) {
                MyThread t = new MyThread(n+1);
                t.start();
                players[n+1] = cells[players[n].col][players[n].row + 1];
            }
            players[n] = cells[players[n].col - 1][players[n].row];
        }
    }

    public void movePlayer(int i, Direction direction) {
        switch (direction) {
            case UP:
                try {
                    players[1] = cells[path.get(i - 1)][path.get(i)];
                } catch (IndexOutOfBoundsException e) {
                   // players[1] = exit;
                    Toast.makeText(getContext(),"Finish", Toast.LENGTH_SHORT).show();
                    player1move = false;
                }

                break;
            case DOWN:
                if(!players[0].bottomWall) {
                    players[0]=cells[players[0].col][players[0].row+1];
                }

                break;
            case LEFT:
                if(!player1.leftWall)
                    player1=cells[player1.col-1][player1.row];
                break;
            case RIGHT:
                if(!player2.rightWall)
                    player2=cells[player2.col+1][player2.row];
                break;
        }

        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            moveByPath();
            return true;
        }
        return super.onTouchEvent(event);
    }


    private Cell getNeighbour(Cell cell) {
        neighbours = new ArrayList<>();
        //left neighbour
        if(cell.col>0)
            if (!cells[cell.col-1][cell.row].visited) {
                neighbours.add(cells[cell.col-1][cell.row]);
        }
        //right
        if(cell.col < COLS-1)
            if (!cells[cell.col+1][cell.row].visited) {
                neighbours.add(cells[cell.col + 1][cell.row]);
            }
        //top
        if(cell.row>0)
            if (!cells[cell.col][cell.row-1].visited) {
                neighbours.add(cells[cell.col][cell.row-1]);
            }
        //bottom
        if(cell.row < ROWS-1)
            if (!cells[cell.col][cell.row+1].visited) {
                neighbours.add(cells[cell.col][cell.row+1]);
            }

        if (neighbours.size() > 0) {
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }
        return null;
    }

    private void removeWall(Cell current, Cell next) {
        if (current.col == next.col && current.row == next.row+1) {
            current.topWall = false;
            next.bottomWall = false;
        }
        if (current.col == next.col && current.row == next.row-1) {
            current.bottomWall = false;
            next.topWall = false;
        }
        if (current.col == next.col + 1 && current.row == next.row) {
            current.leftWall = false;
            next.rightWall = false;
        }
        if (current.col == next.col - 1 && current.row == next.row) {
            current.rightWall = false;
            next.leftWall = false;
        }
    }

    public void createMaze() {
        Stack<Cell> stack = new Stack<>();
        Cell current, next;

        cells = new Cell[COLS][ROWS];

        for (int i = 0; i < COLS;i++) {
            for (int j = 0; j < ROWS;j++) {
                cells[i][j] = new Cell(i,j);
            }
        }

        player = cells[0][0];
        player1 = cells[0][0];
        player2 = cells[0][0];//player's position
        exit = cells[COLS-1][ROWS-1]; //finish position

        players = new Cell[] {player, player1, player2};
        current = cells[0][0]; //start creating maze from this point
        current.visited = true;
        do {
            next = getNeighbour(current);
            if (next != null) { //if we find the neighbour
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            } else {
                current = stack.pop(); //backtrack
            }
        } while (!stack.isEmpty());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        int width = getWidth();
        int height = getHeight();

        if (width/height < COLS/ROWS) {
            cellSize = width / (COLS +1);
        } else {
            cellSize = height/(ROWS+1);
        }

        hMargin = (width - COLS*cellSize) / 2;
        vMargin = (height - ROWS*cellSize) / 2;

        canvas.translate(hMargin,vMargin);

        for (int i = 0; i < COLS;i++) {
            for (int j = 0; j < ROWS;j++) {
                if (cells[i][j].topWall)
                    canvas.drawLine(
                            i*cellSize,
                            j*cellSize,
                            (i+1)*cellSize,
                            j*cellSize,
                            wallPaint);

                if (cells[i][j].bottomWall)
                    canvas.drawLine(
                            i*cellSize,
                            (j+1)*cellSize,
                            (i+1)*cellSize,
                            (j+1)*cellSize,
                            wallPaint);
                if (cells[i][j].leftWall)
                    canvas.drawLine(
                            i*cellSize,
                            j*cellSize,
                            i*cellSize,
                            (j+1)*cellSize,
                            wallPaint);
                if (cells[i][j].rightWall)
                    canvas.drawLine(
                            (i+1)*cellSize,
                            j*cellSize,
                            (i+1)*cellSize,
                            (j+1)*cellSize,
                            wallPaint);
            }
        }

        float margin = cellSize/10;

        canvas.drawRect(
                players[0].col*cellSize+margin,
                players[0].row*cellSize+margin,
                    (players[0].col+1)*cellSize-margin,
                    (players[0].row+1)*cellSize-margin,
                    playerPaint
        );

        canvas.drawRect(
                players[1].col*cellSize+margin,
                players[1].row*cellSize+margin,
                (players[1].col+1)*cellSize-margin,
                (players[1].row+1)*cellSize-margin,
                player1Paint
        );

        canvas.drawRect(
                players[2].col*cellSize+margin,
                players[2].row*cellSize+margin,
                (players[2].col+1)*cellSize-margin,
                (players[2].row+1)*cellSize-margin,
                player2Paint
        );

        canvas.drawRect(
                exit.col*cellSize+margin,
                exit.row*cellSize+margin,
                (exit.col+1)*cellSize-margin,
                (exit.row+1)*cellSize-margin,
                exitPaint
        );
        searchPath(cells,0,0,path);
        canvas.translate(hMargin,vMargin - 2.5f*cellSize);

        //Drawing path
        pathPaint.setStrokeWidth(4);
        pathPaint.setColor(Color.GREEN);
        for (int p = 0; p < path.size(); p += 2) {
            try {
                int pathX = path.get(p);
                int pathY = path.get(p + 1);
                int pathXX = path.get(p + 2);
                int pathYY = path.get(p + 3);
                canvas.drawLine(
                        pathX * cellSize,
                        pathY * cellSize,
                        pathXX * cellSize,
                        pathYY * cellSize,
                        pathPaint);
            } catch (IndexOutOfBoundsException e) {
                int pathX = path.get(p);
                int pathY = path.get(p + 1);
                int pathXX = path.get(path.size()-2);
                int pathYY = path.get(path.size()-1);
                canvas.drawLine(
                        pathX * cellSize,
                        pathY * cellSize,
                        pathXX * cellSize,
                        pathYY * cellSize,
                        pathPaint);
            }
        }
    }

    public class Cell {
        boolean
        topWall = true,
        leftWall = true,
        bottomWall = true,
        rightWall = true,
        visited = false,
        pathVisited = false,
        nextVisited = false;

        int col, row;

        Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }

    public class MyThread extends Thread {
        int n;
        MyThread(int n) {
            this.n = n;
        }
        @Override
        public void run() {
            try {
                ((Activity)getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        movement(n);
                        invalidate();
                    }
                });
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
