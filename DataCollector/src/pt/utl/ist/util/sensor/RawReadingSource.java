package pt.utl.ist.util.sensor;

import java.util.ArrayList;
import java.util.List;

import pt.utl.ist.thesis.signalprocessor.PeakAnalyser;
import pt.utl.ist.util.filters.Filter;
import pt.utl.ist.util.filters.MovingAverageFilter;

public class RawReadingSource extends ReadingSource {
	
	// A list of all the Filters attached to this ReadingSource
	private List<Filter> filters = new ArrayList<Filter>();

	// The FilterAnalyser attached, if it is present
	private PeakAnalyser analyser;
	
	/**
	 * Creates a ReadingSource object, ready to
	 * receive readings and store them.
	 * 
	 * @param bufferSize The size for the internal buffer
	 */
	public RawReadingSource(int bufferSize){
		super(new ReadingCircularBuffer(bufferSize));
	}
	
	/**
	 * Attaches a filter to this ReadingSource.
	 * 
	 * @param f The filter to be attached.
	 */
	public void attachFilter(Filter f) {
		addObserver(f);
		filters.add(f);
	}
	
	/**
	 * Returns a list with all the attached filters.
	 * 
	 * @return The list of all the attached filters.
	 */
	public List<Filter> getFilters(){
		return filters;
	}
	
	public void attachAnalyser(PeakAnalyser fa){
		// TODO Checks to see if a previous filter existed (??)
		
		// Adds new FilterAnalyser
		addObserver(fa);
		analyser = fa;
	}
	
	public PeakAnalyser getAnalyser(){
		return analyser;
	}
	
	/**
	 * Creates and attaches a MovingAverageFilter to
	 * this ReadingSource object.
	 * 
	 * @param numberOfSamples	The number of samples to 
	 * 							compute the average from
	 */
	public void addMovingAverageFilter(final int numberOfSamples){
		// Add a filter
		attachFilter(new MovingAverageFilter(numberOfSamples));
	}
}
