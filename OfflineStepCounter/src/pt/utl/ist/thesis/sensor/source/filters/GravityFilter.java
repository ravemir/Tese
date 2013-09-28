package pt.utl.ist.thesis.sensor.source.filters;

import java.util.Observable;

import pt.utl.ist.thesis.sensor.reading.AccelReading;
import pt.utl.ist.thesis.sensor.reading.SensorReading;
import pt.utl.ist.thesis.util.buffers.ReadingCircularBuffer;

public class GravityFilter extends Filter {
	
	private static final double _gravityAlpha = 0.9;

	public GravityFilter() {
		this(new ReadingCircularBuffer(10));
	}
	
	public GravityFilter(ReadingCircularBuffer rcb) {
		super(rcb);
	}

	@Override
	public void update(Observable observed, Object read) {
		
		// Get acceleration
		AccelReading receivedReading = (AccelReading) read;
		AccelReading lastGravity = (AccelReading) getBuffer().getPreviousReading();
		
		// Compute gravity value
		SensorReading newGravity = new AccelReading(receivedReading.getTimestampString(),
				new double[]{_gravityAlpha*lastGravity.getAcceleration()[0]-(1-_gravityAlpha)*receivedReading.getAcceleration()[0],
			_gravityAlpha*lastGravity.getAcceleration()[1]-(1-_gravityAlpha)*receivedReading.getAcceleration()[1],
			_gravityAlpha*lastGravity.getAcceleration()[2]-(1-_gravityAlpha)*receivedReading.getAcceleration()[2]});
		
		// Add value to queue
		getBuffer().addReading(newGravity);
		
		// Notify 
		pushReading(newGravity);
	}

}
