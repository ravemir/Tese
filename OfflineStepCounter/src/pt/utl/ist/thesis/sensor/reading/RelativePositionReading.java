package pt.utl.ist.thesis.sensor.reading;

public class RelativePositionReading extends SensorReading {

	private final double xCoord;
	private final double yCoord;
	
	/**
	 * Creates a {@link RelativePositionReading} object with the
	 * given formatted timestamp string, and x 
	 * and y coordinates.
	 * 
	 * @param ts	The formatted timestamp string.
	 * @param x		The X coordinate.
	 * @param y		The Y coordinate.
	 */
	public RelativePositionReading(Double ts, double x, double y){
		this(ts.toString(), x, y);
	}
	
	/**
	 * Creates a {@link RelativePositionReading} object with the
	 * given timestamp value, and x and y coordinates.
	 * 
	 * @param ts	The double timestamp value.
	 * @param x		The X coordinate.
	 * @param y		The Y coordinate.
	 */
	public RelativePositionReading(String ts, double x, double y){
		super(ts);
		
		xCoord = x;
		yCoord = y;
	}

	/**
	 * @return the xCoord
	 */
	public final double getXCoord() {
		return xCoord;
	}

	/**
	 * @return the yCoord
	 */
	public final double getYCoord() {
		return yCoord;
	}

	@Override
	public double[] getReading() {
		return new double[]{xCoord, yCoord};
	}
	
	public String toString(){
		return getTimestampString() + ", " + xCoord + ", " + yCoord;
	}
}
