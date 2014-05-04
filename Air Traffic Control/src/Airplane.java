import java.util.Random;

//I just want to see that this shows up on git
//I added another comment

public class Airplane extends Thread{
	private double x; //must be in [0, 100]
	private double xDot;
	private double y; //must be in [0, 100]
	private double yDot; //overall speed (function of xDot and yDot) must
	//be in [5, 10]
	private double theta; //must be in [-pi, pi]
	private double thetaDot; //must be in [-pi/4, pi/4]
	private Simulator s;

	private final int MINSPEED = 5;
	private final int MAXSPEED = 10;
	private final int MINCOORDINATE = 0;
	private final int MAXCOORDINATE = 100;
	private final double MINTHETA = -Math.PI;
	private final double MAXTHETA = Math.PI;
	private final double MINOMEGA = -Math.PI/4;
	private final double MAXOMEGA = Math.PI/4;
	
//	private final double DOWNRANGE_VAR = Math.sqrt(.025);
//	private final double CROSSRANGE_VAR = Math.sqrt(.05);
	private final double DOWNRANGE_VAR = Math.sqrt(0);
	private final double CROSSRANGE_VAR = Math.sqrt(0);

	public Airplane(double[] pose, double s, double omega, Simulator sim){
		this.s = sim;

		if (pose.length != 3){
			throw new IllegalArgumentException("The pose must have length 3");
		}		
		this.x = this.withinBounds(pose[0], MINCOORDINATE, MAXCOORDINATE);
		this.y = this.withinBounds(pose[1], MINCOORDINATE, MAXCOORDINATE);
		this.theta = this.convertTheta(pose[2]);
		this.thetaDot = this.withinBounds(omega, MINOMEGA, MAXOMEGA);

		double speed = this.withinBounds(s, MINSPEED, MAXSPEED);
		this.xDot = speed*Math.cos(this.theta);
		this.yDot = speed*Math.sin(this.theta);

		if (!checkRep()){throw new RuntimeException("The constraints on the state have been violated");}
	}

	public void run(){
		boolean done = false;
		double time = 0;
		double oldTime = 0;
		while (!done){
			synchronized(this.s){
				try{
					time = this.s.getCurrentSec() + .001*this.s.getCurrentMSec();
					if (time >= oldTime + .01){
						double timeDif = time-oldTime;
						int sec = (int)(timeDif);
						int msec = (int) ((timeDif*1000)%1000);
						this.advance(sec, msec);
						oldTime = time;
					}
					else{
						s.wait();
					}
					if (time >= 100){
						done = true;
					}
					
				}
				catch (InterruptedException ie){
					System.err.println("There was an interrupted exception");
					System.err.println(ie);
				}
			}
		}
	}

	/**
	 * check that our internal representation never violates
	 * what we are told.
	 * @return true iff the requirements are met
	 */
	private synchronized boolean checkRep(){
		if (this.x < MINCOORDINATE || this.x > MAXCOORDINATE){
			return false;
		}
		if (this.y < MINCOORDINATE || this.y > MAXCOORDINATE){
			return false;
		}
		if (this.theta < MINTHETA || this.theta > MAXTHETA){
			return false;
		}
		double speed = Math.sqrt(this.xDot*this.xDot + this.yDot*this.yDot);
		if (speed < MINSPEED || speed > MAXSPEED){
			return false;
		}
		if (this.thetaDot < MINOMEGA || this.thetaDot > MAXOMEGA){
			return false;
		}
		return true;
	}

	/**
	 * @return an array of 3 doubles that say the x, y, and theta
	 * of the ground vehicle
	 */
	public synchronized double[] getPosition(){
		double[] returnable = new double[3];
		returnable[0] = this.x;
		returnable[1] = this.y;
		returnable[2] = this.theta;

		return returnable;
	}

	/**
	 * @return an array of 3 doubles that say the x velocity, y
	 * velocity, and angular velocity of the ground vehicle
	 */
	public synchronized double[] getVelocity(){
		double[] returnable = new double[3];
		returnable[0] = this.xDot;
		returnable[1] = this.yDot;
		returnable[2] = this.thetaDot;

		return returnable;
	}

	/**
	 * Given an x, y, and theta desired, the ground vehicle updates its current
	 * position to reflect the new parameters (within legal bounds)
	 * @param desiredPosition
	 */
	public synchronized void setPosition(double[] desiredPosition){
		if (desiredPosition.length != 3){
			throw new IllegalArgumentException("The input array must have"
					+ " exactly 3 entries!");
		}

		double desiredX = desiredPosition[0];
		double desiredY = desiredPosition[1];
		double desiredTheta = desiredPosition[2];

		this.x = withinBounds(desiredX, MINCOORDINATE, MAXCOORDINATE);
		this.y = withinBounds(desiredY, MINCOORDINATE, MAXCOORDINATE);
		this.theta = convertTheta(desiredTheta);		
	}


	/**
	 * If the linear velocity is too great, preserve the angle but scale down the
	 * x and y components to create a legal linear velocity in the same direction.
	 * If the angular velocity is out of bounds, use the boundary closest to the desired
	 * omega.
	 * @param desiredVelocity
	 */
	public synchronized void setVelocity(double[] desiredVelocity){
		if (desiredVelocity.length != 3){
			throw new IllegalArgumentException("The input array must have"
					+ " exactly 3 entries!");
		}

		double desiredXDot = desiredVelocity[0];
		double desiredYDot = desiredVelocity[1];
		double desiredOmega = desiredVelocity[2];

		double velocityAngle = Math.atan(desiredYDot/desiredXDot);
		boolean flippedAngle = desiredXDot < 0; //take care of case when velocities point into
		//quadrants 2 or 3

		double desiredSpeed = Math.sqrt(Math.pow(desiredXDot, 2) + Math.pow(desiredYDot, 2));
		double newSpeed = withinBounds(desiredSpeed, MINSPEED, MAXSPEED);
		if (flippedAngle){
			newSpeed = -1*newSpeed;
		}
		double newXDot = newSpeed*Math.cos(velocityAngle);
		double newYDot = newSpeed*Math.sin(velocityAngle);

		double newOmega = withinBounds(desiredOmega, MINOMEGA, MAXOMEGA);

		this.xDot = newXDot;
		this.yDot = newYDot;
		this.thetaDot = newOmega;
	}

	/**
	 * Derives target velocities from the Control parameter and then
	 * uses setVelocity() to update current velocities to the new
	 * targets.
	 * If c is null, don't change anything
	 * @param c
	 */
	public synchronized void controlVehicle(Control c){
		if (c == null){
			return;
		}
		double desiredSpeed = this.withinBounds(c.getSpeed(), MINSPEED, MAXSPEED);
		double desiredOmega = this.withinBounds(c.getRotVel(), MINOMEGA, MAXOMEGA);

		double desiredXDot = desiredSpeed*Math.cos(this.theta);
		double desiredYDot = desiredSpeed*Math.sin(this.theta);

		double[] desiredVelocities = {desiredXDot, desiredYDot, desiredOmega};
		this.setVelocity(desiredVelocities);
	}

	/**
	 * Describes what the ground vehicle would do if it did not receive
	 * new instructions for sec+.001*msec seconds. Depending on original
	 * positions, velocity, angle, and angular velocity, this method
	 * updates the state variables to represent the new positions and
	 * velocities of the vehicle after a period of time.
	 * @param sec
	 * @param msec
	 */
	public synchronized void advance(int sec, int msec){
		Random r = new Random();
		double errD = r.nextGaussian()*DOWNRANGE_VAR;
		double errC = r.nextGaussian()*CROSSRANGE_VAR;
		
		
		double time = sec + .001*msec;

		if (time < 0){
			throw new RuntimeException("You should not pass in a negative value for time");
		}
		
		double linSpeed = this.withinBounds(Math.sqrt(Math.pow(this.xDot, 2) + Math.pow(this.yDot, 2)), MINSPEED, MAXSPEED);

		double angSpeed = this.withinBounds(this.thetaDot, MINOMEGA, MAXOMEGA);
		
		if (angSpeed == 0){ //if going straight, just update x and y
			this.x = this.withinBounds(this.x + linSpeed*time*Math.cos(this.theta), 0, 100) + Math.cos(this.theta)*errD - Math.sin(this.theta)*errC;
			this.y = this.withinBounds(this.y + linSpeed*time*Math.sin(this.theta), 0, 100) + Math.sin(this.theta)*errD + Math.sin(this.theta)*errC;

			return;
		}
		//if not going straight, use geometry to compute an arc
		double radius = linSpeed/angSpeed;
		double angleChange = time*angSpeed;
		double gamma = Math.PI - this.theta - angleChange/2;

		this.x = this.withinBounds(this.x - 2*radius*Math.sin(angleChange/2)*Math.cos(gamma), MINCOORDINATE, MAXCOORDINATE) + Math.cos(this.theta)*errD - Math.sin(this.theta)*errC;
		this.y = this.withinBounds(this.y + 2*radius*Math.sin(angleChange/2)*Math.sin(gamma), MINCOORDINATE, MAXCOORDINATE) + Math.sin(this.theta)*errD + Math.sin(this.theta)*errC;

		this.theta = this.convertTheta(this.theta + angleChange);

		this.xDot = linSpeed*Math.cos(this.theta);
		this.yDot = linSpeed*Math.sin(this.theta);
	}

	/**
	 * Returns the closest double to desired that is within lowBound and upBound. If
	 * desired is between the two bounds, just return desired. Otherwise, return the
	 * appropriate bound.
	 * @param desired: target double
	 * @param lowBound: lower bound on legal numbers
	 * @param upBound: upper bound on legal numbers
	 * @return: double closest to desired that is within lowBound and upBound
	 */
	private synchronized double withinBounds(double desired, double lowBound, double upBound){
		if (desired < lowBound){
			return lowBound;
		}
		else if (desired > upBound){
			return upBound;
		}
		return desired;
	}

	/**
	 * @param theta: any angle in radians (can be outside bounds of -pi to pi)
	 * @return an equivalent angle in radians between -pi and pi
	 */
	private synchronized double convertTheta(double theta){
		while (theta < 0){
			theta = theta + 2*Math.PI; //easier to work with theta if positive
		}

		if (theta >= -1*Math.PI && theta < Math.PI){
			return theta;
		}
		double modded = theta%(2*Math.PI);
		if (modded < Math.PI){
			return modded;
		}
		return modded - 2*Math.PI;
	}

	/**
	 * The String representation of a ground vehicle displays the position and velocity
	 * state of the vehicle.
	 */
	@Override
	public synchronized String toString(){
		double[] pos = this.getPosition();
		double[] vel = this.getVelocity();

		String posString = "";
		for (int i = 0; i < pos.length; i ++){
			posString = posString + String.format("%.2f", pos[i]) + " ";
		}
		String velString = "";
		for (int i = 0; i < vel.length; i ++){
			velString = velString + String.format("%.2f", vel[i]) + " ";
		}
		return "positions: " + posString + "        velocities: " + velString;
	}
}