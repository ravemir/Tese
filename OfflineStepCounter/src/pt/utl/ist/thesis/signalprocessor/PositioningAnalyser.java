package pt.utl.ist.thesis.signalprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import org.apache.commons.math.stat.descriptive.SynchronizedSummaryStatistics;

import pt.utl.ist.thesis.sensor.exception.AutoGaitModelUninitializedException;
import pt.utl.ist.thesis.sensor.exception.StepWithNoFrequencyException;
import pt.utl.ist.thesis.sensor.reading.GPSReading;
import pt.utl.ist.thesis.sensor.reading.OrientationReading;
import pt.utl.ist.thesis.sensor.reading.RelativePositionReading;
import pt.utl.ist.thesis.sensor.reading.SensorReading;
import pt.utl.ist.thesis.sensor.reading.StepReading;
import pt.utl.ist.thesis.sensor.source.ReadingSource;
import pt.utl.ist.thesis.sensor.source.filters.MovingAverageFilter;
import pt.utl.ist.thesis.util.MathUtils;
import pt.utl.ist.thesis.util.SensorReadingRunnable;

/**
 * Computes positioning changes from step and 
 * orientation data, combining them into a {@link RelativePositionReading}
 * and a {@link GPSReading}, computed from a base
 * absolute position.
 * 
 * @author Carlos Simões
 */

public class PositioningAnalyser extends Analyser implements Observer {

	private static final GPSReading DEFAULT_STARTING_POSITION = new GPSReading();

	// AutoGait model related attributes
	private AutoGaitModel autoGaitModel;

	// Buffers and lists
	private ArrayList<StepReading> stepBuffer = new ArrayList<StepReading>();
	private MovingAverageFilter orientationFilter;
	private SynchronizedSummaryStatistics[] stepOrientationAvg = {
			new SynchronizedSummaryStatistics(), new SynchronizedSummaryStatistics()};
	private ArrayList<RelativePositionReading> rprBuffer = new ArrayList<RelativePositionReading>();

	// State and statistical variables
	private SensorReadingRunnable readingRunnable;

	// Starting abdolute position 
	private GPSReading startingPosition; 

	/**
	 * Creates a {@link PositioningAnalyser} object with
	 * the given {@link AutoGaitModel} object for the given
	 * sample rate, considering the passed reading as the 
	 * absolute starting position.
	 * 
	 * @param rate		The sample rate to be considered.
	 * @param agm		The {@link AutoGaitModel} object to be
	 * 					used on this {@link PositioningAnalyser}.
	 * @param startPos	The absolute position to start from.
	 */
	public PositioningAnalyser(int rate, AutoGaitModel agm, GPSReading startPos){
		// Creates the orientation filter with the given rate
		orientationFilter = new MovingAverageFilter(rate);
		
		// Add first RelativePositionReading
		rprBuffer.add(new RelativePositionReading(0D, 0D, 0D));
		
		// Sets the AutoGait model and starting position 
		autoGaitModel = agm;
		startingPosition = startPos;
	}

	/**
	 * Creates a {@link PositioningAnalyser} object with
	 * the given {@link AutoGaitModel} object for the given
	 * sample rate, considering the default position as the
	 * absolute starting position.
	 * 
	 * @param rate		The sample rate to be considered.
	 * @param agm		The {@link AutoGaitModel} object to be
	 * 					used on this {@link PositioningAnalyser}.
	 */
	public PositioningAnalyser(int rate, AutoGaitModel agm){
		this(rate,agm, DEFAULT_STARTING_POSITION);
	}
	
	/**
	 * Creates a {@link PositioningAnalyser} object with
	 * considering the given sampling rate. The {@link AutoGaitModel}
	 * is not initialized, and thus, the object will not work.
	 * 
	 * @param rate	The sampling rate to be considered.
	 */
	public PositioningAnalyser(int rate){
		this(rate, null);
	}

	/**
	 * Takes a 2D array of data samples and restores the
	 * {@link AutoGaitModel} object with them.
	 * 
	 * @param samples	The samples to reinitialize the 
	 * 					{@link AutoGaitModel} with.
	 */
	public void restoreDataSamples(double[][] samples) { 
		autoGaitModel = new AutoGaitModel(samples);
	}

	@Override
	public void update(ReadingSource readingSource, SensorReading reading) {
		synchronized(this){
			initializationCheck();
			if(reading instanceof OrientationReading){
				// Add either orientation values to the average
				orientationFilter.pushReading(reading);
				stepOrientationAvg[0].addValue(Math.cos(((OrientationReading) reading).getAzimuth()));
				stepOrientationAvg[1].addValue(Math.sin(((OrientationReading) reading).getAzimuth()));
			} else if (reading instanceof StepReading){
				// Add step to list
				StepReading stepRead = (StepReading) reading;
				stepBuffer.add(stepRead);

				// If step doesn't have frequency...
				if(stepRead.getStepFrequency() == null)
					throw new StepWithNoFrequencyException(); 		// TODO Set it to the value of the difference of ts from the last step

				// Compute Step distance from frequency
				double dist = autoGaitModel.
						getLengthFromFrequency(stepRead.getStepFrequency());

				// Get average Orientation value
				double stepOriAvg = Math.atan2(stepOrientationAvg[1].getMean(), stepOrientationAvg[0].getMean());
				OrientationReading lastFilteredOri = 
						(orientationFilter.getBuffer().getCurrentReading() instanceof OrientationReading?  // FIXME Returns AccelReading, should return null orientation reading
								(OrientationReading) orientationFilter.getLastReading(): new OrientationReading());
				double[] oriValues = new double[]{(Double.isNaN(stepOriAvg) ? lastFilteredOri.getAzimuth() : stepOriAvg), 0, 0};
				OrientationReading lastOrientation = new OrientationReading(stepRead.getTimestampString(),
						oriValues);
				System.out.println(lastOrientation.getTimestampString() + ";" + stepRead.getStepIntensity() + ";" + lastOrientation.getAzimuth());

				// Compute and store the RelativePositionReading
				RelativePositionReading prev = rprBuffer.get(rprBuffer.size()-1);
				double xOff = dist*Math.sin(lastOrientation.getAzimuth());
				double yOff = dist*Math.cos(lastOrientation.getAzimuth());
				RelativePositionReading newRelPosRead = 
						new RelativePositionReading(reading.getTimestampString(), 
						prev.getXCoord()+xOff, prev.getYCoord()+yOff);
				rprBuffer.add(newRelPosRead);
				
				// Compute and store the GPSReading
				GPSReading newAbsPosRead = 
						getAbsolutePositionFromRelative(lastOrientation, newRelPosRead,
								startingPosition);

				// Output them
				pushReading(newRelPosRead);
				pushReading(newAbsPosRead);
				if(readingRunnable != null) {
					readingRunnable.run(newRelPosRead);
					readingRunnable.run(newAbsPosRead);
				}
				
				// Clear the step average to start a new average
				stepOrientationAvg[0].clear();
				stepOrientationAvg[1].clear();
			} else {
				throw new UnsupportedOperationException("Tried to update Analyser from '" +
						readingSource.getClass().getSimpleName() + "' observable type, with a '" +
						reading.getClass().getSimpleName() +"' reading type." );
			}
		}
	}

	/**
	 * Checks if this {@link PositioningAnalyser} was
	 * properly initialized.
	 */
	public void initializationCheck() {
		if(autoGaitModel == null)
			throw new AutoGaitModelUninitializedException("The AutoGait model was not initialized.");
	}

	/**
	 * Computes an absolute position from a given
	 * {@link OrientationReading}, a {@link RelativePositionReading},
	 * and {@link GPSReading}.
	 * 
	 * @param orientationReading 		The intended 
	 * 									orientation value.
	 * @param relativePositionReading	The relative position.
	 * @param baseCoord					The base coordinate.
	 * 
	 * @return	The absolute location value.
	 */
	public GPSReading getAbsolutePositionFromRelative(OrientationReading orientationReading,
			RelativePositionReading relativePositionReading, GPSReading baseCoord) {
		return new GPSReading(relativePositionReading.getTimestampString(), relativePositionReading.getXCoord()+baseCoord.getLongitude(), relativePositionReading.getYCoord()+baseCoord.getLatitude(), orientationReading.getAzimuth(), 0.0D);
	}

	public List<RelativePositionReading> getPositions(){
		return rprBuffer;
	}
	
	public void setSensorReadingUpdater(SensorReadingRunnable r){
		readingRunnable = r;
	}
}
