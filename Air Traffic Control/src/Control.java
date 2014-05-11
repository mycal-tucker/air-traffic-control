/**
 * @author Mycal Tucker
 * 
 * Because Control only contains final attributes, the class is immutable.
 * Since it is immutable, it is threadsafe.
 *
 */

import java.lang.IllegalArgumentException;

public class Control
{
	private final double _s;
	private final double _omega;

	public Control (double s, double omega){
		if (omega < -Math.PI)
			this._omega = -Math.PI;
		else if (omega > Math.PI)
			this._omega = Math.PI;
		else
			this._omega = omega;

		if (s < 5)
			this._s = 5;
		else if (omega > 10)
			this._s = 10;
		else
			this._s = s;

	}

	public double getSpeed() {
		return _s;
	}

	public double getRotVel() {
		return _omega;
	}  
}