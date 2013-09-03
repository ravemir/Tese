package pt.utl.ist.thesis.signalprocessor;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math.stat.descriptive.SynchronizedDescriptiveStatistics;

import pt.utl.ist.thesis.sensor.reading.AccelReading;
import pt.utl.ist.thesis.sensor.reading.SensorReading;

public class SignalPeakData {
	
	private ArrayList<AccelReading> allPeaks;
	private ArrayList<AccelReading> unaveragedPeaks;
	private double[] currentMean;
	private double currentNormMean = 0;
	private SensorReading oldestUnaveragedPeak;
	

	private SynchronizedDescriptiveStatistics sdsRatio = new SynchronizedDescriptiveStatistics(10);
	private SynchronizedDescriptiveStatistics sdsElapsedStepValue = new SynchronizedDescriptiveStatistics(15);
	private SynchronizedDescriptiveStatistics sdsPeak = new SynchronizedDescriptiveStatistics(15);
	
	
	public SignalPeakData() {
		allPeaks = new ArrayList<AccelReading>();
		unaveragedPeaks = new ArrayList<AccelReading>();
		currentMean = new double[]{0,0,0};
	}
	
	public ArrayList<AccelReading> getAllPeaks() {
		return allPeaks;
	}

	public ArrayList<AccelReading> getUnaveragedPeaks() {
		return unaveragedPeaks;
	}
	
	public void addAll(Collection<AccelReading> c){
		for (AccelReading r : c){
			// Recalculate mean values
			recomputeMeanValues(r); 
			
			// If first unaveraged peak, ...
			if(unaveragedPeaks.size() == 0)
				// Register the peak
				oldestUnaveragedPeak = r;
			
			// Add peak to lists
			unaveragedPeaks.add(r);
			allPeaks.add(r);
			
			// FIXME Add peak to average
			sdsPeak.addValue(r.getReadingNorm());
		}
	}

	/**
	 * @param r
	 * @return
	 */
	private void recomputeMeanValues(AccelReading r) {
		// Get acceleration components and norm values
		double[] accelR = r.getAcceleration();
		double accelRNorm = r.getReadingNorm();
		
		// Recompute acceleration values
		for (int i = 0; i < currentMean.length; i++) {
			currentMean[i] = ((currentMean[i] * unaveragedPeaks.size()) 
					+ accelR[i])/(unaveragedPeaks.size()+1);
		}
		
		// Recompute norm values
		currentNormMean = ((currentNormMean * unaveragedPeaks.size()) 
				+ accelRNorm)/(unaveragedPeaks.size()+1);
	}
	
	public double[] getCurrentMean(){
		return currentMean;
	}
	
	public double getCurrentNormMean() {
		return currentNormMean;
	}
	
	public SensorReading getOldestUnaveragedPeak() {
		return oldestUnaveragedPeak;
	}
	
	/**
	 * Clears the mean state values, as well as
	 * the readings associated with them.
	 */
	public void clearMeanData(){
		unaveragedPeaks.clear();
		currentMean = new double[]{0,0,0};
		currentNormMean = 0;
		oldestUnaveragedPeak = null;
	}
	
	/**
	 * Adds a peak/step ratio value to the rolling average
	 * calculations.
	 * 
	 * @param v	The value to be added.
	 */
	public void addRatioValue(double v) {
		sdsRatio.addValue(v);
	}
	
	/**
	 * Returns the current rolling average value of
	 * the added peak/step ratio values.
	 * 
	 * @return	The current rolling average value of
	 * 			the added values.
	 */
	public double getRatioAverage(){
		double currMeanValue = sdsRatio.getMean();
		
		return (Double.isNaN(currMeanValue) ? 
				0 : currMeanValue);
	}
	
	/**
	 * Adds a step norm value to the rolling average
	 * calculations.
	 * 
	 * @param v	The value to be added.
	 */
	public void addElapsedStepTimeValue(double v) {
		sdsElapsedStepValue.addValue(v);
	}
	
	/**
	 * Returns the current rolling average value of
	 * the added step norm values.
	 * 
	 * @return	The current rolling average value of
	 * 			the added values.
	 */
	public double getElapsedStepTimeAverage(){
		double currMeanValue = sdsElapsedStepValue.getMean();
		
		return (Double.isNaN(currMeanValue) ? 
				0 : currMeanValue);
	}
	
	public double getPeakMean(){
		double currMeanValue = sdsPeak.getMean();
		return ((Double.isNaN(currMeanValue)) ? 
				0 : currMeanValue);
	}
}