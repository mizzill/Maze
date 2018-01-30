import javax.swing.*;
import java.awt.*;

/*Mitchell McClure
* CS 335
* 10/24/2017
* Maze Generation/Solution implementation
*
*This class represents a piece of the maze. Stores data and methods necessary to display a portion of the maze
* */
public class MazePiece extends JLabel {
    //Global Values
    private boolean visited;
    private boolean paintGray, paintBlue;
    private boolean hasNeighbors;
    private Point pt;
    //Resource Loader

    //Constructor
    public MazePiece(){
        visited = false;
        paintGray = false;
        paintBlue = false;
        hasNeighbors = false;
        pt = new Point(0,0);
    }
    //Constuctor w/ parameters
    public MazePiece(int xPos, int yPos){
        visited = false;
        paintGray = false;
        paintBlue = false;
        hasNeighbors = false;
        pt = new Point(xPos,yPos);
    }
    //Methods



    public void setVisited(boolean visited) {
        this.visited = visited;
    }
    public boolean getVisited(){ return visited; }

    public void setPaintGray(boolean paintGray) {
        this.paintGray = paintGray;
    }

    public void setPaintBlue(boolean paintBlue){
        this.paintBlue = paintBlue;
    }
    public boolean getPaintGray(){
        return paintGray;
    }
    public boolean getPaintBlue(){
        return paintBlue;
    }
    public Point getCoords(){
        return pt;
    }
}
