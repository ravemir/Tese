package pt.utl.ist.util.copy;

import pt.utl.ist.util.sensor.reading.SensorReading;

public class PushThread extends Thread {
	public SensorReading reading;
	public PushThread(SensorReading read){
		reading = read;
	}
}