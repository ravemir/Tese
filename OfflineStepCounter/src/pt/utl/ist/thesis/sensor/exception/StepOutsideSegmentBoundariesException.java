package pt.utl.ist.thesis.sensor.exception;

public class StepOutsideSegmentBoundariesException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1733566889506227952L;
	
	public final boolean isAfterSegment;

	public StepOutsideSegmentBoundariesException(){
		super();
		isAfterSegment = false;
	}
	
	public StepOutsideSegmentBoundariesException(String message, boolean afterSegment){
		super(message);
		isAfterSegment = afterSegment;
	}
}
