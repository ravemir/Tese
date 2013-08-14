package pt.utl.ist.thesis.util.buffers;

import java.util.ArrayList;
import java.util.List;

import pt.utl.ist.thesis.sensor.exception.StepOutsideSegmentBoundariesException;
import pt.utl.ist.thesis.sensor.reading.GPSReading;
import pt.utl.ist.thesis.sensor.reading.StepReading;
import pt.utl.ist.thesis.signalprocessor.AutoGaitModel;
import pt.utl.ist.thesis.util.MathUtils;

public class GPSSegment extends ArrayList<GPSReading> {
	private static final long serialVersionUID = 4751169831054674746L;
	
	// Raw GPSReading Attributes
	private Double dist = 0D;
	private ArrayList<Double> accumulatedHeadingChanges = new ArrayList<Double>();
	
	// Smoothing Attributes
	private boolean doSmoothing;
	private ArrayList<GPSReading> smoothedReadings = new ArrayList<GPSReading>();
	private Double smoothedDistance = 0D;
	private ArrayList<Double> accumulatedSmoothedHeadingChanges = new ArrayList<Double>();
	
	// Step Attributes
	private ArrayList<StepReading> steps = new ArrayList<StepReading>();
	private ArithmeticAverageBuffer stepFreqAverage = new ArithmeticAverageBuffer();
	
	// SLI state Attributes
	private ArrayList<Integer[]> detectedStraightLines = new ArrayList<Integer[]>();
	private ArrayList<Double[]> slSamples = new ArrayList<Double[]>();

	/**
	 * Default constructor, with smoothing.
	 */
	public GPSSegment(){
		this(true);
	}
	
	/**
	 * Creates a {@link GPSSegment}, with or without
	 * {@link GPSReading} smoothing activated.
	 * 
	 * @param doSmooth	Whether or not to smooth added
	 * 					{@link GPSReading}s.
	 */
	public GPSSegment(boolean doSmooth){
		super();
		
		doSmoothing = doSmooth; 
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
		this(array, true);
	}
	
	/**
	 * Creates a {@link GPSSegment} object, initialized with 
	 * the {@link GPSReading} objects inside the given
	 * array, with or without perfoming smoothing.
	 * 
	 * @param	array An array containing the {@link GPSReading}s to 
	 * 			initialize the segment with.
	 * @param	doSmooth Whether to apply smoothing on the
	 * 			{@link GPSReading}s
	 */
	public GPSSegment(GPSReading[] array, boolean doSmooth){
		this(doSmooth);
		
		// Add each of the supplied readings
		for(GPSReading read: array) {
			if(doSmooth) addGPSReading(read);
			else forceAddGPSReading(read);
		}
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
	 * Adds a detected Straight Line to the list.
	 *  
	 * @param straightLine	The Straight Line to add.
	 */
	public void addDetectedStraightLine(Integer[] straightLine) {
		// The received array must have two positions
		if(straightLine.length != 2) 
			throw new RuntimeException("Tried adding an invalid straight line");
		
		// Add the straight line to the internal list
		detectedStraightLines.add(straightLine);
		
		// Compute the respective sample for this straight line
		GPSSegment s = returnAdjustedSegment(straightLine[0], straightLine[1]);
		s.computeStepFrequencyAndLength();
		slSamples.add(new Double[]{
				s.getAverageStepFrequency(), 
				s.getAverageStepLength()});
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
		if(doSmoothing && size() > 2){
			// Get the previous three values to be convoluted
			GPSReading[] unconvolved = {
					get(size()-3),
					get(size()-2),
					get(size()-1)};
			
			// Compute smoothedReadings values
			double newLatitude = MathUtils.arithmeticAverage(new double[]{
					unconvolved[0].getLatLongReading()[0],
					unconvolved[1].getLatLongReading()[0],
					unconvolved[2].getLatLongReading()[0]});
			double newLongitude = MathUtils.arithmeticAverage(new double[]{
					unconvolved[0].getLatLongReading()[1],
					unconvolved[1].getLatLongReading()[1],
					unconvolved[2].getLatLongReading()[1]});
			GPSReading prevSmoothed = (smoothedReadings.isEmpty() ?			// If there is no previous smoothedReadings value,
					getPreviousGPSReading() :						// use the raw, instead
					getPreviousSmoothedGPSReading());
			Double prevLatitude = prevSmoothed.getLatitude();
			Double prevLongitude = prevSmoothed.getLongitude();
			double newHeading = MathUtils.calculateHeading(
					prevLatitude, prevLongitude, 
					newLatitude, newLongitude);
			
			// Create smoothedReadings reading and add 
			GPSReading smoothedReading = new GPSReading(unconvolved[1].getTimestamp(),
					newLatitude, newLongitude, newHeading, unconvolved[1].getSpeed());
			smoothedReadings.add(smoothedReading);
			
			// If there is more than one reading, accumulate smoothedReadings distance and heading changes
			if(smoothedReadings.size() > 1) {
				smoothedDistance += MathUtils.distFrom(prevLatitude, prevLongitude, 
						newLatitude, newLongitude);
				double headingChange = prevSmoothed.getBearing() - newHeading;
				updateCumulativeHeadingChanges(headingChange, 
						accumulatedSmoothedHeadingChanges);
			}
		}
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
		if(!(stepTs >= startSegTs && stepTs <= endSegTs)){
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
	 * Computes the average values for this {@link GPSSegment}'s
	 * {@link StepReading}s frequency and length. This accounts
	 * the whole {@link GPSSegment}, not just the Straight Lines
	 * identified.
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
	
	/**
	 * Returns whether this {@link GPSSegment} is equivalent
	 * to the given one, or not.
	 * 
	 * @param cmp	The {@link GPSSegment} to compare to.
	 * @return		If this {@link GPSSegment} is equivalent 
	 * 				to the other.
	 */
	public boolean equals(GPSSegment cmp){
		boolean result = (size() == cmp.size());
		
		// If the lengths are the same, run through each point...
		for (int i = 0; i < cmp.size() && result; i++)
			// ...and compare them
			result = cmp.get(i).equals(get(i));
		
		return result;
	}

	/**
	 * Adds a {@link GPSReading} to this segment, without
	 * performing all the smoothing operations.
	 * 
	 * @param r	The GPS reading to be added.
	 */
	public void forceAddGPSReading(GPSReading r) {
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
							MathUtils.headingChangeFromDirections(
									lastReading.getBearing(), r.getBearing());
					updateCumulativeHeadingChanges(headingChange, accumulatedHeadingChanges);
				}
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
	 * Returns the current average step frequency value.
	 * 
	 * @return The value of the average step frequency.
	 */
	public Double getAverageStepFrequency() {
		// Return the average step length value
		return (getStepCount() > 0 ? stepFreqAverage.getCurrentValue() : 0);
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
	 * Returns the current Cumulative Heading Change
	 * array, with all the cumulative heading changes.
	 * (these values refer to the unsmoothed {@link GPSReading}s)
	 * 
	 * @return	The Cumulative Heading Change values.
	 */
	public ArrayList<Double> getCumulativeHeadingChanges() {
		return accumulatedHeadingChanges;
	}
	
	/**
	 * Returns the currently detected StraightLines
	 * 
	 * @return The currently detected StraightLines.
	 */
	public ArrayList<Integer[]> getDetectedStraightLines() {
		return detectedStraightLines;
	}
	
	/**
	 * Returns a {@link GPSReading} at the given index.
	 * @param index	The index value to return the 
	 * 				{@link GPSReading} from.
	 * @return		The respective {@link GPSReading}.
	 */
	public GPSReading getGPSReading(int index){
		return get(index);
	}
	
	/**
	 * Returns this {@link GPSSegment}'s {@link GPSReading} count.
	 * 
	 * @return	The number of {@link GPSReading}s in this {@link GPSSegment}.
	 */
	public int getGPSReadingCount(){
		return size();
	}
	
	/**
	 * Returns the last inserted {@link GPSReading}.
	 * 
	 * @return	The last inserted {@link GPSReading}.
	 */
	public GPSReading getPreviousGPSReading(){
		return get(size()-2);
	}
	
	/**
	 * Get the previously inserted smoothedReadings {@link GPSReading}.
	 * 
	 * @return	The previously inserted smoothedReadings 
	 * 			{@link GPSReading}.
	 */
	public GPSReading getPreviousSmoothedGPSReading() {
		return smoothedReadings.get(smoothedReadings.size()-1);
	}

	/**
	 * Returns a list of all the unsmoothed {@link GPSReading}s.
	 * 
	 * @return	All the unsmoothed {@link GPSReading}s.
	 */
	public ArrayList<GPSReading> getReadingList(){
		return this;
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
	 * Returns the current Cumulative Heading Change
	 * array, with all the cumulative heading changes.
	 * (these values refer to the smoothedReadings {@link GPSReading}s)
	 * 
	 * @return	The Cumulative Heading Change values, for
	 * 			the smoothedReadings {@link GPSReading}s.
	 */
	public final ArrayList<Double> getSmoothedCumulativeHeadingChanges() {
		return accumulatedSmoothedHeadingChanges;
	}

	/**
	 * Returns the refered smoothedReadings {@link GPSReading}.
	 * 
	 * @param index	The index of the intended {@link GPSReading}.
	 * @return		The inteded {@link GPSReading}.
	 */
	public GPSReading getSmoothedReading(int index){
		return smoothedReadings.get(index);
	}
	
	/**
	 * Returns this {@link GPSSegment}'s {@link StepReading} count.
	 *  
	 * @return	The number of {@link StepReading}s in this {@link GPSSegment}.
	 */
	public int getStepCount() {
		return getSteps().size();
	}
	
	/**
	 * Returns a list of all the {@link StepReading}s.
	 * 
	 * @return	The list with all the {@link StepReading}s.
	 */
	public ArrayList<StepReading> getSteps() {
		return steps;
	}
	
	/**
	 * Returns an array of {@link GPSSegment}s of the
	 * detected Straight Lines, including {@link StepReading}s.
	 * 
	 * @return	An array containing all the SL {@link GPSSegment}s.
	 */
	public GPSSegment[] getStraightLineSegments(){
		GPSSegment[] slSegments = new GPSSegment[detectedStraightLines.size()];
		
		for (int i = 0; i < detectedStraightLines.size(); i++) {
			slSegments[i] = returnAdjustedSegment(
					detectedStraightLines.get(i)[0], 
					detectedStraightLines.get(i)[1]);
		}
		
		return slSegments;
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
	
	/**
	 * Determine if the current segment is a straight-line.
	 * 
	 * @return	Whether the segment currently being filled
	 * 			is a straight line or not.
	 */
	public boolean hasSegmentSL(Double middleThreshold, Double endThreshold) {
		// Check if SLs have been identified, and if so 
		// return true immediately
		if(!getDetectedStraightLines().isEmpty()) return true;

		// Get Cumulative Heading Change values
		ArrayList<Double> cumHC = new ArrayList<Double>(
				(doSmoothing? getSmoothedCumulativeHeadingChanges() : 
					getCumulativeHeadingChanges()));

		int latestInET = -1;
		int latestRem = -1;
		double toRemove = 0;
		// For every cumHC value...
		for (int i = 0; i < cumHC.size();) {
			Double currentHC = cumHC.get(i);

			// If it is inside MT (and there is a next cumHC)...
			if(currentHC < middleThreshold && currentHC > -middleThreshold &&
					i+1 < cumHC.size()) {
				Double nextHC = cumHC.get(i+1);

				// ...if so, check if next is inside ET...
				if(nextHC < endThreshold && nextHC > -endThreshold) {
					// ...if so, record the latest value inside ET
					latestInET = i+1;
				}

				// Set the index as the next
				i++;
			// ...if not, ...
			} else {
				int firstRemove = 0, lastRemove = 0;
				// ...check if there is a recorded lastest value inside ET
				if (latestInET >= 0){
					// ...if so,...
					Integer[] slStartEnd = new Integer[]{0, 0};

					// Convert current indices	to full cumHC array representation (sum 
					// last index removed + 1, unless there is none removed),...
					slStartEnd[0] += 0 + (latestRem + 1);
					slStartEnd[1] += latestInET + (latestRem + 1);

					// ...convert this value to represent actual segment positions, 
					// by summing 1 to the SL end and...
					slStartEnd[1] += 1;

					// ...register the SL from the start, until the latest recorded value inside ET
					addDetectedStraightLine(slStartEnd);

					// Register all the SL values from cumHC list for removal
					firstRemove = 0;
					lastRemove = latestInET;

					// Register the final SL value for subtraction
					toRemove = cumHC.get(lastRemove);
				// ...if not,...
				} else {
					// Register the first value from the list for removal
					firstRemove = 0;
					lastRemove = 0;

					// Register the first value for subtraction
					toRemove = cumHC.get(lastRemove);
				}

				// Remove the registered indices from the cumHC list
				int countToRemove = (lastRemove - firstRemove) + 1;
				for (int removed = 0; removed < countToRemove; removed++) 
					cumHC.remove(0);

				// Subtract the registered value from all the remaining cumHC's
				for (int k = 0; k < cumHC.size(); k++) 
					cumHC.set(k, cumHC.get(k)-toRemove);

				// Clear the lastest value inside ET as -1, and set the latest removed
				latestRem += countToRemove;
				latestInET = -1;

				// Set the next index as zero
				i = 0;
			}
		}

		return !getDetectedStraightLines().isEmpty();
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
		List<GPSReading> targetList = (doSmoothing? smoothedReadings : this);
		List<GPSReading> subList = targetList.subList(minimumIndex, maximumIndex+1);
		GPSReading[] adjustedGPS = new GPSReading[subList.size()];
		subList.toArray(adjustedGPS);

		// Create new segment with adjusted GPS readings, force added (no smoothing)
		GPSSegment adjustedSegment = new GPSSegment(adjustedGPS, false);

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
	
	/**
	 * Updates the Cumulative Heading Change array 
	 * with a new value.
	 * 
	 * @param headingChange The value to add
	 */
	private void updateCumulativeHeadingChanges(Double headingChange, 
			ArrayList<Double> cumulativeHeadingChanges) {
		ArrayList<Double> cumulHC = cumulativeHeadingChanges;
		double newCumulHC = headingChange;
		
		// Retrieve last accumulated HC value only if it exists
		if(!cumulHC.isEmpty())
			newCumulHC += cumulHC.get(cumulHC.size() - 1);
		
		// Apply modular arithmetic to contain result
		newCumulHC %= 360;
		
		// Add value to array
		cumulHC.add(newCumulHC);
	}
}
