package pt.utl.ist.thesis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import pt.utl.ist.thesis.sensor.reading.AccelReading;
import pt.utl.ist.thesis.sensor.source.RawReadingSource;
import pt.utl.ist.thesis.sensor.source.filters.ButterworthFilter;
import pt.utl.ist.thesis.signalprocessor.StepAnalyser;
import pt.utl.ist.thesis.util.PushThread;
import pt.utl.ist.thesis.util.SensorReadingRunnable;

public class OfflineStepCounter {
//	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\06-03-2013\\logs\\conv\\";
//	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\05-08-2013\\logs\\conv\\";
//	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\10-08-2013\\logs\\conv\\";
//	public static final String accelLogName = "2013-03-06_18h17.log.accel";
//	public static final String accelLogName = "2013-08-10_16h27.log.accel";
//	public static final String accelLogName = "2013-08-05_10h11.log.accel";
//	public static final String accelLogName = "2013-08-05_10h05.log.accel";
	
//	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\16-08-2013\\logs\\conv\\";
//	public static final String baseFilename = "2013-08-16_13h26.log";
	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\24-08-2013\\logs\\conv\\";
	public static final String baseFilename = "2013-08-24_12h56.log";
	public static final String accelLogName = baseFilename + ".accel";
	private static RawReadingSource rs;
	private static Object fileLock = new Object();
	private static PushThread accelPushThread;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long startTime = System.nanoTime();
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
		int sampleRate = 71;
		rs = new RawReadingSource(size);	
		ButterworthFilter bwf = new ButterworthFilter(10, 5, sampleRate, true);
		rs.plugFilterIntoOutput(bwf);
		//rs.addMovingAverageFilter(1);
		//rs.addMovingAverageFilter(25);
		//rs.addMovingAverageFilter(50);
		
		// Create the filter analyser
		StepAnalyser sa = new StepAnalyser(50);
		rs.getFilters().get(0).plugAnalyserIntoOutput(sa);
//		rs.attachAnalyser(fa);
		
		
		bwf.setRunnable(new SensorReadingRunnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
//				System.out.print(reading.getTimestampString() + "/");
			}
		});
		
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
			
			accelPushThread = new PushThread(reading){
				public void run(){
					rs.pushReading(this.reading);
				}
			};
			accelPushThread.run();
			
			// Compute averages, etc. (done in the background)
			
			// Print current state
			//System.out.println("Value:\t\t" + rs.getBuffer().getCurrentReading().getAccelerationNorm());
//			System.out.println("Time: " + reading.getTimestampString());
//			System.out.println("Filtered:\t" + ((AccelReading) ((Filter) rs.getFilters().get(0)).getBuffer().getCurrentReading()).getReadingNorm());
//			System.out.println("Gravity:\t" + ((AccelReading) ((GravityFilter) rs.getFilters().get(1)).getBuffer().getCurrentReading()).getReadingNorm() + ", " 
//								+ ((GravityFilter) rs.getFilters().get(1)).getBuffer().getCurrentReading());

			// Read new line
			try {
				line = lineReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Output relevant states
//		int i = 0;
//		for(AccelReading a :  sa.getNormPeaks()){
//			System.out.print(a.getTimestampString() + "," + a.getReadingNorm() + ";");
//			if (i==3) System.out.print("\n");
//			i = (i+1)%4;
//		}
//		for(AccelReading a :  sa.getSteps()){
//			System.out.print(a.getTimestampString() + ", " + a.getReadingNorm() + "; ");
//			if (i==3) System.out.print("\n");
//			i = (i+1)%4;
//		}
		
		// Close the line reader
		try {
			lineReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("\nCounted " + sa.getSteps().size() + " steps");
		
		long endTime = System.nanoTime();
		System.out.println("Took "+(endTime - startTime)/1000000 + " ms");
	}

	/**
	 * @param line
	 */
	private static AccelReading getAccelReadingFromLine(String line) {
		synchronized (fileLock) {
			String[] bSplit = line.split(",");
			float[] accel = {Float.parseFloat(bSplit[1]),
					Float.parseFloat(bSplit[2]),
					Float.parseFloat(bSplit[3])};
			 return new AccelReading(bSplit[0], accel);
		}
	}

}
