package pt.utl.ist.util.source.filters;

import java.util.Observable;

import pt.utl.ist.thesis.util.buffers.AverageCircularBuffer;
import pt.utl.ist.thesis.util.buffers.ReadingCircularBuffer;
import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.OrientationReading;
import pt.utl.ist.util.sensor.reading.SensorReading;

public final class MovingAverageFilter extends Filter {
	// The Moving Average filter order (i.e. the number
	// of samples to average on each update)
	private final int averageOrder;

	/**
	 * Creates a new {@link MovingAverageFilter} object,
	 * with the number of samples intended for each output.
	 * 
	 * @param averageSamples The number of samples to 
	 * 		average on each update.
	 */
	public MovingAverageFilter(int averageSamples) {
		super(new AverageCircularBuffer(averageSamples));
		averageOrder = averageSamples;
	}

	/**
	 * Invoked during an update to an attached ReadingSource
	 * object. (Part of the Observer pattern)
	 */
	@Override
	public void update(Observable readingSource, Object reading) {
		if(reading instanceof AccelReading || 
				reading instanceof OrientationReading) {
			// Cast the received reading to the appropriate type and push it
			pushReading((SensorReading) reading);
		} else {
			throw new UnsupportedOperationException(getClass().getName() + " does not " +
					"support the reading type '" +reading.getClass().getName() + "'");
		}
	}

	@Override
	public ReadingCircularBuffer getBuffer() {
		return buffer;			// FIXME Is this overriding method still used?
	}
	
	/**
	 * Returns the value of this {@link MovingAverageFilter}'s order.
	 * 
	 * @return The number of samples to average on each update.
	 */
	public int getAverageOrder() {
		return averageOrder;
	}
	
	/**
	 * Attaches a new {@link StepAnalyser} object to this filter.
	 * (part of the Observer pattern)
	 */
	
//	public void attachAnalyser(StepAnalyser fa){
//		// Adds new FilterAnalyser
//		addObserver(fa);
//	}
}