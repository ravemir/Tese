/**
 * 
 */
package pt.utl.ist.thesis.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.thesis.util.buffers.AverageCircularBuffer;
import pt.utl.ist.util.sensor.reading.AccelReading;

/**
 * 
 * @author Carlos
 */
public class AverageCircularBufferTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link pt.utl.ist.thesis.util.buffers.AverageCircularBuffer#AverageCircularBuffer(int)}.
	 */
	@Test
	public void testAverageCircularBuffer() {
		AverageCircularBuffer acb = new AverageCircularBuffer(25);
		assertNotNull(acb);
	}

	/**
	 * Test method for {@link pt.utl.ist.thesis.util.buffers.AverageCircularBuffer#addReading(pt.utl.ist.util.sensor.reading.AccelReading)}.
	 */
	@Test
	public void testUpdateAverage() {
		// Create buffer with size 2
		AverageCircularBuffer acb = new AverageCircularBuffer(2);
		
		// Add 2 values
		acb.addReading(new AccelReading("0", new double[]{0.0d,0.0d,0.0d}));
		acb.addReading(new AccelReading("0", new double[]{1.0d,1.0d,1.0d}));
		
		// Assert equal to average
		double[] currentAcceleration = acb.getCurrentReading().getAcceleration();
		assertArrayEquals(new double[]{0.5d, 0.5d, 0.5d}, currentAcceleration, 0);
		
		// Add 1 value
		acb.addReading(new AccelReading("0", new double[]{2.0d,2.0d,2.0d}));
		
		// Assert equal to average
		currentAcceleration = acb.getCurrentReading().getAcceleration();
		assertArrayEquals(new double[]{1.5d, 1.5d, 1.5d}, currentAcceleration, 0);
		
		// Add 1 value
		acb.addReading(new AccelReading("0", new double[]{4.0d,4.0d,4.0d}));
		
		// Assert equal to average
		currentAcceleration = acb.getCurrentReading().getAcceleration();
		assertArrayEquals(new double[]{3.0d, 3.0d, 3.0d}, currentAcceleration, 0);
	}

}
