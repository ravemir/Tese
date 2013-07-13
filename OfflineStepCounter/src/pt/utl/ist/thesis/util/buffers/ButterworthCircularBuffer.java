package pt.utl.ist.thesis.util.buffers;

import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.OrientationReading;
import pt.utl.ist.util.sensor.reading.SensorReading;
import pt.utl.ist.util.source.filters.ButterworthData;

public class ButterworthCircularBuffer extends ReadingCircularBuffer {

	private final ButterworthData butterworthFilter;
	
	private ReadingCircularBuffer previousInputs;

	public ButterworthCircularBuffer(int size, ButterworthData bwd){
		super(size);
		butterworthFilter = bwd;
		previousInputs = new ReadingCircularBuffer(size);
		//this.zero();
		//previousInputs.zero();
	}

	/**
	 * Adds a reading to this filter. If the input readings'
	 * buffer has filled once (i.e. is warm) then it computes
	 * a filtered output and adds it to the main buffer.
	 */
	@Override
	public void addReading(SensorReading read) {
		// Check if enough inputs have been received
		Boolean shouldFilter = isWarm();
		
		// Add value to the internal buffer (to appear 
		// on the next computations)
		previousInputs.addReading(read);
		
		if(shouldFilter){
			// Get previous values
			ReadingCircularBuffer prevR = getPreviousInputs();
			
			// Get previous filters
			ReadingCircularBuffer prevF = this;
			
			// Sum 'b' terms
			double[] b = butterworthFilter.getB();
			double[] bTerms = new double[]{0, 0, 0, 0}; 
			for (int i = 0; i < b.length; i++) {
				double[] prevRValue = prevR.getPrevNReading(i).getReadingAndNorm();
				for (int j = 0; j < prevRValue.length; j++) {
					bTerms[j] += b[i] * prevRValue[j];
				}
			}
			
			// Sum 'a' terms
			double[] a = butterworthFilter.getA();
			double[] aTerms = bTerms;
			for (int i = 1; i < a.length; i++) {
				// TODO Write explanation for -1 (it's here for a[0] to be excluded)
				double[] prevRValue = prevF.getPrevNReading(i-1).getReadingAndNorm();
				for (int j = 0; j < prevRValue.length; j++) {
					aTerms[j] -= a[i] * prevRValue[j];
				}
			}
			
			// Add filtered reading
			SensorReading newRead;
			if(read instanceof AccelReading){
				newRead = new AccelReading(read.getTimestampString(), aTerms);
			} else if (read instanceof OrientationReading) {
				newRead = new OrientationReading(read.getTimestampString(), aTerms);
			} else
				throw new UnsupportedOperationException("Tried adding an unsupported SensorReading sub-type: " + read.getClass().getSimpleName());
			super.addReading(newRead);
		}
	}
	
	/**
	 * Returns the {@link ReadingCircularBuffer} containing the
	 * previously added input values.
	 * 
	 * @return A {@link ReadingCircularBuffer} with the previously
	 * 		added return values up to the n-th value, where 'n' is
	 * 		the order of the filter.
	 */
	public ReadingCircularBuffer getPreviousInputs() {
		return previousInputs;
	}

	@Override
	public SensorReading getPrevNReading(int n) {
		// Returns the filtered reading, according to 
		// the state of the previous inputs buffer
		return (isWarm()? super.getPrevNReading(n): new AccelReading());
	}

	/**
	 * Returns if this buffer is ready to output filter values.
	 * Normally, this function returns true once the main buffer
	 * is filled for the first time, but for a {@link ButterworthCircularBuffer},
	 * this happens when the input reading's buffer is filled
	 * instead.
	 */
	@Override
	public Boolean isWarm() {
		// This buffer is warm if it has received enough inputs
		return previousInputs.isWarm();
	}
	
	/**
	 * Returns the order of this filter object.
	 * 
	 * @return The order of the filter.
	 */
	public int getFilterOrder(){
		return butterworthFilter.getN();
	}
}
