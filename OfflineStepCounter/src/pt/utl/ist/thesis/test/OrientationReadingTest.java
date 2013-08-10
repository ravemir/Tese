package pt.utl.ist.thesis.test;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.thesis.sensor.reading.OrientationReading;

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
		
		// Test direct reading
		assertArrayEquals(expected, actual, 0.00000001);
		
		// Test cartesian readings
		actual = or.getAverageableReading();
		expected = new double[]{
				0.45359612142D, 0.89120736006D, 
				-0.58850111725D, 0.80849640382D, 
				-0.9874797699D, -0.15774569414D
		};
		assertArrayEquals(expected, actual, 0.00000000001);
		
		// Test instantiating from catersian
		or = new OrientationReading(123456D, expected);
		expected = new double[]{1.1D,2.2D,3.3D}; 
		actual = or.getReading();
		assertArrayEquals(expected, actual, 0.00000000001);
	}

}
