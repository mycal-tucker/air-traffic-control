
public class AirplaneController extends Thread{
	private Simulator s; //S is shared with all other vehicle controllers, so it must
							//be protected.
	private Airplane plane; //is unique to the controller, so doesn't need locks
	private Airport startAirport;
	private Airport endAirport;

	public AirplaneController(Simulator s, Airplane p, Airport startAirport, Airport endAirport){
		if (p == null){
			throw new IllegalArgumentException("plane was null");
		}
		
		this.s = s;
		this.plane = p;
		this.startAirport = startAirport;
		this.endAirport = endAirport;
	}

	public void run(){
		double newTime = 0;
		double oldTime = 0;
		
		while (newTime < 100){
			/*
			 * Must lock on the simulator to make sure that simulator isn't accessed until
			 * this controller has seen a timestep and has updated appropriately.
			 * If it sees that s has updated all vehicles, notifies all threads waiting
			 * on s that it is now free.
			 */
			synchronized(this.s){
				newTime = this.s.getCurrentSec() + .001*this.s.getCurrentMSec();
				while (newTime == oldTime){
					try{
						//wait until time has changed
						s.wait();
						newTime = this.s.getCurrentSec() + .001*this.s.getCurrentMSec();
					}
					catch (InterruptedException ie){
						System.err.println("There was an interrupted exception");
						System.err.println(ie);
					}
				}
				Control newControl = this.getControl((int)(newTime*1000));
				this.plane.controlVehicle(newControl);

				oldTime = newTime;
				
				//since this vehicle is now updated, tell the simulator
				if (this.s.getNumNonUpdated() > 0){
					this.s.setNumNonUpdated(this.s.getNumNonUpdated()-1);
				}
				//if all vehicles are updated, tell all threads waiting on the simulator that
				//it's free now.
				if (this.s.getNumNonUpdated() == 0){
					this.s.notifyAll();
				}
			}
			
		}
	}

	public Control getControl(int time){
		//TODO: logic
		if (time <= 0){
			double[] oldPose = plane.getPosition();
			double[] newPose = new double[3];
			newPose[0] = oldPose[0];
			newPose[1] = oldPose[1];
			//change the angle to point from startAirport to endAirport 
			newPose[2] = Math.atan2(endAirport.getY() - startAirport.getY(), endAirport.getX() - startAirport.getX());
			//TODO unit tests for angle calculation
			
		}
		return new Control(10, 0);
	}
}
