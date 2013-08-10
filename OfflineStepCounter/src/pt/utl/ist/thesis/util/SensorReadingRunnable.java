package pt.utl.ist.thesis.util;

import pt.utl.ist.thesis.sensor.reading.SensorReading;

public abstract class SensorReadingRunnable implements Runnable {

	public SensorReading reading;

	/**
	 * Sets this {@link Runnable}'s {@link SensorReading} value
	 * and runs it.
	 * 
	 * @param r	The value to set on the reading.
	 */
	public void run(SensorReading r){
		synchronized (this) {
			reading = r;
			run();
		}
	}
}
