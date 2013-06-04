package pt.utl.ist.util.sensor.source;

import java.util.Observable;

import pt.utl.ist.thesis.util.buffers.ReadingCircularBuffer;
import pt.utl.ist.util.sensor.reading.AccelReading;

public abstract class ReadingSource extends Observable {
	// The buffer of pushed values
	protected ReadingCircularBuffer buffer;
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
	public void pushReading(AccelReading read){
		buffer.addReading(read);
		
		// Notify the filters (observer pattern)
		notifyFilters(read);
	}

	/**
	 * @param read
	 */
	protected void notifyFilters(AccelReading read) {
		setChanged();
		notifyObservers(read);
	}
}
