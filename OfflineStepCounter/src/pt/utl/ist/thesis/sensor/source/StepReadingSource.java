package pt.utl.ist.thesis.sensor.source;

import java.util.ArrayList;

import pt.utl.ist.thesis.sensor.reading.SensorReading;
import pt.utl.ist.thesis.sensor.reading.StepReading;

public class StepReadingSource extends ReadingSource {

	private static final int INVERSEDEFAULTFREQUENCY = 1 / 1;

	// Contains the last step's timestamp
	private Double previousTimestamp;
	ArrayList<StepReading> stepList = new ArrayList<StepReading>();

	public StepReadingSource() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see pt.utl.ist.thesis.sensor.source.ReadingSource#pushReading(pt.utl.ist.thesis.sensor.reading.SensorReading)
	 */
	@Override
	public void pushReading(SensorReading read) {
		if(read instanceof StepReading)
			pushReading((StepReading) read);
		else
			throw new UnsupportedOperationException("Tried to push a '" +
					read.getClass().getSimpleName() + "' reading type to this " +
					getClass().getSimpleName());
	}



	/**
	 * Pushes a {@link StepReading} to this object's
	 * observers and adding it to this {@link ReadingSource}'s list.
	 * 
	 * @param read The step reading object to add.
	 */
	public void pushReading(StepReading read){
		// See if there has been a previously recorded timestamp
		if(previousTimestamp == null) {
			// ...if not, get the default inverse frequency,
			// convert it to millis and subtract it from the current timestamp
			previousTimestamp = read.getTimestamp() - (INVERSEDEFAULTFREQUENCY*1000);
		}
		
		// Calculate the step's frequency
		StepReading step = read;
		step.setStepFrequency(
				1 / ((step.getTimestamp() - previousTimestamp)/1000));
		
		// Add the StepReading value to the internal array
		stepList.add(step);
		
		// Record the latest timestamp
		previousTimestamp = step.getTimestamp();
		
		// Push the reading to observers
		super.notifyFilters(step);
	}
	
	public ArrayList<StepReading> getStepList() {
		return stepList;
	}
}
