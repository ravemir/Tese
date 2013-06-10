package pt.utl.ist.thesis.signalprocessor;

public class AutoGaitSample {
	private final double freq;
	private final double len;
	
	public AutoGaitSample(double frequency, double length){
		freq = frequency;
		len = length;
	}

	public final double getFreq() {
		return freq;
	}

	public final double getLen() {
		return len;
	}
}
