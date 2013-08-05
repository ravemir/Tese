package pt.utl.ist.util.sensor.reading;

import pt.utl.ist.thesis.util.MathUtils;
import pt.utl.ist.util.sensor.exception.UnsupportedReadingSizeException;

public class OrientationReading extends SensorReading {

	private final Double azimuth;
	private final Double pitch;
	private final Double roll;
	private double[] providedAverageableReading; 
	
	/**
	 * Creates a {@link OrientationReading} object with the
	 * specified  timestamp value, azimuth, pitch
	 * and roll values.
	 * 
	 * @param ts	The Timestamp value.
	 * @param az	The Azimuth value.
	 * @param pit	The Pitch value.
	 * @param ro	The Roll value.
	 */
	public OrientationReading(Double ts, Double az, Double pit, Double ro) {
		super(ts);
		
		// Set the apropriate values
		azimuth = az;
		pitch = pit;
		roll = ro;
		
		// Compute the averageable reading
		providedAverageableReading = computeAverageableReading(getReading());
	}
	
	/**
	 * Creates a {@link OrientationReading} object with the
	 * specified  timestamp value, azimuth, pitch
	 * and roll values inside of the reading array.
	 * 
	 * @param ts		The Timestamp value.
	 * @param reading	The Reading to clone.
	 */
	public OrientationReading(Double ts, double[] reading) {
		this(ts.toString(), reading);
	}
	
	/**
	 * Creates a {@link OrientationReading} object with the
	 * specified  timestamp value, azimuth, pitch
	 * and roll values inside of the reading array.
	 * 
	 * @param ts		The Timestamp value.
	 * @param reading	The Reading to clone.
	 */
	public OrientationReading(String ts, double[] reading) {
		super(ts);
		
		if(reading.length == 3) { // The reading has the APR values
			azimuth = reading[0];
			pitch = reading[1];
			roll = reading[2];
			providedAverageableReading = computeAverageableReading(reading);
		} else if (reading.length == 6) { // The reading has the APR cartesian values
			azimuth = MathUtils.atan2PositiveRange(reading[1], reading[0]);
			pitch = MathUtils.atan2PositiveRange(reading[3], reading[2]);
			roll = MathUtils.atan2PositiveRange(reading[5], reading[4]);
			providedAverageableReading = reading.clone();
		} else {
			azimuth = 0D;
			pitch = 0D;
			roll = 0D;
			throw new UnsupportedReadingSizeException("The supported sizes are 3 and 6. Was: " + reading.length);
		}
	}

	/**
	 * Creates a {@link OrientationReading} object with the
	 * specified  timestamp value, azimuth, pitch
	 * and roll values.
	 * 
	 * @param ts	The Timestamp value.
	 * @param az	The Azimuth value.
	 * @param pit	The Pitch value.
	 * @param ro	The Roll value.
	 */
	public OrientationReading(Double ts, float[] reading) {
		this(ts.toString(), reading);
	}
	
	/**
	 * Creates a {@link OrientationReading} object with the
	 * specified  timestamp value, azimuth, pitch
	 * and roll values.
	 * 
	 * @param ts	The Timestamp value.
	 * @param az	The Azimuth value.
	 * @param pit	The Pitch value.
	 * @param ro	The Roll value.
	 */
	public OrientationReading(String ts, float[] reading) {
		super(ts);
		
		if(reading.length == 3) { // The reading has the APR values
			azimuth = (double) reading[0];
			pitch = (double) reading[1];
			roll = (double) reading[2];
			providedAverageableReading = computeAverageableReading(reading);
		} else if (reading.length == 6) { // The reading has the APR cartesian values
			azimuth = MathUtils.atan2PositiveRange(reading[1], reading[0]);
			pitch = MathUtils.atan2PositiveRange(reading[3], reading[2]);
			roll = MathUtils.atan2PositiveRange(reading[5], reading[4]);
			providedAverageableReading = new double[]{
				reading[0],reading[1],
				reading[2],reading[3],
				reading[4],reading[5]};
		} else {
			azimuth = 0D;
			pitch = 0D;
			roll = 0D;
			throw new UnsupportedReadingSizeException("The supported sizes are 3 and 6. Was: " + reading.length);
		}
	}
	
	/**
	 * Clone the given {@link OrientationReading} object
	 * into a new instance.
	 * 
	 * @param read	The reading to clone from.
	 */
	public OrientationReading(OrientationReading read) {
		this(read.getTimestampString(), read.getReading());
		
		providedAverageableReading = read.getAverageableReading();
	}
	
	/**
	 * Creates a 'zero' reading (non-neuter).
	 */
	public OrientationReading(){
		this(0D, 0D, 0D, 0D);
		
		providedAverageableReading = new double[]{
				0,0,
				0,0,
				0,0};
	}

	/**
	 * Create a 'zero' reading, specifing if it
	 * should be neuter to average operations.
	 * 
	 * @param neuter	If this instance will be neuter.
	 */
	public OrientationReading(boolean neuter) {
		this();
	}

	/**
	 * @return the azimuth
	 */
	public final Double getAzimuth() {
		return azimuth;
	}

	/**
	 * @return the pitch
	 */
	public final Double getPitch() {
		return pitch;
	}

	/**
	 * @return the roll
	 */
	public final Double getRoll() {
		return roll;
	}

	@Override
	public double[] getReading() {
		return new double[] {azimuth, pitch, roll};
	}

	/**
	 * Returns a average friendly reading, which for
	 * circular quantities demands some care. In the
	 * case of angles, they should be converted to a
	 * cartesian space and average those. Every odd
	 * value in the array corresponds to the X value
	 * and every even value to a Y.
	 * 
	 * (Ref: http://en.wikipedia.org/wiki/Mean_of_circular_quantities)
	 */
	@Override
	public double[] getAverageableReading() {
		
		return providedAverageableReading;
	}

	/**
	 * @param reading
	 * @return
	 */
	private double[] computeAverageableReading(double[] reading) {
		return new double[]{
			//		X			and			Y
			Math.cos(reading[0]),Math.sin(reading[0]),
			Math.cos(reading[1]),Math.sin(reading[1]), 
			Math.cos(reading[2]), Math.sin(reading[2]) 
		};
	}
	
	/**
	 * @param reading
	 * @return
	 */
	private double[] computeAverageableReading(float[] reading) {
		return new double[]{
			//		X			and			Y
			Math.cos(reading[0]),Math.sin(reading[0]),
			Math.cos(reading[1]),Math.sin(reading[1]), 
			Math.cos(reading[2]), Math.sin(reading[2]) 
		};
	}
	
	public static OrientationReading getNeuterReading(){
		return new OrientationReading(true);
	}
}
