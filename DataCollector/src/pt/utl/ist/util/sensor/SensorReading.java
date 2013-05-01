package pt.utl.ist.util.sensor;

public abstract class SensorReading {
	private final Double timestamp;
	
	public SensorReading(Double ts){
		timestamp = ts;
	}
	
	public SensorReading(SensorReading sr){
		this(sr.getTimestamp());
	}

	public Double getTimestamp() {
		return timestamp;
	}
	
	public abstract Object getReading();
}
