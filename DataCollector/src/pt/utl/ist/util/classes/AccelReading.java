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

	private final String formattedTs;

	private final float[] acceleration;
	private final double norm;
	
	/**
	 * Creates an AccelReading object with the
	 * specified timestamp and acceleration
	 * values.
	 * 
	 * @param ts	The timestamp double value.
	 * @param accel	The read acceleration values.
	 */
	public AccelReading(Double ts, float[] accel){
		this(ts.toString(), accel);
	}
	
	/**
	 * Creates an AccelReading object with the
	 * specified formatted timestamp string and
	 * acceleration values.
	 * 
	 * @param ts	The formatted timestamp string.
	 * @param accel	The read acceleration values.
	 */
	public AccelReading(String ts, float[] accel) {
		super(Double.valueOf(ts));
		formattedTs = ts;
		acceleration = accel;
		norm = Math.sqrt(Math.pow(acceleration[0], 2) + 
				Math.pow(acceleration[1], 2) + 
				Math.pow(acceleration[2], 2));
	}

	@Override
	public float[] getReading() {
		return getAcceleration();
	}
	
	@Override
	public String toString() {
		return formattedTs;
	}

	public float[] getAcceleration() {
		return acceleration;
	}
	
	public double getAccelerationNorm(){
		return norm;
	}
}
