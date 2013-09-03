package pt.utl.ist.thesis.signalprocessor;

import java.util.ArrayList;
import java.util.TreeMap;

import pt.utl.ist.thesis.sensor.reading.AccelReading;
import pt.utl.ist.thesis.sensor.reading.ExtremityType;
import pt.utl.ist.thesis.sensor.reading.SensorReading;
import pt.utl.ist.thesis.sensor.reading.StepReading;
import pt.utl.ist.thesis.sensor.source.ReadingSource;
import pt.utl.ist.thesis.sensor.source.StepReadingSource;
import pt.utl.ist.thesis.source.filters.ButterworthFilter;
import pt.utl.ist.thesis.source.filters.Filter;
import pt.utl.ist.thesis.source.filters.MovingAverageFilter;
import pt.utl.ist.thesis.util.buffers.ReadingCircularBuffer;

public class StepAnalyser extends Analyser {

	private static final double GRAVITY = 9.44;

	private int _analysisBufferSize = 71;
	
	private static final double KFACTOR = 11.5;	// Chosen heuristically. Should be computed?
												// Value before which a peak is always discarded.
												// Gravity magnitude is a good pick.
	private static final double PEAKTHRESHFACTOR = 0.7;	// Multiplication factor to lower the step threshold
														// Depends on the variance of intensity of each step
														// i.e. if a step is a lot smaller than the previous,
														// this value should be lower to accommodate it.
	
	private TreeMap<Integer, ReadingCircularBuffer> buffersByOrder = new TreeMap<Integer, ReadingCircularBuffer>();
	private SignalPeakData peakData = new SignalPeakData();
	
	// FIXME Remove after testing
	private Runnable runnable;

	private int readCount = 0;

	// TODO Move this attribute into PeakData
	private boolean isStepArmed = true;

	private boolean wasDisarmedStepDetected = false;

	private double lastStepTimestamp = 0;

	public StepAnalyser(int sampleRate){
		super(new StepReadingSource());
//		this.sampleRate = sampleRate;
	}
	
	public ArrayList<AccelReading> getNormPeaks() {
		return peakData.getAllPeaks();
	}
	
	public ArrayList<StepReading> getSteps() {
		// Obtain our reading
		return getStepReadingSource().getStepList();
	}

	/**
	 * Returns this {@link Analyser}'s {@link ReadingSource},
	 * which is a StepReadingSource.
	 * 
	 * @return	This Analyser's StepReadingSource.
	 */
	public StepReadingSource getStepReadingSource() {
		return (StepReadingSource) getReadingSource();
	}

	/**
	 * Add an {@link AccelReading} object resulting from
	 * a mean value computation.
	 * 
	 * @param order The order of the mean that was computed.
	 * @param reading The reading to be inserted.
	 */
	private void addAvgReading(int order, SensorReading reading) {
		// Gets the respective buffer if exists, creates if not
		ReadingCircularBuffer targetBuffer = getBufferByOrder(order);
		
		// Adds the reading to the respective buffer
		targetBuffer.addReading(reading);
		buffersByOrder.put(order, targetBuffer);
	}

	/**
	 * Gets the stored buffer for the specified order,
	 * or creates a new one if it doesn't exist.
	 * 
	 * @param order The order associated with the intended buffer.
	 * @return The intended buffer.
	 */
	private ReadingCircularBuffer getBufferByOrder(int order) {
		ReadingCircularBuffer targetBuffer;
		
		// If the buffer was previously created..
		if(buffersByOrder.containsKey(order))
			// Get it from the list
			targetBuffer = buffersByOrder.get(order);
		else
			// ...or create it
			targetBuffer = new ReadingCircularBuffer(_analysisBufferSize);
		
		return targetBuffer;
	}

	
	
	/**
	 * Takes the gathered data and processes it into a
	 * new state.
	 */
	public void processState() {
		synchronized(buffersByOrder) {
			// Get the buffers to analyse (first the average buffers, then the raw buffer)
			ArrayList<ReadingCircularBuffer> bufferCollection = 
					new ArrayList<ReadingCircularBuffer>(buffersByOrder.values());
			
			for(ReadingCircularBuffer circBuffer : bufferCollection){
				// Count peaks
				ArrayList<AccelReading> peakList = countPeaksInBuffer(circBuffer);
				Boolean wasPeakDetected = !(peakList.isEmpty());
				
				// Add them to the peak list
				synchronized(peakData) {
					peakData.addAll(peakList);
				}
				
				// Clear state
				if(wasPeakDetected) {
					for(ReadingCircularBuffer rcb : buffersByOrder.values()){
						rcb.clearOld();
					}
				} else	// ...if no peaks were detected, might  
						// aswell assume that last peak was an 
						// oscillation or an incomplete step
					isStepArmed = true;
			}
		}
		
		synchronized(peakData) {
			// Check if it is time to count the steps
			ArrayList<AccelReading> unaveragedPeaks = peakData.getUnaveragedPeaks();
			if(unaveragedPeaks.size() >= 2){
				// Compute the average peakValue
				double peakMean = peakData.getCurrentNormMean();
//				double peakMean = peakData.getStepAverage();
//				double peakMean = peakData.getPeakMean();
				
				// Computes the rolling average of the Peak/Step ratio
//				double currentRatioAverage = (peakData.getRatioAverage() == 0? 
//								PEAKTHRESHFACTOR : peakData.getRatioAverage());
				double currentRatioAverage = PEAKTHRESHFACTOR;
				for(AccelReading r : unaveragedPeaks){
					// Calculate the time since the latest peak
					// and obtain the elapsed step time average
					double elapsedTime = getPeakElapsedTime(r);
					double elapsedStepTimeAverage = peakData.getElapsedStepTimeAverage();
					
					double stepTimeRelaxationCoefficient = getStepTimeRelaxationCoefficient(r);
					if(r.getExtremityType() == ExtremityType.PEAK
							&& r.getReadingNorm() > KFACTOR
//							&& isStepArmed
							&& !wasDisarmedStepDetected
							&& elapsedTime > stepTimeRelaxationCoefficient * elapsedStepTimeAverage
//							&& r.getReadingNorm() > currentRatioAverage*peakMean
							) {
						// Push a new StepReading object and add it to the list
						
						// Disarm the the step, if it hasn't, or trigger "disarmed step"
						if(isStepArmed)
							isStepArmed = false;
						else
							wasDisarmedStepDetected = true;
						
						// Register a new step
						registerNewStep(r, elapsedTime);
						
						// Adds a Peak Ratio value to the average
						peakData.addRatioValue(peakMean/r.getReadingNorm());
						
						
						// FIXME Remove after testing
						executeRunnable();
					} else if(r.getExtremityType() == ExtremityType.VALLEY
							&& r.getReadingNorm() < GRAVITY
							&& !isStepArmed
//							&& wasDisarmedStepDetected
							){
						// Rearm the step, reset "disarmed step" detection
						isStepArmed = true;
						wasDisarmedStepDetected = false;
					}
				}
				
				// Clear the peak list
				peakData.clearMeanData();
			}
		}
	}

	/**
	 * @param r
	 * @return
	 */
	public double getStepTimeRelaxationCoefficient(AccelReading r) {
		return r.getReadingNorm() >= 19 ? 0.4 : 0.6;
	}

	/**
	 * @param r
	 * @return
	 */
	public double getPeakElapsedTime(AccelReading r) {
		Double currentTimestamp = r.getTimestamp();
		return currentTimestamp - 
				(lastStepTimestamp == 0? 
						currentTimestamp - 1000 : 
						lastStepTimestamp);
	}

	/**
	 * @param r
	 */
	public void registerNewStep(AccelReading r, double elapsedTime) {
		// Create the StepReading and push it
		StepReading step = new StepReading(r);
		pushReading(step);
		
		// Register this step's timestamp
		lastStepTimestamp = step.getTimestamp();
		
		// Register the elapsed time of the latest step 
		peakData.addElapsedStepTimeValue((elapsedTime >= 1500 ? 1500 : elapsedTime));
	}

	/**
	 * Counts the peaks in the given buffer, and 
	 * returns a list containing them.
	 * 
	 * @param circBuffer	The buffer in which the peaks
	 * 						will be counted.
	 * @return A list containing the peaks found.
	 */
	private ArrayList<AccelReading> countPeaksInBuffer(ReadingCircularBuffer circBuffer) {
		// Get the values from the buffer and create a list to store the peaks
		SensorReading[] bufferValues = circBuffer.getBufferValues();
		ArrayList<AccelReading> peaks = new ArrayList<AccelReading>();
		
		// Count peaks
		for(int i = 2; i < circBuffer.samplesWarmed() ; i++) {
			// Detect peak in the norm value, if the slopes are right
//			double fwdSlope = computeNormSlope(bufferValues, i-1, i);
//			double bwdSlope = computeNormSlope(bufferValues, i-2, i-1);
			double fwdSlope = computeNormSlope(circBuffer, i-1, i);
			double bwdSlope = computeNormSlope(circBuffer, i-2, i-1);
//			AccelReading extremity = (AccelReading) bufferValues[i-1];
			AccelReading extremity = (AccelReading) circBuffer.getFromIndex(i-1);
			if(fwdSlope < 0 && bwdSlope > 0){
				extremity.setExtremityType(ExtremityType.PEAK);
				peaks.add(extremity);
			} // ...or a valley
			else if (fwdSlope > 0 && bwdSlope < 0){
				extremity.setExtremityType(ExtremityType.VALLEY);
				peaks.add(extremity);
			}
		}
		
		return peaks;
	}

	/**
	 * Computes the slope on the array position
	 * described by the index values.
	 * 
	 * @param bufferValues The array containing the values.
	 * @param backIndex The index of the first value in the slope.
	 * @param frontIndex The index of the next value in the slope.
	 * @return The computed slope value.
	 */
	private double computeNormSlope(SensorReading[] bufferValues, int backIndex, int frontIndex) {
		double x = ((AccelReading) bufferValues[frontIndex]).getReadingNorm()-
				((AccelReading) bufferValues[backIndex]).getReadingNorm();
		double y = (bufferValues[frontIndex]).getTimestamp()-
				(bufferValues[backIndex]).getTimestamp();
		
		return x / y;
	}
	
	private double computeNormSlope(ReadingCircularBuffer rcb, int backIndex, int frontIndex) {
//		private double computeNormSlope(SensorReading[] bufferValues, int backIndex, int frontIndex) {
//			double x = ((AccelReading) bufferValues[frontIndex]).getReadingNorm()-
//					((AccelReading) bufferValues[backIndex]).getReadingNorm();
//			double y = (bufferValues[frontIndex]).getTimestamp()-
//					(bufferValues[backIndex]).getTimestamp();
			// FIXME Remove after tested
			
			double x = (rcb.getFromIndex(frontIndex)).getTimestamp()-
					(rcb.getFromIndex(backIndex)).getTimestamp();
			double y = ((AccelReading) rcb.getFromIndex(frontIndex)).getReadingNorm()-
					((AccelReading) rcb.getFromIndex(backIndex)).getReadingNorm();
			
			return y / x;
		}
	
	private void addRawReading(SensorReading currentReading) {
		ReadingCircularBuffer rcb = getBufferByOrder(0);
		
		rcb.addReading(currentReading);
	}

	

	@Override
	public void update(ReadingSource rs, SensorReading reading) {
			// Determine the type of ReadingSource/Filter that was pushed
			if(rs instanceof Filter)
				updateFromFilter((ReadingSource) rs, reading);
			else if(rs instanceof ReadingSource)
				updateFromRaw((ReadingSource) rs, reading);
			else {
				throw new UnsupportedOperationException("Tried to update Analyser from '"
						+ rs.getClass().getSimpleName() + "' observable type." );
			}
			readCount++;
			
			//... and if the sufficient number of readings has passed,...
			
			if(readCount >= _analysisBufferSize) {
				// ...process the state...
				processState();
				
				// ... and set the readCount to account for the first two values
				readCount = 2;
			}
	}
	
	public void updateFromFilter(ReadingSource f, Object reading){
		synchronized(buffersByOrder){
			if(f instanceof MovingAverageFilter){
				// Get this filter's order and the latest reading
				int order = ((MovingAverageFilter) f).getAverageOrder();
//				SensorReading currentReading = (SensorReading) f.getBuffer().getCurrentReading();
				SensorReading currentReading = (SensorReading) reading;
		
				// Adds the reading to the buffer with the associated order
				addAvgReading(order, currentReading);
			} else if(f instanceof ButterworthFilter){
				// Get this filter's order and the latest reading
				int order = ((ButterworthFilter) f).getFilterOrder();
//				SensorReading currentReading = (SensorReading) f.getBuffer().getCurrentReading();
				SensorReading currentReading = (SensorReading) reading;
		
				// Adds the reading to the buffer with the associated order
				addAvgReading(order, currentReading);
			} else {
				throw new UnsupportedOperationException("Tried to update Analyser from '"
						+ f.getClass().getSimpleName() + "' filter type." );
			}
		}
	}
	
	public void updateFromRaw(ReadingSource rs, Object reading){
		synchronized(buffersByOrder) {
			// Get this ReadingSource's latest reading
			SensorReading currentReading = (SensorReading) rs.getBuffer().getCurrentReading();
	
			// Adds the reading to the buffer with the associated 
			// order (adding to an AverageCircularBuffer should be
			// the same thing)
			addRawReading(currentReading);
		}
	}
	
	// FIXME Remove after testing
	public void setRunnable(Runnable r){
		runnable = r;
	}
	public void executeRunnable(){
		if(runnable != null) runnable.run();
	}
	
	/**
	 * @param _analysisBufferSize the _analysisBufferSize to set
	 */
	public final void setAnalysisBufferSize(int analysisBufferSize) {
		_analysisBufferSize = analysisBufferSize;
	}
}
