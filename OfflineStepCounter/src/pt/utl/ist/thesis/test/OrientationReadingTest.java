package pt.utl.ist.thesis.test;

import static org.junit.Assert.assertArrayEquals;

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
		double[] expected = new double[]{1.1D, 2.2D, 3.3D};
		double[] actual = or.getReading();
		
		assertArrayEquals(expected, actual, 0.0001);
	}

}
