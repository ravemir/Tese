package pt.utl.ist.thesis.signalprocessor;

import java.util.ArrayList;
import java.util.TreeMap;

import pt.utl.ist.thesis.util.buffers.ReadingCircularBuffer;
import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.SensorReading;
import pt.utl.ist.util.sensor.reading.StepReading;
import pt.utl.ist.util.sensor.source.ReadingSource;
import pt.utl.ist.util.sensor.source.StepReadingSource;
import pt.utl.ist.util.source.filters.ButterworthFilter;
import pt.utl.ist.util.source.filters.Filter;
import pt.utl.ist.util.source.filters.MovingAverageFilter;

public class StepAnalyser extends Analyser {

	private static final int _analysisBufferSize = 100;

//	private final int sampleRate;
	
	private static final double KFACTOR = 9; // TODO Chosen heuristically. Should be computed?
												// 		Value before which a peak is always discarded.
												//		Gravity magnitude is a good pick.
	private static final double PEAKTHRESHFACTOR = 0.7; // Multiplication factor to lower the step threshold
														// Depends on the variance of intensity of each step
														// i.e. if a step is a lot smaller than the previous,
														// this value should be lower to accommodate it.
	
	private ReadingCircularBuffer rawBuffer = new ReadingCircularBuffer(_analysisBufferSize);
	private TreeMap<Integer, ReadingCircularBuffer> avgBuffers = new TreeMap<Integer, ReadingCircularBuffer>();
	private SignalPeakData peakData = new SignalPeakData();

//	private StepReadingSource steps = new StepReadingSource();

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
		ReadingCircularBuffer targetBuffer = getAvgBuffer(order);
		
		// Adds the reading to the respective buffer
		targetBuffer.addReading(reading);
		avgBuffers.put(order, targetBuffer);
	}

	/**
	 * Gets the stored buffer for the specified order,
	 * or creates a new one if it doesn't exist.
	 * 
	 * @param order The order associated with the intended buffer.
	 * @return The intended buffer.
	 */
	private ReadingCircularBuffer getAvgBuffer(int order) {
		ReadingCircularBuffer targetBuffer;
		
		// If the buffer was previously created..
		if(avgBuffers.containsKey(order))
			// Get it from the list
			targetBuffer = avgBuffers.get(order);
		else
			// ...or create it
			targetBuffer = new ReadingCircularBuffer(_analysisBufferSize);
		
		return targetBuffer;
	}

	/**
	 * Takes the gathered data and processes it into a
	 * new state.
	 */
	public void processState(){
		// Get the buffers to analyse (first the average buffers, then the raw buffer)
		ArrayList<ReadingCircularBuffer> bufferCollection = new ArrayList<ReadingCircularBuffer>(avgBuffers.values()); 
		bufferCollection.add(rawBuffer);
		
		for(ReadingCircularBuffer circBuffer : bufferCollection){
			// Count peaks
			ArrayList<AccelReading> peakList = countPeaksInBuffer(circBuffer);
			Boolean wasPeakDetected = !(peakList.isEmpty());
			
			// Add them to the peak list
			peakData.addAll(peakList);
			
			// Clear state
			if(wasPeakDetected) {
				for(ReadingCircularBuffer rcb : avgBuffers.values()){
					rcb.clearOld();
				}
				rawBuffer.clearOld();
			}
		}
		
		// Check if it is time to count the steps
		ArrayList<AccelReading> unaveragedPeaks = peakData.getUnaveragedPeaks();
		if(unaveragedPeaks.size() >= 2){
			// Compute the average peakValue
			double peakMean = peakData.getCurrentNormMean();
			
			for(AccelReading r : unaveragedPeaks){
				// If a peak is bigger than threshold...
				if(r.getReadingNorm() > KFACTOR && 
						r.getReadingNorm() > PEAKTHRESHFACTOR*peakMean) {
					// Push a new StepReading object and add it to the list
//					steps.pushReading(new StepReading(r));
					pushReading(new StepReading(r));
				}				
			}
			
			// Clear the peak list
			peakData.clearMeanData();
		}
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
			// Detect peak in X, if case
				// If so, detect if it is lower than previous.
			
			// Detect peak in the norm value, if case
			double fwdSlope = computeNormSlope(bufferValues, i-1, i);
			double bwdSlope = computeNormSlope(bufferValues, i-2, i-1);
			if(fwdSlope < 0 && bwdSlope > 0){
				// If so, see if bigger than thresholds
				peaks.add((AccelReading) bufferValues[i-1]);
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
		double y = ((SensorReading) bufferValues[frontIndex]).getTimestamp()-
				((SensorReading) bufferValues[backIndex]).getTimestamp();
		
		return x / y;
	}
	
	private void addRawReading(SensorReading currentReading) {
		rawBuffer.addReading(currentReading);
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
		
		// ...and Process the state
		processState();
	}
	
	public void updateFromFilter(ReadingSource f, Object reading){
		if(f instanceof MovingAverageFilter){
			// Get this filter's order and the latest reading
			int order = ((MovingAverageFilter) f).getAverageOrder();
			SensorReading currentReading = (SensorReading) f.getBuffer().getCurrentReading();
	
			// Adds the reading to the buffer with the associated order
			addAvgReading(order, currentReading);
		} else if(f instanceof ButterworthFilter){
			// Get this filter's order and the latest reading
			int order = ((ButterworthFilter) f).getFilterOrder();
			SensorReading currentReading = (SensorReading) f.getBuffer().getCurrentReading();
	
			// Adds the reading to the buffer with the associated order
			addAvgReading(order, currentReading);
		} else {
			throw new UnsupportedOperationException("Tried to update Analyser from '"
					+ f.getClass().getSimpleName() + "' filter type." );
		}
		
	}
	
	public void updateFromRaw(ReadingSource rs, Object reading){
		// Get this ReadingSource's latest reading
		SensorReading currentReading = (SensorReading) rs.getBuffer().getCurrentReading();

		// Adds the reading to the buffer with the associated 
		// order (adding to an AverageCircularBuffer should be
		// the same thing)
		addRawReading(currentReading);
	}
}
