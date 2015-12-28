import java.io.*;
import java.awt.*;
import javax.swing.*;

/**
 * Ski Jump Simulation
 * 
 * This program simulates and displays the trajectory of a ski jumper by using numerical methods (Euler's method)
 * The program is a specific simulation of Wolfgang Loitzl ski jumping at Whistler's 140m hill on Feb 22nd 2011.
 * Real life data was used to plot the altitude of the hill and to approximate initial conditions and constraints 
 * as well as the changing angle of the skier in flight. This was done in order to make the results of the simulation 
 * as realistic as possible.
 * The simulation is conducted by the application and the graphical display is accomplished by adding a GraphicsPanel
 * object (extend JPanel) to a JFrame and passing it the results of the simulation
 * 
 * The simulation of the ski jump consists of 3 phases:
 * Phase1:  Euler's method along with Newton's laws used to simulate the motion of the skier along the takeoff ramp
 *          Time is incremented then acceleration is calculated and from this velocity and position are determined
 *          In the phase, the acceleration, change in velocity and position are all in the direction of the hill 
 * Phase2:  The jump at the takeoff point is simulated by increasing the x and y components of the skier's velocity
 *          This increase in velocity is calculated using Newton's laws and assuming a skier can jump 0.4m in the air
 * Phase3:  Euler's method is used again along with Newton's laws to simulate the motion of the skier in the air
 *          In this phase the forces acting on the skier are weight, lift and drag. The lift and drag each depend
 *          on the angle of the skier relative to the angle of the velocity vector (see the methods for details).
 *          The x and y components of acceleration are calculated for each time increment and from this the change 
 *          in velocity components and x and y positions are determined.
 * Results: The resulting distance of the skier's jump is calculated as the distance from the takeoff to the landing
 *          point. At this point all the results have been passed to the GraphicsPanel object and its repaint();
 *          method is called in order to display the graphical description of the hill and skier's trajectory
 *          Data as a function of time is also output to an excel file.
 *         
 * Data for the geometry of whistler 140m hill used for this simulation can be found at the following url address
 * http://skijump.callaghanwintersportsclub.ca/wp-content/uploads/2013/03/CertificateJumpingHills.pdf
 * 
 * Information regarding the physical model used for the ski jump can be found at the following url addresses
 * http://e-jst.teiath.gr/issue_16/stathopoulos_16.pdf
 * http://cds.cern.ch/record/1009275/files/p269.pdf
 * 
 * The results and video of the run that is being simulated by this program can be seen at the following url addresses 
 * http://www.fis-ski.com/pdf/2010/JP/3042/2010JP3042RL.pdf
 * http://www.youtube.com/watch?v=pI_0tORrnzA&t=16m29s
 * 
 * Note: Positive y is taken as up and positive x is take as right (towards the base of the ski jumping hill)
 * Note: acceleration ang velocity are magnitudes while angVel is their direction (relative to the positive x-axis)
 * 
 * @author  Demetrios Koziris
 * @version 5/12/2013
 */

public class SkiJump
{
    //Constant variables 
    public static final double dt = 0.001;                                      // (s)          the time increment for Euler's methed
    public static final double g = 9.81;                                        // (N/kg)       acceleration due to gravity
    public static final double airDensity = 1.13;                               // (kg/m^3)     calculated using temp. and humidity values from the competition and elevation of the hill
    public static final double height = 1.8;                                    // (m)          height of Wolfgang
    public static final double massSki = 2*(height*1.45);                       // (kg)         weight of the skis is calculated according to ski jumping regulations
    public static final double massBody = 63;                                   // (kg)         weight of Wolfgang
    public static final double mass = massSki + massBody + 2;                   // (kg)         total mass of the skier (body + skis + clothes/bindings/boots)
    public static final double frictionCoeff = 0.05;                            //              between snow and waxed skis
    public static final double frontalAreaBody = height*0.3;                    // (m^2)        calculated using height and an approximation of the average width of the body 
    public static final double frontalAreaDuringTakeoff = frontalAreaBody*0.5;  // (m^2)        approximated as half the frontal body area when standing
    public static final double frontalAreaSkis = 2*(height*1.45*.1);            // (m^2)        calculated according to ski jumping regulations
    public static final double startPosition = 6.25;                            // (m)          found in the competition video
    
    
    public static void main (String [] args)
    {        
        //Setup file for data output
        PrintWriter outputFile = null;
        try 
        {
            outputFile = new PrintWriter(new FileOutputStream("SkiJumpResultsData.xls",false));
            outputFile.println("t\tslopeDist\thillAltitude\tposX\tposY\tvelocity\tvelX\tvelY\tacceleration\taccX\taccY\tvelAngle");
        }
        catch(FileNotFoundException e) 
        {
            System.out.println("File error.  Program aborted.");
            System.exit(0);
        }
        
        //a JFrame object is created and a GraphicsPanel object is added to it
        JFrame display = new JFrame("Ski Jumping Simulation Graphical Display");
        GraphicsPanel panel = new GraphicsPanel();
        display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        display.add(panel);
        display.setSize(1200,623);
        display.setVisible(true);
        //calls the method that will pass the geometry of the hill as a Polygon object to the panel in the JFrame
        displayHill(panel);
        
        
        
        //variables are declared and initial conditions set
        double t = 0;
        double slopeDist = startPosition;  
        //staring position is given as a distance along the slope so the x position of this starting position is \
        //calculated using the method posXForDistanceAlongSlope()
        double posX = posXForDistanceAlongSlope(slopeDist); 
        double posY = hillAltitude(posX);
        double velX = 0;                                            //x component
        double velY = 0;                                            //y comnponent
        double velocity = 0;                                        //magnitude
        double accX = 0;                                            //x component                                           
        double accY = 0;                                            //y component
        double acceleration = Math.sqrt(accX*accX + accY*accY);     //magnitude
        double velAngle = Math.atan(velY/velX);                     //direction of the skier's velocity and aceleration (angle relative to the positive x-axis)
        boolean inAir = false;                                      
        //result variables
        double takeoffSpeed;
        double finalDistance;
        
        
        /////////////////////
        //PHASE1: Takeoff  //
        /////////////////////
        while (slopeDist <= 102.15)
        {
            velAngle = angleOfSlope(slopeDist);
            acceleration = g*(Math.sin(-velAngle) - frictionCoeff*Math.cos(-velAngle)) - (0.5*airDensity*frontalAreaDuringTakeoff*0.5*Math.pow(velocity,2))/mass; 
            accX = acceleration*Math.cos(velAngle);
            accY = acceleration*Math.sin(velAngle);
            velocity += acceleration*dt;
            velX = velocity*Math.cos(velAngle);
            velY = velocity*Math.sin(velAngle);
            
            slopeDist += velocity*dt + 0.5*acceleration*Math.pow(dt,2);
            posX = posXForDistanceAlongSlope(slopeDist);
            posY = hillAltitude(posX);
            t += dt;
            
            //Results passed to the panel for graphics display and to the excel file
            panel.updateTrajectoryData(t, posX, posY, velX, velY, velocity, velAngle, accX, accY, acceleration);
            outputFile.printf("%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t\n", t, slopeDist, hillAltitude(posX), posX, posY, velocity, velX, velY, acceleration, accX, accY, velAngle);
        }
        takeoffSpeed = velocity;
        

        /////////////////////
        //PHASE2: Jump     //
        /////////////////////
        velX = velocity*Math.cos(angleOfSlope(slopeDist)) + Math.sqrt(2*g*0.4)*Math.sin(angleOfSlope(slopeDist));
        velY = velocity*-Math.sin(angleOfSlope(slopeDist)) + Math.sqrt(2*g*0.4)*Math.cos(angleOfSlope(slopeDist));
        velocity = Math.sqrt(velX*velX + velY*velY);
        inAir = true;
        double jumpStart = t;
        
        
        /////////////////////
        //PHASE3: Fly      //
        /////////////////////
        while (inAir)
        {
            //Euler's method used to calculate acceleration components and from this velocity and position are found
            //time is then incremented
            
            velAngle = Math.atan(velY/velX); //angle of the velocity and the acceleration
            
            accX = (liftForce(velocity, velAngle, t-jumpStart)*-Math.sin(velAngle) + dragForce(velocity, velAngle, t-jumpStart)*-Math.cos(velAngle))/mass;
            accY = -g + (liftForce(velocity, velAngle, t-jumpStart)*Math.cos(velAngle) + dragForce(velocity, velAngle, t-jumpStart)*-Math.sin(velAngle))/mass;
            acceleration = Math.sqrt(accX*accX + accY*accY);
            
            velX += accX*dt;
            velY += accY*dt;
            velocity = Math.sqrt(velX*velX + velY*velY);
            
            posX += velX*dt + 0.5*accX*Math.pow(dt,2);
            posY += velY*dt + 0.5*accY*Math.pow(dt,2);
            
            t += dt;
            
            //if statement will alow Euler's method to keep iterating so long as the y position of the skier's is higher than the hill by a third of his height
            //this value (a third of his height) is the approximate distance from his center of mass to the bottom of his skis when in his stance during the flight
            if (posY >= hillAltitude(posX)+height/3)
            {
                //Results passed to the panel for graphics display and to the excel file
                panel.updateTrajectoryData(t, posX, posY, velX, velY, velocity, velAngle, accX, accY, acceleration);
                outputFile.printf("%f\t %f\t %f\t %f\t %f\t %f\t %f\t %f\t %f\t %f\t %f\t %f\t\n", t, slopeDist, hillAltitude(posX), posX, posY, velocity, velX, velY, acceleration, accX, accY, velAngle);
            }
            else
            {
                //the skier has landed
                inAir = false;
            } 
        }
        
        /////////////////////
        //RESULTS          //
        /////////////////////
        //resulting distance of the skier's jump is calculated as the distance from the takeoff to the landingpoint
        finalDistance = Math.sqrt(Math.pow((posX-88.64),2) + Math.pow((88.15-posY),2));    
        
        //results are output to the panel, setReady argument is called with the true argument and the repaint(); method is called causing the panel to display the results
        panel.setResultsData(mass, height, startPosition, takeoffSpeed, finalDistance);
        panel.setReady(true);
        panel.repaint();
        
        //completes the output of results to the excel file
        outputFile.close();
    }
    
    /**
     * Calculates the drag force magnitude as a function of the air density, projected area of the skier, drag coefficient, and velocity
     * Coefficient of drag is calculated as a function of angle of attack
     * @param velocity      the velocity of the skier
     * @param velAngle      the angle of the skier's velocity
     * @param t             time since takeoff
     */
    public static double dragForce (double velocity, double velAngle, double t)
    { 
        double [] angAtt = angleOfAttack(velAngle, t);
        double dragCoeff = 0.0103*angAtt[0]*180/3.14;
        double projectedArea = frontalAreaSkis*Math.sin(angAtt[0]) + frontalAreaBody*Math.sin(angAtt[1]);
        double drag = Math.abs(0.5*airDensity*projectedArea*dragCoeff*Math.pow(velocity,2));
        return drag;
    }
    
    /**
     * Calculates the lift force magnitude as a function of the air density, projected area of the skier, lift coefficient, and velocity
     * Coefficient of lift is calculated as a function of angle of attack
     * @param velocity      the velocity of the skier
     * @param velAngle      the angle of the skier's velocity
     * @param t             time since takeoff
     */
    public static double liftForce (double velocity, double velAngle, double t)
    {
        double [] angAtt = angleOfAttack(velAngle, t);
        double liftCoeff = Math.abs(-0.00025*Math.pow(angAtt[0]*180/3.14,2) + 0.0228*angAtt[0]*180/3.14 - 0.092);
        double projectedArea = frontalAreaSkis*Math.cos(angAtt[0]) + frontalAreaBody*Math.cos(angAtt[1]);
        double lift = Math.abs(0.5*airDensity*projectedArea*liftCoeff*Math.pow(velocity,2));
        return lift;
    }
    
    /**
     * Returns the angle of attack of the skier as a function of his time in the air. (Values taken from real ski jump data)
     * angAtt[0] is the angle of the skis relative to the velocity
     * angAtt[1] is the angle of the body relative to the velocity
     * @param velAngle      angle of the skier's velocity
     * @param t             time since takeoff
     * @return              array containing angles of attack
     */
    public static double[] angleOfAttack (double velAngle, double t)
    {   
        double [] angAtt = new double[2];
        if (t <= 0.04)
        {
            angAtt[0] = Math.abs(velAngle + 0.209);
            angAtt[1] = Math.abs(velAngle + 1.187 + 0.209);
        }
        else if (t <= 0.21)
        {
            angAtt[0] = Math.abs(velAngle + 0.087);
            angAtt[1] = Math.abs(velAngle - 1.047 + 0.087);
        }
        else if (t <= 0.63)
        {
            angAtt[0] = Math.abs(velAngle - 0.209);
            angAtt[1] = Math.abs(velAngle - 0.349 - 0.209);
        }
        else if (t <= 1.05)
        {
            angAtt[0] = Math.abs(velAngle - 0.122);
            angAtt[1] = Math.abs(velAngle - 0.349 - 0.122);
        }
        else if (t <= 1.43)
        {
            angAtt[0] = Math.abs(velAngle - 0.105);
            angAtt[1] = Math.abs(velAngle - 0.349 - 0.105);
        }
        else if (t <= 2.04)
        {
            angAtt[0] = Math.abs(velAngle - 0.035);
            angAtt[1] = Math.abs(velAngle - 0.349 - 0.035);
        }
        else if (t <= 2.26)
        {
            angAtt[0] = Math.abs(velAngle - 0.017);
            angAtt[1] = Math.abs(velAngle - 0.349 - 0.017);
        }
        else if (t <= 2.71)
        {
            angAtt[0] = Math.abs(velAngle - 0.017);
            angAtt[1] = Math.abs(velAngle - 0.349 - 0.017);
        }
        else if (t <= 3.26)
        {
            angAtt[0] = Math.abs(velAngle - 0.017);
            angAtt[1] = Math.abs(velAngle - 0.349 - 0.017);
        }
        else 
        {
            angAtt[0] = Math.abs(velAngle - 0.035);
            angAtt[1] = Math.abs(velAngle - 0.349 - 0.035);
        }
        return angAtt;
    }
    
    /**
     * Returns the altitude of the hill (y value) for a given x position
     * @param posX          x position
     * @return              corresponding y position        
     */
    public static double hillAltitude (double posX)
    {
        double posY = 0;
        if (posX <= 44.32)
        {
            posY = 136.63 - 0.7*posX;
        } 
        else if (posX <=   82.17)
        {
            posY = -Math.sqrt(10000 - Math.pow((posX-101.68),2)) + 187.52;
        }
        else if (posX <= 88.642)
        {
            posY = 105.87 - 0.2*posX;
        }
        else if (posX <= 142.55)
        {
                posY = Math.sqrt(8047.18 - Math.pow((posX-88.64),2)) - 5.14;
        }
        else if (posX <= 186.96)
        {
            posY = 174.04 - 0.754*posX;
        }
        else if (posX <= 208.67)
        {
            posY = -Math.sqrt(113232.25 - Math.pow((posX-389.47),2)) + 301.81;
        }
        else if (posX <= 270.46)
        {
            posY = -Math.sqrt(13225 - Math.pow((posX-270.46),2)) + 115;
        }
        return posY;
    }
    
    /**
     * Returns the x position for a given distance along the takeoff slope
     * @param slopeDist         distance along the takeoff slope
     * @return                  corresponding x position
     */
    public static double posXForDistanceAlongSlope (double slopeDist)
    {
        double posX = 0;
        if (slopeDist <= 54.1)
        {
            posX = Math.cos(0.611)*slopeDist;
        } 
        else if (slopeDist <= 95.55)
        {
            posX = 44.32 + 57.36 - Math.cos(0.96 + (slopeDist-54.1)/100)*100;
        }
        else if (slopeDist <= 102.15)
        {
            posX = 82.17 + Math.cos(0.196)*(slopeDist-95.55);
        }
        else
        {
            posX = 88.642;
        }
        return posX;
    }
    
    /**
     * Returns the angle of the takeoff slope for a given position (expressed as a distance along the takeoff slope)
     * @param slopeDist         distance along the takeoff slope
     * @return                  the angle (relative to the positive x axis)
     */
    public static double angleOfSlope (double slopeDist)
    {
        double slopeAngle = 0;
        if (slopeDist <= 54.1)
        {
            slopeAngle = -0.611;
        } 
        else if (slopeDist <= 95.55)
        {
            slopeAngle = -0.611 + (slopeDist-54.1)/100;
        }
        else if (slopeDist <= 102.15)
        {
            slopeAngle = -0.196;
        }
        return slopeAngle;
    }
    
    /**
     * Creates a polygon and adds points to it describing the geometry of the hill. Values are according to real data describing the whistler hill geometry
     * The hill Polygon object is then passed the panel GraphicsPanel object which will display it at a later point
     * @param panel             A GraphicsPanel object used to display the results of the ski jump simulation
     */
    public static void displayHill (GraphicsPanel panel)
    {
        Polygon hill = new Polygon();
        hill.addPoint(0, 600);
        double dx = .01;
        double hillX = 0;
        double hillY = hillAltitude(hillX);
        hill.addPoint((int)hillX*4, (int)(600-hillY*4));
        while (hillX < 300)
        {
            hillX += dx;
            hillY = hillAltitude(hillX);
            hill.addPoint((int)hillX*4, (int)(600-hillY*4));
        }
        panel.setHillShape(hill);
    }
}
