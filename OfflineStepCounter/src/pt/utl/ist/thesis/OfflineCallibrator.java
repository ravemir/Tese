package pt.utl.ist.thesis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import pt.utl.ist.thesis.signalprocessor.StepAnalyser;
import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.GPSReading;
import pt.utl.ist.util.sensor.source.RawReadingSource;
import pt.utl.ist.util.source.filters.ButterworthFilter;
import pt.utl.ist.util.source.filters.GravityFilter;

public class OfflineCallibrator {

	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\06-03-2013\\logs\\conv\\";
	//	public static final String accelLogName = "2013-03-06_18h17.log.accel";
	public static final String accelLogName = "2013-03-06_18h30.log.accel";
	public static final String locLogName = "2013-03-06_18h30.log.loc";


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Grab log file reader
		BufferedReader accelLineReader;
		BufferedReader locLineReader;
		try {
			accelLineReader = new BufferedReader(new FileReader(baseFolder + accelLogName));
			locLineReader = new BufferedReader(new FileReader(baseFolder + locLogName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		// Create buffer w/two average observers
		int sampleFreq = 100;
		int size = sampleFreq;
		RawReadingSource accelRs = new RawReadingSource(size);
		accelRs.attachFilter(new ButterworthFilter(10, 5, sampleFreq, true));
		accelRs.attachFilter(new GravityFilter());
		//rs.addMovingAverageFilter(1);
		//rs.addMovingAverageFilter(25);
		//rs.addMovingAverageFilter(50);
		
		// Create ReadingSource for GPSReadings and attach it
		RawReadingSource locRs = new RawReadingSource();

		// Create the filter analyser
		StepAnalyser fa = new StepAnalyser(sampleFreq);
		accelRs.getFilters().get(0).attachAnalyser(fa);
		//				rs.attachAnalyser(fa);

		// Loop until end of file
		String accelLine = null;
		String gpsLine = null;
		try {
			accelLine = accelLineReader.readLine();
			gpsLine = locLineReader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while(accelLine != null) {
			// Parse String values
			AccelReading accelReading = getAccelReadingFromLine(accelLine);
			GPSReading gpsReading = getGPSReadingFromLine(gpsLine);

			// Add sensor reading object to buffer
			accelRs.pushReading(accelReading);
			// Push the GPS reading
			locRs.pushReading(gpsReading);

			// Print current state
			//System.out.println("Value:\t\t" + rs.getBuffer().getCurrentReading().getAccelerationNorm());
			System.out.println("Time: " + accelReading.getTimestampString());
			System.out.println("Filtered:\t" + ((ButterworthFilter) accelRs.getFilters().get(0)).getBuffer().getCurrentReading().getAccelerationNorm());
			System.out.println("Gravity:\t" + ((GravityFilter) accelRs.getFilters().get(1)).getBuffer().getCurrentReading().getAccelerationNorm() + ", " 
					+ ((GravityFilter) accelRs.getFilters().get(1)).getBuffer().getCurrentReading());

			// Read new line
			try {
				accelLine = accelLineReader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// TODO Output relevant states
		for(AccelReading a :  fa.getNormPeaks()){	// Count peaks
			System.out.println("Peak: " + a.getTimestampString() + ", " + a.getAccelerationNorm() + ", " + a);
		}
		for(AccelReading a :  fa.getSteps()){		// Count steps
			System.out.println("Step: " + a.getTimestampString() + ", " + a.getAccelerationNorm() + ", " + a);
		}

		// Close the line reader
		try {
			accelLineReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
	
	/**
	 * @param line
	 */
	private static GPSReading getGPSReadingFromLine(String line) {
		String[] bSplit = line.split(",");
		return new GPSReading(new Double(bSplit[0]), new Double(bSplit[1]), 
				new Double(bSplit[2]), new Double(bSplit[4]), new Double(bSplit[5]));
	}
}
