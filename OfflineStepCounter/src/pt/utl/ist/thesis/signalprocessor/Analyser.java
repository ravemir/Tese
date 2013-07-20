package pt.utl.ist.thesis.signalprocessor;

import java.util.Observable;
import java.util.Observer;

import pt.utl.ist.util.sensor.reading.SensorReading;
import pt.utl.ist.util.sensor.source.RawReadingSource;
import pt.utl.ist.util.sensor.source.ReadingSource;

public abstract class Analyser implements Observer {

	private ReadingSource rs;
	
	public final ReadingSource getReadingSource() {
		return rs;
	}

	public Analyser() {
		super();
		rs = new RawReadingSource();
	}
	
	public Analyser(ReadingSource readingSource){
		super();
		rs = readingSource;
	}

	@Override
	public void update(Observable readingSource, Object reading){
		// Process Reading on the implemented Analyser class
		update((ReadingSource) readingSource, (SensorReading) reading);
	}
	
	public abstract void update(ReadingSource rs, SensorReading reading);
	
	protected void pushReading(SensorReading reading){
		rs.pushReading(reading);
	}
	
	/**
	 * Attaches an {@link Analyser} object into this {@link Analyser}'s
	 * {@link ReadingSource} object, so it can push
	 * updates through it.
	 * 
	 * @param a	The {@link Analyser} to attach.
	 */
	public void attachToAnalyser(Analyser a){
		rs.plugAnalyserIntoInput(a);
	}
}