package pt.utl.ist.thesis.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.util.sensor.reading.OrientationReading;

public class OrientationReadingTest {

	private OrientationReading or;

	@Before
	public void setUp() throws Exception {
		or = new OrientationReading(123456D, 1.1D, 2.2D, 3.3D);
	}

	@Test
	public void testOrientationReading() {
		Double[] expected = new Double[]{1.1D, 2.2D, 3.3D};
		Double[] actual = or.getReading();
		
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual[i], 0.0001);
		}
	}

}
