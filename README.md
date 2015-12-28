# Ski Jumping Simulation
## Final Project for Introduction to Computer Programming in Engineering and Science Course at CEGEP Dawson
#### Author: Demetrios Koziris
#### Date: 2013-05-12

To run the simulation, open the SkiJumpSimulation.jar in the build folder.

This program simulates and displays the trajectory of a ski jumper by using numerical methods (Euler's method) The program is a specific simulation of Wolfgang Loitzl ski jumping at Whistler's 140m hill on Feb 22nd 2011.

Real life data was used to plot the altitude of the hill and to approximate initial conditions and constraints 
as well as the changing angle of the skier in flight. This was done in order to make the results of the simulation 
as realistic as possible.

The simulation is conducted by the application and the graphical display is accomplished by adding a GraphicsPanel
object (extend JPanel) to a JFrame and passing it the results of the simulation

The simulation of the ski jump consists of 3 phases:

### Phase 1:  

Euler's method along with Newton's laws used to simulate the motion of the skier along the takeoff ramp. Time is incremented then acceleration is calculated and from this velocity and position are determined. In the phase, the acceleration, change in velocity and position are all in the direction of the hill. 

### Phase 2:  

The jump at the takeoff point is simulated by increasing the x and y components of the skier's velocity. This increase in velocity is calculated using Newton's laws and assuming a skier can jump 0.4m in the air.

### Phase 3:  

Euler's method is used again along with Newton's laws to simulate the motion of the skier in the air. In this phase the forces acting on the skier are weight, lift and drag. The lift and drag each depend on the angle of the skier relative to the angle of the velocity vector (see the methods for details). The x and y components of acceleration are calculated for each time increment and from this the change in velocity components and x and y positions are determined.

### Results: 

The resulting distance of the skier's jump is calculated as the distance from the takeoff to the landing
         point. At this point all the results have been passed to the GraphicsPanel object and its repaint();
         method is called in order to display the graphical description of the hill and skier's trajectory
         Data as a function of time is also output to an excel file.

### Resources:

Data for the geometry of whistler 140m hill used for this simulation can be found at the following url address
http://skijump.callaghanwintersportsclub.ca/wp-content/uploads/2013/03/CertificateJumpingHills.pdf

Information regarding the physical model used for the ski jump can be found at the following url addresses
http://e-jst.teiath.gr/issue_16/stathopoulos_16.pdf
http://cds.cern.ch/record/1009275/files/p269.pdf

The results and video of the run that is being simulated by this program can be seen at the following url addresses 
http://www.fis-ski.com/pdf/2010/JP/3042/2010JP3042RL.pdf
http://www.youtube.com/watch?v=pI_0tORrnzA&t=16m29s
