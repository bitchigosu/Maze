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

import static com.example.konstantin.maze.MainActivity.handler1;

public class MazeView extends View {

    Canvas canvas;

    private final List<Integer> path = new ArrayList<>();
    private final List<Integer> path1 = new ArrayList<>();
    private final List<Integer> path2 = new ArrayList<>();
    ArrayList<Cell> neighbours;

    private Cell[][] cells;
    public Cell player, exit, player1, player2;
    private Cell[] players;
    public static final int COLS = 15, ROWS = 15;
    private static final float WALL_THICKNESS = 4;
    private float cellSize, hMargin, vMargin;
    private Paint wallPaint, playerPaint, exitPaint, pathPaint, player1Paint, player2Paint, wall1Paint, visitedPath;
    private Random random;
    private volatile int delay = 500;
    private Runnable runnable1,runnable2,runnable3;
    private Runnable[] runnable;
    MyThread thread1,thread2, thread3;
    MyThread threads[];
    volatile boolean[] isAlive;
    public int path1Size, path2Size;


    public MazeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        wall1Paint = new Paint();
        wall1Paint.setColor(Color.GRAY);

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
        pathPaint.setStrokeWidth(4);

        visitedPath = new Paint();
        visitedPath.setColor(Color.WHITE);

        random = new Random();
        createMaze();
    }

    private void moveByPath(int n) {
        if (threads[n] == null) {
            threads[n] = new MyThread();
            threads[n].setN(n);
        }
        threads[n].start();
    }

    // check if we got stuck
    private boolean tooPick(int n) {
        if ((players[n].bottomWall && players[n].topWall && players[n].rightWall)
                || (players[n].bottomWall && players[n].topWall && players[n].leftWall)
                || (players[n].bottomWall && players[n].leftWall && players[n].rightWall)
                || (players[n].topWall && players[n].rightWall && players[n].leftWall))
            return true;
        return false;
    }

    private boolean searchPath(Cell[][] maze, int x, int y, int destX, int destY
            , List<Integer> path, int n) {

        int dx,dy;

        if (maze[x][y] == cells[destX][destY]) {  //if path point equals exit point - we found the path
            path.add(x);
            path.add(y);
            return true;
        }

        if (!maze[x][y].pathVisited[n]) {
            maze[x][y].pathVisited[n] = true;
            maze[x][y].goalVisited[n] = true;

            if (!maze[x][y].leftWall) {
                dx = -1;
                dy = 0;
                if (searchPath(maze, x + dx, y + dy, destX, destY, path, n)) {
                    path.add(x);
                    path.add(y);
                    return true;
                }
            }

            if (!maze[x][y].rightWall) {
                dx = 1;
                dy = 0;
                if (searchPath(maze, x + dx, y + dy, destX, destY, path, n)) {
                    path.add(x);
                    path.add(y);
                    return true;
                }
            }

            if (!maze[x][y].topWall) {
                dx = 0;
                dy = -1;
                if (searchPath(maze, x + dx, y + dy, destX, destY, path, n)) {
                    path.add(x);
                    path.add(y);
                    return true;
                }
            }

            if (!maze[x][y].bottomWall) {
                dx = 0;
                dy = 1;
                if (searchPath(maze, x + dx, y + dy, destX, destY, path, n)) {
                    path.add(x);
                    path.add(y);
                    return true;
                }
            }
        }
        return false;
    }

    private void moveThreads(int n, int x, int y) {
        if (n < 3) {
            if (threads[n] == null) {
                threads[n] = new MyThread();
            }
            threads[n].setN(n);
            if (!isAlive[n]) {
                switch (n) {
                    case 1:
                            searchPath(cells, players[n].row, players[n].col, x, y, path1, n);
                            path1Size = path1.size() - 1;
                            threads[1].start();
                            break;

                    case 2:
                            searchPath(cells, players[n].row, players[n].col, x, y, path2, n);
                            path2Size = path2.size() - 1;
                            threads[2].start();
                            break;
                }

            }
        }
    }

    private void moveByPath1(int n,int i) {
        try {
            players[n].busy = true;
            players[n] = cells[path1.get(i - 1)][path1.get(i)];
            players[n].goalVisited[n] = true;
            players[n].toVisit = true;
            if (i == 0 || i == 1) {
                players[n].busy = false;
               // players[n] = cells[path1.get(0)][path1.get(1)];
            }
        } catch (IndexOutOfBoundsException e) {
            //players[n] = cells[path1.get(0)][path1.get(1)];
            players[n].busy = false;
            threads[n] = null;
            moveByPath(n);
        }
    }

    private void moveByPath2(int n,int i) {
        try {
            players[n] = cells[path2.get(i - 1)][path2.get(i)];
            players[n].goalVisited[n] = true;
            players[n].toVisit = true;
            if (i == 0 || i == 1) {
                players[n].busy = false;
                // players[n] = cells[path1.get(0)][path1.get(1)];
            }
        } catch (IndexOutOfBoundsException e) {
           // players[n] = cells[path2.get(0)][path2.get(1)];
            players[n].busy = false;
            threads[n] = null;
            moveByPath(n);
        }
    }

    private void movement1(final int n) throws ArrayIndexOutOfBoundsException {
        if (n <= 2) {
                invalidate();
                if (!players[n].bottomWall && !cells[players[n].col][players[n].row + 1].toVisit) {
                    cells[players[n].col][players[n].row + 1].toVisit = true;
                    if (players[n].row != 0 && !cells[players[n].col][players[n].row - 1].toVisit && !players[n].topWall) {
                        if (!players[n+1].busy)
                            moveThreads(n + 2,players[n].col,players[n].row - 1);
                        moveThreads(n + 1,players[n].col,players[n].row - 1);
                    } else if (players[n].col != COLS - 1 && !cells[players[n].col + 1][players[n].row].toVisit && !players[n].rightWall) {
                        if (!players[n+1].busy)
                            moveThreads(n + 2,players[n].col + 1,players[n].row);
                        moveThreads(n + 1,players[n].col + 1,players[n].row);
                    } else if (players[n].col != 0 && !cells[players[n].col - 1][players[n].row].toVisit && !players[n].leftWall) {
                        if (!players[n+1].busy)
                            moveThreads(n + 2,players[n].col - 1, players[n].row);
                        moveThreads(n + 1,players[n].col - 1, players[n].row);
                    }
                    players[n] = cells[players[n].col][players[n].row + 1];

                } else if (!players[n].rightWall && !cells[players[n].col + 1][players[n].row].toVisit) {
                    cells[players[n].col + 1][players[n].row].toVisit = true;
                    if (players[n].row != 0 && !cells[players[n].col][players[n].row - 1].toVisit && !players[n].topWall) {
                        if (!players[n+1].busy)
                            moveThreads(n + 2,players[n].col,players[n].row - 1);
                        moveThreads(n + 1,players[n].col,players[n].row - 1);
                    } else if (players[n].row != ROWS - 1 && !cells[players[n].col][players[n].row + 1].toVisit && !players[n].bottomWall) {
                        if (!players[n+1].busy)
                            moveThreads(n + 2,players[n].col,players[n].row + 1);
                        moveThreads(n + 1,players[n].col,players[n].row + 1);
                    } else if (players[n].col != 0 && !cells[players[n].col - 1][players[n].row].toVisit && !players[n].leftWall) {
                        if (!players[n+1].busy)
                            moveThreads(n + 2,players[n].col - 1,players[n].row);
                        moveThreads(n + 1,players[n].col - 1,players[n].row);
                    }
                    players[n] = cells[players[n].col + 1][players[n].row];
                } else if (!players[n].topWall && !cells[players[n].col][players[n].row - 1].toVisit) {
                    cells[players[n].col][players[n].row - 1].toVisit = true;
                    if (players[n].row != ROWS - 1 && !cells[players[n].col][players[n].row + 1].toVisit && !players[n].bottomWall) {
                        if (!players[n+1].busy)
                            moveThreads(n + 2,players[n].col,players[n].row + 1);
                        moveThreads(n + 1,players[n].col,players[n].row + 1);
                    } else if (players[n].col != COLS - 1 && !cells[players[n].col + 1][players[n].row].toVisit && !players[n].rightWall) {
                        if (!players[n+1].busy)
                            moveThreads(n + 2,players[n].col,players[n].row + 1);
                        moveThreads(n + 1,players[n].col + 1,players[n].row);
                    } else if (players[n].col != 0 && !cells[players[n].col - 1][players[n].row].toVisit && !players[n].leftWall) {
                        if (!players[n+1].busy)
                            moveThreads(n + 2,players[n].col,players[n].row + 1);
                        moveThreads(n + 1,players[n].col - 1,players[n].row);
                    }
                    players[n] = cells[players[n].col][players[n].row - 1];
                } else if (!players[n].leftWall && !cells[players[n].col - 1][players[n].row].toVisit) {
                    cells[players[n].col - 1][players[n].row].toVisit = true;
                    if (players[n].row != 0 && !cells[players[n].col][players[n].row - 1].toVisit && !players[n].topWall) {
                        if (!players[n+1].busy)
                            moveThreads(n + 2,players[n].col,players[n].row - 1);
                        moveThreads(n + 1,players[n].col,players[n].row - 1);
                    } else if (players[n].col != COLS - 1 && !cells[players[n].col + 1][players[n].row].toVisit && !players[n].rightWall) {
                        if (!players[n+1].busy)
                            moveThreads(n + 2,players[n].col,players[n].row - 1);
                        moveThreads(n + 1,players[n].col + 1,players[n].row);
                    } else if (players[n].row != ROWS - 1 && !cells[players[n].col][players[n].row + 1].toVisit && !players[n].bottomWall) {
                        if (!players[n+1].busy)
                            moveThreads(n + 2,players[n].col,players[n].row - 1);
                        moveThreads(n + 1,players[n].col,players[n].row + 1);
                    }
                    players[n] = cells[players[n].col - 1][players[n].row];
                }
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            moveByPath(0);
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
        runnable = new Runnable[] {runnable1,runnable2,runnable3};
        threads = new MyThread[] {thread1,thread2,thread3};
        isAlive = new boolean[3];

        player = cells[0][0];
        player1 = cells[0][0];
        player2 = cells[0][0];//player's position
        exit = cells[COLS-1][ROWS-1]; //finish position

        players = new Cell[] {player, player1, player2};
        current = cells[0][0]; //start creating maze from this point
        current.visited = true;
        for(int i = 0; i < 3; i ++) {
            players[i].toVisit = true;
        }
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

    private void drawVisitedPath(Canvas canvas, int i, int j) {
        canvas.drawRect(
                i*cellSize,
                j*cellSize,
                (i+1)*cellSize,
                (j+1)*cellSize,
                visitedPath
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.canvas = canvas;
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
        canvas.drawRect(0,
                0,
                COLS * cellSize,
                ROWS  * cellSize,
                wall1Paint
        );

        for (int i = 0; i < COLS;i++) {
            for (int j = 0; j < ROWS;j++) {
                if(cells[i][j].toVisit)
                    drawVisitedPath(canvas,i,j);
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

        searchPath(cells,0,0,COLS-1,ROWS-1,path, 0);
        canvas.translate(hMargin,vMargin - 2.5f*cellSize);

        //Drawing path
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
        visited = false;
        boolean toVisit;
        boolean[] pathVisited = new boolean[3];
        boolean[] goalVisited = new boolean[3];
        volatile boolean busy = true;

        int col, row;

        Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }

    public class MyThread extends Thread {
        private int n;
        @Override
        public void run() {
                handler1.postDelayed(runnable[n] = new Runnable() {
                    @Override
                    public void run() {
                        isAlive[n] = true;
                        switch (n) {
                            case 0:
                                movement1(n);
                                handler1.postDelayed(runnable[n], delay);
                                break;
                                case 1:
                                    if (players[n].busy) {
                                            moveByPath1(1, path1Size);
                                            path1Size -= 2;
                                            handler1.postDelayed(runnable[n], delay);
                                        } else {
                                            players[n].busy = false;
                                            movement1(n);
                                            handler1.postDelayed(runnable[n], delay);
                                        }
                                        break;
                                case 2:
                                    if (players[n].busy) {
                                            moveByPath2(2, path2Size);
                                            path2Size -= 2;
                                            handler1.postDelayed(runnable[n], delay);
                                        } else {
                                            players[n].busy = false;
                                            movement1(n);
                                            handler1.postDelayed(runnable[n], delay);
                                        }
                                        break;
                        }
                    }
                }, delay);
        }

        public void setN(int n) {
            this.n = n;
        }
    }

}
