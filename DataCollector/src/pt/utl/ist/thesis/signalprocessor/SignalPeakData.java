package pt.utl.ist.thesis.signalprocessor;

import java.util.ArrayList;
import java.util.Collection;

import pt.utl.ist.util.sensor.AccelReading;

public class SignalPeakData {
	
	private ArrayList<AccelReading> allPeaks;
	private ArrayList<AccelReading> unaveragedPeaks;
	private double[] currentMean;
	private double currentNormMean = 0;
	private AccelReading oldestUnaveragedPeak;
	
												
	static final double PEAKTHRESHFACTOR = 0.7;	//	Value before which a peak is always discarded.
												//	Gravity magnitude is a good pick.
	
	
	static final double KFACTOR = 10.5;			// Multiplication factor to lower the step threshold
												// Depends on the variance of intensity of each step
												// i.e. if a step is a lot smaller than the previous,
												// this value should be lower to accommodate it.
												// TODO Chosen heuristically. Should be computed?

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
		}
	}

	/**
	 * @param r
	 * @return
	 */
	private void recomputeMeanValues(AccelReading r) {
		// Get acceleration components and norm values
		double[] accelR = r.getAcceleration();
		double accelRNorm = r.getAccelerationNorm();
		
		// Recompute acceleration values
		for (int i = 0; i < currentMean.length; i++) {
			currentMean[i] = ((currentNormMean * unaveragedPeaks.size()) 
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
	
	public AccelReading getOldestUnaveragedPeak() {
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
}