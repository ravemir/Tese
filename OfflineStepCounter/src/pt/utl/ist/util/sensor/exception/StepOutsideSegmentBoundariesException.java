package pt.utl.ist.util.sensor.exception;

public class StepOutsideSegmentBoundariesException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1733566889506227952L;

	public StepOutsideSegmentBoundariesException(){
		super();
	}
	
	public StepOutsideSegmentBoundariesException(String message){
		super(message);
	}
}
