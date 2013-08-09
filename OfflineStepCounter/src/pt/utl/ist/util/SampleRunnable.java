package pt.utl.ist.util;

public abstract class SampleRunnable implements Runnable {

	public double[] sample;

	/**
	 * Runs the Runnable after setting the sample value
	 * @param s	The value to set on the sample.
	 */
	public void run(double[] s){
		synchronized (this) {
			sample = s;
			run();
		}
	}
}
