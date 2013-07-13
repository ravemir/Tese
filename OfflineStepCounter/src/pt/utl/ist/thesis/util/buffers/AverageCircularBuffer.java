package pt.utl.ist.thesis.util.buffers;

import pt.utl.ist.util.sensor.reading.AccelReading;
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
	public void addReading(SensorReading read){		// TODO Change to receive SensorReading
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

		// Add new term value of the respective type of reading to the buffer
		if(read instanceof AccelReading){
			avgTerms.addReading(new AccelReading(read.getTimestampString(), latestTerm));
		}
		// TODO Uncomment when read is SensorReading
//		else if (read instanceof OrientationReading){
//			avgTerms.addReading(new OrientationReading(read.getTimestampString(), latestTerm));
//		}
		
		
		// Add new average value to the main buffer
		super.addReading(new AccelReading(read.getTimestampString(),newAccel));
	}

	
	// TODO Verify this: isWarm is a check to be done by the users of the code, not by the class itself
//	@Override
//	public AccelReading getCurrentReading() {
//		// Only return the current value if the buffer has filled
//		return (isWarm() ? super.getCurrentReading() : new AccelReading());
//	}

}
