
import java.util.ArrayList;

/**
 * @author Mycal Tucker
 */

public class Simulator extends Thread{
	private int time; //current time of simulation in milliseconds
	private ArrayList<Airplane> groundVList; //the Airplanes that will be simulated
	private ArrayList<Airport> airportList;

	private boolean running; //whether or not the simulation has started
	private DisplayClient dc;
	private int numNonUpdatedGV; //number of ground vehicles that haven't been updated yet
	//(used in the run() method)

	public Simulator(DisplayClient dc){
		this.dc = dc;
		this.running = false;
		this.groundVList = new ArrayList<Airplane>();
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
		if (i < 0 || i >= this.groundVList.size()){
			throw new IllegalArgumentException("index out of bounds");
		}
		return this.groundVList.get(i);
	}

	/**
	 * 
	 * @param gv: ground vehicle to add the list of ground vehicles
	 * Note: assumes that the ground vehicle arrives non-updated
	 */
	public void addAirplane(Airplane a){
		this.groundVList.add(a);
		a.start();
		this.numNonUpdatedGV ++;
	}

	public int getNumNonUpdated(){
		return this.numNonUpdatedGV;
	}

	public void setNumNonUpdated(int newNum){
		this.numNonUpdatedGV = newNum;
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

		while (this.time < 10000){ //100 seconds == 100,000 milliseconds
			/*
			 * Must lock on this (the simulator) to guarantee that all vehicles
			 * get updated exactly once at each time step.
			 */
			synchronized(this){				
				double[] x = new double[this.groundVList.size()];
				double[] y = new double[this.groundVList.size()];
				double[] theta = new double[this.groundVList.size()];
				for (int i = 0; i < this.groundVList.size(); i ++){
					Airplane temp = this.groundVList.get(i);
					x[i] = temp.getPosition()[0];
					y[i] = temp.getPosition()[1];
					theta[i] = temp.getPosition()[2];
				}
				dc.update(this.groundVList.size(), x, y, theta);
				dc.traceOn();
				
				
				this.time += 10;
				
				notifyAll();
				//wait for all gv's to update
				while (this.numNonUpdatedGV > 0){
					try{
						this.wait();
						//this.numNonUpdatedGV --;
					}
					catch(InterruptedException ie){
						System.err.println("There was an ie error");
						System.err.println(ie);
					}
				}
				
				this.numNonUpdatedGV = this.groundVList.size();
			}
		}
		
		dc.traceOff();
		dc.clear();
	}
	
	private void addAirport(Airport a){
		this.airportList.add(a);
	}


	private void printInfo(){
		for (Airplane gv: this.groundVList){
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
		
		
		Airport a1 = new Airport(25, 25, 2);
		Airport a2 = new Airport(50, 50, 2);
		Airport a3 = new Airport(25, 75, 2);
		
		s.addAirport(a1);
		s.addAirport(a2);
		s.addAirport(a3);
		
		tempDC.sendAirportMessage(s.airportList);
		
		//start with 50 fuel
		Airplane plane1 = new Airplane(getStartPose(), getStartSpeed(), getStartOmega(), s, 50);
		//departure time of 50
		AirplaneController cont1 = new AirplaneController(s, plane1, a1, a2, 50);
		
		Airplane plane2 = new Airplane(getStartPose(), getStartSpeed(), getStartOmega(), s, 50);
		AirplaneController cont2 = new AirplaneController(s, plane1, a3, a2, 50);
		
		s.addAirplane(plane1);
		cont1.start();
		s.addAirplane(plane2);
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