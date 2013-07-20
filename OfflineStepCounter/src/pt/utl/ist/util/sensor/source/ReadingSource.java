package pt.utl.ist.util.sensor.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import pt.utl.ist.thesis.signalprocessor.Analyser;
import pt.utl.ist.thesis.util.buffers.ReadingCircularBuffer;
import pt.utl.ist.util.sensor.reading.SensorReading;
import pt.utl.ist.util.source.filters.Filter;

public abstract class ReadingSource extends Observable {
	// The buffer of pushed values
	protected ReadingCircularBuffer buffer;
	protected final Object bufferLock = new Object();
	protected List<Filter> filters = new ArrayList<Filter>();

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
		if(buffer != null) buffer.addReading(read);
		
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
	 * Plugs the output end of the given {@link Analyser}
	 * to this {@link ReadingSource}'s input.
	 * 
	 * @param a	The {@link Analyser} to be plugged.
	 */
	public void plugAnalyserIntoInput(Analyser a) {
		// Adds new FilterAnalyser
		addObserver(a);
	}

	/**
	 * Plugs the output end of the given {@link Filter}
	 * to this {@link ReadingSource}'s input.
	 * 
	 * @param f	The {@link Filter} to be plugged.
	 */
	public void plugFilterIntoInput(Filter f) {
		addObserver(f);
		filters.add(f);
	}
}
