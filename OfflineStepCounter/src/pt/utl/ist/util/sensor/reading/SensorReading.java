package pt.utl.ist.util.sensor.reading;

public abstract class SensorReading {
	// The properly formatted timestamp double 
	// value in millisecond units.
	private final Double timestamp;
	
	public SensorReading(Double ts){
		timestamp = ts;
	}
	
	public SensorReading(SensorReading sr){
		this(sr.getTimestamp());
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
}
