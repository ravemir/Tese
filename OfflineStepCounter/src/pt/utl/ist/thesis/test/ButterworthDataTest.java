package pt.utl.ist.thesis.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import pt.utl.ist.thesis.source.filters.ButterworthData;

public class ButterworthDataTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testButterworthDataIntDoubleDoubleBoolean() {
		// Create filter object
		ButterworthData butterworthFilter = 
				new ButterworthData(10, 10, 50, true);
		
		// Create expected results
		double[] expectedA = new double[]{1, -1.992401482, 3.019482863, 
				-2.818522426, 2.038720637, -1.054544621,
				0.414446269, -0.115718625, 0.022498509,
				-0.002668912,0.000148764452177762};
		double[] expectedB = new double[]{0.0004994540782331, 0.004994541,
				0.022475434, 0.059934489,0.104885356,
				0.125862428, 0.104885356,0.059934489,
				0.022475434,0.004994541, 0.0004994540782331};
		
		// Test the cases
		assertArrayEquals(expectedA, butterworthFilter.getA(), 0.000000001);
		assertArrayEquals(expectedB, butterworthFilter.getB(), 0.000000001);
	}

	@Test
	@Ignore
	public void testButterworthDataIntDoubleDoubleDoubleBoolean() {
		fail("Not yet implemented");
	}

}
