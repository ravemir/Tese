package pt.utl.ist.thesis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import pt.utl.ist.thesis.signalprocessor.AutoGaitModel;
import pt.utl.ist.thesis.signalprocessor.PositioningAnalyser;
import pt.utl.ist.thesis.signalprocessor.StepAnalyser;
import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.OrientationReading;
import pt.utl.ist.util.sensor.reading.RelativePositionReading;
import pt.utl.ist.util.sensor.reading.SensorReading;
import pt.utl.ist.util.sensor.reading.StepReading;
import pt.utl.ist.util.sensor.source.RawReadingSource;
import pt.utl.ist.util.source.filters.ButterworthFilter;
import pt.utl.ist.util.source.filters.MovingAverageFilter;

public class OfflinePositioning {

	//	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\06-03-2013\\logs\\conv\\";
	public static final String baseFolder = "C:\\Users\\Carlos\\Dropbox\\Tese\\Dissertacao\\Dados\\07-07-2013\\logs\\conv\\";
	public static final String accelLogName = "2013-07-07_22h00.log.accel";
	public static final String locLogName = "2013-07-07_22h00.log.ori";
	//	public static final String accelLogName = "2013-03-06_18h18.log.accel";
	//	public static final String locLogName = "2013-03-06_18h18.log.ori";
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
		accelRs.plugFilterIntoInput(new ButterworthFilter(10, 5, sampleFreq, true));

		// Create ReadingSource for OrientationReadings and attach the filters
		RawReadingSource oriRs = new RawReadingSource();
		oriRs.addUnboundedOrientationFilter(size);
		MovingAverageFilter maf = new MovingAverageFilter(sampleFreq);
		maf.plugFilterIntoInput(oriRs.getFilters().get(0));

		// Create the StepAnalyser and attach the acceleration source
		StepAnalyser sa = new StepAnalyser(sampleFreq);
		accelRs.getFilters().get(0).plugAnalyserIntoInput(sa);

		// Create an initialized AutoGaitModel
		AutoGaitModel agm = new AutoGaitModel(getSegmentSamples());

		// Create the PositioningAnalyser and attach the StepAnalyser
		PositioningAnalyser pa = new PositioningAnalyser(sampleFreq, agm);
		sa.attachToAnalyser(pa);
		oriRs.plugAnalyserIntoInput(pa);

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

		// Push SensorReading objects into their
		// respective reading sources
		for(SensorReading sr : readingQueue){
			if(sr instanceof AccelReading)
				accelRs.pushReading(sr);
			else if(sr instanceof OrientationReading)
				oriRs.pushReading(sr);
		}

		// Output relevant states
		for(AccelReading a :  sa.getNormPeaks()){	// Count peaks
			System.out.println("Peak: " + a);
		}
		for(StepReading s :  sa.getSteps()){		// Count steps
			System.out.println("Step: " + s);
		}
		for(RelativePositionReading p :  pa.getPositions()){		// Count positions
			System.out.println("Position: " + p);
		}

		// Close the line reader
		try {
			accelLineReader.close();
			locLineReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 */
	public static double[][] getSegmentSamples() {
		return new double[][]{
				new double[]{0.5, 0.4},
				new double[]{0.333333333333333, 0.5},
				new double[]{0.25, 0.6},
				new double[]{0.2, 0.7},
				new double[]{0.142857142857143, 0.9},
				new double[]{0.125, 1.0},
				new double[]{0.111111111111111, 1.1},
				new double[]{0.1, 1.2},
				new double[]{0.5, 0.4},
				new double[]{0.333333333333333, 0.5},
				new double[]{0.25, 0.6},
				new double[]{0.2, 0.7},
				new double[]{0.142857142857143, 0.9},
				new double[]{0.125, 1.0},
				new double[]{0.111111111111111, 1.1},
				new double[]{0.1, 1.2},
				new double[]{0.5, 0.4},
				new double[]{0.333333333333333, 0.5},
				new double[]{0.25, 0.6},
				new double[]{0.2, 0.7},
				new double[]{0.142857142857143, 0.9},
				new double[]{0.125, 1.0},
				new double[]{0.111111111111111, 1.1},
				new double[]{0.1, 1.2},
				new double[]{0.5, 0.4},
				new double[]{0.333333333333333, 0.5},
				new double[]{0.25, 0.6},
				new double[]{0.2, 0.7},
				new double[]{0.142857142857143, 0.9},
				new double[]{0.125, 1.0},
				new double[]{0.111111111111111, 1.1},
				new double[]{0.1, 1.2},
				new double[]{0.5, 0.4},
				new double[]{0.333333333333333, 0.5},
				new double[]{0.25, 0.6},
				new double[]{0.2, 0.7},
				new double[]{0.142857142857143, 0.9},
				new double[]{0.125, 1.0},
				new double[]{0.111111111111111, 1.1},
				new double[]{0.1, 1.2},
				new double[]{0.5, 0.4},
				new double[]{0.333333333333333, 0.5},
				new double[]{0.25, 0.6},
				new double[]{0.2, 0.7},
				new double[]{0.142857142857143, 0.9},
				new double[]{0.125, 1.0},
				new double[]{0.111111111111111, 1.1},
				new double[]{0.1, 1.2}};
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
}
