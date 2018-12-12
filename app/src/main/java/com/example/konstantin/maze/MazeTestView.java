package com.example.konstantin.maze;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

public class MazeTestView extends View implements Serializable{
    static int N = 3;
    int minX, minY;

    public static final int COLS = 10, ROWS = 10;
    private static final float WALL_THICKNESS = 4;
    private transient Paint wallPaint, playerPaint,player1Paint, exitPaint, pathPaint, wall1Paint, visitedPath;
    private Random random;
    static volatile public Cell[][] cells;
    ArrayList<Cell> neighbours;
    Canvas canvas;
    volatile Cell[] players;
    boolean[] visitedBy, busy, moving;
    private float cellSize, hMargin, vMargin;
    Queue<Integer> cellToMove, cellsToRemember;
    boolean allCellsAreVisited;
    volatile List<Integer>[] pathList, templist;
    MyThread[] threads;
    int[][] distances, closestPlayer;
    volatile Queue<Integer> qAll;
    static int[] stepCounter;


    public MazeTestView(Context context, @Nullable AttributeSet attrs) {
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

        exitPaint = new Paint();
        exitPaint.setColor(Color.BLUE);

        pathPaint = new Paint();
        pathPaint.setColor(Color.GREEN);
        pathPaint.setStrokeWidth(4);

        visitedPath = new Paint();
        visitedPath.setColor(Color.WHITE);

        cells = new Cell[COLS][ROWS];
        players = new Cell[N];
        random = new Random();
        visitedBy = new boolean[N];
        busy = new boolean[N];
        moving = new boolean[N];
        stepCounter = new int[N];
        cellToMove = new LinkedList<>();
        pathList = new ArrayList[N];
        templist = new ArrayList[N];
        cellsToRemember = new LinkedList<>();
        allCellsAreVisited = false;
        threads = new MyThread[N];
        closestPlayer = new int[COLS][ROWS];
        qAll = new LinkedList<>();

        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }

        for (int i = 0; i < N; i++) {
            players[i] = cells[0][0];
            players[i].toVisit = true;
            threads[i] = new MyThread();
            pathList[i] = new ArrayList<>();
            templist[i] = new ArrayList<>();
        }

        createMaze();
    }

    public void createMaze() {
        Stack<Cell> stack = new Stack<>();
        Cell current, next;

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

    private Cell getNeighbour(Cell cell) {
        neighbours = new ArrayList<>();
        //left neighbour
        if (cell.col > 0)
            if (!cells[cell.col - 1][cell.row].visited) {
                neighbours.add(cells[cell.col - 1][cell.row]);
            }
        //right
        if (cell.col < COLS - 1)
            if (!cells[cell.col + 1][cell.row].visited) {
                neighbours.add(cells[cell.col + 1][cell.row]);
            }
        //top
        if (cell.row > 0)
            if (!cells[cell.col][cell.row - 1].visited) {
                neighbours.add(cells[cell.col][cell.row - 1]);
            }
        //bottom
        if (cell.row < ROWS - 1)
            if (!cells[cell.col][cell.row + 1].visited) {
                neighbours.add(cells[cell.col][cell.row + 1]);
            }

        if (neighbours.size() > 0) {
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }
        return null;
    }

    private void removeWall(Cell current, Cell next) {
        if (current.col == next.col && current.row == next.row + 1) {
            current.topWall = false;
            next.bottomWall = false;
        }
        if (current.col == next.col && current.row == next.row - 1) {
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < N; i ++) {
                movement2();
                if (!pathList[i].isEmpty()) {
                    MyThread t2 = new MyThread();
                    t2.setN(i);
                    t2.start();
                    stepCounter[i]++;
                }
            }
            if (isAllCellsAreVisited())
                Toast.makeText(getContext(),"All cells are visited!",Toast.LENGTH_LONG).show();
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        this.canvas = canvas;
        canvas.drawColor(Color.WHITE);

        int width = getWidth();
        int height = getHeight();

        if (width / height < COLS / ROWS) {
            cellSize = width / (COLS + 1);
        } else {
            cellSize = height / (ROWS + 1);
        }

        hMargin = (width - COLS * cellSize) / 2;
        vMargin = (height - ROWS * cellSize) / 2;

        canvas.translate(hMargin, vMargin);
        canvas.drawRect(0,
                0,
                COLS * cellSize,
                ROWS * cellSize,
                wall1Paint
        );

        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                if (cells[i][j].toVisit)
                    drawVisitedPath(canvas,i,j);
                if (cells[i][j].topWall)
                    canvas.drawLine(
                            i * cellSize,
                            j * cellSize,
                            (i + 1) * cellSize,
                            j * cellSize,
                            wallPaint);

                if (cells[i][j].bottomWall)
                    canvas.drawLine(
                            i * cellSize,
                            (j + 1) * cellSize,
                            (i + 1) * cellSize,
                            (j + 1) * cellSize,
                            wallPaint);
                if (cells[i][j].leftWall)
                    canvas.drawLine(
                            i * cellSize,
                            j * cellSize,
                            i * cellSize,
                            (j + 1) * cellSize,
                            wallPaint);
                if (cells[i][j].rightWall)
                    canvas.drawLine(
                            (i + 1) * cellSize,
                            j * cellSize,
                            (i + 1) * cellSize,
                            (j + 1) * cellSize,
                            wallPaint);
            }
        }

        float margin = cellSize / 10;

        for (int i = 0; i < N; i++) {
            canvas.drawRect(
                    players[i].col * cellSize + margin,
                    players[i].row * cellSize + margin,
                    (players[i].col + 1) * cellSize - margin,
                    (players[i].row + 1) * cellSize - margin,
                    playerPaint
            );
        }
    }

    private void drawVisitedPath(Canvas canvas, int i, int j) {
        canvas.drawRect(
                i * cellSize,
                j * cellSize,
                (i + 1) * cellSize,
                (j + 1) * cellSize,
                visitedPath
        );
    }

    private boolean someFixMethod(int i, int j) {
        boolean[] fix = new boolean[N];
        int count = 0;
        for (int k = 0; k < N; k++) {
            fix[k] = !cells[i][j].mVisited[k];
            if (fix[k])
                count++;
        }
        return count == N;
    }

    private int halfMovement() {
        distances = new int[COLS][ROWS];
        List<Integer> temp = new ArrayList<>();
        List<Integer> freePlayers = takeAllFreePlayers();
        if (freePlayers.size() > 0) {
            for (int i = 0; i < COLS; i++) {
                for (int j = 0; j < ROWS; j++) {
                    for (int k : freePlayers) {
                        if (someFixMethod(i,j)) {
                            searchPath(cells, players[k].col, players[k].row, i, j, templist[k], k);
                            temp.add(templist[k].size() - 1);
                        }
                    }
                    if (temp.size() == 0)
                        continue;
                    int min = Math.min(temp.get(0), temp.get(temp.size() - 1));
                    distances[i][j] = min;
                    closestPlayer[i][j] = freePlayers.get(temp.indexOf(min));
                    temp.clear();
                    for (List<Integer> aTemplist : templist) aTemplist.clear();
                    resetSearchPath();
                }
            }
        }

        int minDistance = getMinValue(distances);
        if (minDistance == 1000 || minDistance == -1)
            return -1;
        int minPlayer = closestPlayer[minX][minY];
        cells[minX][minY].mVisited[minPlayer] = true;
        searchPath(cells, players[minPlayer].col, players[minPlayer].row, minX, minY, pathList[minPlayer], minPlayer);

        return minPlayer;
    }

    private void movement2() {
        int minPlayer = halfMovement();
        if (minPlayer != -1) {
            newMovement(minPlayer);
        }
        List<Integer> freePlayers = takeAllFreePlayers();

        if (freePlayers.size() > 0 && qAll.size() > 0) {
            List<Integer> temp = new ArrayList<>();
            int x = qAll.remove();
            int y = qAll.remove();
            for (int k : freePlayers) {
                templist[k].removeAll(templist[k]);
                searchPath(cells, players[k].col, players[k].row, x, y, templist[k], k);
                temp.add(templist[k].size() - 1);
                templist[k].clear();
            }
            if (temp.size() == 0)
                return;
            int min = Math.min(temp.get(0), temp.get(temp.size() - 1));
            int pl = freePlayers.get(temp.indexOf(min));
            templist[pl].removeAll(templist[pl]);
            searchPath(cells,players[pl].col,players[pl].row, x, y, pathList[pl], pl);
            busy[pl] = true;
            newMovement(pl);
        }
    }

    private synchronized void newMovement(int n) {
        busy[n] = true;
            try {
                int i = pathList[n].size() - 1;
                players[n] = cells[pathList[n].get(i - 1)][pathList[n].get(i)];
                pathList[n].remove(pathList[n].size() - 1);
                pathList[n].remove(pathList[n].size() - 1);
                players[n].toVisit = true;
                players[n].mVisited[n] = true;
                moving[n] = true;
                if (pathList[n].size() == 0) {
                    moving[n] = false;
                    pathList[n].removeAll(pathList[n]);
                    threads[n].setRunning(false);
                } else {
                    return;
                }
            } catch (IndexOutOfBoundsException e) {
                pathList[n].clear();
                moving[n] = false;
                threads[n].setRunning(false);
            }
        busy[n] = false;
    }

    boolean isAllCellsAreVisited() {
        int count = 0;
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                if (cells[i][j].toVisit)
                    count++;
            }
        }
        return count == (ROWS) * (COLS);
    }

    public int getMinValue(int[][] numbers) {
        int count = 0;
        int minValue = 1000;
        for (int j = 0; j < numbers.length; j++) {
            for (int i = 0; i < numbers[j].length; i++) {
                if (numbers[j][i] <= minValue && numbers[j][i] != 0) {
                    if (count < 1) {
                        minValue = numbers[j][i];
                        minX = j;
                        minY = i;
                    }
                    if (minValue == 3) {
                        count++;
                        if (count > 1) {
                            qAll.add(j);
                            qAll.add(i);
                        }
                    }
                }
            }
        }
        return minValue ;
    }

    private List<Integer> takeAllFreePlayers() {
        for (int i = 0; i < N; i++)
            if (pathList[i].isEmpty()) {
                busy[i] = false;
            }
        List<Integer> freePlayersId = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            if (!busy[i])
                freePlayersId.add(i);
        }
        return freePlayersId;
    }

    private boolean searchPath(Cell[][] maze, int startX, int startY, int destX, int destY
            , List<Integer> path,int n) {

        int dx, dy;

        if (maze[startX][startY] == cells[destX][destY]) {  //if path point equals exit point - we found the path
            path.add(startX);
            path.add(startY);
            return true;
        }

        if (!maze[startX][startY].pathVisited[n]) {
            maze[startX][startY].pathVisited[n] = true;

            if (!maze[startX][startY].leftWall) {
                dx = -1;
                dy = 0;
                if (searchPath(maze, startX + dx, startY + dy, destX, destY, path, n)) {
                    path.add(startX);
                    path.add(startY);
                    return true;
                }
            }

            if (!maze[startX][startY].rightWall) {
                dx = 1;
                dy = 0;
                if (searchPath(maze, startX + dx, startY + dy, destX, destY, path, n)) {
                    path.add(startX);
                    path.add(startY);
                    return true;
                }
            }

            if (!maze[startX][startY].topWall) {
                dx = 0;
                dy = -1;
                if (searchPath(maze, startX + dx, startY + dy, destX, destY, path, n)) {
                    path.add(startX);
                    path.add(startY);
                    return true;
                }
            }

            if (!maze[startX][startY].bottomWall) {
                dx = 0;
                dy = 1;
                if (searchPath(maze, startX + dx, startY + dy, destX, destY, path, n)) {
                    path.add(startX);
                    path.add(startY);
                    return true;
                }
            }
        }
        return false;
    }

    private void resetSearchPath() {
        for (int x = 0; x < COLS; x ++)
            for (int y = 0; y < ROWS; y++)
                for (int n = 0; n < N; n++)
                cells[x][y].pathVisited[n] = false;
    }

    public static int getStepCounter(int i) {
        return stepCounter[i];
    }



    public static Cell[][] getCells() {
        return cells;
    }

    public static void setCells(Cell[][] _cells) {
        cells = new Cell[COLS][ROWS];
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }
        for (int i = 0; i < COLS; i++)
            for (int j = 0; j < ROWS; j++) {
                cells[i][j].bottomWall = _cells[i][j].bottomWall;
                cells[i][j].topWall = _cells[i][j].topWall;
                cells[i][j].leftWall = _cells[i][j].leftWall;
                cells[i][j].rightWall = _cells[i][j].rightWall;
            }
    }
    public static void setN(int n) {
        N = n;
    }



    public class MyThread extends Thread {
        private volatile int n;
        volatile boolean running;

        @Override
        public void run() {
            newMovement(n);
        }

        public void setN(int n) {
            this.n = n;
        }
        void setRunning(boolean running) {
            this.running = running;
        }
    }
}
