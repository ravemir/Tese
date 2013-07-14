package pt.utl.ist.thesis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import pt.utl.ist.thesis.signalprocessor.StepAnalyser;
import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.source.RawReadingSource;
import pt.utl.ist.util.source.filters.ButterworthFilter;
import pt.utl.ist.util.source.filters.Filter;
import pt.utl.ist.util.source.filters.GravityFilter;

public class OfflineStepCounter {

	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\06-03-2013\\logs\\conv\\";
//	public static final String accelLogName = "2013-03-06_18h17.log.accel";
	public static final String accelLogName = "2013-03-06_18h30.log.accel";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Grab log file reader
		BufferedReader lineReader;
		try {
			lineReader = new BufferedReader(new FileReader(baseFolder + accelLogName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		// Create buffer w/two average observers
		int size = 50;
		RawReadingSource rs = new RawReadingSource(size);		
		rs.attachFilter(new ButterworthFilter(10, 5, 50, true));
		rs.attachFilter(new GravityFilter());
		//rs.addMovingAverageFilter(1);
		//rs.addMovingAverageFilter(25);
		//rs.addMovingAverageFilter(50);
		
		// Create the filter analyser
		StepAnalyser fa = new StepAnalyser(50);
		rs.getFilters().get(0).attachAnalyser(fa);
//		rs.attachAnalyser(fa);
		
		// Loop until end of file
		String line = null;
		try {
			line = lineReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(line != null) {
			// Parse String values
			AccelReading reading = getAccelReadingFromLine(line);
			
			// Add sensor reading object to buffer
			rs.pushReading(reading);
			
			// Compute averages, etc. (done in the background)
			
			// Print current state
			//System.out.println("Value:\t\t" + rs.getBuffer().getCurrentReading().getAccelerationNorm());
			System.out.println("Time: " + reading.getTimestampString());
			System.out.println("Filtered:\t" + ((AccelReading) ((Filter) rs.getFilters().get(0)).getBuffer().getCurrentReading()).getReadingNorm());
			System.out.println("Gravity:\t" + ((AccelReading) ((GravityFilter) rs.getFilters().get(1)).getBuffer().getCurrentReading()).getReadingNorm() + ", " 
								+ ((GravityFilter) rs.getFilters().get(1)).getBuffer().getCurrentReading());

			// Read new line
			try {
				line = lineReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Output relevant states
		for(AccelReading a :  fa.getNormPeaks()){
			System.out.println("Peak: " + a.getTimestampString() + ", " + a.getReadingNorm() + ", " + a);
		}
		for(AccelReading a :  fa.getSteps()){
			System.out.println("Step: " + a.getTimestampString() + ", " + a.getReadingNorm() + ", " + a);
		}
		
		// Close the line reader
		try {
			lineReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param line
	 */
	private static AccelReading getAccelReadingFromLine(String line) {
		String[] bSplit = line.split(",");
		float[] accel = {Float.parseFloat(bSplit[1]),
				Float.parseFloat(bSplit[2]),
				Float.parseFloat(bSplit[3])};
		 return new AccelReading(bSplit[0], accel);
	}

}
