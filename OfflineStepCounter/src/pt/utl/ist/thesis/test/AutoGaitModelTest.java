package pt.utl.ist.thesis.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.thesis.signalprocessor.AutoGaitModel;

public class AutoGaitModelTest {

	private AutoGaitModel model;
	private final double equalsPrecision = 0.000000000001;

	@Before
	public void setUp() throws Exception {
		model = new AutoGaitModel();
	}

	@Test
	public void test() {
		// TODO Get real data to test regression model
//		fail("Not yet implemented");
	}

	@Test
	public void randomTest() {
		// Initialize sample data
		Double[][] samples = new Double[][]{
				new Double[]{3.257158837949090, 0.673322743980495},
				new Double[]{1.955185975035316, 1.408055570523893},
				new Double[]{4.076744245584139, 0.816248869595501},
				new Double[]{2.710845385117329, 0.521779584148963},
				new Double[]{1.818562807526728, 1.385857162415872},
				new Double[]{4.701107653799445, 1.475698054027302},
				new Double[]{4.392119776315621, 0.826643967751324},
				new Double[]{2.795766080202269, 0.433343068128718},
				new Double[]{3.150127921406015, 0.609677635094480},
				new Double[]{2.976519052203943, 0.790463815335062},
				new Double[]{1.117937234391840, 1.013875288810337},
				new Double[]{1.576107018369505, 0.614654097337014},
				new Double[]{2.407524407736195, 1.023411707258500},
				new Double[]{1.229391985036637, 1.153458936520420},
				new Double[]{4.237113084207406, 0.566096080820688},
				new Double[]{1.054345018878542, 0.440901181026967},
				new Double[]{1.207016726764754, 0.656011047861992},
				new Double[]{0.936469431024507, 0.682533962311059},
				new Double[]{1.215555059301112, 0.809000111656569},
				new Double[]{2.234923552109106, 0.909429941593342}};

		// Add values to model
		for(Double[] value : samples)
			model.addSampleToModel(value[0], value[1]);
		
		// Check if returned Alpha and Beta values are correct
		assertEquals(0.011583465545763, model.getAlpha(), equalsPrecision);
		assertEquals(0.812114390163156, model.getBeta(), equalsPrecision);

		// Check if expected step length values match
		Double[] frequenciesToTest = new Double[]{
				0.519027405741216, 1.386162950021830, 4.024971651571720,
				0.243179360054517, 4.651385283442418, 3.678621227991719,
				2.494183971637538, 2.934772799014851, 1.262689540880456,
				2.348359258081663, 4.819133842505874, 2.779348021820943,
				2.653565570939608, 1.234812494871767, 2.495598945208818,
				3.157894432051079, 3.427764150242164, 2.038024556776106,
				1.900439577867935, 4.941111815492000};
		Double[] expectedLengths = new Double[]{
				0.818126526234866, 0.828170960935547, 0.858737510611810,
				0.814931249901789, 0.865993551333980, 0.854725572413511,
				0.841005684263414, 0.846109229765187, 0.826740710954940,
				0.839316528718219, 0.867936660988243, 0.844308872213604,
				0.842851875527558, 0.826417798152981, 0.841022074561025,
				0.848693751513977, 0.851819778096488, 0.835721777397991,
				0.834128066535194, 0.869349588635670};
		for (int i = 0; i < expectedLengths.length; i++) 
			assertEquals(expectedLengths[i],
					model.getLengthFromFrequency(frequenciesToTest[i]),
					equalsPrecision);

		// Check if the expected step frequency values match
		Double[] lengthsToTest = new Double[]{
				0.345286639487463, 1.362201609842970, 1.395944193167087,
				1.255420648302254, 0.418454734386689, 0.614245420644859,
				0.702428207955356, 1.115673541652806, 0.463863764826444,
				1.165472998298088, 0.428114233928690, 1.084508818402272,
				0.893008723967124, 1.234862067877530, 1.158044494080833,
				1.384464672667580, 1.369107005196947, 0.700995663284995,
				1.138494998801753, 0.537371792023115};
		Double[] expectedFrequencies = new Double[]{
				 -40.301216318328137, 47.489002104471837, 50.401997631657480,
				  38.270607046545948, -33.984618352878130, -17.082018221280443,
				  -9.469202612505020, 26.206246333653258, -30.064459030924230,
				  30.505430929881324, -33.150714241553352, 23.515797337417098,
				   6.983603782855603, 36.495785828879868, 29.864128533123843,
				  49.410971202290987, 48.085144539280797, -9.592874122097800,
				  28.176421585505718, -23.718514727273295};
		for (int i = 0; i < expectedLengths.length; i++) 
			assertEquals(expectedFrequencies[i],
					model.getFrequencyFromLength(lengthsToTest[i]), 
					equalsPrecision);
	}

}
