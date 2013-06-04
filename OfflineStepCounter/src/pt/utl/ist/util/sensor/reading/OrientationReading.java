package pt.utl.ist.util.sensor.reading;

public class OrientationReading extends SensorReading {

	private final Double azimuth;
	private final Double pitch;
	private final Double roll;
	
	public OrientationReading(Double ts, Double az, Double pit, Double ro) {
		super(ts);
		azimuth = az;
		pitch = pit;
		roll = ro;
	}
	
	@Override
	public Double[] getReading() {
		return new Double[] {azimuth, pitch, roll};
	}
}
