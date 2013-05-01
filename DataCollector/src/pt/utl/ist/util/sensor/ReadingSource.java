package pt.utl.ist.util.sensor;

import java.util.Observable;

public abstract class ReadingSource extends Observable {
	// The buffer of pushed values
	protected ReadingCircularBuffer buffer;
	protected final Object bufferLock = new Object();

	public ReadingSource(ReadingCircularBuffer rcb){
		buffer = rcb;
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
		setChanged();
		notifyObservers(read);
	}
}
