package pt.utl.ist.thesis.sensor.source.filters;

import java.util.Observable;

import pt.utl.ist.thesis.sensor.reading.OrientationReading;
import pt.utl.ist.thesis.sensor.reading.SensorReading;
import pt.utl.ist.thesis.util.buffers.ReadingCircularBuffer;

public class UnboundedOrientationFilter extends Filter {

	private ReadingCircularBuffer inputEntries;
	
	private int[] turnCount = new int[]{0,0,0};
	private final double[] threshold = new double[]{Math.PI, Math.PI, Math.PI};
	
	public UnboundedOrientationFilter(){
		this(25);
	}
	
	public UnboundedOrientationFilter(int size){
		this(new ReadingCircularBuffer(size));
		inputEntries = new ReadingCircularBuffer(size);
	}
	
	public UnboundedOrientationFilter(ReadingCircularBuffer rcb) {
		super(rcb);
	}

	@Override
	public void update(Observable ReadingSource, Object reading) {
		if(reading instanceof OrientationReading) {
			// Cast the received reading to the appropriate type and push it
			pushReading((OrientationReading) reading);
		} else {
			throw new UnsupportedOperationException(getClass().getName() + " does not " +
					"support the reading type '" +reading.getClass().getName() + "'");
		}
	}

	/* 
	 * Pushes a SensorReading, after verifying if the
	 * bounds have been crossed and turns added.
	 */
	@Override
	public void pushReading(SensorReading read) {
		// See if any threshold has been crossed
		double[] readArray = read.getReading();
		double[] prevReadArray = inputEntries.getCurrentReading().getReading();
		for (int i = 0; i < readArray.length; i++) {
			double delta = readArray[i]-prevReadArray[i];
			// If so, add/remove one turn
			if(delta <= -threshold[i])
				turnCount[i]++;
			else if(delta >= threshold[i])
				turnCount[i]--;
		}
		
		// Create new reading with turns added
		double[] newOrientation = new double[readArray.length];
		for (int i = 0; i < readArray.length; i++)
			newOrientation[i] = readArray[i] + (turnCount[i]*(2*Math.PI));
		OrientationReading newReading = new OrientationReading(read.getTimestampString(), newOrientation);
		
		// Store the received reading and push the new one
		inputEntries.addReading(read);
		super.pushReading(newReading);
	}
}
