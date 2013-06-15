package pt.utl.ist.thesis.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AccelReadingTest.class, AverageCircularBufferTest.class,
		ButterworthCircularBufferTest.class, ButterworthDataTest.class,
		GPSReadingTest.class, GPSSegmentTest.class, MathUtilsTest.class,
		MovingAverageFilterTest.class, OrientationReadingTest.class,
		StepAnalyserTest.class, StandardAverageBufferTest.class,
		StepReadingTest.class })
public class RunAllThesisTests {

}
