package pt.utl.ist.thesis.sensor.reading;

import pt.utl.ist.thesis.util.MathUtils;

public class GPSReading extends SensorReading {

	private static double timestamp = 1349448947000D;
	private final Double latitude;
	private final Double longitude;
	private final Double bearing;
	private Double speed;
	
	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	/**
	 * Builds a {@link GPSReading} object, using the latitude, 
	 * longitude, bearing change (ranged ]0;360] degrees) and
	 * speed.
	 * 
	 * @param ts		The timestamp of this reading.
	 * @param lat		The latitude value of this reading.
	 * @param lon		The longitude value of this reading.
	 * @param bearValue	The bearing value of this reading (ranged
	 * 					]0;360] degrees) related to North.
	 * @param spd		The speed value of this reading (in m/s).
	 */
	public GPSReading(Double ts, Double lat, Double lon, Double bearValue, Double spd) {
		this(ts.toString(), lat, lon, bearValue, spd);
	}

	/**
	 * Builds a {@link GPSReading} object, using the latitude, 
	 * longitude, bearing change (ranged ]0;360] degrees) and
	 * speed.
	 * 
	 * @param ts		The timestamp of this reading.
	 * @param lat		The latitude value of this reading.
	 * @param lon		The longitude value of this reading.
	 * @param bearValue	The bearing value of this reading (ranged
	 * 					]0;360] degrees) related to North.
	 * @param spd		The speed value of this reading (in m/s).
	 */
	public GPSReading(String ts, Double lat, Double lon, Double bearValue, Double spd) {		
		super(ts);
		latitude = lat;
		longitude = lon;
		bearing = bearValue;
		speed = spd;
	}
	
	public GPSReading(Double lat, Double lon, Double head, Double speed){
		this(timestamp, lat, lon, head, speed);
		timestamp += 1000;
	}
	
	public GPSReading(Double lat, Double lon){
		// NOTE:	180º bearing matches 0º in the 180º range,
		//			meaning no course change.
		this(lat,lon, 180D, 0D);
	}
	
	public GPSReading(){
		this(0D, 0D, 0D, 0D, 0D);
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public Double getBearing() {
		return bearing;
	}
	
	public Double getSpeed() {
		return speed;
	}

	@Override
	public double[] getReading() {
		return new double[]{latitude, longitude, bearing, speed};
	}
	
	public double[] getLatLongReading(){
		return new double[]{latitude, longitude};
	}
	
	public String toString(){
		return getTimestamp() + ", " + 
				getLatitude() + ", " +
				getLongitude() + ", " +
				getBearing();
	}
	
	/**
	 * Compares this {@link GPSReading} object with
	 * another, excluding the timestamp values in the
	 * comparison.
	 * 
	 * @param cmp	The object to compare this one to.
	 * @return		The result of the comparison.
	 */
	public boolean equals(GPSReading cmp){	
		return	MathUtils.equalsDouble(getLatitude(),cmp.getLatitude()) &&
				MathUtils.equalsDouble(getLongitude(),cmp.getLongitude()) &&
				MathUtils.equalsDouble(getBearing(),cmp.getBearing()) && 
				MathUtils.equalsDouble(getSpeed(),cmp.getSpeed());
	}
}
