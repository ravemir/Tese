package pt.utl.ist.util.copy;

import pt.utl.ist.util.sensor.reading.SensorReading;

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
