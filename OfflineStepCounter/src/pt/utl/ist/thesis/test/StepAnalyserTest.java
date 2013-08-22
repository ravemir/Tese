package pt.utl.ist.thesis.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.utl.ist.thesis.sensor.reading.AccelReading;
import pt.utl.ist.thesis.sensor.source.RawReadingSource;
import pt.utl.ist.thesis.signalprocessor.Analyser;
import pt.utl.ist.thesis.signalprocessor.StepAnalyser;

public class StepAnalyserTest {

	private static RawReadingSource rs;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		rs = new RawReadingSource(50);
		rs.addMovingAverageFilter(3);
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFilterAnalyser() {
		// Test argument constructor
		Analyser faArgs = new StepAnalyser(50);
		assertNotNull(faArgs);
	}

	@Test
	public void testGetPeaks() {
		// Create analyser and attach it to the average filter
		StepAnalyser sa = new StepAnalyser(50);
		rs.getFilters().get(0).plugAnalyserIntoOutput(sa);
		sa.set_analysisBufferSize(3);
		
		// Add readings
		rs.pushReading(new AccelReading("0", new double[]{0,0,0}));
		rs.pushReading(new AccelReading("1", new double[]{1*55,1*55,1*55}));
		rs.pushReading(new AccelReading("2", new double[]{-1,-1,-1}));
		
		// Count peaks (there should be one)
		ArrayList<AccelReading> normPeaks = sa.getNormPeaks();
		assertEquals(1, normPeaks.size());
		assertEquals("1", normPeaks.get(0).getTimestampString());
		
		// Create a new reading source, attaching the 
		// analyser to the raw source
		rs = new RawReadingSource(50);
		rs.plugAnalyserIntoOutput(sa);
		
		// Add readings (there are some extra readings, in order to force detection)
		rs.pushReading(new AccelReading("3", new double[]{-1,-1,-1}));
		rs.pushReading(new AccelReading("4", new double[]{0,0,0}));
		rs.pushReading(new AccelReading("5", new double[]{20,20,20}));
		rs.pushReading(new AccelReading("6", new double[]{-2,-2,-2}));
		rs.pushReading(new AccelReading("7", new double[]{-2,-2,-2}));
		rs.pushReading(new AccelReading("8", new double[]{-2,-2,-2}));
		
		// Count peaks (a new one should be counted, added to the first one)
		normPeaks = sa.getNormPeaks();
		assertEquals(2, normPeaks.size());
		assertEquals("1", normPeaks.get(0).getTimestampString());
		assertEquals("5", normPeaks.get(1).getTimestampString());
	}
}
