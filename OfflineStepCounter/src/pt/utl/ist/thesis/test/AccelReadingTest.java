package pt.utl.ist.thesis.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.thesis.sensor.reading.AccelReading;
import pt.utl.ist.thesis.util.MathUtils;

public class AccelReadingTest {

	private AccelReading[] ars;

	@Before
	public void setUp() throws Exception {
		ars = new AccelReading[]{new AccelReading(),
				new AccelReading("1", new double[]{1,2,3}),
				new AccelReading("2", new double[]{1,2,3,4}),
				new AccelReading(3D, new double[]{1,2,3}),
				new AccelReading(4D, new double[]{1,2,3,4}),
				new AccelReading("5", new float[]{1,2,3}),
				new AccelReading("6", new float[]{1,2,3,4}),
				new AccelReading(7D, new float[]{1,2,3}),
				new AccelReading(8D, new float[]{1,2,3,4})};
	}

	@Test
	public void testGetReadingAndNorm() {
		double[] expected = new double[]{0,
				MathUtils.norm(new double[]{1,2,3}), 4,
				MathUtils.norm(new double[]{1,2,3}), 4,
				MathUtils.norm(new double[]{1,2,3}), 4,
				MathUtils.norm(new double[]{1,2,3}),4};
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], ars[i].getReadingNorm(), 0.001);
		}
	}

}
