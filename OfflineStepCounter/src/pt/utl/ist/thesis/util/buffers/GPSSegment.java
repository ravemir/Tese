package pt.utl.ist.thesis.util.buffers;

import java.util.ArrayList;
import java.util.List;

import pt.utl.ist.thesis.sensor.exception.StepOutsideSegmentBoundariesException;
import pt.utl.ist.thesis.sensor.reading.GPSReading;
import pt.utl.ist.thesis.sensor.reading.StepReading;
import pt.utl.ist.thesis.signalprocessor.AutoGaitModel;
import pt.utl.ist.thesis.util.MathUtils;

public class GPSSegment extends ArrayList<GPSReading> {
	/**
	 * 
	 */
	private Double dist = 0D;
	
	private static final long serialVersionUID = 4751169831054674746L;
	
	private ArrayList<GPSReading> smoothed = new ArrayList<GPSReading>();
	
	private ArrayList<StepReading> steps = new ArrayList<StepReading>();
	
	public ArrayList<StepReading> getSteps() {
		return steps;
	}

	private ArithmeticAverageBuffer stepFreqAverage = new ArithmeticAverageBuffer();

	private ArrayList<Double> accumulatedHeadingChanges = new ArrayList<Double>();

	private ArrayList<Integer[]> detectedStraightLines = new ArrayList<Integer[]>();
	
	private ArrayList<Double[]> slSamples = new ArrayList<Double[]>();
	
	/**
	 * @return the detectedStraightLines
	 */
	public ArrayList<Integer[]> getDetectedStraightLines() {
		return detectedStraightLines;
	}
	
	/**
	 * Returns true if this {@link GPSSegment} currently has
	 * detected straight lines stored.
	 * 
	 * @return	If this {@link GPSSegment} has straight lines or not.
	 */
	public boolean hasDetectedStraightLines(){
		return !detectedStraightLines.isEmpty();
	}
	
	public void addDetectedStraightLine(Integer[] straightLine) {
		// The received array must have two positions
		if(straightLine.length != 2) 
			throw new RuntimeException("Tried adding an invalid straight line");
		
		// Add the straight line to the internal list
		detectedStraightLines.add(straightLine);
		
		// TODO Compute the respective sample for this straight line
		GPSSegment s = returnAdjustedSegment(straightLine[0], straightLine[1]);
		s.computeStepFrequencyAndLength();
		slSamples.add(new Double[]{
				s.getAverageStepFrequency(), 
				s.getAverageStepLength()});
	}

	/**
	 * Default constructor.
	 */
	public GPSSegment(){
		super();
	}
	
	/**
	 * Creates a {@link GPSSegment} object, initialized with 
	 * the {@link GPSReading} objects inside the given
	 * array, with or without perfoming smoothing.
	 * 
	 * @param	array An array containing the {@link GPSReading}s to 
	 * 			initialize the segment with.
	 * @param	doForceAdd Whether to perform force add with
	 * 			the {@link GPSReading}s
	 */
	public GPSSegment(GPSReading[] array, boolean doForceAdd){
		this();
		
		// Add each of the supplied readings
		for(GPSReading read: array) {
			if(doForceAdd) forceAddGPSReading(read);
			else addGPSReading(read);
		}
	}
	
	/**
	 * Creates a {@link GPSSegment} object, initialized with 
	 * the {@link GPSReading} objects inside the given
	 * array.
	 * 
	 * @param	array An array containing the objects to 
	 * 			initialize the segment with.
	 */
	public GPSSegment(GPSReading[] array){
		this(array, false);
	}
	
	/**
	 * Creates a {@link GPSSegment} object, initialized with
	 * both the {@link GPSReading} and {@link StepReading}
	 * objects supplied in their respective array. As with
	 * normal step insertions, the provided steps must have
	 * occurred between the first and last {@link GPSReading}.
	 * 
	 * @param gpsArray	An array containing the {@link GPSReading}
	 * 					objects to initialize the segment with.
	 * @param stepArray	An array containing the {@link StepReading}
	 * 					objects to initialize the segment with.
	 * @throws Exception	A given step is outside of the
	 * 						{@link GPSReading}'s boundaries.
	 */
	public GPSSegment(GPSReading[] gpsArray, StepReading[] stepArray) throws Exception{
		this(gpsArray);
		
		// Add each of the supplied steps to the array
		for(StepReading step : stepArray)
			addStepReading(step);
	}
	
	/**
	 * Adds a {@link StepReading} to this segment, checking
	 * if it is placed within its boundaries.
	 * 
	 * @param step	The step to be added. 
	 * @throws StepOutsideSegmentBoundariesException
	 */
	public void addStepReading(StepReading step)
			throws StepOutsideSegmentBoundariesException {
		// Check if the step is within this segment's
		// boundaries and throw an exception otherwise
		Double stepTs = step.getTimestamp();
		GPSReading startGpsReading = get(0);
		GPSReading endGpsReading = get(size()-1);
		Double startSegTs = startGpsReading.getTimestamp();
		Double endSegTs = endGpsReading.getTimestamp();
		if(!(stepTs >= startSegTs && stepTs <= endSegTs)){ // FIXME Verificar se este critério é bom para incluir ou não
			throw new StepOutsideSegmentBoundariesException("The supplied step " +
					"is outside of this segment's timespan. (should be between '" + 
					startGpsReading.getTimestampString() + "' and '" + 
					endGpsReading.getTimestampString() + "' and was '" +
					step.getTimestampString() + "')", stepTs > endSegTs);
		}
		
		// From this point, this method has the same behavior
		forceAddStepReading(step);
	}

	/**
	 * Adds a {@link StepReading} to this {@link GPSSegment}, ignoring
	 * if it belongs within the boundaries of the {@link GPSReading}s.
	 * 
	 * @param step The step reading to be added.
	 */
	public void forceAddStepReading(StepReading step) {
		// Add the frequency value to average
		Double f = step.getStepFrequency();
		if(f == null) // If this step has no frequency value, then 
			step.setStepFrequency(1D);
		stepFreqAverage.addValue(step.getStepFrequency());
		
		// Add the step to the ArrayList
		steps.add(step);
	}

	/**
	 * Adds a {@link GPSReading} to this segment, performing
	 * all the smoothing operations.
	 * 
	 * @param r	The GPS reading to be added.
	 */
	public void addGPSReading(GPSReading r){
		// Add the GPSReading as if forced
		forceAddGPSReading(r);
		
		// Perform smoothing operations and add it to the secondary list
		if(size() > 2){
			// Get the previous three values to be convoluted
			GPSReading[] convolved = {
					get(size()-3),
					get(size()-2),
					get(size()-1)};
			
			// Compute smoothing values
			double newLatitude = MathUtils.plainAverage(new double[]{
					convolved[0].getLatLongReading()[0],
					convolved[1].getLatLongReading()[0],
					convolved[2].getLatLongReading()[0]});
			double newLongitude = MathUtils.plainAverage(new double[]{
					convolved[0].getLatLongReading()[1],
					convolved[1].getLatLongReading()[1],
					convolved[2].getLatLongReading()[1]});
			
			// Create smoothed reading and add 
			GPSReading smoothedReading = new GPSReading(convolved[1].getTimestamp(), 
					newLatitude, newLongitude, convolved[1].getBearing(), convolved[1].getSpeed());
			smoothed.add(smoothedReading);
		}
	}

	/**
	 * Adds a {@link GPSReading} to this segment, without
	 * performing all the smoothing operations.
	 * 
	 * @param r	The GPS reading to be added.
	 */
	public void forceAddGPSReading(GPSReading r) {
		//		// If reading's speed is 0
		//		if(r.getSpeed() == 0){
		//			// Compute it and set it
		//			double newSpeed = MathUtils.distFrom(getLastGPSReading().getLatitude(), lng1, lat2, lng2);
		//			r.setSpeed(newSpeed);
		//		}
				
				// Add reading to internal queue
				add(r);
				
				// If this isn't the first reading
				if(size() > 1){
					// Acumulate segment distance (sum-up method)
					GPSReading lastReading = getPreviousGPSReading();
					Double long2 = r.getLongitude();
					Double long1 = lastReading.getLongitude();
					Double lat2 = r.getLatitude();
					Double lat1 = lastReading.getLatitude();
					dist += MathUtils.distFrom(lat1, long1, 
							lat2, long2);
					
					// Compute Heading Change and accumulate it 
					Double headingChange =
							MathUtils.headingChangeFromBearings(
									lastReading.getBearing(), r.getBearing());
					updateCumulativeHeadingChanges(headingChange);
				}
	}

	/**
	 * Updates the Cumulative Heading Change array 
	 * with a new value.
	 * 
	 * @param headingChange The value to add
	 */
	private void updateCumulativeHeadingChanges(Double headingChange) {
		ArrayList<Double> cumulHC = accumulatedHeadingChanges;
		double newCumulHC = headingChange;
		
		// Retrieve last accumulated HC value only if it exists
		if(!cumulHC.isEmpty())
			newCumulHC += cumulHC.get(cumulHC.size() - 1);
		
		// Apply modular arithmetic to contain result
		newCumulHC %= 360;
		
		// Add value to array
		cumulHC.add(newCumulHC);
	}
	
	public GPSReading getSmoothedReading(int index){
		return smoothed.get(index);
	}
	
	public ArrayList<GPSReading> getReadingList(){
		return this;
	}
	
	public ArrayList<Double> getCumulativeHeadingChanges() {
		return accumulatedHeadingChanges;
	}
	
	public GPSReading getGPSReading(int index){
		return get(index);
	}
	
	public GPSReading getPreviousGPSReading(){
		return get(size()-2);
	}

	/**
	 * Computes the average values for this {@link GPSSegment}'s
	 * {@link StepReading}s frequency and length,
	 * TODO
	 */
	public void computeStepFrequencyAndLength() {
		// Compute average step length
		Double stepLength = getAverageStepLength();
		
		// Update steps with computed length and average frequency
		for(StepReading r : steps) {
			r.setStepLength(stepLength);
			r.setStepFrequency(stepFreqAverage.getCurrentValue());
		}
	}

	public int getStepCount() {
		return steps.size();
	}
	
	public int getGPSReadingCount(){
		return size();
	}
	
	public boolean equals(GPSSegment cmp){
		boolean result = (size() == cmp.size());
		
		// If the lengths are the same, run through each point...
		for (int i = 0; i < cmp.size() && result; i++)
			// ...and compare them
			result = cmp.get(i).equals(get(i));
		
		return result;
	}
	
	/**
	 * Returns the value of the average step length,
	 * as described in the AutoGait paper ('B. Walking 
	 * Profile Calibration', section 1).
	 * 
	 * @return The value of the average step length.
	 */
	public Double getAverageStepLength(){
		// Get the step count
		int stepCount = getStepCount();
		
		// Compute average step length
		Double stepLength = (dist / stepCount != 0 ? dist / stepCount : 0);
		return stepLength;
	}
	
	/**
	 * Returns the current average step frequency value.
	 * 
	 * @return The value of the average step frequency.
	 */
	public Double getAverageStepFrequency() {
		// Return the average step length value
		return (getStepCount() > 0 ? stepFreqAverage.getCurrentValue() : 0);
	}
	
	/**
	 * Returns this {@link GPSSegment}'s valid step
	 * samples, ready to be inserted into the
	 * {@link AutoGaitModel}.
	 * 
	 * @return	An ArrayList containing the samples.
	 */
	public List<Double[]> getSegmentStepSamples(){
		return slSamples;
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
	private GPSSegment returnAdjustedSegment(int minimumIndex, int maximumIndex) {
		// Get an adjusted sublist of GPSReading values
		List<GPSReading> subList = subList(minimumIndex, maximumIndex+1);
		GPSReading[] adjustedGPS = new GPSReading[subList.size()];
		subList.toArray(adjustedGPS);

		// Create new segment with adjusted GPS readings, force added (no smoothing)
		GPSSegment adjustedSegment = new GPSSegment(adjustedGPS, true);

		// Reinsert step readings
		for(StepReading step : getSteps()){
			try {
				adjustedSegment.addStepReading(step);
			} catch (StepOutsideSegmentBoundariesException e) {
				// This Step shouldn't be included in the new segment
			}
		}
		
		return adjustedSegment;
	}
	
	public GPSSegment[] getStraightLineSegments(){
		GPSSegment[] slSegments = new GPSSegment[detectedStraightLines.size()];
		
		for (int i = 0; i < detectedStraightLines.size(); i++) {
			slSegments[i] = returnAdjustedSegment(
					detectedStraightLines.get(i)[0], 
					detectedStraightLines.get(i)[1]);
		}
		
		return slSegments;
	}
}

