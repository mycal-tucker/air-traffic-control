
import java.util.ArrayList;

/**
 * @author Mycal Tucker
 */

public class Simulator extends Thread{
	private int time; //current time of simulation in milliseconds
	private ArrayList<Airplane> airplaneList; //the Airplanes that will be simulated
	private ArrayList<Airport> airportList;

	private boolean running; //whether or not the simulation has started
	private DisplayClient dc;
	private int numNonUpdatedPlanes; 

	public Simulator(DisplayClient dc){
		this.dc = dc;
		this.running = false;
		this.airplaneList = new ArrayList<Airplane>();
		this.airportList = new ArrayList<Airport>();
	}

	/**
	 * @return how many seconds have elapsed in the simulation. Returns 0 if
	 * the simulation hasn't started yet.
	 */
	public synchronized int getCurrentSec(){
		if (this.running){
			return this.time/1000;
		}
		return 0;
	}

	/**
	 * @return how many milliseconds have elapsed since the last full second
	 * in the simulation. Returns 0 if the simulation hasn't started yet.
	 */
	public synchronized int getCurrentMSec(){
		if (this.running){
			return this.time%1000;
		}
		return 0;
	}
	
	/**
	 * Return the ground vehicle at index i in simulator.
	 * Used primarily for testing purposes.
	 * If the index is illegal, throw an illegal argument exception
	 */
	public synchronized Airplane getAirplaneAtInd(int i){
		if (i < 0 || i >= this.airplaneList.size()){
			throw new IllegalArgumentException("index out of bounds");
		}
		return this.airplaneList.get(i);
	}

	/**
	 * 
	 * @param gv: ground vehicle to add the list of ground vehicles
	 * Note: assumes that the ground vehicle arrives non-updated
	 */
	public void addAirplane(Airplane a){
		this.airplaneList.add(a);
		a.start();
		this.numNonUpdatedPlanes ++;
	}

	public int getNumNonUpdated(){
		return this.numNonUpdatedPlanes;
	}

	public void setNumNonUpdated(int newNum){
		this.numNonUpdatedPlanes = newNum;
	}

	/**
	 * Just updates the time. Airplanes must pull the time
	 * on their own.
	 */
	public void run(){
		dc.traceOff();
		dc.clear();
		
		this.running = true;
		this.time = 0;

		while (this.time < 100000){ //100 seconds == 100,000 milliseconds
			/*
			 * Must lock on this (the simulator) to guarantee that all vehicles
			 * get updated exactly once at each time step.
			 */
			synchronized(this){				
				double[] x = new double[this.airplaneList.size()];
				double[] y = new double[this.airplaneList.size()];
				double[] theta = new double[this.airplaneList.size()];
				for (int i = 0; i < this.airplaneList.size(); i ++){
					Airplane temp = this.airplaneList.get(i);
					x[i] = temp.getPosition()[0];
					y[i] = temp.getPosition()[1];
					theta[i] = temp.getPosition()[2];
					//System.out.println("theta: " + theta[i]);
				}
				dc.update(this.airplaneList.size(), x, y, theta);
				dc.traceOn();
				
				
				this.time += 10;
				
				notifyAll();
				//wait for all gv's to update
				while (this.numNonUpdatedPlanes > 0){
					try{
						this.wait();
						//this.numNonUpdatedPlanes --;
					}
					catch(InterruptedException ie){
						System.err.println("There was an ie error");
						System.err.println(ie);
					}
				}
				
				this.numNonUpdatedPlanes = this.airplaneList.size();
			}
		}
		
		dc.traceOff();
		dc.clear();
	}
	
	private void addAirport(Airport a){
		this.airportList.add(a);
	}


	public void printInfo(){
		for (Airplane gv: this.airplaneList){
			String output = new String();
			output = output + String.format("%.2f", this.time/1000.0) + "\t"; //2 decimal places
			output = output + String.format("%.2f", gv.getPosition()[0]) + "\t" + 
					String.format("%.2f", gv.getPosition()[1]); //2 decimal places
			output = output + "\t" + String.format("%.1f", 180.0/Math.PI*gv.getPosition()[2]); //1 decimal place
			System.out.println(output);
			System.out.println();
		}
	}

	/**
	 * Constructs a Simulator and then runs it.
	 * @param argv
	 */
	public static void main(String[] argv) {
		if (argv.length <= 1) {
			System.err.println("Usage: Simulator <numVehicles> <hostname> where "
					+ "<numVehicles> is the number of vehicles to simulate and"
					+ "<hostname> is where DisplayServer is running");
			System.exit(-1);
		}
		//int numVehicles = Integer.parseInt(argv[0]);
		//if numVehicles == 0, assumes user meant no followers,
		//so adds a random vehicle but no followers.
		
		String host = argv[1];

		DisplayClient tempDC = new DisplayClient(host);
		Simulator s = new Simulator(tempDC);
		
		
		Airport a1 = new Airport(25, 50, 2, s);
		Airport a2 = new Airport(75, 50, 2, s);
		Airport a3 = new Airport(25, 75, 2, s);
		//Airport a4 = new Airport(75, 25, 2, s);
		
		s.addAirport(a1);
		s.addAirport(a2);
		s.addAirport(a3);
		//s.addAirport(a4);
		
		//tempDC.sendAirportMessage(s.airportList);
		
		//start with 50 fuel
		double[] p1startPose = {25, 25, 0};
		Airplane plane1 = new Airplane(p1startPose, 5, 0, s, 50);
		plane1.setPlaneName("plane1");
		
		double[] p2startPose = {5, 5, 0};
		Airplane plane2 = new Airplane(p2startPose, 5, 0, s, 50);
		plane2.setPlaneName("plane2");
		
		AirplaneController cont1 = new AirplaneController(s, plane1, a1, a2, 100);
		cont1.addOtherAirplane(plane2);
		AirplaneController cont2 = new AirplaneController(s, plane2, a2, a1, 100);
		cont2.addOtherAirplane(plane1);
		s.addAirplane(plane1);
		s.addAirplane(plane2);
		cont1.start();
		cont2.start();

		s.run();
	}
	
	private static double[] getStartPose(){
		double[] startPose = {Math.random()*100, Math.random()*100, Math.random()*2*Math.PI - Math.PI};
		return startPose;
	}
	
	private static double getStartSpeed(){
		return Math.random()*5 + 5;
	}
	
	private static double getStartOmega(){
		return Math.random()*Math.PI/2 - Math.PI/4;
	}
}