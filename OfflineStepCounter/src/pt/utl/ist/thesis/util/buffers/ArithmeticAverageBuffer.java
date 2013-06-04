package pt.utl.ist.thesis.util.buffers;

import java.util.ArrayList;

public class ArithmeticAverageBuffer {
	private ArrayList<Double> history = new ArrayList<Double>();
	
	private Double sum = 0D;
	
	public void addValue(Double value){
		// Add to sum
		sum += value;
		
		// Compute new average
		Double avg = new Double(sum /(history.size() + 1));
		
		// Add to history
		history.add(avg);
	}
	
	public Double getHistoryValue(int index){
		return history.get(index);
	}
	
	public Double getCurrentValue(){
		return getHistoryValue(history.size() - 1);
	}
}
