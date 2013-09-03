package pt.utl.ist.thesis.util.buffers;

import java.util.ArrayList;
import java.util.List;

import pt.utl.ist.thesis.sensor.reading.AccelReading;
import pt.utl.ist.thesis.sensor.reading.GPSReading;
import pt.utl.ist.thesis.sensor.reading.OrientationReading;
import pt.utl.ist.thesis.sensor.reading.SensorReading;
import pt.utl.ist.thesis.source.filters.Filter;
import pt.utl.ist.thesis.util.MathUtils;

public class ReadingCircularBuffer {
	
	// WORKAROUND
	private String readType;
	
	private int positionIndex = -1;
	private SensorReading[] readings;

	List<Filter> observers = new ArrayList<Filter>();
	
	public ReadingCircularBuffer(int size) {
		readings = new SensorReading[size];
	}
	
	public SensorReading[] getBufferValues() {
			return readings;
	}
	
	/**
	 * Adds a reading to this buffer, accounting
	 * for the circular properties of the buffer.
	 * 
	 * @param read The reading to be inserted
	 */
	public void addReading(SensorReading read) {
		checkType(read);
		
		// Check the received the reading's type, and clone it
		SensorReading add;
	    if(read instanceof AccelReading)
	    	add = new AccelReading((SensorReading) read);
	    else if(read instanceof OrientationReading)
	    	add = new OrientationReading((OrientationReading) read);
	    else {
	    	throw new UnsupportedOperationException("Tried to add an supported reading of type '" 
	    			+ read.getClass().getSimpleName() + "'");
	    }
		
		// Update the buffer index
	    positionIndex = ((positionIndex+1) % readings.length);
		
	    // Add the new reading to the buffer
    	readings[positionIndex] = add;
	}

	/**
	 * @param read
	 */
	public void checkType(SensorReading read) {
		if(readType == null) readType = read.getClass().getSimpleName();
	}
	
	/**
	 * Returns the current reading, which was the last
	 * one to be added before this call.
	 * 
	 * @return The last reading to be added.
	 */
	public SensorReading getCurrentReading() {
		SensorReading current = getPrevNReading(0);
		
		return (current!=null? current: getEmptyReading());
	}
	
	/**
	 * Returns the oldest value inside the buffer. If the buffer
	 * never filled, it returns a null-like reading.
	 * 
	 * @return The oldest value in the buffer.
	 */
	public SensorReading getOldestReading() {
		SensorReading lastValue = getPrevNReading(readings.length-1);
		
		return (lastValue!=null? lastValue: getEmptyReading());
	}

	/**
	 * @return
	 */
	public SensorReading getEmptyReading() {
		if(readType == null)					// FIXME Dirty workaround. Should pass the reading type and 
			return new AccelReading();			// not care if there was a previous reading
		else if (readType.equals("OrientationReading"))
			return OrientationReading.getNeuterReading();
		else if (readType.equals("GPSReading"))
			return new GPSReading();
		else
			return new AccelReading();
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
			SensorReading[] tmp = new AccelReading[readings.length];
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
	public SensorReading getPreviousReading() {
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
	public SensorReading getPrevNReading(int n) {
		SensorReading read = readings[MathUtils.altMod(positionIndex-n,readings.length)];
		return (read != null? read: getEmptyReading());
	}
	
	/**
	 * Fills this buffer with zeroed values.
	 */
	public void zero() {
		for (int i = 0; i < readings.length; i++)
			addReading(getEmptyReading());
	}
	
	/**
	 * TODO
	 * @param i
	 * @return
	 */
	public SensorReading getFromIndex(int i){
		// Get the number of warmed readings, and use that to determine the beginning
		int beginning = MathUtils.altMod(positionIndex - ((readings.length - samplesToWarm()) - 1), readings.length);
		int index = beginning + i;
		SensorReading read = readings[MathUtils.altMod(index, readings.length)];
		
		return (read != null? read: getEmptyReading());
	}
}
