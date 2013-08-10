package pt.utl.ist.thesis.util;

import pt.utl.ist.thesis.sensor.reading.SensorReading;

public class PushThread extends Thread {
	public SensorReading reading;
	public PushThread(SensorReading read){
		reading = read;
	}
}