package pt.utl.ist.thesis.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.thesis.util.buffers.ArithmeticAverageBuffer;

public class StandardAverageBufferTest {

	private ArithmeticAverageBuffer buffer;

	@Before
	public void setUp() throws Exception {
		buffer = new ArithmeticAverageBuffer();
	}

	@Test
	public void testGetCurrentValue() {
		// Initialize individual and expected values
		Double[] values = new Double[]{1D, 2D, 3D, 4D, 5D};
		Double[] expected = new Double[]{1D, 1.5D, 2D, 2.5D, 3D};
		
		// Add each value and check the current average value
		for (int i = 0; i < values.length; i++) {
			buffer.addValue(values[i]);
			assertEquals(expected[i], buffer.getCurrentValue(), 0.00001);
		}
		
		// Check each history average value for correctness
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], buffer.getHistoryValue(i));
		}
	}

}
