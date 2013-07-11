package pt.utl.ist.util.sensor.source;

import java.util.Observable;

import pt.utl.ist.thesis.signalprocessor.Analyser;
import pt.utl.ist.thesis.util.buffers.ReadingCircularBuffer;
import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.SensorReading;

public abstract class ReadingSource extends Observable {
	// The buffer of pushed values
	protected ReadingCircularBuffer buffer; // FIXME This circular buffer only supports AccelReadings
	protected final Object bufferLock = new Object();

	public ReadingSource(ReadingCircularBuffer rcb){
		buffer = rcb;
	}
	
	protected ReadingSource(){
		this(null);
	}
	
	public ReadingCircularBuffer getBuffer() {
		return buffer;
	}
	
	/**
	 * Pushes a reading into the internal buffers, also
	 * notifying observing entities (such as filters).
	 * 
	 * @param read The reading to be added.
	 */
	public void pushReading(SensorReading read){
		// If a buffer was created, add the reading to it
		if(buffer != null) buffer.addReading((AccelReading) read); // TODO Push a reading as a SensorReading
		
		// Notify the filters (observer pattern)
		notifyFilters(read);
	}

	/**
	 * @param read
	 */
	protected void notifyFilters(SensorReading read) {
		setChanged();
		notifyObservers(read);
	}

	/**
	 * Attaches a new {@link Analyser} object to this filter.
	 * (part of the Observer pattern)
	 */
	public void attachAnalyser(Analyser fa) {
		// Adds new FilterAnalyser
		addObserver(fa);
	}
	
}
