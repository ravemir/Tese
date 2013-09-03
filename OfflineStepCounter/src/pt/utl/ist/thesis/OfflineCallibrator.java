package pt.utl.ist.thesis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import pt.utl.ist.thesis.sensor.reading.AccelReading;
import pt.utl.ist.thesis.sensor.reading.GPSReading;
import pt.utl.ist.thesis.sensor.reading.SensorReading;
import pt.utl.ist.thesis.sensor.source.RawReadingSource;
import pt.utl.ist.thesis.sensor.source.ReadingSource;
import pt.utl.ist.thesis.signalprocessor.AutoGaitModelerAnalyser;
import pt.utl.ist.thesis.signalprocessor.StepAnalyser;
import pt.utl.ist.thesis.source.filters.ButterworthFilter;
import pt.utl.ist.thesis.util.PushThread;
import pt.utl.ist.thesis.util.SampleRunnable;

public class OfflineCallibrator {

//	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\06-03-2013\\logs\\conv\\";
//	public static final String accelLogName = "2013-03-06_18h18.log.accel";
//	public static final String locLogName = "2013-03-06_18h18.log.loc";
//	public static final String accelLogName = "2013-03-06_18h30.log.accel";
//	public static final String locLogName = "2013-03-06_18h30.log.loc";
//	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\05-08-2013\\logs\\conv\\";
//	public static final String baseFilename = "2013-08-05_10h06.log";
//	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\10-08-2013\\logs\\conv\\";
//	public static final String baseFilename = "2013-08-10_16h27.log";
	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\16-08-2013\\logs\\conv\\";
	public static final String baseFilename = "2013-08-16_12h42.log";
	
//	public static final String oriLogName = baseFilename + ".ori";
	public static final String accelLogName = baseFilename + ".accel";
	public static final String locLogName = baseFilename + ".loc";
	
	private static ReadingSource locRs;
	private static RawReadingSource accelRs;
	private static PushThread accelPushThread;
	private static PushThread locPushThread;
	private static String print = "";


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long startTime = System.nanoTime();
		
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
		int sampleFreq = 71;
		int size = sampleFreq;
		accelRs = new RawReadingSource(size);
		accelRs.plugFilterIntoOutput(new ButterworthFilter(10, 5, sampleFreq, true));
		locRs = new RawReadingSource();

		// Create the StepAnalyser
		StepAnalyser sa = new StepAnalyser(sampleFreq);
		accelRs.getFilters().get(0).plugAnalyserIntoOutput(sa);
		
		// Create the AutoGaitModelerAnalyser and attach the StepAnalyser
		boolean doSmooth = true;
		AutoGaitModelerAnalyser agma = new AutoGaitModelerAnalyser(doSmooth);
		sa.plugAnalyserIntoOutput(agma);
		locRs.plugAnalyserIntoOutput(agma);
		
		agma.setSampleUpdater(new SampleRunnable() {
			@Override
			public void run() {
				print += sample[0] + ", " + sample[1] + "\n";
			}
		});

		// Get the first lines
		String accelLine = null; String gpsLine = null;
		try {
			accelLine = accelLineReader.readLine();
			gpsLine = locLineReader.readLine();
		} catch (IOException e) {
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
		
		// Loop both reading queues until either is empty
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
			try{
			if(sr instanceof AccelReading) {
				accelPushThread = new PushThread(sr){
					public void run(){
						accelRs.pushReading(reading);
					}
				};
				accelPushThread.run();
			} else if(sr instanceof GPSReading) {
				locPushThread = new PushThread(sr){
					public void run(){
						locRs.pushReading(reading);
					}
				};
				locPushThread.run();
			}
			} catch(Exception e){}
		}
		
		// Output relevant states
//		for(AccelReading a :  sa.getNormPeaks()){	// Count peaks
//			System.out.println("Peak: " + a);
//		}
//		for(StepReading s :  sa.getSteps()){		// Count steps
//			System.out.println("Step: " + s);
//		}

		// Close the line reader
		try {
			accelLineReader.close();
			locLineReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

//		List<GPSSegment> l1 = agma.getSegments();
//		List<StepReading> l2 = sa.getSteps();
//		int j = 0;
//		for (int i = 0; i < l1.size(); i++) {
//			GPSSegment s = l1.get(i);
//			GPSReading gpsReadingStart = s.get(0);
//			GPSReading gpsReadingEnd = s.get(s.size()-1);
////			System.out.println("Segment: " + gpsReadingStart + " to " + gpsReadingEnd + "(" + s.getDistance() + "m)");
//			
//			for (; j < l2.size(); j++) {
//				StepReading r = l2.get(j);
//				if(r.getTimestamp() <= gpsReadingEnd.getTimestamp())
//					System.out.println("Step: " + r.getTimestampString());
//				else break;
//			}
//		}
		
		System.out.println(print);
		System.out.println("Alpha: " + agma.getAGCoefficients()[0] + "; Beta: " + agma.getAGCoefficients()[1]);
		
		long endTime = System.nanoTime();
		System.out.println("\nTook "+(endTime - startTime)/1000000 + " ms");
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
		return new GPSReading(bSplit[0], new Double(bSplit[1]), 
				new Double(bSplit[2]), new Double(bSplit[4]), new Double(bSplit[5]));
	}
}
