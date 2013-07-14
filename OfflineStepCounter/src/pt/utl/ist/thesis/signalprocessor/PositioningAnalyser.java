package pt.utl.ist.thesis.signalprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import pt.utl.ist.thesis.util.SensorReadingRunnable;
import pt.utl.ist.util.sensor.exception.StepOutsideSegmentBoundariesException;
import pt.utl.ist.util.sensor.reading.GPSReading;
import pt.utl.ist.util.sensor.reading.OrientationReading;
import pt.utl.ist.util.sensor.reading.RelativePositionReading;
import pt.utl.ist.util.sensor.reading.SensorReading;
import pt.utl.ist.util.sensor.reading.StepReading;
import pt.utl.ist.util.sensor.source.ReadingSource;
import pt.utl.ist.util.source.filters.MovingAverageFilter;

/**
 * Computes positioning changes from 
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
	private ArrayList<RelativePositionReading> rprBuffer = new ArrayList<RelativePositionReading>();

	// State and statistical variables
	private SensorReadingRunnable readingRunnable;


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
		orientationFilter = new MovingAverageFilter(rate);
		autoGaitModel = agm;
		startingPosition = startPos;

		// Add first RelativePositionReading
		rprBuffer.add(new RelativePositionReading(0D, 0D, 0D));
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
			if(reading instanceof OrientationReading){
				// Add either all or Azimuth values to an average
				orientationFilter.pushReading(reading);
			} else if (reading instanceof StepReading){
				try {
					// Add step to list
					StepReading stepRead = (StepReading) reading;
					stepBuffer.add(stepRead);

					// If step doesn't have frequency...
					if(stepRead.getStepFrequency() == null)
						throw new StepOutsideSegmentBoundariesException(); 		// TODO Set it to the value of the difference of ts from the last step

					// Compute Step distance from frequency
					double dist = autoGaitModel.
							getLengthFromFrequency(stepRead.getStepFrequency());

					// Get average Orientation value
					OrientationReading lastOrientation = 
							(orientationFilter.getBuffer().getCurrentReading() instanceof OrientationReading?  // FIXME Returns AccelReading, should return null orientation reading
									(OrientationReading) orientationFilter.getLastReading(): new OrientationReading());

					// Compute and store the RelativePositionReading
					RelativePositionReading prev = rprBuffer.get(rprBuffer.size()-1);
					double xOff = dist*Math.sin(lastOrientation.getAzimuth());
					double yOff = dist*Math.cos(lastOrientation.getAzimuth());
					RelativePositionReading newRelPosRead = new RelativePositionReading(reading.getTimestampString(), 
							prev.getXCoord()+xOff, prev.getYCoord()+yOff);
					rprBuffer.add(newRelPosRead);

					// Output it
					if(readingRunnable != null) readingRunnable.run(newRelPosRead);
				} catch (StepOutsideSegmentBoundariesException e) {
					e.printStackTrace();
				}
			} else {
				throw new UnsupportedOperationException("Tried to update Analyser from '" +
						readingSource.getClass().getSimpleName() + "' observable type, with a '" +
						reading.getClass().getSimpleName() +"' reading type." );
			}
		}
	}

	public List<RelativePositionReading> getPositions(){
		return rprBuffer;
	}
	
	public void setSensorReadingUpdater(SensorReadingRunnable r){
		readingRunnable = r;
	}
}
