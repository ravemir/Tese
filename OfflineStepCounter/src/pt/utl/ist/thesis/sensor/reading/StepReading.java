package pt.utl.ist.thesis.sensor.reading;


public class StepReading extends AccelReading {
	private Double stepFrequency;
	private Double stepLength;

	public StepReading(AccelReading ar){
		super(ar);
	}
	
	public StepReading(AccelReading ar, Double freq, Double len){
		super(ar);
		stepFrequency = freq;
		stepLength = len;
	}
	
	/**
	 * Returns the frequency value of this step.
	 * 
	 * @return	The frequency value of this step.
	 */
	public Double getStepFrequency() {
		return stepFrequency;
	}

	/**
	 * Returns the length value of this step.
	 * 
	 * @return	The length value of this step.
	 */
	public Double getStepLength() {
		return stepLength;
	}
	
	public void setStepFrequency(Double stepF) {
		this.stepFrequency = stepF;
	}

	public void setStepLength(Double stepL) {
		this.stepLength = stepL;
	}

	/**
	 * Returns the intensity of this step, expressed
	 * by the norm of the acceleration values.
	 * 
	 * @return	The length of this step (represented by 
	 * 			the aceleration norm)
	 */
	public Double getStepIntensity(){
		return getReadingNorm();
	}
	
	/**
	 * Returns the acceleration reading's values.
	 * 
	 * @return The acceleration values of this reading.
	 */
	public double[] getAccelerationReading(){
		return getAcceleration();
	}
	
	public String toString(){
		return getTimestampString() + ", " + super.toString();
	}
}
