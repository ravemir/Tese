package pt.utl.ist.thesis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import pt.utl.ist.thesis.sensor.reading.AccelReading;
import pt.utl.ist.thesis.sensor.reading.OrientationReading;
import pt.utl.ist.thesis.sensor.reading.RelativePositionReading;
import pt.utl.ist.thesis.sensor.reading.SensorReading;
import pt.utl.ist.thesis.sensor.reading.StepReading;
import pt.utl.ist.thesis.sensor.source.RawReadingSource;
import pt.utl.ist.thesis.signalprocessor.AutoGaitModel;
import pt.utl.ist.thesis.signalprocessor.PositioningAnalyser;
import pt.utl.ist.thesis.signalprocessor.StepAnalyser;
import pt.utl.ist.thesis.source.filters.ButterworthFilter;
import pt.utl.ist.thesis.util.PushThread;
import pt.utl.ist.thesis.util.SensorReadingRunnable;

public class OfflinePositioning {

	//	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\06-03-2013\\logs\\conv\\";
//	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\07-07-2013\\logs\\conv\\";
//	public static final String accelLogName = "2013-07-07_22h00.log.accel";
//	public static final String oriLogName = "2013-07-07_22h00.log.ori";
//	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\21-07-2013\\logs\\conv\\";
//	public static final String accelLogName = "2013-07-21_16h54-sprint.log.accel";
//	public static final String oriLogName = "2013-07-21_16h54-sprint.log.ori";
	//	public static final String accelLogName = "2013-03-06_18h18.log.accel";
	//	public static final String oriLogName = "2013-03-06_18h18.log.ori";
	//	public static final String accelLogName = "2013-03-06_18h30.log.accel";
	//	public static final String oriLogName = "2013-03-06_18h30.log.loc";
	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\05-08-2013\\logs\\conv\\";
	public static final String baseFilename = "2013-08-05_10h06.log";
//	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\10-08-2013\\logs\\conv\\";
//	public static final String baseFilename = "2013-08-10_16h27.log";
		
	public static final String oriLogName = baseFilename + ".ori";
	public static final String accelLogName = baseFilename + ".accel";
	
	private static RawReadingSource accelRs;
	private static RawReadingSource oriRs;
	private static PushThread accelPushThread;
	private static PushThread oriPushThread;


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
			locLineReader = new BufferedReader(new FileReader(baseFolder + oriLogName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		// Create buffer w/two average observers
		int sampleFreq = 50;
		int size = sampleFreq;
		accelRs = new RawReadingSource(size);
		ButterworthFilter bwF = new ButterworthFilter(10, 5, sampleFreq, true);
		accelRs.plugFilterIntoOutput(bwF);

		oriRs = new RawReadingSource();
		oriRs.addUnboundedOrientationFilter(size);
//		MovingAverageFilter maf = new MovingAverageFilter(sampleFreq);
//		maf.plugFilterIntoOutput(oriRs.getFilters().get(0));

		// Create the StepAnalyser and attach the acceleration source
		StepAnalyser sa = new StepAnalyser(sampleFreq);
		bwF.plugAnalyserIntoOutput(sa);

		// Create an initialized AutoGaitModel
		AutoGaitModel agm = new AutoGaitModel(getSegmentSamples());
		double[][] forged = forgeAGValues();
		if(forged.length != 0) agm = new AutoGaitModel(forged);

		// Create the PositioningAnalyser and attach the StepAnalyser
		PositioningAnalyser pa = new PositioningAnalyser(sampleFreq, agm);
		sa.plugAnalyserIntoOutput(pa);
		oriRs.getFilters().get(0).plugAnalyserIntoOutput(pa);

		// Get the first lines
		String accelLine = null; String oriLine = null;
		try {
			accelLine = accelLineReader.readLine();
			oriLine = locLineReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Read all the readings in the file
		Queue<AccelReading> accels = new LinkedList<AccelReading>();
		Queue<OrientationReading> orients = new LinkedList<OrientationReading>();
		while(accelLine != null) {
			// Parse String values and add them to the lists
			accels.add(getAccelReadingFromLine(accelLine));
			if(oriLine != null) orients.add(getOrientationReadingFromLine(oriLine));

			// Read new line
			try {
				accelLine = accelLineReader.readLine();
				oriLine = locLineReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Loop both reading queues until either is empty
		Queue<SensorReading> readingQueue = new LinkedList<SensorReading>();
		while(!accels.isEmpty() && !orients.isEmpty()){
			// Check both lists for the reading 
			// with the lowest TS
			SensorReading reading;
			if(accels.peek().getTimestamp() < 
					orients.peek().getTimestamp())
				reading = accels.remove();
			else
				reading = orients.remove();

			// Insert the one with the lowest TS into result
			readingQueue.add(reading);
		}

		// Empty both queues for remaining readings
		for(SensorReading sr : accels) readingQueue.add(sr);
		for(SensorReading sr : orients) readingQueue.add(sr);

		// Display positioning event
		pa.setSensorReadingUpdater(new SensorReadingRunnable() {
			@Override
			public void run() {
				String s = "New Position event: ";
				for(double d : reading.getReading())
					s += d + ",";
				s += "\n";
				System.out.println(s);
			}
		});
		
		// Push SensorReading objects into their
		// respective reading sources
		for(SensorReading sr : readingQueue){
			if(sr instanceof AccelReading) {
				accelPushThread = new PushThread(sr){
					public void run(){
						accelRs.pushReading(reading);
					}
				};
				accelPushThread.run();
			} else if(sr instanceof OrientationReading) {
				oriPushThread = new PushThread(sr){
					public void run(){
						oriRs.pushReading(reading);
					}
				};
				oriPushThread.run();
			}
		}

		// Output relevant states
//		for(AccelReading a :  sa.getNormPeaks()){	// Count peaks
//			System.out.println("Peak: " + a);
//		}
		double stepDistance = 0; 
		for(StepReading s :  sa.getSteps()){		// Count steps
			System.out.println("Step: " + s);
			stepDistance += agm.getLengthFromFrequency(s.getStepFrequency());
		}
		
		List<RelativePositionReading> positions = pa.getPositions();
		double distance = 0;
		for (int i = 1; i < positions.size(); i++) 
			distance += Math.sqrt(Math.pow(positions.get(i).getXCoord()-positions.get(i-1).getXCoord(), 2) +
					Math.pow(positions.get(i).getYCoord()-positions.get(i-1).getYCoord(), 2));
		
		for(RelativePositionReading p :  positions){		// Count positions
			System.out.println("Position: " + p);
		}
		
		System.out.println("No. of positions: " + sa.getSteps().size());
		System.out.println("Distance covered from steps: " + stepDistance);
		System.out.println("Distance covered from positions: " + distance);

		// Close the line reader
		try {
			accelLineReader.close();
			locLineReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		long endTime = System.nanoTime();
		System.out.println("Took "+(endTime - startTime)/1000000 + " ms");
	}

	/**
	 * @return
	 */
	public static double[][] getSegmentSamples() {
//		return new double[][]{
//				new double[]{0.5, 0.4},
//				new double[]{0.333333333333333, 0.5},
//				new double[]{0.25, 0.6},
//				new double[]{0.2, 0.7},
//				new double[]{0.142857142857143, 0.9},
//				new double[]{0.125, 1.0},
//				new double[]{0.111111111111111, 1.1},
//				new double[]{0.1, 1.2},
//				new double[]{0.5, 0.4},
//				new double[]{0.333333333333333, 0.5},
//				new double[]{0.25, 0.6},
//				new double[]{0.2, 0.7},
//				new double[]{0.142857142857143, 0.9},
//				new double[]{0.125, 1.0},
//				new double[]{0.111111111111111, 1.1},
//				new double[]{0.1, 1.2},
//				new double[]{0.5, 0.4},
//				new double[]{0.333333333333333, 0.5},
//				new double[]{0.25, 0.6},
//				new double[]{0.2, 0.7},
//				new double[]{0.142857142857143, 0.9},
//				new double[]{0.125, 1.0},
//				new double[]{0.111111111111111, 1.1},
//				new double[]{0.1, 1.2},
//				new double[]{0.5, 0.4},
//				new double[]{0.333333333333333, 0.5},
//				new double[]{0.25, 0.6},
//				new double[]{0.2, 0.7},
//				new double[]{0.142857142857143, 0.9},
//				new double[]{0.125, 1.0},
//				new double[]{0.111111111111111, 1.1},
//				new double[]{0.1, 1.2},
//				new double[]{0.5, 0.4},
//				new double[]{0.333333333333333, 0.5},
//				new double[]{0.25, 0.6},
//				new double[]{0.2, 0.7},
//				new double[]{0.142857142857143, 0.9},
//				new double[]{0.125, 1.0},
//				new double[]{0.111111111111111, 1.1},
//				new double[]{0.1, 1.2},
//				new double[]{0.5, 0.4},
//				new double[]{0.333333333333333, 0.5},
//				new double[]{0.25, 0.6},
//				new double[]{0.2, 0.7},
//				new double[]{0.142857142857143, 0.9},
//				new double[]{0.125, 1.0},
//				new double[]{0.111111111111111, 1.1},
//				new double[]{0.1, 1.2}};
		return new double[][] {
				new double[]{0.51, 0.439},
				new double[]{0.65, 0.5179},
				new double[]{0.78, 0.5807},
				new double[]{0.91, 0.6255},
				new double[]{1, 0.6837},
				new double[]{1.07, 0.7417},
				new double[]{1.14, 0.8},
				new double[]{1.22, 0.8393}};
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
	private static OrientationReading getOrientationReadingFromLine(String line) {
		String[] bSplit = line.split(",");
		return new OrientationReading(new Double(bSplit[0]), new Double(bSplit[1]), 
				new Double(bSplit[2]), new Double(bSplit[3]));
	}
	
	private static double[][] forgeAGValues(){
//		double alpha = 0.453; // Participant A
//		double beta = 0.23;
		double alpha = 0.064; // Participant B
		double beta = 0.612;
//		double alpha = 0.539; // Participant C
//		double beta = 0.2156;
		
		double[][] generatedForge = new double[100][2];
		for (int i = 0; i < generatedForge.length; i++) 
			generatedForge[i] = new double[]{i, alpha*i+beta};
		
		return ((alpha != 0 && beta != 0) ? generatedForge : 
			new double[][]{});
	}
}
