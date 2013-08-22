package pt.utl.ist.thesis.sensor.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import pt.utl.ist.thesis.sensor.reading.SensorReading;
import pt.utl.ist.thesis.signalprocessor.Analyser;
import pt.utl.ist.thesis.source.filters.Filter;
import pt.utl.ist.thesis.util.SensorReadingRunnable;
import pt.utl.ist.thesis.util.buffers.ReadingCircularBuffer;

public abstract class ReadingSource extends Observable {
	// The buffer of pushed values
	protected ReadingCircularBuffer buffer;
	protected final Object bufferLock = new Object();
	protected List<Filter> filters = new ArrayList<Filter>();
	
	
	// A user-definable Runnable
	protected SensorReadingRunnable runnable = null;

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
		SensorReading pushReading = read;
		if(buffer != null) {
			buffer.addReading(read);
			
			// ...and set the buffered value to be pushed
			pushReading = buffer.getCurrentReading();
		}

		// FIXME Remove after testing the runnable issue
		executeRunnable(pushReading);
		
		// Notify the filters (observer pattern)
		notifyFilters(pushReading);
	}

	/**
	 * @param read
	 */
	protected void notifyFilters(SensorReading read) {
		setChanged();
		notifyObservers(read);
	}

	/**
	 * Plugs the input end of the given {@link Analyser}
	 * to this {@link ReadingSource}'s output.
	 * 
	 * @param a	The {@link Analyser} to be plugged.
	 */
	public void plugAnalyserIntoOutput(Analyser a) {
		// Adds new FilterAnalyser
		addObserver(a);
	}

	/**
	 * Plugs the input end of the given {@link Filter}
	 * to this {@link ReadingSource}'s output.
	 * 
	 * @param f	The {@link Filter} to be plugged.
	 */
	public void plugFilterIntoOutput(Filter f) {
		addObserver(f);
		filters.add(f);
	}
	
	/**
	 * Set this {@link ReadingSource}'s {@link Runnable}
	 * object, making it possible to be executed.
	 * 
	 * @param sensorReadingRunnable	The {@link Runnable} to be set.
	 */
	public void setRunnable(SensorReadingRunnable sensorReadingRunnable){
		runnable = sensorReadingRunnable;
	}
	
	/**
	 * Executes the saved runnable, if it exists.
	 */
	public void executeRunnable(SensorReading sr){
		if(runnable != null) runnable.run(sr);
	}
}
