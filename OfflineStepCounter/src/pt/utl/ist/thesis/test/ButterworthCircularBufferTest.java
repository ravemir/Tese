package pt.utl.ist.thesis.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.utl.ist.thesis.util.buffers.ButterworthCircularBuffer;
import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.SensorReading;
import pt.utl.ist.util.source.filters.ButterworthData;

public class ButterworthCircularBufferTest {

	private ButterworthCircularBuffer bwcb;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		ButterworthData butterworthData = new ButterworthData(10, 5, 50, true);
		bwcb = new ButterworthCircularBuffer(10,butterworthData);
	}

	@Test
	public void testAddReading() {
		// Create reading values array FIXME Include norm values
		AccelReading[] readings = new AccelReading[]{
				new AccelReading("0", new double[]{-0.651,3.601,7.623}),
				new AccelReading("0", new double[]{-0.804,3.715,7.661}),
				new AccelReading("0", new double[]{-0.612,3.371,7.814}),
				new AccelReading("0", new double[]{-0.383,3.486,7.738}),
				new AccelReading("0", new double[]{-0.881,3.524,7.585}),
				new AccelReading("0", new double[]{-0.689, 3.524, 7.623}),
				new AccelReading("0", new double[]{-0.689, 3.447, 7.623}),
				new AccelReading("0", new double[]{-0.689, 3.486, 7.546}),
				new AccelReading("0", new double[]{-0.727, 3.486, 7.546}),
				new AccelReading("0", new double[]{-0.651, 3.447, 7.585}),
				new AccelReading("0", new double[]{-0.612, 3.486, 7.661}),
				new AccelReading("0", new double[]{-0.574, 3.447, 7.738}),
				new AccelReading("0", new double[]{-0.612, 3.447, 7.661}),
				new AccelReading("0", new double[]{-0.651, 3.486, 7.546}),
				new AccelReading("0", new double[]{-0.689, 3.486, 7.546}),
				new AccelReading("0", new double[]{-0.727, 3.486, 7.431}),
				new AccelReading("0", new double[]{-0.804, 3.601, 7.316}),
				new AccelReading("0", new double[]{-0.842, 3.639, 7.316}),
				new AccelReading("0", new double[]{-0.957, 3.601, 7.316}),
				new AccelReading("0", new double[]{-1.034, 3.562, 7.355}),
				new AccelReading("0", new double[]{-1.072, 3.562, 7.316})
		};
		
		// Create expected values array FIXME Include norm values
		AccelReading[] expected = new AccelReading[]{
				new AccelReading("0", new double[]{0, 0, 0}),
				new AccelReading("0", new double[]{0, 0, 0}),
				new AccelReading("0", new double[]{0, 0, 0}),
				new AccelReading("0", new double[]{0, 0, 0}),
				new AccelReading("0", new double[]{0, 0, 0}),
				new AccelReading("0", new double[]{0, 0, 0}),
				new AccelReading("0", new double[]{0, 0, 0}),
				new AccelReading("0", new double[]{0, 0, 0}),
				new AccelReading("0", new double[]{0, 0, 0}),
				new AccelReading("0", new double[]{0, 0, 0}),
				new AccelReading("0", new double[]{-0.001192, 0.006020, 0.013145, 0.014509}),
				new AccelReading("0", new double[]{-0.008343, 0.042057, 0.091808, 0.101341}),
				new AccelReading("0", new double[]{-0.031276, 0.157445, 0.343631, 0.379327}),
				new AccelReading("0", new double[]{-0.083023, 0.417652, 0.911431, 1.006124}),
				new AccelReading("0", new double[]{-0.174229, 0.876617, 1.912947, 2.111678}),
				new AccelReading("0", new double[]{-0.306150, 1.542423, 3.366174, 3.715738}),
				new AccelReading("0", new double[]{-0.466124, 2.355180, 5.141337, 5.674792}),
				new AccelReading("0", new double[]{-0.628626, 3.191827, 6.971257, 7.693548}),
				new AccelReading("0", new double[]{-0.762364, 3.899664, 8.523857, 9.405145}),
				new AccelReading("0", new double[]{-0.840933, 4.346363, 9.510015, 10.490522}),
				new AccelReading("0", new double[]{-0.852651, 4.466043, 9.782743, 10.788217})
		};
		
	
		// Test each value
		for (int i = 0; i < readings.length; i++) {
			bwcb.addReading(readings[i]);
			SensorReading currentReading = bwcb.getCurrentReading();
			assertArrayEquals(expected[i].getReadingAndNorm(), currentReading.getReadingAndNorm(), 0.001);
		}
	}

}
