package pt.utl.ist.util.filters;

import java.util.Observer;

import pt.utl.ist.thesis.signalprocessor.PeakAnalyser;
import pt.utl.ist.util.sensor.ReadingCircularBuffer;
import pt.utl.ist.util.sensor.ReadingSource;

public abstract class Filter extends ReadingSource implements Observer {

	public Filter(ReadingCircularBuffer rcb){
		super(rcb);
	}
	
	public abstract ReadingCircularBuffer getBuffer();
	
	/**
	 * Attaches a new {@link PeakAnalyser} object to this filter.
	 * (part of the Observer pattern)
	 */
	public void attachAnalyser(PeakAnalyser fa){
		// Adds new FilterAnalyser
		addObserver(fa);
	}
}