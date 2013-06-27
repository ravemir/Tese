package pt.utl.ist.thesis.acceldir.sql;

public class AutoGaitSegmentData {

	private long id;
	private Double stepFrequency;
	private Double stepLength;
	
	public final long getId() {
		return id;
	}
	
	public final void setId(long id) {
		this.id = id;
	}
	
	public final Double getStepLength() {
		return stepLength;
	}
	
	public final void setStepLength(Double stepLength) {
		this.stepLength = stepLength;
	}
	
	public final Double getStepFrequency() {
		return stepFrequency;
	}
	
	public final void setStepFrequency(Double stepFrequency) {
		this.stepFrequency = stepFrequency;
	}

	public final Double[] getSample() {
		return new Double[]{getStepFrequency(), getStepFrequency()};
	}
}
