package pt.utl.ist.thesis.util.buffers;

import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.OrientationReading;
import pt.utl.ist.util.sensor.reading.SensorReading;


public class AverageCircularBuffer extends ReadingCircularBuffer {

	private final int averageOrder;
	
	// Buffer that stores each average term as it arrives
	private ReadingCircularBuffer avgTerms;
	
	public AverageCircularBuffer(int size) {
		super(size);
		averageOrder = size;
		avgTerms = new ReadingCircularBuffer(size);
	}
	
	@Override
	public void addReading(SensorReading read){
		checkType(read);
		// Get the current value and compute the average term value
		double[] latestTerm = read.getAverageableReading();
		for (int i = 0; i < latestTerm.length; i++)  // TODO For circular quantities, this vector should double in size, and accomodate both cartesian coordinates
			latestTerm[i] /= averageOrder;

		// Extract oldest term value from the previous average value
		double[] newReading = new double[latestTerm.length];
		double[] lastAvgValue = getCurrentReading().getAverageableReading();
		double[] oldestAvgTerm = avgTerms.getOldestReading().getAverageableReading();
		for (int i = 0; i < lastAvgValue.length; i++)
			newReading[i] = lastAvgValue[i] - oldestAvgTerm[i];
		
		// Add the new term value to the average value
		for (int i = 0; i < latestTerm.length; i++)
			newReading[i] += latestTerm[i];

		// Instantiate the respective types of readings to insert in the buffer
		SensorReading newTermReading, newAvgReading;
		if(read instanceof AccelReading){
			newTermReading = new AccelReading(read.getTimestampString(), latestTerm);
			newAvgReading = new AccelReading(read.getTimestampString(),newReading);
		} else if (read instanceof OrientationReading){
			newTermReading = new OrientationReading(read.getTimestampString(), latestTerm);
			newAvgReading = new OrientationReading(read.getTimestampString(),newReading);
		} else
			throw new UnsupportedOperationException("Tried adding an unsupported SensorReading sub-type: " + read.getClass().getSimpleName());
		
		// Add new term value and new average value to the respective buffers
		avgTerms.addReading(newTermReading);
		super.addReading(newAvgReading);
	}

	/* (non-Javadoc)
	 * @see pt.utl.ist.thesis.util.buffers.ReadingCircularBuffer#checkType(pt.utl.ist.util.sensor.reading.SensorReading)
	 */
	@Override
	public void checkType(SensorReading read) {
		super.checkType(read);
		
		avgTerms.checkType(read);
	}
	
	
}
