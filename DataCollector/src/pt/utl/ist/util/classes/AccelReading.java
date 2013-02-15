/**
 * 
 */
package pt.utl.ist.util.classes;

/**
 * Class that represents a reading from an accelerometer
 * sensor. All attributes are private and final, with the
 * 'norm' attribute computed on instantiation.
 * 
 * @author Carlos Simões
 */
public class AccelReading extends SensorReading {

	
	private final float[] acceleration;
	private final double norm;

	public AccelReading(Double ts, float[] accel) {
		super(ts);
		acceleration = accel;
		norm = Math.sqrt(Math.pow(acceleration[0], 2) + 
				Math.pow(acceleration[1], 2) + 
				Math.pow(acceleration[2], 2));
	}

	@Override
	public float[] getReading() {
		return getAcceleration();
	}

	public float[] getAcceleration() {
		return acceleration;
	}
	
	public double getAccelerationNorm(){
		return norm;
	}
}
