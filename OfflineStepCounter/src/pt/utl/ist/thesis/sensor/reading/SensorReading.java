package pt.utl.ist.thesis.sensor.reading;


public abstract class SensorReading {
	// The properly formatted timestamp double 
	// value in millisecond units.
	private final Double timestamp;
	protected final String formattedTs;
	protected ExtremityType extType = ExtremityType.NONE;
	
	public SensorReading(Double ts){
		timestamp = ts;
		formattedTs = ts.toString();
	}

	public SensorReading(String ts){
		timestamp = Double.valueOf(ts);
		formattedTs = ts;
	}

	public double[] getAverageableReading(){
		return getReading();
	}

	/**
	 * @return the extType
	 */
	public final ExtremityType getExtremityType() {
		return extType;
	}
	
	public abstract double[] getReading();
	
	public double[] getReadingAndNorm(){
		double[] reading = getReading();
		double[] tmp = new double[reading.length+1];
		for (int i = 0; i < reading.length; i++) {
			tmp[i] = reading[i];
		}
		tmp[tmp.length] = 0;
		
		return tmp;
	}
	
	/**
	 * Returns the timestamp of this value in 
	 * millisecond units.
	 * 
	 * @return	The timestamp double value of
	 * 			this reading.
	 */
	public Double getTimestamp() {
		return timestamp;
	}
	/**
	 * @return The formatted timestamp string.
	 */
	public final String getTimestampString() {
		return formattedTs;
	}
	
	/**
	 * @param extType the extType to set
	 */
	public final void setExtremityType(ExtremityType extType) {
		this.extType = extType;
	}
}
