package pt.utl.ist.thesis.signalprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.math.stat.descriptive.SynchronizedSummaryStatistics;

import pt.utl.ist.thesis.util.buffers.GPSSegment;
import pt.utl.ist.util.sensor.exception.StepOutsideSegmentBoundariesException;
import pt.utl.ist.util.sensor.reading.GPSReading;
import pt.utl.ist.util.sensor.reading.StepReading;

/**
 * Handles all the required processing of the GPS
 * data for the AutoGait system: segmentation,
 * smoothing and Straight Line Identification.
 * 
 * @author Carlos Simões
 */
public class AutoGaitModelerAnalyser implements Observer {

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
	
	@Override
	public void update(Observable rs, Object reading) {
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
				
				// Add all steps to the currentSegment
				try {
					for(StepReading step : stepBuffer)
						currentSegment.addStepReading(step);
				} catch(StepOutsideSegmentBoundariesException e) {
					e.printStackTrace();
				}
				
				// Clear the step buffer
				stepBuffer.clear();
			} else if (reading instanceof StepReading){
				// TODO	Perform updates referring to
				// 		segmentation, smoothing and SLI.
				
				// Add StepReading to buffer 
				stepBuffer.add((StepReading) reading);
			} else {
				throw new UnsupportedOperationException("Tried to update Analyser from '"
						+ rs.getClass().getSimpleName() + "' observable type." );
			}
		}
	}

	/**
	 * 
	 */
	private void segment() {
		// If SLI conditions verify and segment 
		// has more than one step and GPS reading
		boolean isStraightLine = isCurrentSegmentSL();
		if(isStraightLine && currentSegment.size() > 1 
				&& currentSegment.getStepCount() > 1) {
			processCurrentSegment();
		}
		
		// Create new GPSSegment
		currentSegment = new GPSSegment();
	}

	/**
	 * 
	 */
	private void processCurrentSegment() {
		// Compute step lengths and add currentSegment to array
		currentSegment.computeStepFrequencyAndLength();
		segments.add(currentSegment);
		
		// Add this segment's values to the model
		autoGaitModel.addSampleToModel(currentSegment.getAverageStepFrequency(), 
				currentSegment.getAverageStepLength());
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
	public boolean isCurrentSegmentSL() {
		// Get Cumulative Heading Change values
		GPSSegment seg = currentSegment;
		ArrayList<Double> cumHC = new ArrayList<Double>(seg.getCumulativeHeadingChanges());
		
		// Do SLI Algorithm
		int baseIndex = 0;
		int maximumIndex = 0;
		int minimumIndex = baseIndex;
		while(!cumHC.isEmpty() && maximumIndex == 0) {
			Boolean minNeedsUpdate = false;
			// Find the maximum cumulative heading value that...
			for(int i = 1; i < cumHC.size(); i++) {
				// If previous value is inside the MT value...
				if(cumHC.get(i-1) < middleThreshold && cumHC.get(i-1) > -middleThreshold){
					// If minimum index needs an update, update it
					minimumIndex = (minNeedsUpdate ? i-1 : minimumIndex);
					minNeedsUpdate = false;
					
					// ...and if this one is inside the ET value
					if((cumHC.get(i) < endThreshold && cumHC.get(i) > -endThreshold)){
						maximumIndex = i + baseIndex;
					}
				} else
					minNeedsUpdate = true;
			}
			
			// If we reach the end of the segment 
			// and no straight line is found
			if(maximumIndex == 0){
				// Discard the first value, recompute the cumHC
				for (int i = 1; i < cumHC.size(); i++) {
					// Remove the value of this cumulative heading
					// from the other cumulative headings
					double newHC = cumHC.get(i)-cumHC.get(0);
					cumHC.set(i, newHC);
				}
				cumHC.remove(0);
				
				// ...and set the base index a unit higher 
				// than the starting position
				baseIndex++;
				
				// Also adjust the minimumIndex
				minimumIndex = baseIndex;
			}
		}
		
		// If the segment has a straight line...
		if(maximumIndex != 0){
			// Adjust it to match
			adjustSegment(minimumIndex, maximumIndex+1);
			return true;
		} else {
			// ...or return false
			return false;
		}
	}

	/**
	 * Readjusts a segment to start and end on the specified
	 * {@link GPSReading} values' indexes.
	 * 
	 * @param minimumIndex	The index indicating the beginning 
	 * 						of the new segment.
	 * @param maximumIndex	The index indicating the end of
	 * 						the new segment.
	 */
	private void adjustSegment(int minimumIndex, int maximumIndex) {
		// Get an adjusted sublist of GPSReading values
		List<GPSReading> subList = currentSegment.subList(minimumIndex, maximumIndex+1);
		GPSReading[] adjustedGPS = new GPSReading[subList.size()];
		subList.toArray(adjustedGPS);
		
		// Create new segment with adjusted GPS readings
		currentSegment = new GPSSegment(adjustedGPS);
		
		// Reinsert step readings
		for(StepReading step : currentSegment.getSteps()){
			try {
				currentSegment.addStepReading(step);
			} catch (StepOutsideSegmentBoundariesException e) {
				e.printStackTrace();
			}
		}
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
}
