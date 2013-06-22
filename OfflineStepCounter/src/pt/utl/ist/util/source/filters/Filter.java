package pt.utl.ist.util.source.filters;

import java.util.Observer;

import pt.utl.ist.thesis.util.buffers.ReadingCircularBuffer;
import pt.utl.ist.util.sensor.source.ReadingSource;

public abstract class Filter extends ReadingSource implements Observer {

	public Filter(ReadingCircularBuffer rcb){
		super(rcb);
	}
	
	public abstract ReadingCircularBuffer getBuffer();
}