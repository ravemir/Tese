package pt.utl.ist.thesis.util.buffers;

import java.util.ArrayList;
import java.util.List;

import pt.utl.ist.thesis.util.MathUtils;
import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.source.filters.Filter;

public class ReadingCircularBuffer {
	
	private int positionIndex = -1;
	private AccelReading[] readings;

	List<Filter> observers = new ArrayList<Filter>();
	
	public ReadingCircularBuffer(int size) {
		readings =  new AccelReading[size];
	}
	
	public AccelReading[] getBufferValues() {
			return readings;
	}
	
	/**
	 * Adds a reading to this buffer, accounting
	 * for the circular properties of the buffer.
	 * 
	 * @param read The reading to be inserted
	 */
	public void addReading(AccelReading read) {
		// Update the buffer index
	    positionIndex = ((positionIndex+1) % readings.length);
		
	    // Add the new reading to the buffer
    	readings[positionIndex] = new AccelReading(read); // FIXME Should not need to clone like this
	}
	
	/**
	 * Returns the current reading, which was the last
	 * one to be added before this call.
	 * 
	 * @return The last reading to be added.
	 */
	public AccelReading getCurrentReading() {
		AccelReading current = getPrevNReading(0); // FIXME colocado aqui porque positionindex começa a -1, e rebenta
		
		return (current!=null? current: new AccelReading());
	}
	
	/**
	 * Returns the oldest value inside the buffer. If the buffer
	 * never filled, it returns a null-like reading.
	 * 
	 * @return The oldest value in the buffer.
	 */
	public AccelReading getOldestReading() {
		AccelReading lastValue = getPrevNReading(readings.length-1);
		
		return (lastValue!=null? lastValue: new AccelReading());
	}

	/**
	 * Indicates if the buffer has already been filled 
	 * once, which means that it is ready to compute
	 * new values.
	 * 
	 * @return State indicating if the buffer is warm.
	 */
	public Boolean isWarm() {
		return readings[readings.length-1] != null;
	}
	
	/**
	 * Returns the number of samples required to warm-up
	 * the filter.
	 * 
	 * @return The number of samples needed to warm.
	 */
	public int samplesToWarm() {
		int ret = (isWarm() ? 0 : (readings.length-1)-positionIndex);
		return ret;
	}
	
	/**
	 * Returns the number of samples inserted into
	 * the buffer as a part of the warmup process.
	 * 
	 * @return The number of samples inside the buffer.
	 */
	public int samplesWarmed(){
		return readings.length-samplesToWarm();
	}
	
	
	/**
	 * Clears the values older than the current and
	 * previous readings, if this buffer contains more
	 * than two readings.
	 */
	public void clearOld() {
		int samplesWarmed = samplesWarmed();
		if(samplesWarmed > 2) {
			AccelReading[] tmp = new AccelReading[readings.length];
			tmp[0] = getPreviousReading();
			tmp[1] = getCurrentReading();
			readings = tmp;
			positionIndex = 1;
		}
	}

	/**
	 * Get the reading that was inserted on the last
	 * operation.
	 * 
	 * @return The last reading to be inserted.
	 */
	public AccelReading getPreviousReading() {
		return getPrevNReading(1);
	}
	
	/**
	 * Get the reading that was inserted 'n'
	 * operations ago.
	 * 
	 * @param n	Number of operations to go back to.
	 * @return	The reading that was inserted 'n'
	 * 		operations back.
	 */
	public AccelReading getPrevNReading(int n) {
		AccelReading read = readings[MathUtils.altMod(positionIndex-n,readings.length)];
		return (read != null? read: new AccelReading());
	}
	
	/**
	 * Fills this buffer with zeroed values.
	 */
	public void zero() {
		for (int i = 0; i < readings.length; i++)
			addReading(new AccelReading());
	}
}
