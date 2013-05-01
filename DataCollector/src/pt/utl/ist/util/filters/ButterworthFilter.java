package pt.utl.ist.util.filters;

import java.util.Observable;

import pt.utl.ist.util.sensor.AccelReading;
import pt.utl.ist.util.sensor.ButterworthCircularBuffer;
import pt.utl.ist.util.sensor.ReadingCircularBuffer;

public class ButterworthFilter extends Filter {

	/**
	 * Create a low-pass or high-pass Butterworth filter
	 * object, cutting at the specified frequency, designed
	 * for the specified sampling frequency and with
	 * the specified order.
	 * 
	 * @param filtOrder The order of the created filter.
	 * @param cuttingFreq The frequency to cut from/to.
	 * @param sampleRate The sampling rate of the incoming
	 * 		data.
	 * @param lowpass Whether the resulting filter will
	 * 		be low or high pass.
	 */
	public ButterworthFilter(int filtOrder, double cuttingFreq, double sampleRate,
			boolean lowpass){
		super(new ButterworthCircularBuffer(filtOrder,
				new ButterworthData(filtOrder, cuttingFreq,
						sampleRate, lowpass)));
	}
	
	/**
	 * Create a band-pass or band-stop Butterworth filter
	 * object, cutting at the specified frequencies, designed
	 * for the specified sampling frequency and with
	 * the specified order.
	 * 
	 * @param filtOrder The order of the created filter.
	 * @param cuttingFreq1 The first frequency to cut from/to.
	 * @param cuttingFreq1 The second frequency to cut from/to.
	 * @param sampleRate The sampling rate of the incoming
	 * 		data.
	 * @param bandpass Whether the resulting filter will
	 * 		be band-pass or band-stop.
	 */
	public ButterworthFilter(int filtOrder, double cuttingFreq1, double cuttingFreq2,
			double sampleRate, boolean bandpass){
		super(new ButterworthCircularBuffer(filtOrder, 
				new ButterworthData(filtOrder, cuttingFreq1, cuttingFreq2,
						sampleRate, bandpass)));
	}

	/**
	 * Invoked during an update to an attached ReadingSource
	 * object. (Part of the Observer pattern)
	 */
	@Override
	public void update(Observable readingSource, Object reading) {
		// Cast the received reading to the appropriate type
		AccelReading receivedRead = (AccelReading) reading;

		// Notify all Filters or FilterAnalysers (also Observers)
		pushReading(receivedRead);
	}
	

	@Override
	public ReadingCircularBuffer getBuffer() {
		return buffer;
	}
	
	/**
	 * Returns the order of this {@link ButterworthFilter}.
	 * 
	 * @return The order of this {@link ButterworthFilter}
	 */
	public int getFilterOrder(){
		return ((ButterworthCircularBuffer) buffer).getFilterOrder();
	}

}
