package pt.utl.ist.util.sensor;


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
	public void addReading(AccelReading read){
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

		// Add new term value to the buffer
		avgTerms.addReading(new AccelReading(read.getTimestampString(), latestTerm));
		
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
