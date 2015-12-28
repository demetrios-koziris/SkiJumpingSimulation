
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Ski Jump Simulation
 * GraphicsPanel is a custom JPanel class which has all the variables and methods defined for a JPanel class
 * GraphicsPanel serves to display the results of the SkiJump class (ski hill, skier's trajectory, and data)
 * GraphicsPanel also makes use of a mouseMotionListener to allow the user to track the change in the data
 * variables (t, posX, posY... etc) along the skier's trajectory. This data is stores in ArrayLists.
 *
 * @author  Demetrios Koziris
 * @version 5/17/2013
 */

class GraphicsPanel extends JPanel {

    //booleans used to control when the panel displays
    private boolean readyForInitial;
    private boolean readyForTrack;
    
    //this polygon will store the geometric shape of the ski hill used in the simulation
    private Polygon hillShape;
    
    //these will sotre the results of the ski jump simulation
    private double mass;
    private double height;
    private double startPosition;
    private double takeoffSpeed;
    private double finalDistance;
    
    //these variables are used to store the data that changes along the skier's trajectory
    private int indexCounter;
    private ArrayList<Double> t;
    private ArrayList<Double> posX;
    private ArrayList<Double> posY;
    private ArrayList<Double> velX;
    private ArrayList<Double> velY;
    private ArrayList<Double> velocity;
    private ArrayList<Double> velAngle;
    private ArrayList<Double> accX;
    private ArrayList<Double> accY;
    private ArrayList<Double> acceleration;
    
    //this is index used to display the data above as a function of the mouse's position
    private int trackIndex;


    /**
     * Constructor for the GraphicsPanel Class
     */
    public GraphicsPanel() {
        
        readyForInitial = false;
        readyForTrack = false;
        
        t = new ArrayList<Double>();
        posX = new ArrayList<Double>();
        posY = new ArrayList<Double>();
        velX = new ArrayList<Double>();
        velY = new ArrayList<Double>();
        velocity = new ArrayList<Double>();
        velAngle = new ArrayList<Double>();
        accX = new ArrayList<Double>();
        accY = new ArrayList<Double>();
        acceleration = new ArrayList<Double>();
        
        indexCounter = 0;
        
        //the mouseMoved method of this MouseMotionListener is called everytime the mouse moves
        addMouseMotionListener(new MouseAdapter() 
            {
                public void mouseMoved(MouseEvent e) 
                {
                    track(e.getX(),e.getY());
                }
            }
        );
    }
    
    /**
     * Sets the hillShape Polygon object to the parameter Polygon object
     * @param hillShape         Polygon object with the geometry of the ski jump hill
     */
    public void setHillShape (Polygon hillShape)
    {
        this.hillShape = hillShape;
    }
    
    /**
     * This method updates the data stored in ArrayLists (changes along the skier's jump trajectory)
     * The indexCounter is incremented each time this method is called, insuring data is proporly added to the Array Lists
     * @param t                 time 
     * @param posX              skier's x position
     * @param posY              skier's y position
     * @param velX              x compnent of the skier's velocity
     * @param velY              y compnent of the skier's velocity
     * @param velocity          magnitude of the skier's velocity vector
     * @param velAngle          direction of the velocity vector (and of the acceleration vector)
     * @param accX              x compnent of the skier's acceleration
     * @param accY              y compnent of the skier's acceleration
     * @param acceleration      magnitude of the skier's acceleration vector
     */
    public void updateTrajectoryData (double t, double posX, double posY, double velX, double velY, double velocity, double velAngle, double accX, double accY, double acceleration)
    {
        this.t.add(indexCounter, t);
        this.posX.add(indexCounter, posX);
        this.posY.add(indexCounter, posY);
        this.velX.add(indexCounter, velX);
        this.velY.add(indexCounter, velY);
        this.velocity.add(indexCounter, velocity);
        this.velAngle.add(indexCounter, velAngle);
        this.accX.add(indexCounter, accX);
        this.accY.add(indexCounter, accY);
        this.acceleration.add(indexCounter, acceleration);
        indexCounter++;
    }
    
    /**
     * Sets the result variables to the parameter values
     * @param mass              skier's total mass
     * @param height            skier's height
     * @param startPosition     skier's start position (expressed as a distnace along the takeoff slope)
     * @param takeoffSpeed      velocity of the skier before the jump
     * @param finalDistance     skier's distance (measured as the distance from the takeoff point to the landing point)
     */
    public void setResultsData (double mass, double height, double startPosition, double takeoffSpeed, double finalDistance)
    {
        this.mass = mass;
        this.height = height;
        this.startPosition = startPosition;
        this.takeoffSpeed = takeoffSpeed;
        this.finalDistance = finalDistance;
    }
    
    /**
     * Sets the readyForInitial variable to the parameter value
     * @param readyForInitial   controls when the panel's paintComponent method does anything
     */
    public void setReady (boolean readyForInitial)
    {
        this.readyForInitial = readyForInitial;
    }
    
    /**
     * Is called when the mouse moves over the panel and determines the index (trackIndex) corresponding to the user's mouse position
     * @param mouseX        mouse's x position
     * @param mouseY        mouse's y position
     */
    public void track(int mouseX, int mouseY){
        boolean search = true;
        int i = 0;
        while (search && i < indexCounter)
        {
            if (posX.get(i) >= mouseX/4.)
            {
                trackIndex = i;
                search = false;
            }
            else
            {
                i++;
            }
        }
        repaint();
    }

    /**
     * This method paints objects to the panel when the appropriote method is called
     * @param g         Graphics object used to paint objects to the panel
     */
    protected void paintComponent(Graphics g) {
        //readyForInitial is set to true by the SkiJump class once the GraphicsPanel has received all the data it needs to display the skier's trajectory
        if (readyForInitial)
        {
            //background
            g.setColor(new Color(135,206,250)); 
            g.fillRect(0,0,1200,600);
            
            //ski hill
            g.setColor(Color.WHITE); 
            g.fillPolygon(hillShape);
            
            //skier's trajectory
            g.setColor(Color.RED); 
            for (int i = 0; i < indexCounter; i++)
            {
                g.fillOval((int)(posX.get(i)*4)-2, (int)(600-posY.get(i)*4)-2, 4, 4);
            }
            
            //information for the user and positive axis directions
            g.setColor(Color.BLACK);
            g.fillRect(50,465,2,50);
            g.fillRect(50,513,50,2);
            g.drawString("+y", 44, 460);
            g.drawString("+x", 102, 515);
            g.drawString("Angles are measured relative to the positive x axis", 50, 540);
            g.drawString("Velocity and Acceleration are in the direction of the Velocity Angle", 50, 555);
            
            //this information displayed does not change as the user tracks the ski jump trajectory with the mouse
            g.drawString("Hill Location:", 900, 50);
            g.drawString("Skier mass:", 900, 65);
            g.drawString("Skier height:", 900, 80);
            g.drawString("Start position:",900,95);
            g.drawString("Takeoff speed:",900,110);
            g.drawString("Final Distance:",900,125);
            g.drawString("Trajectory tracker", 900, 160);
            g.drawString("Time:", 900, 175);
            g.drawString("X Position:", 900, 190);
            g.drawString("Y Position:", 900, 205);
            g.drawString("X Velocity:", 900, 220);
            g.drawString("Y Velocity:", 900, 235);
            g.drawString("Velocity:", 900, 250);
            g.drawString("Velocity Angle:", 900, 265);
            g.drawString("X Acceleration:", 900, 280);
            g.drawString("Y Acceleration:", 900, 295);
            g.drawString("Acceleration:", 900, 310);
            g.drawString("kg", 1090, 65);
            g.drawString("m", 1090, 80);
            g.drawString("m", 1090, 95);
            g.drawString("m/s", 1090, 110);
            g.drawString("m", 1090, 125);
            g.drawString("s", 1090, 175);
            g.drawString("m", 1090, 190);
            g.drawString("m", 1090, 205);
            g.drawString("m/s", 1090, 220);
            g.drawString("m/s", 1090, 235);
            g.drawString("m/s", 1090, 250);
            g.drawString("rad", 1090, 265);
            g.drawString("m/s^2", 1090, 280);
            g.drawString("m/s^2", 1090, 295);
            g.drawString("m/s^2", 1090, 310);
            g.drawString("Whistler", 1030, 50);
            g.drawString(mass + "", 1030, 65);
            g.drawString(height + "", 1030, 80);
            g.drawString(startPosition + "", 1030, 95);
            g.drawString(Math.round(takeoffSpeed*100)/100. + "", 1030, 110);
            g.drawString(Math.round(finalDistance*100)/100. + "", 1030, 125);
            
            g.setColor(Color.RED);
            readyForTrack = true;
        }
        //this allows the user to track the trajectory of the ski jumper
        if (readyForTrack)
        {
            //two lines pinpointing the position of the skier for the user's given mouse position
            g.fillRect((int)(Math.round(posX.get(trackIndex)*100)/100.*4),0,1,600);
            g.fillRect(0,(int)(600-Math.round(posY.get(trackIndex)*100)/100.*4),1200,1);
            
            //updates the displayed variables of the skier's trajectory for the user's given mouse position
            g.drawString(Math.round(t.get(trackIndex)*1000)/1000. + "", 1030, 175);
            g.drawString(Math.round(posX.get(trackIndex)*100)/100. + "", 1030, 190);
            g.drawString(Math.round(posY.get(trackIndex)*100)/100. + "", 1030, 205);
            g.drawString(Math.round(velX.get(trackIndex)*100)/100. + "", 1030, 220);
            g.drawString(Math.round(velY.get(trackIndex)*100)/100. + "", 1030, 235);
            g.drawString(Math.round(velocity.get(trackIndex)*100)/100. + "", 1030, 250);
            g.drawString(Math.round(velAngle.get(trackIndex)*100)/100. + "", 1030, 265);
            g.drawString(Math.round(accX.get(trackIndex)*100)/100. + "", 1030, 280);
            g.drawString(Math.round(accY.get(trackIndex)*100)/100. + "", 1030, 295);
            g.drawString(Math.round(acceleration.get(trackIndex)*100)/100. + "", 1030, 310);
            
        }
    }  
}