package pt.utl.ist.thesis.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.thesis.sensor.reading.GPSReading;

public class GPSReadingTest {

	private GPSReading gpsr;

	@Before
	public void setUp() throws Exception {
		gpsr = new GPSReading(123456D, 38.5778D, 9.003D, 35D, 0.7D);
	}

	@Test
	public void testGPSReading() {
		double[] expected = new double[]{38.5778D, 9.003D, 35D, 0.7D};
		double[] actual = gpsr.getReading();
		
		// Test by value method
		assertArrayEquals(expected, actual, 0.0001);
		
		// Test by equals method
		assertTrue(gpsr.equals(new GPSReading(expected[0], expected[1], expected[2], expected[3])));
	}

}
