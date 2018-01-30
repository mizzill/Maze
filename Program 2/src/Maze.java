import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

/*Mitchell McClure
* CS 335
* 10/24/2017
* Maze Generation/Solution implementation
*
* This is the Maze Class. Will set up GUI for the user to interact with the maze
* A Driver Class
* Purpose: Create an implementation that generates a Maze from 10x10 to 50x50 and can find and display a solution to
* the user
* */
public class Maze extends JFrame implements ActionListener {
    //Global(Private) Variables
    private JPanel mazeView, settingsView;
    private JLabel rowLabel, columnLabel, percentageLabel,speedLabel;
    private JButton start_stop, generate, solve;
    private JSlider speedSlider, rowSlider, columnSlider;
    private JCheckBox animationBox;
    private Timer timer; // Used for events in animation
    //MazeData
    private int rows, columns, speed, percentage;
    private MazeController maze; //Maze Controller Object. Responsible for generating, solving, and drawing of maze.

    //mazeMode flags whether to solve or generate. COntinuing tells the maze controller to keep iterating frames
    //ShowAnimation flags whether the user wants to see the animation or should it be instant. Running signifies the timer is running
    private boolean mazeMode, continuing, showAnimation, running; // MazeMode -> true = solve || false = generate

    //Constants for sliders
    static final int ROW_COLUMN_MIN = 10;
    static final int ROW_COLUMN_MAX = 50;
    static final int ROW_COLUMN_INIT = 10;

    static final int SPEED_MIN = 1;
    static final int SPEED_MAX = 100;
    static final int SPEED_INIT = 1;

    //Constructor
    public Maze(){
        //Initialize Values
        super("Maze");
        Container c = getContentPane();
        continuing = true;

        rows = ROW_COLUMN_INIT;
        columns = ROW_COLUMN_INIT;
        speed = SPEED_INIT;
        percentage = 0;
        mazeMode = false;
        showAnimation = false;
        running = false;

        settingsView = new JPanel();

        //Set Layout & Border of settingsView
        settingsView.setLayout( new GridLayout(11, 1, 0,5) );
        settingsView.setBorder( BorderFactory.createEmptyBorder(10,10,10,10));
        //Create Settings Components
        setupSettings();

        //Initialize maze object
        maze = new MazeController(rows,columns,speed);

        //Add maze object to mazeView or fill board or whatever

        c.add(maze,BorderLayout.CENTER);

        c.add(settingsView, BorderLayout.EAST);

        setSize(900, 600);
        setVisible(true);
    }

    //Initalize labels, components, and add handlers to objects in the settigns panel on the right
    private void setupSettings(){

        rowLabel = new JLabel("Rows: " + rows, SwingConstants.CENTER);
        columnLabel = new JLabel("Columns: " + rows, SwingConstants.CENTER);
        speedLabel = new JLabel("Speed: " + speed + " fps", SwingConstants.CENTER);
        percentageLabel = new JLabel("Percentage: " + percentage + "%", SwingConstants.CENTER);

        start_stop = new JButton("Start");
        generate = new JButton("Generate Maze");
        solve = new JButton("Solve Maze");

        animationBox = new JCheckBox("Show Animation");
        animationBox.setSelected(false);

        //Slider for rows
        rowSlider =  new JSlider(JSlider.HORIZONTAL, ROW_COLUMN_MIN,
                ROW_COLUMN_MAX, ROW_COLUMN_INIT);
        rowSlider.setMajorTickSpacing( ROW_COLUMN_MAX - ROW_COLUMN_MIN );
        rowSlider.setPaintTicks( true );
        rowSlider.setPaintLabels( true );

        //Slider for Columns
        columnSlider =  new JSlider(JSlider.HORIZONTAL, ROW_COLUMN_MIN,
                ROW_COLUMN_MAX, ROW_COLUMN_INIT);
        columnSlider.setMajorTickSpacing( ROW_COLUMN_MAX - ROW_COLUMN_MIN );
        columnSlider.setPaintTicks( true );
        columnSlider.setPaintLabels( true );

        //Slider for Speed
        speedSlider =  new JSlider(JSlider.HORIZONTAL, SPEED_MIN,
                SPEED_MAX, SPEED_INIT);
        speedSlider.setMajorTickSpacing( SPEED_MAX - SPEED_MIN );
        speedSlider.setPaintTicks( true );
        speedSlider.setPaintLabels( true );


        //Add Components to JPanel
        settingsView.add(generate);
        settingsView.add(solve);
        settingsView.add(speedLabel);
        settingsView.add(speedSlider);
        settingsView.add(rowLabel);
        settingsView.add(rowSlider);
        settingsView.add(columnLabel);
        settingsView.add(columnSlider);
        settingsView.add(animationBox);
        settingsView.add(start_stop);
        settingsView.add(percentageLabel);

        //****************************
        //Handlers
        //****************************

        animationBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
               showAnimation = (e.getStateChange()==ItemEvent.SELECTED);
            }
        });
        generate.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                //Set generate flag.
                mazeMode = false;
            }
        });
        solve.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                //Set Solve flag
                mazeMode = true;
            }
        });

        //Since it is a delay, the values work counterintuitevely. So I Subtract the speed from the max so as the user
        //Slides from 1 to 100 the animation transition from slow to fast instead of short to long delay
        timer = new javax.swing.Timer(SPEED_MAX- speed, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                maze.updatePercentage(percentageLabel); //Update Percentage on each timer update
                if(continuing) { //If the solver or generate algorithm hasnt reached the end
                    if (!mazeMode) // If Generate
                        continuing = maze.generateIteration();
                    else // Else Solve
                        continuing = maze.solveIteration();
                }
                 else {
                    //Last timer delay the generation or solution finished so reset flag for next time Start is pressed.
                    continuing = true;
                    running = false;
                    start_stop.setText("Start"); //Update Label
                    timer.stop();

                }
            }
            });
        start_stop.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                //get values from sliders
                rows = rowSlider.getValue();
                columns = columnSlider.getValue();
                speed = SPEED_MAX - speedSlider.getValue();
                percentageLabel.setText("Percentage: 0%");
                continuing = false;


                if (!mazeMode) {
                    maze.updateValues(rows, columns, speed);
                    maze.resetMaze();
                    if (!showAnimation) // If not showAnimation, generate maze in one step
                        maze.generateMaze();
                    else {
                        timer.setDelay(speed);
                        timerCheck();
                    }

                }
                else if (mazeMode) {
                    //call maze.solve
                    if (!showAnimation) {// If not showAnimation, solve maze in one step
                        maze.solveMaze();
                        maze.updatePercentage(percentageLabel);
                    }
                    else {
                        timer.setDelay(speed);
                        timerCheck();
                    }
                }
            }


        });

        rowSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                rowLabel.setText("Rows: " + rowSlider.getValue() );
            }
        });
        columnSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                columnLabel.setText("Columns: " + columnSlider.getValue());
            }
        });
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                speed = speedSlider.getValue();
                timer.setDelay(SPEED_MAX - speed);
                speedLabel.setText("Speed: " + speedSlider.getValue() + "fps");
            }
        });

    }

    private void timerCheck(){
        if (!running) {
            running = true;
            start_stop.setText("Stop");
            continuing = true;
            timer.start();
        } else {
            running = false;
            timer.stop();
            start_stop.setText("Start");
        }

    }
    public void actionPerformed(ActionEvent e) { }

    public static void main(String args[])
    {
        Maze m = new Maze();
        m.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { System.exit(0); }
        });
    }
}
