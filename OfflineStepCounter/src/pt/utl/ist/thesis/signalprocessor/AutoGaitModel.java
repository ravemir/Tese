package pt.utl.ist.thesis.signalprocessor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.regression.SimpleRegression;

import pt.utl.ist.thesis.util.buffers.GPSSegment;

public class AutoGaitModel {

	private SimpleRegression reg = new SimpleRegression();
	private List<AutoGaitSample> sampleList = new ArrayList<AutoGaitSample>();
	
	public AutoGaitModel(double[][] samples){
		// Add samples to the regression model
		reg.addData(samples);
		
		// Add values to the sample list
		for(double[] line : samples)
			addSampleToModel(line[0], line[1]);
	}
	
	public final List<AutoGaitSample> getSampleList() {
		return sampleList;
	}

	public AutoGaitModel(GPSSegment[] segments){
		
		for(GPSSegment seg : segments){
			// Compute each segment's average step length and frequency
			seg.computeStepFrequencyAndLength();
			Double averageStepFrequency = seg.getAverageStepFrequency();
			Double averageStepLength = seg.getAverageStepLength();
			
			addSampleToModel(averageStepFrequency, averageStepLength);
		}
	}

	/**
	 * Adds a sample to the model with the given frequency
	 * and length values.
	 * 
	 * @param averageStepFrequency
	 * @param averageStepLength
	 */
	public void addSampleToModel(Double averageStepFrequency,
			Double averageStepLength) {
		// ...insert it into the regression model
		reg.addData(averageStepFrequency,
				averageStepLength);
		
		// ...and sample list
		sampleList.add(new AutoGaitSample(averageStepFrequency, 
				averageStepLength));
	}
	
	public void addSampleToModel(double[] sample){
		addSampleToModel(sample[0], sample[1]);
	}
	
	public AutoGaitModel() {}

	public Double getAlpha(){
		return reg.getSlope();
	}
	
	public Double getBeta(){
		return reg.getIntercept();
	}
	
	/**
	 * Returns the expected step length value from the
	 * given step frequency.
	 * 
	 * @param frequency	The frequency to return the length from.
	 * @return			The corresponding step length value.
	 */
	public Double getLengthFromFrequency(Double frequency){
		return getAlpha()*frequency + getBeta();
	}
	
	/**
	 * Returns the expected step frequency value from the
	 * given step length.
	 * 
	 * @param length	The length to return the frequency from.
	 * @return			The corresponding step frequency value.
	 */
	public Double getFrequencyFromLength(Double length){
		return (length - getBeta()) / getAlpha();
	}
}
