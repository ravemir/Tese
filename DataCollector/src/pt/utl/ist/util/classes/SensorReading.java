package pt.utl.ist.util.classes;

public abstract class SensorReading {
	private final Double timestamp;
	
	public SensorReading(Double ts){
		timestamp = ts;
	}

	public Double getTimestamp() {
		return timestamp;
	}
	
	public abstract Object getReading();
}
