package pt.utl.ist.thesis.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.utl.ist.thesis.util.MathUtils;

public class MathUtilsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testAltMod() {
		// 1 Mod 5 = 1
		assertEquals(1, MathUtils.altMod(1, 5));
		
		// 6 Mod 5 = 1
		assertEquals(1, MathUtils.altMod(6, 5));
		
		// 3 Mod 5 = 3
		assertEquals(3, MathUtils.altMod(3, 5));
		
		// 8 Mod 5 = 3
		assertEquals(3, MathUtils.altMod(8, 5));
		
		// -1 Mod 5 = 4
		assertEquals(4, MathUtils.altMod(-1, 5));
		
		// -6 Mod 5 = 4
		assertEquals(4, MathUtils.altMod(-6, 5));
		
		// -3 Mod 5 = 2
		assertEquals(2, MathUtils.altMod(-3, 5));
		
		// -8 Mod 5 = 2
		assertEquals(2, MathUtils.altMod(-8, 5));
	}

	@Test
	public void testSum() {
		// 0 + 0 = 0
		assertEquals(0, MathUtils.sum(new double[]{0}),0);
		
		// 1 + 1 = 2
		assertEquals(2, MathUtils.sum(new double[]{1,1}),0);
		
		// -1 + 1 = 0
		assertEquals(0, MathUtils.sum(new double[]{-1,1}),0);
		
		// 1 + 22 + 333 = 356
		assertEquals(356, MathUtils.sum(new double[]{1,22,333}),0);
	}
	
	@Test
	public void testPlainAverage() {
		// (-1+0+1)/3 = 0
		assertEquals(0, MathUtils.plainAverage(new double[]{-1,0,1}), 0);
		
		// (-2+-1+0+1+2)/5 =0 
		assertEquals(0, MathUtils.plainAverage(new double[]{-2,-1,0,1,2}), 0);
		
		// (1+2+3)/3 = 2
		assertEquals(2, MathUtils.plainAverage(new double[]{1,2,3}), 0);
		
		// (1.1+2.22+3.333)/3 = 2.217666666
		assertEquals(2.217666666, MathUtils.plainAverage(new double[]{1.1,2.22,3.333}), 0.000000001);
	}
	
	@Test
	public void testNorm(){
		// (0,0,0) = 0
		assertEquals(0, MathUtils.norm(new double[]{0,0,0}), 0);
		
		// (1,2,3) = 3.7416573867739
		assertEquals(3.7416573867739, MathUtils.norm(new double[]{1,2,3}), 0.000000001);
		
		// (-1,-2,-3) = 3.7416573867739
		assertEquals(3.7416573867739, MathUtils.norm(new double[]{-1,-2,-3}), 0.000000001);
	}

	@Test
	public void testHeadingChangeFromBearings(){
		// 355� -> 4�
		assertEquals(9D, MathUtils.headingChangeFromBearings(355, 4), 0.000000001);
		
		// 4� -> 355�
		assertEquals(-9D, MathUtils.headingChangeFromBearings(4, 355), 0.000000001);
	}
}