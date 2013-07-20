package pt.utl.ist.thesis.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import pt.utl.ist.util.sensor.reading.AccelReading;
import pt.utl.ist.util.sensor.reading.SensorReading;
import pt.utl.ist.util.sensor.source.RawReadingSource;
import pt.utl.ist.util.source.filters.Filter;
import pt.utl.ist.util.source.filters.MovingAverageFilter;

public class MovingAverageFilterTest {

	private RawReadingSource rrs;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		rrs = new RawReadingSource(50);
	}

	@Test
	@Ignore
	public void testGetBuffer() {
		fail("Not yet implemented");
	}

	@Test
	public void testMovingAverageFilter() {
		Filter maf = new MovingAverageFilter(25);
		assertNotNull(maf);
	}

	@Test
	public void testGetAverageOrder() {
		int averageSamples = 25;
		MovingAverageFilter maf = new MovingAverageFilter(averageSamples);
		assertNotNull(maf);
		
		int actualAverageSamples = maf.getAverageOrder();
		assertEquals(averageSamples, actualAverageSamples);
	}
	
	@Test
	public void testUpdate() {
		// Add the filter to the source
		rrs.addMovingAverageFilter(25);
		
		// Create readings and expected results
		Filter maf = (Filter) rrs.getFilters().get(0);
		SensorReading[] inputReadings = {new AccelReading("0", new double[]{-0.804,5.324,6.244}),
				new AccelReading("1", new double[]{-1.11,5.631,6.282}),
				new AccelReading("2", new double[]{-0.804,5.478,5.822}),
				new AccelReading("3", new double[]{0.153,4.367,3.447})};
		SensorReading[] expectedReadings = {new AccelReading("0", new double[]{-0.03216,0.21296,0.24976}),
				new AccelReading("1", new double[]{-0.07656,0.4382,0.50104}),
				new AccelReading("2", new double[]{-0.10872,0.65732,0.73392}),
				new AccelReading("3", new double[]{-0.1026,0.832,0.8718})};
		
		// Match all expected results to actual ones
		for (int i = 0; i < inputReadings.length; i++) {
			rrs.pushReading(inputReadings[i]);
			SensorReading currentReading = maf.getBuffer().getCurrentReading();
			assertArrayEquals(expectedReadings[i].getReading(), currentReading.getReading(), 0.00001);
		}
	}

}
