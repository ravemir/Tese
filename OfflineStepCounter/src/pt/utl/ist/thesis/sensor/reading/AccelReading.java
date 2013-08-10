/**
 * 
 */
package pt.utl.ist.thesis.sensor.reading;

import pt.utl.ist.thesis.util.MathUtils;

/**
 * Class that represents a reading from an accelerometer
 * sensor. All attributes are private and final, with the
 * 'norm' attribute computed on instantiation.
 * 
 * @author Carlos Simões
 */
public class AccelReading extends SensorReading {

	private final double[] acceleration;
	private final double norm;
	
	/**
	 * Creates an {@link AccelReading}AccelReading object with the
	 * specified timestamp and acceleration
	 * values.
	 * 
	 * @param ts	The timestamp double value, 
	 * 				in milliseconds.
	 * @param accel	The read acceleration values.
	 */
	public AccelReading(Double ts, float[] accel){
		this(ts.toString(), accel);
	}
	
	/**
	 * Creates an {@link AccelReading} object with the
	 * specified formatted timestamp string and
	 * acceleration values.
	 * 
	 * @param ts	The formatted timestamp string,
	 * 				in milliseconds.
	 * @param accel	The read acceleration values.
	 */
	public AccelReading(String ts, float[] accel) {
		super(ts);
		
		// Copies the acceleration values
		acceleration = new double[3];
		for (int i = 0; i < 3; i++) {
			acceleration[i] = (double) accel[i];
		}
		
		// If the acceleration vector has length 3...
		if (accel.length == 3){
			//...the norm must be calculated
			norm = MathUtils.norm(acceleration);
		} else if (accel.length == 4){
			//...but if it has length 4, the norm is already included
			norm = accel[3];
		} else {
			throw new UnsupportedOperationException("Tried to create a reading with length " + accel.length);
		}
	}
	
	public AccelReading(Double ts, double[] accel){
		this(ts.toString(), accel);
	}
	
	public AccelReading(String ts, double[] accel){
		super(ts);
		
		// Copies the acceleration values
		acceleration = new double[3];
		for (int i = 0; i < 3; i++) {
			acceleration[i] = accel[i];
		}
		
		// If the acceleration vector has length 3...
		if (accel.length == 3) {
			// ...the norm must be calculated
			norm = MathUtils.norm(acceleration);
		} else if(accel.length == 4){
			// ...but if it has length 4, the norm is already included
			norm = accel[3];
		} else {
			throw new UnsupportedOperationException("Tried to create a reading with length " + accel.length);
		}
	}
	
	/**
	 * Creates an {@link AccelReading}AccelReading object with a
	 * timestamp string of zero and zeroed
	 * acceleration values.
	 */
	public AccelReading() {
		this("0", new double[]{0,0,0});
	}
	
	/**
	 * Clones a given {@link AccelReading} object.
	 * 
	 * @param ar The object to be cloned.
	 */
	public AccelReading(SensorReading ar){
		this(ar.formattedTs, ar.getReadingAndNorm());
	}

	@Override
	public double[] getReading() {
		return getAcceleration();
	}
	
	/**
	 * Returns a single double vector with both the
	 * acceleration and norm values together.
	 * 
	 * @return The vector with both types of value.
	 */
	@Override
	public double[] getReadingAndNorm(){
		double[] ret = new double[4];
		
		// Copy the acceleration values
		for (int i = 0; i < acceleration.length; i++) {
			ret[i] = acceleration[i];
		}
		
		// Copy the norm value
		ret[3] = norm;
		
		return ret;
	}

	public double[] getAcceleration() {
		return acceleration;
	}
	
	public double getReadingNorm(){
		return norm;
	}
	
	public String toString(){
		return	norm + ", " +
				acceleration[0] + ", " +
				acceleration[1] + ", " +
				acceleration[2];
	}
}
