package com.example.konstantin.maze;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class MazeView extends View {

    private enum Direction {UP, RIGHT,DOWN, LEFT};

    private final List<Integer> path = new ArrayList<Integer>();
    ArrayList<Cell> neighbours;

    private Cell[][] cells;
    public Cell player, exit;
    public static final int COLS = 10, ROWS = 15;
    private static final float WALL_THICKNESS = 4;
    private float cellSize, hMargin, vMargin;
    private Paint wallPaint, playerPaint, exitPaint, pathPaint;
    private Random random;


    public MazeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        playerPaint = new Paint();
        playerPaint.setColor(Color.RED);

        exitPaint = new Paint();
        exitPaint.setColor(Color.BLUE);

        pathPaint = new Paint();
        pathPaint.setColor(Color.GREEN);

        random = new Random();

        createMaze();
    }

    private void drawPath(List<Integer> path) {
        Canvas canvas = new Canvas();
        canvas.translate(hMargin,vMargin);

    }

    private boolean searchPath(Cell[][] maze, int x, int y
            , List<Integer> path) {

        int dx,dy;

        if (maze[x][y] == exit) {
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

    private void movePlayer(Direction direction) {
        switch (direction) {
            case UP:
                if(!player.topWall)
                    player=cells[player.col][player.row-1];
                break;
            case DOWN:
                if(!player.bottomWall)
                    player=cells[player.col][player.row+1];
                break;
            case LEFT:
                if(!player.leftWall)
                    player=cells[player.col-1][player.row];
                break;
            case RIGHT:
                if(!player.rightWall)
                    player=cells[player.col+1][player.row];
                break;
        }
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            return true;
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();

            float playerCenterX = hMargin + (player.col + 0.5f)*cellSize;
            float playerCenterY = vMargin + (player.row + 0.5f)*cellSize;


            float dx = x - playerCenterX;
            float dy = y - playerCenterY;

            float absX = Math.abs(dx);
            float absY = Math.abs(dy);

            if (absX > cellSize || absY > cellSize) {
                if(absX > absY) {
                    if (dx > 0)
                        movePlayer(Direction.RIGHT);
                    else
                        movePlayer(Direction.LEFT);
                }
                else {
                    if (dy > 0)
                        movePlayer(Direction.DOWN);
                    else
                        movePlayer(Direction.UP);
                }
            }
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
        exit = cells[COLS-1][ROWS-1];

        current = cells[0][0];
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
                player.col*cellSize+margin,
                    player.row*cellSize+margin,
                    (player.col+1)*cellSize-margin,
                    (player.row+1)*cellSize-margin,
                    playerPaint
        );

        canvas.drawRect(
                exit.col*cellSize+margin,
                exit.row*cellSize+margin,
                (exit.col+1)*cellSize-margin,
                (exit.row+1)*cellSize-margin,
                exitPaint
        );
        searchPath(cells,0,0,path);
        canvas.translate(hMargin + cellSize/2,vMargin);

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
        pathVisited = false;

        int col, row;

        public Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }
}
