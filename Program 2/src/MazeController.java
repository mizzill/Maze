

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

/*Mitchell McClure
* CS 335
* 10/24/2017
* Maze Generation/Solution implementation
*
* This Class controls the generation, drawing, and solving of the maze
* */
public class MazeController extends JPanel {
    //Global Variables
    //Might not need Speed
    private int rows, columns, speed, percent;
    private MazePiece maze[][]; //Holds Maze Pieces
    private boolean adj[][]; // Adjacency Matrix
    private int mazePos[][]; //Holds the Relative position of an index as if it were a 1D array. Makes accessing values in Adjacency Matrix Easier

    private MazePiece currPiece; // For Generation and Solving

    private Stack<MazePiece> iterativeStack = new Stack<>(); //Used in Generation Iteration
    Stack<MazePiece> solverIterativeStack = new Stack<>();  //Used in Solution Iteration
    private ArrayList<Integer> dir; // Array list for possible directions

    private final int HEIGHT = 525; //Size of maze height
    private final int WIDTH = 600; // Size of maze width
    //Constructor
    public MazeController(){
        rows = 10;
        columns = 10;
        speed = 1;
        percent = 0;
        //Create 2D array
        maze = new MazePiece[rows][columns];
        mazePos = new int[rows][columns];
        adj = new boolean[rows*columns][rows*columns];
        createAdjacencyMatrix();
        //Fill it in
        for(int i = 0; i < rows; i ++){
            for( int j = 0; j < columns; j++){
                maze[i][j] = new MazePiece(i,j);
                mazePos[i][j] = (i*columns)+j;
            }
        }

        currPiece = maze[0][0];
        //These stacks need to have a value on the stack to start iterating. Starts at top left
        iterativeStack.push(currPiece);
        solverIterativeStack.push(currPiece);

    }

    //Constructor with Parameters
    public MazeController(int r, int c, int s){
        rows = r;
        columns = c;
        speed = s;
        percent = 0;

        //Create 2D array
        maze = new MazePiece[rows][columns];
        mazePos = new int[rows][columns];
        adj = new boolean[rows*columns][rows*columns];
        createAdjacencyMatrix();
        //Fill it in
        for(int i = 0; i < rows; i ++){
            for( int j = 0; j < columns; j++){
                maze[i][j] = new MazePiece(i,j);
                mazePos[i][j] = (i*columns)+j;
            }
        }

        currPiece = maze[0][0];
        //These stacks need to have a value on the stack to start iterating. Starts at top left
        iterativeStack.push(currPiece);
        solverIterativeStack.push(currPiece);


    }

    //@override Paint Method.
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        setBackground (Color.GREEN);
        Graphics2D picture = (Graphics2D) g;
        picture.setStroke(new BasicStroke(3));
        //Use picture for drawing

        int pieceHeight = HEIGHT / rows;
        int pieceWidth = WIDTH / columns;

        //For each maze piece, decide what lines need to be drawn and whether it should be blue or gray
        for(int i = 0; i < rows; i ++) {
            for (int j = 0; j < columns; j++) {
                //Square was in active path of the solution
                if(maze[i][j].getPaintBlue()){
                    picture.setColor(Color.BLUE);
                    picture.fillRect(j*pieceWidth,i*pieceHeight,pieceWidth,pieceHeight);
                    picture.setColor(Color.BLACK);
                }
                //Square was traversed but hit a dead end and doesnt contributed to the active path of solution
                else if( maze[i][j].getPaintGray() ){
                    picture.setColor(Color.GRAY);
                    picture.fillRect((j*pieceWidth),(i*pieceHeight),pieceWidth,pieceHeight);
                    picture.setColor(Color.BLACK);
                }
                picture.setColor(Color.BLACK);
                //Outside Borders
                if( i == 0){ //Top Row, Draw top all the wat down
                    //Draw Right
                    picture.drawLine(j*pieceWidth, i * pieceHeight, (j+1)*pieceWidth, i*pieceHeight);
                    if( j == 0){ //Set the Starting Square to blue
                        picture.setColor(Color.BLUE);
                        picture.fillRect(j*pieceWidth,i*pieceHeight,pieceWidth,pieceHeight);
                        g.setColor(Color.BLACK);

                    }
                }
                //If Bottom Row
                else if ( i == (rows-1) ){
                    //Draw Right further Down
                    picture.drawLine(j*pieceWidth, (i+1) * pieceHeight, (j+1)*pieceWidth, (i+1)*pieceHeight);
                    if( j == (columns - 1)) {
                        picture.setColor(Color.RED);

                        picture.fillRect(j*pieceWidth, i*pieceHeight, pieceWidth, pieceHeight);
                        g.setColor(Color.BLACK);
                    }
                }
                //First Columns: Draw Line All the way down
                if(j == 0){
                    picture.drawLine(j*pieceWidth, (i) * pieceHeight, (j)*pieceWidth, (i+1)*pieceHeight);
                }
                if(j == (columns -1) ){ //Draw line all the way down
                    picture.drawLine((j+1)*pieceWidth, (i) * pieceHeight, (j+1)*pieceWidth, (i+1)*pieceHeight);

                }
                /*
                * CHECK ADJACENCY MATRIX TO KNOW WHETHER TO BREAK WALLS AND GENERATE WHAT WILL LOOK LIKE A MAZE
                * */
                if( j != 0) {
                    if (!adj[mazePos[i][j]][mazePos[i][j - 1]]) {
                        //Draw Down
                        picture.drawLine(j * pieceWidth, i * pieceHeight, j * pieceWidth, (i + 1) * pieceHeight);
                    }
                }
                if( i != 0){
                    if (!adj[mazePos[i][j]][mazePos[i - 1][j]]) {
                        picture.drawLine(j * pieceWidth, i * pieceHeight, (j + 1) * pieceWidth, i * pieceHeight);
                    }
                }
            }
        }


    }

    /*
    * Generate Maze
    * Starts in Top left corner, Pushes onto stack. While Stack is not empty, call getPossibleNeighbors which adds to the
    * dir Array List the neighbors of the current piece. Shuffle these directions and take the one off the top.
    * If this tile has already been visited, remove it from arraylist. Repeat until valid neighbor in which case,
    * push onto stack and mark that the pieces are adjacent. If no valid neighbor, pop value off the stack.
    * Will pop until last MazePiece with another neighbor is found and takes next path.
    * */
    public void generateMaze(){
        Stack<MazePiece> stack = new Stack<>();
        dir = new ArrayList<Integer>();
        //Clear out adjacency matrix

        //If animation is not shown, start from beginning and do whole maze. If animation is shown, start from where you
        //last left off
        currPiece = maze[0][0];



        stack.push(currPiece);
        while(!stack.isEmpty()) {
            currPiece= stack.peek();
            Point pt = currPiece.getCoords();
            int x = pt.x;
            int y = pt.y;

            currPiece.setVisited(true);

            getPossibleNeighbors(x, y); //Fills the arrayList dir with possible directions


            boolean validNeighbor = false;

            //Loop through directions, if the one on top is a valid neighbor, exit loop, else check rest of directions.
            //if direction is not valid, remove from list.If no more valid neighbors, pop the maze piece from stack
            int firstNode = 0;
            int nextNode = 0;
            while (!validNeighbor && !dir.isEmpty()) {
                Collections.shuffle(dir);
                int size = dir.size();
                int nextDir = dir.get(size - 1);

                switch (nextDir) {
                    case 1:
                        if (!maze[x - 1][y].getVisited()) {
                            validNeighbor = true;
                            firstNode = mazePos[x][y];
                            nextNode = mazePos[x - 1][y];
                            adj[firstNode][nextNode] = true;
                            adj[nextNode][firstNode] = true;
                            stack.push(maze[x - 1][y]);
                        }
                        break;
                    case 2:
                        if (!maze[x][y + 1].getVisited()) {
                            validNeighbor = true;
                            firstNode = mazePos[x][y];
                            nextNode = mazePos[x][y + 1];
                            adj[firstNode][nextNode] = true;
                            adj[nextNode][firstNode] = true;
                            stack.push(maze[x][y + 1]);
                        }
                        break;
                    case 3:
                        if (!maze[x + 1][y].getVisited()) {
                            validNeighbor = true;
                            firstNode = mazePos[x][y];
                            nextNode = mazePos[x + 1][y];
                            adj[firstNode][nextNode] = true;
                            adj[nextNode][firstNode] = true;
                            stack.push(maze[x + 1][y]);
                        }
                        break;
                    case 4:
                        if (!maze[x][y - 1].getVisited()) {
                            validNeighbor = true;
                            firstNode = mazePos[x][y];
                            nextNode = mazePos[x][y - 1];
                            adj[firstNode][nextNode] = true;
                            adj[nextNode][firstNode] = true;
                            stack.push(maze[x][y - 1]);
                        }
                        break;
                }

                if (!validNeighbor) { // Direction was invalid, remove from list
                    dir.remove(dir.indexOf(nextDir));
                }
            }
            if(dir.isEmpty()) // No valid directions, Deadend
                stack.pop();


        }
        repaint();
        clearVisited(); // Set up maze pices for solution algorithms
    }
    /*generateIteration
    * Like generateMaze() but only produces on step of the solution and is called from within timer events in Maze.java
    * */
    public boolean generateIteration(){

        //iterativeStack is initialized and has values pushed onto it elsewhere
        dir = new ArrayList<Integer>();
        //Currpiece should be stored in the object between each iteration
        boolean continueGen = true;

        currPiece = iterativeStack.peek();

        Point pt = currPiece.getCoords();
        int x = pt.x;
        int y = pt.y;

        currPiece.setVisited(true); //Mark that the cell has been visited

        getPossibleNeighbors(x, y); //Fills the arrayList dir with possible directions


        boolean validNeighbor = false;

        //Loop through directions, if the one on top is a valid neighbor, exit loop, else check rest of directions.
        //if direction is not valid, remove from list.If no more valid neighbors, pop the maze piece from stack
        int firstNode = 0;
        int nextNode = 0;
        while (!validNeighbor && !dir.isEmpty()) {

            Collections.shuffle(dir);
            int size = dir.size();
            int nextDir = dir.get(size - 1);

            switch (nextDir) {
                case 1:
                    if (!maze[x - 1][y].getVisited()) {
                        validNeighbor = true;
                        firstNode = mazePos[x][y];
                        nextNode = mazePos[x - 1][y];
                        adj[firstNode][nextNode] = true;
                        adj[nextNode][firstNode] = true;
                        iterativeStack.push(maze[x - 1][y]);
                    }
                    break;
                case 2:
                    if (!maze[x][y + 1].getVisited()) {
                        validNeighbor = true;
                        firstNode = mazePos[x][y];
                        nextNode = mazePos[x][y + 1];
                        adj[firstNode][nextNode] = true;
                        adj[nextNode][firstNode] = true;
                        iterativeStack.push(maze[x][y + 1]);
                    }
                    break;
                case 3:
                    if (!maze[x + 1][y].getVisited()) {
                        validNeighbor = true;
                        firstNode = mazePos[x][y];
                        nextNode = mazePos[x + 1][y];
                        adj[firstNode][nextNode] = true;
                        adj[nextNode][firstNode] = true;
                        iterativeStack.push(maze[x + 1][y]);
                    }
                    break;
                case 4:
                    if (!maze[x][y - 1].getVisited()) {
                        validNeighbor = true;
                        firstNode = mazePos[x][y];
                        nextNode = mazePos[x][y - 1];
                        adj[firstNode][nextNode] = true;
                        adj[nextNode][firstNode] = true;
                        iterativeStack.push(maze[x][y - 1]);
                    }
                    break;
            }

            if (!validNeighbor) {
                dir.remove(dir.indexOf(nextDir));
            }
        }
        if (dir.isEmpty())
            iterativeStack.pop();

        if(iterativeStack.isEmpty()) { //If stack is empty, bottom right was reached. Maze is generated. Set flag to false to stop
            continueGen = false;
            clearVisited(); // Set up tiles for solution algorithms
        }

        repaint();
        return continueGen; //Return whether it needs to continue generating ot not
    }

    /*
    * Solve Maze Iteratively
    * While solverIterativeStack is not empty, call getPossibleNeighbors which adds to the
    * dir Array List the neighbors of the current piece. Take first value. Values were added in order they should be taken
    * using wall following algorithm
    * If this tile has already been visited, remove it from arraylist. Repeat until valid neighbor in which case,
    * push onto stack and mark that the pieces are adjacent. If no valid neighbor, pop value off the stack.
    * Will pop until last MazePiece with another neighbor is found and takes next path.
    * */
    public boolean solveIteration() {

        dir = new ArrayList<Integer>();
        boolean continueSolving = true;

        currPiece = solverIterativeStack.peek(); // Get Current Piece off top

        //Store Necessary data
        Point pt = currPiece.getCoords();
        int x = pt.x;
        int y = pt.y;

        //record the visit
        currPiece.setVisited(true);
        //Get Neighbors. Method is slightly tweaked for the solver. Adds to the dir array list
        getPossibleNeighborsSolver(x, y);

        boolean validNeighbor = false; //When a valid neighbor is found flag it with this

        //While not a valid neighbor and directions aren't exhaused
        while (!validNeighbor && !dir.isEmpty()) {
            int nextDir = dir.get(0); // Get first item
            //Switch on next item, if its not visited, push it to stack and set flag
            switch (nextDir) {
                case 1:
                    if (!maze[x - 1][y].getVisited()) {
                        validNeighbor = true;
                        solverIterativeStack.push(maze[x - 1][y]);
                    }
                    break;
                case 2:
                    if (!maze[x][y + 1].getVisited()) {
                        validNeighbor = true;
                        solverIterativeStack.push(maze[x][y + 1]);
                    }
                    break;
                case 3:
                    if (!maze[x + 1][y].getVisited()) {
                        validNeighbor = true;
                        solverIterativeStack.push(maze[x + 1][y]);
                    }
                    break;
                case 4:
                    if (!maze[x][y - 1].getVisited()) {
                        validNeighbor = true;
                        solverIterativeStack.push(maze[x][y - 1]);
                    }
                    break;
            } //Not valid, remove from array list
            if (!validNeighbor) {
                dir.remove(dir.indexOf(nextDir));

            }

        }
        //If its a valid position and wasn't backtracked on, set blue
        if (validNeighbor && !currPiece.getPaintGray()) {
            currPiece.setPaintBlue(true);

        }
        //No valid directions, hit dead end. Unflag blue, flag that it should be gray, pop from stack
        if (dir.isEmpty()) {
            currPiece.setPaintBlue(false);
            currPiece.setPaintGray(true);
            solverIterativeStack.pop();
        }
        //When you hit the end, pop rest of arrays off stack. Otherwise program would visit all areas of maze
        if (x == rows - 1 && y == columns - 1) {
            while (!solverIterativeStack.isEmpty()) {
                solverIterativeStack.pop();
            }
            continueSolving = false; // Stop solver
        }

        //Set the stack up for next solver call on next generated maze
        if (solverIterativeStack.isEmpty()) {
            solverIterativeStack.push(maze[0][0]);
            continueSolving = false;
        }

        repaint();


        return continueSolving;
    }

    /*Maze Solving Algorithm
    Very similiar to generation algorithm, except in the way neighbors are generated and flags are set for drawing
    *
    * */
    public void solveMaze(){
        Stack<MazePiece> solverStack = new Stack<>(); //Keeps track of position
        dir = new ArrayList<Integer>();
        currPiece = maze[0][0]; // Start in top left
        solverStack.push(currPiece);
        clearVisited(); // Make sure all pieces are clear of visited flags
        while(!solverStack.isEmpty()) // While theres are maze pieces on the stack
        {
            currPiece = solverStack.peek(); // Grab top one

            //and necessary data
            Point pt = currPiece.getCoords();
            int x = pt.x;
            int y = pt.y;

            currPiece.setVisited(true);

            getPossibleNeighborsSolver(x, y);
            boolean validNeighbor = false;
            //While not a valid neighbor and directions aren't exhaused
            while (!validNeighbor && !dir.isEmpty()) {
                int nextDir = dir.get(0); // Get first item
                //Switch on next item, if its not visited, push it to stack and set flag
                switch (nextDir) {
                    case 1:
                        if (!maze[x - 1][y].getVisited()) {
                            validNeighbor = true;
                            solverStack.push(maze[x - 1][y]);
                        }
                        break;
                    case 2:
                        if (!maze[x][y + 1].getVisited()) {
                            validNeighbor = true;
                            solverStack.push(maze[x][y + 1]);
                        }
                        break;
                    case 3:
                        if (!maze[x + 1][y].getVisited()) {
                            validNeighbor = true;
                            solverStack.push(maze[x + 1][y]);
                        }
                        break;
                    case 4:
                        if (!maze[x][y - 1].getVisited()) {
                            validNeighbor = true;
                            solverStack.push(maze[x][y - 1]);
                        }
                        break;
                }//Not valid, remove from array list
                if (!validNeighbor) {
                    dir.remove(dir.indexOf(nextDir));

                }

            }//If its a valid position and wasn't backtracked on, set blue
            if (validNeighbor && !currPiece.getPaintGray()) {
                currPiece.setPaintBlue(true);

            }
            //No valid directions, hit dead end. Unflag blue, flag that it should be gray, pop from stack
            if (dir.isEmpty()) {
                currPiece.setPaintBlue(false);
                currPiece.setPaintGray(true);
                solverStack.pop();
            }
            //When you hit the end, pop rest of arrays off stack. Otherwise program would visit all areas of maze
            if (x == rows - 1 && y == columns - 1) {
                while (!solverStack.isEmpty()) {
                    solverStack.pop();
                }
            }

        }
        repaint();

    }

    //Count all visited squares, divided by the total number of squares and displays it to screen
    //Inefficient solution but it worked
    public void updatePercentage(JLabel label){
        int count = 0;
        for(int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if( maze[i][j].getPaintBlue() || maze[i][j].getPaintGray())
                    count++;
            }
        }
        int size = rows*columns;
        int calcPerc = (int)(((double)count / size)*100);

        label.setText("Percentage: " + calcPerc +"%");
    }
    public void updateValues( int r, int c, int s){
        rows = r;
        columns = c;
        speed = s;
    }

    //Initialize or reset adjacency matric
    private void createAdjacencyMatrix(){
        int size = rows*columns;
        adj = new boolean[size][size];
        for(int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
               adj[i][j] = false;

            }
        }


    }

    private void getPossibleNeighborsSolver(int x, int y){
        dir. clear();
        int original,first,second,third,fourth;
        //Go back in and change array indices to these variables for readability
        //orig pos = mazePos[x][y]
        original = mazePos[x][y];
        try {
            //first = mazePos[x-1][y]
            first = mazePos[x - 1][y];
        }
        catch(ArrayIndexOutOfBoundsException e){
            first = 0;

        }
        try {    //second = mazePos[x][y+1]
            second = mazePos[x][y + 1];
        }

        catch(ArrayIndexOutOfBoundsException e){
            second = 0;

        }
        try {
            //third = mazePos[x+1][y]]
            third = mazePos[x + 1][y];
        }
        catch(ArrayIndexOutOfBoundsException e){
            third = 0;
        }
        try {
            // fourth = mazePos[x][y-1]]
            fourth = mazePos[x][y - 1];
        }
        catch(ArrayIndexOutOfBoundsException e){
            fourth = 0;
        }

        //POSITIONAL LOGIC TO FIGURE OUT NEIGHBORS OF THE SQUARE, IF THEY'RE ADJACENT
        // AND ADDS THEM TO DIR IN ORDER DEPENDING ON RIGHT HAND RULE
        if(x == 0) { // First row
            if ( y == 0) { // && First Column
                if(adj[original][second])
                    dir.add(2);
                if(adj[original][third]) {
                    dir.add(3);
                }
            }
            else if( y == (columns-1)) // Last column
            {
                if(adj[original][third])
                    dir.add(3);
                if(adj[original][fourth])
                    dir.add(4);
            }
            else { // In between
                if(adj[original][second])
                    dir.add(2);
                if(adj[original][third])
                    dir.add(3);
                if(adj[original][fourth])
                    dir.add(4);
            }
        }
        else if( x == (rows-1) ){ //Last row
            if(y == 0) { //First column
                if(adj[original][second] ) {
                    dir.add(2);
                }
                if(adj[original][first])
                    dir.add(1);
            }
            else if (y == (columns-1)){ //&& Last column
                //Do Nothing. This is the Solution square. Add no directions and pop off stack
            }
            else{ // In between
                if(adj[original][first] ) { // put back below 2
                    dir.add(1);
                }
                if(adj[original][fourth] ) {
                    dir.add(4);
                }
                if(adj[original][second] ) {
                    dir.add(2);
                }
            }
        }
        else if (y == 0) // First column in between first and last row
        {
            if(adj[original][second] ) {
                dir.add(2);
            }
            if(adj[original][first] ) {
                dir.add(1);
            }
            if(adj[original][third] ) {
                dir.add(3);
            }
        }
        else if (y == (columns-1) ){ // Last column in between first and last row
            if(adj[original][fourth] )
                dir.add(4);
            if(adj[original][third] ) {
                dir.add(3);
            }
            if(adj[original][first] ) {
                dir.add(1);
            }
        }
        else { // Position in middle of maze
            //2,4,1,3
            if(adj[original][second] )
                dir.add(2);
            if(adj[original][first] ) {
                dir.add(1);
            }
            if(adj[original][third] ) {
                dir.add(3);
            }
            if(adj[original][fourth] ) //Put back at bottom
            {
                dir.add(4);
            }

        }
    }
    private void getPossibleNeighbors(int x, int y){
        dir.clear();

        //POSITIONAL LOGIC TO FIGURE OUT NEIGHBORS OF THE SQUARE, IF THEY'RE NOT VISITED
        // AND ADDS THEM TO DIR
        if(x == 0) {// First row
            if ( y == 0) {// && First Column
                dir.add(2);
                dir.add(3);
            }
            else if( y == (columns-1))// Last column
            {
                dir.add(3);
                dir.add(4);
            }
            else {// In between
                dir.add(2);
                dir.add(3);
                dir.add(4);
            }
        }
        else if( x == (rows-1) ){//Last row
            if(y == 0) {//First column
                dir.add(1);
                dir.add(2);
            }
            else if (y == (columns-1)){//&& Last column
                //Do Nothing. This is the Solution square. Add no directions and pop off stack
            }
            else{// In between
                dir. add(1);
                dir.add(2);
                dir.add(4);
            }
        }
        else if (y == 0)// First column in between first and last row
        {
            dir.add(1);
            dir.add(2);
            dir.add(3);
        }
        else if (y == (columns-1) ){// Last column in between first and last row
            dir.add(1);
            dir.add(3);
            dir.add(4);
        }
        else {// Position in middle of maze
            dir.add(1);
            dir.add(2);
            dir.add(3);
            dir.add(4);
        }
    }

    /* Reset Maze to a row x column grid and re initializes values*/
    public void resetMaze(){
        //Create 2D array
        maze = new MazePiece[rows][columns];
        mazePos = new int[rows][columns];
        adj = new boolean[rows*columns][rows*columns];

        createAdjacencyMatrix();

        for(int i = 0; i < rows; i ++){
            for( int j = 0; j < columns; j++){
                maze[i][j] = new MazePiece(i,j);
                mazePos[i][j] = (i*columns)+j;

            }
        }
        for(MazePiece[] row: maze){
            for(MazePiece piece: row){
                piece.setVisited(false);
            }
        }
        currPiece = maze[0][0];
        iterativeStack.empty();
        iterativeStack.push(currPiece);
        solverIterativeStack.empty();
        solverIterativeStack.push(currPiece);
    }
    public void clearVisited(){
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j ++){
                maze[i][j].setVisited(false);
            }
        }
    }
}
