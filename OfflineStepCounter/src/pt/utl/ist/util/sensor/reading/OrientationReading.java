package pt.utl.ist.util.sensor.reading;

public class OrientationReading extends SensorReading {

	private final Double azimuth;
	private final Double pitch;
	private final Double roll; 
	
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
		
		azimuth = az;
		pitch = pit;
		roll = ro;
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
		
		azimuth = reading[0];
		pitch = reading[1];
		roll = reading[2];
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
		
		azimuth = (double) reading[0];
		pitch = (double) reading[1];
		roll = (double) reading[2];
	}
	
	/**
	 * Clone the given {@link OrientationReading} object
	 * into a new instance.
	 * 
	 * @param read	The reading to clone from.
	 */
	public OrientationReading(OrientationReading read) {
		this(read.getTimestampString(), read.getReading());
	}
	
	public OrientationReading(){
		this(0D, 0D, 0D, 0D);
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
}
