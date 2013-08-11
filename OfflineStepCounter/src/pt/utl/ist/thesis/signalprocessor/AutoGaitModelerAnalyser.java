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

	// AutoGait model related attributes
	private AutoGaitModel autoGaitModel;
	private Double middleThreshold;
	private Double endThreshold;

	// Buffers and lists
	private ArrayList<GPSSegment> segments = new ArrayList<GPSSegment>();
	private ArrayList<StepReading> stepBuffer = new ArrayList<StepReading>();

	// State and statistical variables
	private GPSSegment currentSegment;
	private SynchronizedSummaryStatistics speedStats = new SynchronizedSummaryStatistics();
	private SampleRunnable sampleRunnable;

	/**
	 * Builds a new {@link AutoGaitModelerAnalyser} object
	 * with the default Middle-point and End-point
	 * threshold values ({@link AutoGaitModelerAnalyser#middleThreshold} 
	 * and {@link AutoGaitModelerAnalyser#endThreshold}).
	 */
	public AutoGaitModelerAnalyser() {
		this(35D, 10D);
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
		currentSegment = new GPSSegment();
		middleThreshold = MT;
		endThreshold = ET;
		autoGaitModel = new AutoGaitModel();
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
	 * Replaces the current segment with a new one.
	 */
	public void resetCurrentSegment() {
		currentSegment = new GPSSegment();
	}

	/**
	 * Processes this segment, and adds the 
	 * resulting samples to the AutoGait model.
	 */
	private void processCurrentSegment() {
		// Compute step lengths 
		//		currentSegment.computeStepFrequencyAndLength();			// TODO Remove this:

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

	public void forceStepAdd(StepReading read){
		currentSegment.forceAddStepReading(read);
	}

	/**
	 * Determine if the current segment is a straight-line.
	 * 
	 * @return	Whether the segment currently being filled
	 * 			is a straight line or not.
	 */
	public boolean hasCurrentSegmentSL() {
		// Check if SLs have been identified, and if so 
		// return true immediately
		if(!currentSegment.getDetectedStraightLines().isEmpty()) return true;

		// Get Cumulative Heading Change values
		GPSSegment seg = currentSegment;
		ArrayList<Double> cumHC = new ArrayList<Double>(seg.getCumulativeHeadingChanges());

		int latestInET = -1;
		int latestRem = -1;
		double toRemove = 0;
		// TODO For every cumHC value...
		for (int i = 0; i < cumHC.size();) {
			Double currentHC = cumHC.get(i);

			// TODO If it is inside MT (and there is a next cumHC)...
			if(currentHC < middleThreshold && currentHC > -middleThreshold &&
					i+1 < cumHC.size()) {
				Double nextHC = cumHC.get(i+1);

				// TODO ...if so, check if next is inside ET...
				if(nextHC < endThreshold && nextHC > -endThreshold) {
					// TODO ...if so, record the latest value inside ET
					latestInET = i+1;
				}

				// TODO Set the index as the next
				i++;
				// TODO ...if not, ...
			} else {
				int firstRemove = 0, lastRemove = 0;
				// ...check if there is a recorded lastest value inside ET
				if (latestInET >= 0){
					// TODO ...if so,...
					Integer[] slStartEnd = new Integer[]{0, 0};

					// TODO Convert current indices	to full cumHC array representation (sum last index
					// removed + 1, unless there is no previous removed index),...
					slStartEnd[0] += 0 + (latestRem + 1);				// CHECKED
					slStartEnd[1] += latestInET + (latestRem + 1);

					// TODO ...convert this value to represent actual segment positions, by summing 1 to the SL end and...
					slStartEnd[1] += 1;									// CHECKED

					// TODO ...register the SL from the start, until the latest recorded value inside ET
					currentSegment.addDetectedStraightLine(slStartEnd);

					// TODO Register all the SL values from cumHC list for removal
					firstRemove = 0;
					lastRemove = latestInET;

					// TODO Register the final SL value for subtraction
					toRemove = cumHC.get(lastRemove);
					// TODO ...if not,...
				} else {

					// TODO Register the first value from the list for removal
					firstRemove = 0;
					lastRemove = 0;

					// TODO Register the first value for subtraction
					toRemove = cumHC.get(lastRemove);
				}

				// TODO Remove the registered indices from the cumHC list
				int countToRemove = (lastRemove - firstRemove) + 1;
				for (int removed = 0; removed < countToRemove; removed++) 
					cumHC.remove(0);

				// TODO Subtract the registered value from all the remaining cumHC's
				for (int k = 0; k < cumHC.size(); k++) 
					cumHC.set(k, cumHC.get(k)-toRemove);

				// TODO Clear the lastest value inside ET as -1, and set the latest removed
				latestRem += countToRemove;
				latestInET = -1;


				// TODO Set the next index as zero
				i = 0;
			}
		}

		return !currentSegment.getDetectedStraightLines().isEmpty();

		//		// Do SLI Algorithm
		//		int baseIndex = 0;
		//		int maximumIndex = 0;
		//		int minimumIndex = baseIndex;
		//		while(!cumHC.isEmpty() && maximumIndex == 0) { // FIXME For the 10-08-2013 log, this has a bug on the "1.37614857E12" timestamp reading
		//			Boolean minNeedsUpdate = false;
		//			// Find the maximum cumulative heading value that...
		//			for(int i = 1; i < cumHC.size(); i++) {
		//				// If previous value is inside the MT value...
		//				if(cumHC.get(i-1) < middleThreshold && cumHC.get(i-1) > -middleThreshold){
		//					// If minimum index needs an update, update it
		//					minimumIndex = (minNeedsUpdate ? i-1 : minimumIndex);
		//					minNeedsUpdate = false;
		//
		//					// ...and if this one is inside the ET value
		//					if((cumHC.get(i) < endThreshold && cumHC.get(i) > -endThreshold)){
		//						maximumIndex = i + baseIndex;
		//					}
		//				} else
		//					minNeedsUpdate = true;
		//			}
		//
		//			// If we reach the end of the segment 
		//			// and no straight line is found
		//			if(maximumIndex == 0){
		//				// Discard the first value, recompute the cumHC
		//				for (int i = 1; i < cumHC.size(); i++) {
		//					// Remove the value of this cumulative heading
		//					// from the other cumulative headings
		//					double newHC = cumHC.get(i)-cumHC.get(0);
		//					cumHC.set(i, newHC);
		//				}
		//				cumHC.remove(0);
		//
		//				// ...and set the base index a unit higher 
		//				// than the starting position
		//				baseIndex++;
		//
		//				// Also adjust the minimumIndex
		//				minimumIndex = baseIndex;
		//			}
		//		}
		//
		//		// If the segment has a straight line...
		//		if(maximumIndex != 0){
		//			// Adjust it to match
		//			adjustSegment(minimumIndex, maximumIndex+1);
		//			return true;
		//		} else {
		//			// ...or return false
		//			return false;
		//		}
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
	 * @return the segments
	 */
	public final ArrayList<GPSSegment> getSegments() {
		return segments;
	}

	public GPSSegment getCurrentSegment(){
		return currentSegment;
	}

	public void setCurrentSegment(GPSSegment currentSegment) {
		this.currentSegment = currentSegment;
	}

	public Double getMiddleThreshold() {
		return middleThreshold;
	}

	public Double getEndThreshold() {
		return endThreshold;
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

	public void setSampleUpdater(SampleRunnable r){
		sampleRunnable = r;
	}
}
