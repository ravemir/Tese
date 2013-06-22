package pt.utl.ist.thesis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import pt.utl.ist.thesis.signalprocessor.AutoGaitModelerAnalyser;
import pt.utl.ist.thesis.signalprocessor.StepAnalyser;
import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.GPSReading;
import pt.utl.ist.util.sensor.reading.SensorReading;
import pt.utl.ist.util.sensor.source.RawReadingSource;
import pt.utl.ist.util.source.filters.ButterworthFilter;

public class OfflineCallibrator {

	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\06-03-2013\\logs\\conv\\";
	public static final String accelLogName = "2013-03-06_18h18.log.accel";
	public static final String locLogName = "2013-03-06_18h18.log.loc";
//	public static final String accelLogName = "2013-03-06_18h30.log.accel";
//	public static final String locLogName = "2013-03-06_18h30.log.loc";


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
		//accelRs.attachFilter(new GravityFilter());
		//rs.addMovingAverageFilter(1);
		//rs.addMovingAverageFilter(25);
		//rs.addMovingAverageFilter(50);
		
		// Create ReadingSource for GPSReadings and attach it
		RawReadingSource locRs = new RawReadingSource();

		// Create the StepAnalyser
		StepAnalyser sa = new StepAnalyser(sampleFreq);
		accelRs.getFilters().get(0).attachAnalyser(sa);
		
		// Create the AutoGaitModelerAnalyser and attach the StepAnalyser
		AutoGaitModelerAnalyser agma = new AutoGaitModelerAnalyser();
		sa.attachAnalyser(agma);

		// Get the first lines
		String accelLine = null; String gpsLine = null;
		try {
			accelLine = accelLineReader.readLine();
			gpsLine = locLineReader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Read all the readings in the file
		Queue<AccelReading> accels = new LinkedList<AccelReading>();
		Queue<GPSReading> locs = new LinkedList<GPSReading>();
		while(accelLine != null) {
			// Parse String values and add them to the lists
			accels.add(getAccelReadingFromLine(accelLine));
			if(gpsLine != null) locs.add(getGPSReadingFromLine(gpsLine));

			// Read new line
			try {
				accelLine = accelLineReader.readLine();
				gpsLine = locLineReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// TODO	Loop both reading queues until either 
		//		is empty
		Queue<SensorReading> readingQueue = new LinkedList<SensorReading>();
		while(!accels.isEmpty() && !locs.isEmpty()){
			// Check both lists for the reading 
			// with the lowest TS
			SensorReading reading;
			if(accels.peek().getTimestamp() < 
					locs.peek().getTimestamp())
				reading = accels.remove();
			else
				reading = locs.remove();
			
			// Insert the one with the lowest TS into result
			readingQueue.add(reading);
		}
			
		// Empty both queues
		for(SensorReading sr : accels) readingQueue.add(sr);
		for(SensorReading sr : locs) readingQueue.add(sr);
		
		// Push SensorReading objects into their
		// respective reading sources
		for(SensorReading sr : readingQueue){
			if(sr instanceof AccelReading)
				accelRs.pushReading(sr);
			else if(sr instanceof GPSReading)
				locRs.pushReading(sr);
		}
		
		// TODO Output relevant states
		for(AccelReading a :  sa.getNormPeaks()){	// Count peaks
			System.out.println("Peak: " + a.getTimestampString() + ", " + a.getAccelerationNorm() + ", " + a);
		}
		for(AccelReading a :  sa.getSteps()){		// Count steps
			System.out.println("Step: " + a.getTimestampString() + ", " + a.getAccelerationNorm() + ", " + a);
		}

		// Close the line reader
		try {
			accelLineReader.close();
			locLineReader.close();
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
