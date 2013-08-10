package pt.utl.ist.thesis.source.filters;

import java.util.Observer;

import pt.utl.ist.thesis.sensor.reading.SensorReading;
import pt.utl.ist.thesis.sensor.source.ReadingSource;
import pt.utl.ist.thesis.util.buffers.ReadingCircularBuffer;

public abstract class Filter extends ReadingSource implements Observer {

	public Filter(ReadingCircularBuffer rcb){
		super(rcb);
	}

	public SensorReading getLastReading() {
		return getBuffer().getCurrentReading();
	}

	public ReadingCircularBuffer getBuffer() {
		return buffer;
	}
}