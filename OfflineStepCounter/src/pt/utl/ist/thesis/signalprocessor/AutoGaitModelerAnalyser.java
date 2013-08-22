package pt.utl.ist.thesis.signalprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import org.apache.commons.math.stat.descriptive.SynchronizedSummaryStatistics;

import pt.utl.ist.thesis.sensor.exception.StepOutsideSegmentBoundariesException;
import pt.utl.ist.thesis.sensor.reading.GPSReading;
import pt.utl.ist.thesis.sensor.reading.SensorReading;
import pt.utl.ist.thesis.sensor.reading.StepReading;
import pt.utl.ist.thesis.sensor.source.ReadingSource;
import pt.utl.ist.thesis.util.SampleRunnable;
import pt.utl.ist.thesis.util.buffers.GPSSegment;

/**
 * Handles all the required processing of the GPS
 * data for the AutoGait system: segmentation,
 * smoothing and Straight Line Identification.
 * 
 * @author Carlos Simões
 */
public class AutoGaitModelerAnalyser extends Analyser implements Observer {

	// AutoGait model related Attributes
	private AutoGaitModel autoGaitModel;
	private Double middleThreshold;
	private Double endThreshold;
	private boolean doSmoothing;

	// Buffers and lists
	private ArrayList<GPSSegment> segments = new ArrayList<GPSSegment>();
	private ArrayList<StepReading> stepBuffer = new ArrayList<StepReading>();

	// State and statistical Attributes
	private GPSSegment currentSegment;
	private SynchronizedSummaryStatistics speedStats = new SynchronizedSummaryStatistics();
	private SampleRunnable sampleRunnable;

	/**
	 * Builds a new {@link AutoGaitModelerAnalyser} object
	 * with the default Middle-point and End-point
	 * threshold values ({@link AutoGaitModelerAnalyser#middleThreshold} 
	 * and {@link AutoGaitModelerAnalyser#endThreshold}).
	 * Smoothing is active.
	 */
	public AutoGaitModelerAnalyser() {
		this(true);
	}
	
	/**
	 * Builds a new {@link AutoGaitModelerAnalyser} object
	 * with the default Middle-point and End-point
	 * threshold values ({@link AutoGaitModelerAnalyser#middleThreshold} 
	 * and {@link AutoGaitModelerAnalyser#endThreshold}),
	 * and with smoothing either active/inactive.
	 * 
	 * @param doSmooth
	 */
	public AutoGaitModelerAnalyser(boolean doSmooth){
		this(35D, 10D, doSmooth);
	}

	/**
	 * Builds a new {@link AutoGaitModelerAnalyser} object
	 * while specifying the Middle-point and End-point
	 * threshold values.
	 * 
	 * @param MT The value of the middle-point threshold.
	 * @param ET The value of the end-point threshold.
	 */
	public AutoGaitModelerAnalyser(Double MT, Double ET){
		this(MT, ET, true);
	}
	
	/**
	 * Builds a new {@link AutoGaitModelerAnalyser} object
	 * while specifying the Middle-point and End-point
	 * threshold values.
	 * 
	 * @param MT		The value of the middle-point threshold.
	 * @param ET		The value of the end-point threshold.
	 * @param doSmooth	Whether to perform {@link GPSSegment} smoothing.
	 */
	public AutoGaitModelerAnalyser(Double MT, Double ET, boolean doSmooth){
		middleThreshold = MT;
		endThreshold = ET;
		autoGaitModel = new AutoGaitModel();
		doSmoothing = doSmooth;
		resetCurrentSegment();
	}

	/**
	 * Forcefully adds a step, overriding the regular
	 * step adding procedure.
	 * 
	 * @param read	The {@link StepReading} to be added.
	 */
	public void forceStepAdd(StepReading read){
		currentSegment.forceAddStepReading(read);
	}

	/**
	 * Returns the current {@link GPSSegment} in this 
	 * {@link AutoGaitModelerAnalyser}.
	 * 
	 * @return	This {@link Analyser}'s current {@link GPSSegment}.
	 */
	public GPSSegment getCurrentSegment(){
		return currentSegment;
	}

	/**
	 * Computes the 'speed limit' value associated with the
	 * unrealistic movement section of the Segmentation phase. 
	 * 
	 * @return The speed limit value for unrealistic movement detection.
	 */
	public double getCurrentSpeedLimit() {
		double ret = speedStats.getMean() + 
				2*speedStats.getStandardDeviation();
		return (ret>0?ret:10);
	}

	/**
	 * Returns this {@link AutoGaitModelerAnalyser}'s End
	 * Point Threshold value.
	 *  
	 * @return	The ET value.
	 */
	public Double getEndThreshold() {
		return endThreshold;
	}

	/**
	 * Returns this {@link AutoGaitModelerAnalyser}'s Middle
	 * Point Threshold value.
	 *  
	 * @return	The MT value.
	 */
	public Double getMiddleThreshold() {
		return middleThreshold;
	}

	/**
	 * Returns all the registered {@link GPSSegment}s to date.
	 * 
	 * @return The {@link GPSSegment}s registered to date.
	 */
	public final ArrayList<GPSSegment> getSegments() {
		return segments;
	}

	/**
	 * Determine if the current segment is a straight-line.
	 * 
	 * @return	Whether the segment currently being filled
	 * 			is a straight line or not.
	 */
	public boolean hasCurrentSegmentSL() {
		return currentSegment.hasSegmentSL(middleThreshold, endThreshold);
	}

	/**
	 * Processes this segment, and adds the 
	 * resulting samples to the AutoGait model.
	 */
	private void processCurrentSegment() {
		// Add currentSegment to array
		segments.add(currentSegment);

		// Add this segment's samples to the model
		List<Double[]> samples = currentSegment.getSegmentStepSamples();
		for(Double[] s : samples) {
			double[] primitiveSample = new double[]{s[0], s[1]};
			autoGaitModel.addSampleToModel(primitiveSample);

			// Run the sampleRunnable
			if(sampleRunnable != null) sampleRunnable.run(primitiveSample);
		}
	}

	/**
	 * Replaces the current segment with a new one.
	 */
	public void resetCurrentSegment() {
		currentSegment = new GPSSegment(doSmoothing);
	}

	/**
	 * Takes a 2D array of data samples and restores the
	 * {@link AutoGaitModel} object with them.
	 * 
	 * @param samples	The samples to reinitialize the 
	 * 					{@link AutoGaitModel} with.
	 */
	public void restoreDataSamples(double[][] samples){
		autoGaitModel = new AutoGaitModel(samples);
	}

	/**
	 * Finishes the current segment.
	 */
	private void segment() {
		// If SLI conditions verify and segment 
		// has more than one step and GPS reading
		boolean hasStraightLine = hasCurrentSegmentSL();
		if(hasStraightLine && currentSegment.size() > 1 
				&& currentSegment.getStepCount() > 1) {
			processCurrentSegment();
		}
		
		// Create new GPSSegment
		resetCurrentSegment();
	}

	/**
	 * Sets the current {@link GPSSegment} in this 
	 * {@link AutoGaitModelerAnalyser}.
	 * 
	 * @param segment The {@link GPSSegment} to set.
	 */
	public void setCurrentSegment(GPSSegment segment) {
		currentSegment = segment;
	}

	/**
	 * Sets the {@link SampleRunnable}, to run whenever
	 * there is a new sample added to the {@link AutoGaitModel}.
	 * 
	 * @param r	The {@link SampleRunnable} to be set (and run).
	 */
	public void setSampleUpdater(SampleRunnable r){
		sampleRunnable = r;
	}

	@Override
	public void update(ReadingSource readingSource, SensorReading reading) {
		synchronized(this){
			if(reading instanceof GPSReading){
				// Perform updates referring to
				// segmentation, smoothing and SLI.

				// Update mean and standard deviation
				GPSReading gpsReading = (GPSReading) reading;
				speedStats.addValue(gpsReading.getSpeed());

				// If segmentation conditions verify...
				Double speedLimit = getCurrentSpeedLimit();
				boolean speedConditions = (gpsReading.getSpeed() > speedLimit);
				boolean stepConditions = stepBuffer.isEmpty() && currentSegment.size() > 1;
				if(stepConditions || speedConditions){
					segment();
				}

				// Add value to currentSegment
				currentSegment.addGPSReading(gpsReading);

				// Add all steps to the currentSegment, saving those that can't be added
				for(StepReading step : stepBuffer){
					try {
						currentSegment.addStepReading(step);
					} catch(StepOutsideSegmentBoundariesException e) {
						// If StepReading should've appeared after this segment, store it
					}
				}

				// Clear the step buffer
				stepBuffer.clear();
				

			} else if (reading instanceof StepReading){
				// Perform updates referring to
				// segmentation, smoothing and SLI.

				// Add StepReading to buffer 
				stepBuffer.add((StepReading) reading);
			} else {
				throw new UnsupportedOperationException("Tried to update Analyser from '" +
						readingSource.getClass().getSimpleName() + "' observable type, with a '" +
						reading.getClass().getSimpleName() +"' reading type." );
			}
		}
	}
	
	public double[] getAGCoefficients(){
		return new double[]{autoGaitModel.getAlpha(),
				autoGaitModel.getBeta()};
	}
}
