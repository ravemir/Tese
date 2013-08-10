package pt.utl.ist.thesis.sensor.source;

import java.util.List;

import pt.utl.ist.thesis.signalprocessor.Analyser;
import pt.utl.ist.thesis.source.filters.Filter;
import pt.utl.ist.thesis.source.filters.MovingAverageFilter;
import pt.utl.ist.thesis.source.filters.UnboundedOrientationFilter;
import pt.utl.ist.thesis.util.buffers.ReadingCircularBuffer;

public class RawReadingSource extends ReadingSource {
	
	// The FilterAnalyser attached, if it is present
	private Analyser analyser;
	
	/**
	 * Creates a ReadingSource object, ready to
	 * receive readings and store them.
	 * 
	 * @param bufferSize The size for the internal buffer
	 */
	public RawReadingSource(int bufferSize){
		super(new ReadingCircularBuffer(bufferSize));
	}
	
	public RawReadingSource() {
		super();
	}
	
	/**
	 * Returns a list with all the attached filters.
	 * 
	 * @return The list of all the attached filters.
	 */
	public List<Filter> getFilters() {
		return filters;
	}
	
	public Analyser getAnalyser() {
		return analyser;
	}
	
	/**
	 * Creates and attaches a {@link MovingAverageFilter}Filter to
	 * this {@link ReadingSource}Source object.
	 * 
	 * @param numberOfSamples	The number of samples to 
	 * 							compute the average from
	 */
	public void addMovingAverageFilter(final int numberOfSamples) {
		// Add a filter
		plugFilterIntoOutput(new MovingAverageFilter(numberOfSamples));
	}
	
	/**
	 * Creates and attaches a {@link UnboundedOrientationFilter}
	 * this {@link ReadingSource} object.
	 * 
	 * @param numberOfSamples	The number of samples to 
	 * 							compute the average from
	 */
	public void addUnboundedOrientationFilter(final int numberOfSamples) {
		// Add a filter
		plugFilterIntoOutput(new UnboundedOrientationFilter(numberOfSamples));
	}
}
