package pt.utl.ist.thesis.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.thesis.util.MathUtils;
import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.SensorReading;
import pt.utl.ist.util.sensor.reading.StepReading;

public class StepReadingTest {

	private StepReading step;
	private SensorReading ar;
	private double[] values;

	@Before
	public void setUp() throws Exception {
		values = new double[]{9,9,9};
		ar = new AccelReading(123456D, values);
		step = new StepReading(ar,  1D, 0.7D);
	}

	@Test
	public void testStepReading() {
		// Check step frequency
		assertEquals(1D, step.getStepFrequency(), 0.0001);
		
		// Check step length
		assertEquals(0.7D, step.getStepLength(), 0.0001);
		
		// Check step intensity
		assertEquals(MathUtils.norm(values), step.getStepIntensity(), 0.0001);
	}

}
