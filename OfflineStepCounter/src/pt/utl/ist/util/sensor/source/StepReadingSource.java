package pt.utl.ist.util.sensor.source;

import java.util.ArrayList;

import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.StepReading;

public class StepReadingSource extends ReadingSource {

	private static final int INVERSEDEFAULTFREQUENCY = 1 / 1;

	// Contains the last step's timestamp
	private Double previousTimestamp;
	
	ArrayList<StepReading> stepList = new ArrayList<StepReading>();

	public StepReadingSource() {
		super();
	}

	/**
	 * Pushes a {@link StepReading} to this object's
	 * observers and adding it to this {@link ReadingSource}'s list.
	 * 
	 * @param read The step reading object to add.
	 */
	public void pushReading(AccelReading read){
		// See if there has been a previously recorded timestamp
		if(previousTimestamp == null) {
			previousTimestamp = read.getTimestamp() - INVERSEDEFAULTFREQUENCY;
		}
		
		// Calculate the step's frequency
		StepReading step = (StepReading) read;
		step.setStepFrequency(
				1 / (step.getTimestamp() - previousTimestamp));
		
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
