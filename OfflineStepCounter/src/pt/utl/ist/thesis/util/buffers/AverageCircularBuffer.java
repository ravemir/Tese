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
		double[] newAccel = new double[3];
		
		// Get the current value and compute the average term value
		double[] latestTerm = read.getReading();
		for (int i = 0; i < latestTerm.length; i++) 
			latestTerm[i] /= averageOrder;

		// Recompute average values
		// Extract last value
		double[] lastAvgValue = getCurrentReading().getReading();
		double[] lastAvgTerm = avgTerms.getOldestReading().getReading();
		for (int i = 0; i < lastAvgValue.length; i++)
			newAccel[i] = lastAvgValue[i] - lastAvgTerm[i];
		
		// Inject new value
		for (int i = 0; i < latestTerm.length; i++)
			newAccel[i] += latestTerm[i];

		// Instantiate the respective types of readings to insert in the buffer
		SensorReading newTermReading, newAvgReading;
		if(read instanceof AccelReading){
			newTermReading = new AccelReading(read.getTimestampString(), latestTerm);
			newAvgReading = new AccelReading(read.getTimestampString(),newAccel);
		} else if (read instanceof OrientationReading){
			newTermReading = new OrientationReading(read.getTimestampString(), latestTerm);
			newAvgReading = new OrientationReading(read.getTimestampString(),newAccel);
		} else
			throw new UnsupportedOperationException("Tried adding an unsupported SensorReading sub-type: " + read.getClass().getSimpleName());
		
		// Add new term value and new average value to the respective buffers
		avgTerms.addReading(newTermReading);
		super.addReading(newAvgReading);
	}
}
