package pt.utl.ist.util.sensor.reading;

public class DummyReading extends SensorReading {

	public DummyReading() {
		super("-1");
	}

	@Override
	public double[] getReading() {
		return new double[]{0,0,0,0,0};
	}
}
