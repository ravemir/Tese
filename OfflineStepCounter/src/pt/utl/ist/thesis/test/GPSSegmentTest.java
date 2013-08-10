package pt.utl.ist.thesis.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.thesis.sensor.exception.StepOutsideSegmentBoundariesException;
import pt.utl.ist.thesis.sensor.reading.AccelReading;
import pt.utl.ist.thesis.sensor.reading.GPSReading;
import pt.utl.ist.thesis.sensor.reading.StepReading;
import pt.utl.ist.thesis.util.buffers.GPSSegment;

public class GPSSegmentTest {

	private GPSSegment seg;
	private GPSReading[] expectedReadings;

	@Before
	public void setUp() throws Exception {
		seg = new GPSSegment();
	}

	@Test
	public void testGetGPSReading() {
		// Add a set of simple GPSReading objects
		addSimpleReadings();
		
		// Test the first segment
		GPSReading gpsr = seg.getGPSReading(0);
		assertArrayEquals(expectedReadings[0].getReading(),
				gpsr.getReading(), 0.000001);

		// Test every value in the segment 
		for (int i = 0; i < seg.size(); i++) {
			gpsr = seg.get(i);
			assertArrayEquals(expectedReadings[i].getReading(),
					gpsr.getReading(), 0.000001);
		}
		
		// Test using 'equals' method
		GPSSegment expectedSeg = new GPSSegment(new GPSReading[]{
				new GPSReading(33.234D, 9.234D, 0.333D, 0D), 
				new GPSReading(33.233D, 9.233D, 0.353D, 0D),
				new GPSReading(33.234D, 9.235D, 0.233D, 0D)});
		
		assertTrue(seg.equals(expectedSeg));
		
		GPSSegment unexpectedSeg = new GPSSegment(new GPSReading[]{
				new GPSReading(33.235D, 9.234D, 0.333D, 0D), 
				new GPSReading(33.233D, 9.233D, 0.354D, 0D),
				new GPSReading(33.234D, 9.235D, 0.233D, 1D)});
		
		assertTrue(!seg.equals(unexpectedSeg));
	}

	@Test
	public void testAddStepReading(){
		// Add a set of simple GPSReading objects
		addSimpleReadings();
		
		// Add StepReadings within the boundaries
		try {
			seg.addStepReading(new StepReading(
					new AccelReading(123456.2D, new double[3]),
					1D, 0.7D));
			seg.addStepReading(new StepReading(
					new AccelReading(123457.9D, new double[3]),
					1D, 0.7D));
		} catch (StepOutsideSegmentBoundariesException e) {
			e.printStackTrace();
			fail("A StepReading should have been added, but failed to.");
		}
		
		// Add StepReading outside the boundaries
		try {
			seg.addStepReading(new StepReading(
					new AccelReading(123458.0001D, new double[3]),
					1D, 0.7D));
			fail("Allowed misbehaved step to be added.");
		} catch (StepOutsideSegmentBoundariesException e) {}
	}
	
	@Test
	public void testGetSmoothedReading() {
		// Create array of expected reading values
		GPSReading[][] expectedSmoothedReadings = initializeSmoothedArray();

		// Create the segments and add the values
		GPSSegment[] segmentsToSmooth = initializeSegmentsToSmooth();

		// For every segment... 
		for (int i = 0; i < segmentsToSmooth.length; i++) {
			GPSSegment seg = segmentsToSmooth[i];
			// ...test every value...
			for (int j = 0; j < seg.size() - 2; j++) {
				GPSReading gpsr = seg.getSmoothedReading(j);
				assertArrayEquals(expectedSmoothedReadings[i][j].getLatLongReading(),
						gpsr.getLatLongReading(), 0.0000000000001);
			}
		}
	}

	/**
	 * @return
	 */
	public GPSSegment[] initializeSegmentsToSmooth() {
		return new GPSSegment[]{
				new GPSSegment(new GPSReading[]{
						new GPSReading(38.7581593700000, -9.16897837000000),
						new GPSReading(38.7581593700000, -9.16897837000000),
						new GPSReading(38.7581640800000, -9.16897488000000),
						new GPSReading(38.7581549900000, -9.16898990000000),
						new GPSReading(38.7581549900000, -9.16898990000000),
						new GPSReading(38.7581549200000, -9.16898855000000),
						new GPSReading(38.7581530200000, -9.16900609000000),
						new GPSReading(38.7581530200000, -9.16900609000000),
						new GPSReading(38.7581537600000, -9.16900451000000),
						new GPSReading(38.7581537600000, -9.16900451000000),
						new GPSReading(38.7581548200000, -9.16900035000000),
						new GPSReading(38.7581548300000, -9.16900055000000),
						new GPSReading(38.7581549700000, -9.16900090000000),
						new GPSReading(38.7581549700000, -9.16900090000000),
						new GPSReading(38.7581549600000, -9.16900128000000),
						new GPSReading(38.7581549800000, -9.16900132000000),
						new GPSReading(38.7581549800000, -9.16900133000000),
						new GPSReading(38.7581549800000, -9.16900133000000),
						new GPSReading(38.7581553400000, -9.16900103000000),
						new GPSReading(38.7581555800000, -9.16900095000000),
						new GPSReading(38.7581555700000, -9.16900123000000),
						new GPSReading(38.7581555700000, -9.16900123000000),
						new GPSReading(38.7581610800000, -9.16900445000000),
						new GPSReading(38.7582296400000, -9.16900997000000),
						new GPSReading(38.7582390700000, -9.16901436000000),
						new GPSReading(38.7583662800000, -9.16899914000000),
						new GPSReading(38.7583867600000, -9.16899712000000),
						new GPSReading(38.7584056400000, -9.16896491000000),
						new GPSReading(38.7584250100000, -9.16899167000000),
						new GPSReading(38.7584396300000, -9.16899158000000),
						new GPSReading(38.7584638400000, -9.16902904000000),
						new GPSReading(38.7584946100000, -9.16900631000000),
						new GPSReading(38.7585290800000, -9.16893612000000)}),
				new GPSSegment(new GPSReading[]{
						new GPSReading(38.7586005200000, -9.16895964000000),
						new GPSReading(38.7586352600000, -9.16887097000000),
						new GPSReading(38.7586415000000, -9.16886311000000),
						new GPSReading(38.7586430000000, -9.16886309000000),
						new GPSReading(38.7586495600000, -9.16885791000000),
						new GPSReading(38.7586510300000, -9.16885959000000),
						new GPSReading(38.7586480500000, -9.16886039000000),
						new GPSReading(38.7586565700000, -9.16887412000000),
						new GPSReading(38.7586714200000, -9.16886408000000),
						new GPSReading(38.7586777600000, -9.16885701000000),
						new GPSReading(38.7586864600000, -9.16885381000000),
						new GPSReading(38.7587058000000, -9.16885815000000),
						new GPSReading(38.7587257600000, -9.16885844000000),
						new GPSReading(38.7587394200000, -9.16885986000000),
						new GPSReading(38.7587520100000, -9.16885485000000),
						new GPSReading(38.7587646500000, -9.16884965000000),
						new GPSReading(38.7587818800000, -9.16883788000000),
						new GPSReading(38.7588021300000, -9.16884837000000),
						new GPSReading(38.7588255200000, -9.16884886000000),
						new GPSReading(38.7588273600000, -9.16884868000000),
						new GPSReading(38.7588040100000, -9.16883996000000),
						new GPSReading(38.7588083100000, -9.16882905000000),
						new GPSReading(38.7588084100000, -9.16882786000000),
						new GPSReading(38.7588139000000, -9.16881567000000),
						new GPSReading(38.7588283900000, -9.16880063000000),
						new GPSReading(38.7588473800000, -9.16879964000000),
						new GPSReading(38.7588676500000, -9.16879511000000),
						new GPSReading(38.7588852300000, -9.16879540000000),
						new GPSReading(38.7588883800000, -9.16879842000000),
						new GPSReading(38.7589118600000, -9.16880511000000),
						new GPSReading(38.7589344900000, -9.16877533000000),
						new GPSReading(38.7589495400000, -9.16876163000000),
						new GPSReading(38.7589581200000, -9.16875775000000),
						new GPSReading(38.7589887000000, -9.16874292000000),
						new GPSReading(38.7589997100000, -9.16872982000000),
						new GPSReading(38.7590114300000, -9.16873378000000),
						new GPSReading(38.7590311300000, -9.16873381000000),
						new GPSReading(38.7590311900000, -9.16874626000000),
						new GPSReading(38.7590387500000, -9.16874651000000),
						new GPSReading(38.7590493000000, -9.16875022000000),
						new GPSReading(38.7590516200000, -9.16874910000000),
						new GPSReading(38.7590616100000, -9.16874622000000),
						new GPSReading(38.7590698200000, -9.16874703000000),
						new GPSReading(38.7590778500000, -9.16875451000000)}),
				new GPSSegment(new GPSReading[]{
						new GPSReading(38.7591237500000, -9.16878646000000),
						new GPSReading(38.7591265400000, -9.16877619000000),
						new GPSReading(38.7591355800000, -9.16877047000000),
						new GPSReading(38.7591427700000, -9.16876831000000),
						new GPSReading(38.7591580600000, -9.16876831000000),
						new GPSReading(38.7591682400000, -9.16876547000000),
						new GPSReading(38.7591787800000, -9.16876612000000),
						new GPSReading(38.7591842000000, -9.16876701000000),
						new GPSReading(38.7591869300000, -9.16876970000000),
						new GPSReading(38.7591907900000, -9.16876450000000),
						new GPSReading(38.7591916800000, -9.16877217000000),
						new GPSReading(38.7591947200000, -9.16877276000000),
						new GPSReading(38.7591958300000, -9.16877331000000),
						new GPSReading(38.7592058400000, -9.16878013000000),
						new GPSReading(38.7592345000000, -9.16874600000000),
						new GPSReading(38.7592572700000, -9.16873635000000),
						new GPSReading(38.7592584800000, -9.16871980000000),
						new GPSReading(38.7592601600000, -9.16870613000000),
						new GPSReading(38.7592643900000, -9.16869749000000),
						new GPSReading(38.7592647500000, -9.16870311000000),
						new GPSReading(38.7592654900000, -9.16872254000000),
						new GPSReading(38.7592694900000, -9.16872819000000),
						new GPSReading(38.7592762000000, -9.16874717000000),
						new GPSReading(38.7592746300000, -9.16875354000000),
						new GPSReading(38.7592700300000, -9.16875783000000),
						new GPSReading(38.7592645500000, -9.16876083000000),
						new GPSReading(38.7592601200000, -9.16876155000000),
						new GPSReading(38.7592534700000, -9.16876453000000),
						new GPSReading(38.7592479600000, -9.16876835000000),
						new GPSReading(38.7592471600000, -9.16875938000000),
						new GPSReading(38.7592810300000, -9.16878516000000),
						new GPSReading(38.7593037100000, -9.16880442000000),
						new GPSReading(38.7593152900000, -9.16880224000000),
						new GPSReading(38.7593275600000, -9.16881421000000),
						new GPSReading(38.7593535700000, -9.16881530000000),
						new GPSReading(38.7594036000000, -9.16879877000000),
						new GPSReading(38.7594207900000, -9.16879315000000),
						new GPSReading(38.7594420000000, -9.16877199000000),
						new GPSReading(38.7594473000000, -9.16876963000000),
						new GPSReading(38.7594298300000, -9.16877675000000),
						new GPSReading(38.7594268500000, -9.16875741000000),
						new GPSReading(38.7594429700000, -9.16874978000000),
						new GPSReading(38.7594330300000, -9.16874168000000),
						new GPSReading(38.7594469400000, -9.16873484000000),
						new GPSReading(38.7594469400000, -9.16873484000000),
						new GPSReading(38.7594859300000, -9.16875439000000),
						new GPSReading(38.7594859300000, -9.16875439000000),
						new GPSReading(38.7594914700000, -9.16875650000000),
						new GPSReading(38.7594854000000, -9.16877166000000),
						new GPSReading(38.7594854000000, -9.16877166000000),
						new GPSReading(38.7594890800000, -9.16878362000000),
						new GPSReading(38.7594897400000, -9.16878470000000),
						new GPSReading(38.7594897400000, -9.16878470000000),
						new GPSReading(38.7594900700000, -9.16878813000000),
						new GPSReading(38.7594900700000, -9.16878813000000),
						new GPSReading(38.7594903500000, -9.16878875000000),
						new GPSReading(38.7594909100000, -9.16878906000000),
						new GPSReading(38.7594910400000, -9.16878893000000),
						new GPSReading(38.7594917100000, -9.16878845000000),
						new GPSReading(38.7595307900000, -9.16880061000000),
						new GPSReading(38.7596015900000, -9.16878119000000),
						new GPSReading(38.7596429600000, -9.16877979000000),
						new GPSReading(38.7596568100000, -9.16877147000000),
						new GPSReading(38.7596892400000, -9.16875844000000),
						new GPSReading(38.7596969600000, -9.16874476000000),
						new GPSReading(38.7597071200000, -9.16873390000000),
						new GPSReading(38.7596976700000, -9.16873032000000),
						new GPSReading(38.7596934900000, -9.16872202000000),
						new GPSReading(38.7597069200000, -9.16870843000000),
						new GPSReading(38.7597057400000, -9.16868946000000),
						new GPSReading(38.7597030200000, -9.16867502000000),
						new GPSReading(38.7596986800000, -9.16865933000000),
						new GPSReading(38.7597022000000, -9.16864075000000),
						new GPSReading(38.7597062700000, -9.16864748000000),
						new GPSReading(38.7597068800000, -9.16865005000000),
						new GPSReading(38.7597193000000, -9.16863145000000),
						new GPSReading(38.7597412600000, -9.16858750000000),
						new GPSReading(38.7597421400000, -9.16857383000000),
						new GPSReading(38.7597533900000, -9.16855901000000),
						new GPSReading(38.7597620000000, -9.16854768000000),
						new GPSReading(38.7597645300000, -9.16853432000000),
						new GPSReading(38.7597801800000, -9.16851942000000),
						new GPSReading(38.7597839800000, -9.16850536000000),
						new GPSReading(38.7597839800000, -9.16850536000000),
						new GPSReading(38.7597855300000, -9.16849522000000),
						new GPSReading(38.7597942100000, -9.16845712000000),
						new GPSReading(38.7597969500000, -9.16844292000000),
						new GPSReading(38.7598033400000, -9.16842635000000),
						new GPSReading(38.7598013500000, -9.16841491000000),
						new GPSReading(38.7598043300000, -9.16839132000000),
						new GPSReading(38.7598063300000, -9.16837727000000),
						new GPSReading(38.7598183000000, -9.16838596000000),
						new GPSReading(38.7598061300000, -9.16837288000000),
						new GPSReading(38.7597984000000, -9.16833034000000),
						new GPSReading(38.7597990700000, -9.16831552000000),
						new GPSReading(38.7597951600000, -9.16829739000000),
						new GPSReading(38.7598012100000, -9.16827893000000),
						new GPSReading(38.7598191200000, -9.16827670000000),
						new GPSReading(38.7598252100000, -9.16827488000000),
						new GPSReading(38.7598297100000, -9.16826759000000),
						new GPSReading(38.7598422900000, -9.16826972000000),
						new GPSReading(38.7598375100000, -9.16825649000000),
						new GPSReading(38.7598345100000, -9.16824297000000),
						new GPSReading(38.7598170200000, -9.16822596000000),
						new GPSReading(38.7598219400000, -9.16821109000000),
						new GPSReading(38.7598218100000, -9.16819053000000),
						new GPSReading(38.7598192300000, -9.16817772000000),
						new GPSReading(38.7598126400000, -9.16816670000000),
						new GPSReading(38.7598095600000, -9.16815479000000),
						new GPSReading(38.7598143300000, -9.16815122000000),
						new GPSReading(38.7598132300000, -9.16813596000000),
						new GPSReading(38.7598068300000, -9.16811679000000),
						new GPSReading(38.7598038400000, -9.16810508000000),
						new GPSReading(38.7598007800000, -9.16809016000000),
						new GPSReading(38.7597978300000, -9.16808550000000),
						new GPSReading(38.7597901400000, -9.16807644000000),
						new GPSReading(38.7597809200000, -9.16806147000000),
						new GPSReading(38.7597775900000, -9.16804427000000),
						new GPSReading(38.7597744500000, -9.16803090000000),
						new GPSReading(38.7597626600000, -9.16802529000000),
						new GPSReading(38.7597578500000, -9.16799017000000),
						new GPSReading(38.7597536000000, -9.16797966000000),
						new GPSReading(38.7597513200000, -9.16796819000000),
						new GPSReading(38.7597565000000, -9.16794511000000)}),
					new GPSSegment(new GPSReading[]{
							new GPSReading(38.7597211500000, -9.16794238000000),
							new GPSReading(38.7597262500000, -9.16792153000000),
							new GPSReading(38.7597333900000, -9.16790863000000),
							new GPSReading(38.7597459700000, -9.16788769000000),
							new GPSReading(38.7597681300000, -9.16787166000000),
							new GPSReading(38.7597754400000, -9.16784896000000),
							new GPSReading(38.7597816000000, -9.16783400000000),
							new GPSReading(38.7597966200000, -9.16782407000000),
							new GPSReading(38.7597992300000, -9.16782298000000),
							new GPSReading(38.7597998400000, -9.16782114000000),
							new GPSReading(38.7597741300000, -9.16778939000000),
							new GPSReading(38.7597741400000, -9.16777460000000),
							new GPSReading(38.7597830900000, -9.16774456000000),
							new GPSReading(38.7597803300000, -9.16772768000000),
							new GPSReading(38.7597812200000, -9.16771767000000),
							new GPSReading(38.7597794500000, -9.16769398000000),
							new GPSReading(38.7597879300000, -9.16765805000000),
							new GPSReading(38.7597909300000, -9.16763786000000),
							new GPSReading(38.7597961800000, -9.16764324000000),
							new GPSReading(38.7598069200000, -9.16763259000000),
							new GPSReading(38.7598078300000, -9.16762843000000),
							new GPSReading(38.7598090700000, -9.16762115000000),
							new GPSReading(38.7598005700000, -9.16760019000000),
							new GPSReading(38.7598184400000, -9.16759527000000),
							new GPSReading(38.7598232000000, -9.16756774000000),
							new GPSReading(38.7598502000000, -9.16752643000000),
							new GPSReading(38.7598552000000, -9.16751088000000),
							new GPSReading(38.7598669700000, -9.16749330000000),
							new GPSReading(38.7598806500000, -9.16746798000000),
							new GPSReading(38.7598647100000, -9.16751671000000),
							new GPSReading(38.7598609900000, -9.16751214000000),
							new GPSReading(38.7598788500000, -9.16749833000000),
							new GPSReading(38.7598835900000, -9.16748718000000),
							new GPSReading(38.7598892600000, -9.16746310000000),
							new GPSReading(38.7599108000000, -9.16744904000000),
							new GPSReading(38.7599173900000, -9.16743953000000)}),
						new GPSSegment(new GPSReading[]{
								new GPSReading(38.7598998700000, -9.16739717000000),
								new GPSReading(38.7599179200000, -9.16738716000000),
								new GPSReading(38.7599171100000, -9.16737036000000),
								new GPSReading(38.7599053400000, -9.16735908000000),
								new GPSReading(38.7599009100000, -9.16734972000000),
								new GPSReading(38.7599452500000, -9.16737196000000),
								new GPSReading(38.7599572800000, -9.16737201000000),
								new GPSReading(38.7599699100000, -9.16737501000000),
								new GPSReading(38.7599824000000, -9.16737567000000),
								new GPSReading(38.7599947800000, -9.16737680000000),
								new GPSReading(38.7600029500000, -9.16738441000000),
								new GPSReading(38.7600064500000, -9.16739981000000),
								new GPSReading(38.7599871400000, -9.16735576000000),
								new GPSReading(38.7599930100000, -9.16734121000000),
								new GPSReading(38.7599945500000, -9.16733961000000),
								new GPSReading(38.7600421700000, -9.16737942000000),
								new GPSReading(38.7600470400000, -9.16739268000000),
								new GPSReading(38.7600470400000, -9.16739268000000),
								new GPSReading(38.7600530500000, -9.16738563000000),
								new GPSReading(38.7600538900000, -9.16738631000000),
								new GPSReading(38.7600478700000, -9.16738309000000)})};
	}

	/**
	 * @return
	 */
	public GPSReading[][] initializeSmoothedArray() {
		return new GPSReading[][]{
				new GPSReading[]{
						new GPSReading(38.7581609400000, -9.16897720666667),
						new GPSReading(38.7581594800000, -9.16898105000000),
						new GPSReading(38.7581580200000, -9.16898489333333),
						new GPSReading(38.7581549666667, -9.16898945000000),
						new GPSReading(38.7581543100000, -9.16899484666667),
						new GPSReading(38.7581536533333, -9.16900024333333),
						new GPSReading(38.7581532666667, -9.16900556333333),
						new GPSReading(38.7581535133333, -9.16900503666667),
						new GPSReading(38.7581541133333, -9.16900312333333),
						new GPSReading(38.7581544700000, -9.16900180333333),
						new GPSReading(38.7581548733333, -9.16900060000000),
						new GPSReading(38.7581549233333, -9.16900078333333),
						new GPSReading(38.7581549666667, -9.16900102666667),
						new GPSReading(38.7581549700000, -9.16900116666667),
						new GPSReading(38.7581549733333, -9.16900131000000),
						new GPSReading(38.7581549800000, -9.16900132666667),
						new GPSReading(38.7581551000000, -9.16900123000000),
						new GPSReading(38.7581553000000, -9.16900110333334),
						new GPSReading(38.7581554966667, -9.16900107000000),
						new GPSReading(38.7581555733333, -9.16900113666667),
						new GPSReading(38.7581574066667, -9.16900230333333),
						new GPSReading(38.7581820966667, -9.16900521666667),
						new GPSReading(38.7582099300000, -9.16900959333333),
						new GPSReading(38.7582783300000, -9.16900782333333),
						new GPSReading(38.7583307033333, -9.16900354000000),
						new GPSReading(38.7583862266667, -9.16898705666667),
						new GPSReading(38.7584058033333, -9.16898456666667),
						new GPSReading(38.7584234266667, -9.16898272000000),
						new GPSReading(38.7584428266667, -9.16900409666667),
						new GPSReading(38.7584660266667, -9.16900897666667),
						new GPSReading(38.7584958433333, -9.16899049000000)},
					new GPSReading[]{
						new GPSReading(38.7586257600000, -9.16889790666667),
						new GPSReading(38.7586399200000, -9.16886572333333),
						new GPSReading(38.7586446866667, -9.16886137000000),
						new GPSReading(38.7586478633333, -9.16886019666667),
						new GPSReading(38.7586495466667, -9.16885929666667),
						new GPSReading(38.7586518833333, -9.16886470000000),
						new GPSReading(38.7586586800000, -9.16886619666667),
						new GPSReading(38.7586685833333, -9.16886507000000),
						new GPSReading(38.7586785466667, -9.16885830000000),
						new GPSReading(38.7586900066667, -9.16885632333333),
						new GPSReading(38.7587060066667, -9.16885680000000),
						new GPSReading(38.7587236600000, -9.16885881666667),
						new GPSReading(38.7587390633333, -9.16885771666667),
						new GPSReading(38.7587520266667, -9.16885478666667),
						new GPSReading(38.7587661800000, -9.16884746000000),
						new GPSReading(38.7587828866667, -9.16884530000000),
						new GPSReading(38.7588031766667, -9.16884503666667),
						new GPSReading(38.7588183366667, -9.16884863666667),
						new GPSReading(38.7588189633333, -9.16884583333333),
						new GPSReading(38.7588132266667, -9.16883923000000),
						new GPSReading(38.7588069100000, -9.16883229000000),
						new GPSReading(38.7588102066667, -9.16882419333333),
						new GPSReading(38.7588169000000, -9.16881472000000),
						new GPSReading(38.7588298900000, -9.16880531333333),
						new GPSReading(38.7588478066667, -9.16879846000000),
						new GPSReading(38.7588667533333, -9.16879671666667),
						new GPSReading(38.7588804200000, -9.16879631000000),
						new GPSReading(38.7588951566667, -9.16879964333334),
						new GPSReading(38.7589115766667, -9.16879295333333),
						new GPSReading(38.7589319633333, -9.16878069000000),
						new GPSReading(38.7589473833333, -9.16876490333333),
						new GPSReading(38.7589654533333, -9.16875410000000),
						new GPSReading(38.7589821766667, -9.16874349666667),
						new GPSReading(38.7589999466667, -9.16873550666667),
						new GPSReading(38.7590140900000, -9.16873247000000),
						new GPSReading(38.7590245833333, -9.16873795000000),
						new GPSReading(38.7590336900000, -9.16874219333334),
						new GPSReading(38.7590397466667, -9.16874766333333),
						new GPSReading(38.7590465566667, -9.16874861000000),
						new GPSReading(38.7590541766667, -9.16874851333333),
						new GPSReading(38.7590610166667, -9.16874745000000),
						new GPSReading(38.7590697600000, -9.16874925333333)},
					new GPSReading[]{
						new GPSReading(38.7591286233333, -9.16877770666667),
						new GPSReading(38.7591349633333, -9.16877165666667),
						new GPSReading(38.7591454700000, -9.16876903000000),
						new GPSReading(38.7591563566667, -9.16876736333333),
						new GPSReading(38.7591683600000, -9.16876663333333),
						new GPSReading(38.7591770733333, -9.16876620000000),
						new GPSReading(38.7591833033333, -9.16876761000000),
						new GPSReading(38.7591873066667, -9.16876707000000),
						new GPSReading(38.7591898000000, -9.16876879000000),
						new GPSReading(38.7591923966667, -9.16876981000000),
						new GPSReading(38.7591940766667, -9.16877274666667),
						new GPSReading(38.7591987966667, -9.16877540000000),
						new GPSReading(38.7592120566667, -9.16876648000000),
						new GPSReading(38.7592325366667, -9.16875416000000),
						new GPSReading(38.7592500833333, -9.16873405000000),
						new GPSReading(38.7592586366667, -9.16872076000000),
						new GPSReading(38.7592610100000, -9.16870780666667),
						new GPSReading(38.7592631000000, -9.16870224333333),
						new GPSReading(38.7592648766667, -9.16870771333333),
						new GPSReading(38.7592665766667, -9.16871794666667),
						new GPSReading(38.7592703933333, -9.16873263333333),
						new GPSReading(38.7592734400000, -9.16874296666667),
						new GPSReading(38.7592736200000, -9.16875284666667),
						new GPSReading(38.7592697366667, -9.16875740000000),
						new GPSReading(38.7592649000000, -9.16876007000000),
						new GPSReading(38.7592593800000, -9.16876230333333),
						new GPSReading(38.7592538500000, -9.16876481000000),
						new GPSReading(38.7592495300000, -9.16876408666667),
						new GPSReading(38.7592587166667, -9.16877096333333),
						new GPSReading(38.7592773000000, -9.16878298666667),
						new GPSReading(38.7593000100000, -9.16879727333333),
						new GPSReading(38.7593155200000, -9.16880695666667),
						new GPSReading(38.7593321400000, -9.16881058333333),
						new GPSReading(38.7593615766667, -9.16880942666667),
						new GPSReading(38.7593926533333, -9.16880240666667),
						new GPSReading(38.7594221300000, -9.16878797000000),
						new GPSReading(38.7594366966667, -9.16877825666667),
						new GPSReading(38.7594397100000, -9.16877279000000),
						new GPSReading(38.7594346600000, -9.16876793000000),
						new GPSReading(38.7594332166667, -9.16876131333333),
						new GPSReading(38.7594342833333, -9.16874962333333),
						new GPSReading(38.7594409800000, -9.16874210000000),
						new GPSReading(38.7594423033333, -9.16873712000000),
						new GPSReading(38.7594599366667, -9.16874135666667),
						new GPSReading(38.7594729333333, -9.16874787333333),
						new GPSReading(38.7594877766667, -9.16875509333333),
						new GPSReading(38.7594876000000, -9.16876085000000),
						new GPSReading(38.7594874233333, -9.16876660666667),
						new GPSReading(38.7594866266667, -9.16877564666667),
						new GPSReading(38.7594880733333, -9.16877999333333),
						new GPSReading(38.7594895200000, -9.16878434000000),
						new GPSReading(38.7594898500000, -9.16878584333333),
						new GPSReading(38.7594899600000, -9.16878698666667),
						new GPSReading(38.7594901633333, -9.16878833666667),
						new GPSReading(38.7594904433333, -9.16878864666667),
						new GPSReading(38.7594907666667, -9.16878891333333),
						new GPSReading(38.7594912200000, -9.16878881333333),
						new GPSReading(38.7595045133333, -9.16879266333333),
						new GPSReading(38.7595413633333, -9.16879008333333),
						new GPSReading(38.7595917800000, -9.16878719666667),
						new GPSReading(38.7596337866667, -9.16877748333333),
						new GPSReading(38.7596630033333, -9.16876990000000),
						new GPSReading(38.7596810033333, -9.16875822333333),
						new GPSReading(38.7596977733333, -9.16874570000000),
						new GPSReading(38.7597005833333, -9.16873632666667),
						new GPSReading(38.7596994266667, -9.16872874666667),
						new GPSReading(38.7596993600000, -9.16872025666667),
						new GPSReading(38.7597020500000, -9.16870663666667),
						new GPSReading(38.7597052266667, -9.16869097000000),
						new GPSReading(38.7597024800000, -9.16867460333333),
						new GPSReading(38.7597013000000, -9.16865836666667),
						new GPSReading(38.7597023833333, -9.16864918666667),
						new GPSReading(38.7597051166667, -9.16864609333333),
						new GPSReading(38.7597108166667, -9.16864299333333),
						new GPSReading(38.7597224800000, -9.16862300000000),
						new GPSReading(38.7597342333333, -9.16859759333333),
						new GPSReading(38.7597455966667, -9.16857344666667),
						new GPSReading(38.7597525100000, -9.16856017333333),
						new GPSReading(38.7597599733333, -9.16854700333333),
						new GPSReading(38.7597689033333, -9.16853380666667),
						new GPSReading(38.7597762300000, -9.16851970000000),
						new GPSReading(38.7597827133333, -9.16851004666667),
						new GPSReading(38.7597844966667, -9.16850198000000),
						new GPSReading(38.7597879066667, -9.16848590000000),
						new GPSReading(38.7597922300000, -9.16846508666667),
						new GPSReading(38.7597981666667, -9.16844213000000),
						new GPSReading(38.7598005466667, -9.16842806000000),
						new GPSReading(38.7598030066667, -9.16841086000000),
						new GPSReading(38.7598040033333, -9.16839450000000),
						new GPSReading(38.7598096533333, -9.16838485000000),
						new GPSReading(38.7598102533333, -9.16837870333333),
						new GPSReading(38.7598076100000, -9.16836306000000),
						new GPSReading(38.7598012000000, -9.16833958000000),
						new GPSReading(38.7597975433333, -9.16831441666667),
						new GPSReading(38.7597984800000, -9.16829728000000),
						new GPSReading(38.7598051633333, -9.16828434000000),
						new GPSReading(38.7598151800000, -9.16827683666667),
						new GPSReading(38.7598246800000, -9.16827305666667),
						new GPSReading(38.7598324033333, -9.16827073000000),
						new GPSReading(38.7598365033333, -9.16826460000000),
						new GPSReading(38.7598381033333, -9.16825639333333),
						new GPSReading(38.7598296800000, -9.16824180666667),
						new GPSReading(38.7598244900000, -9.16822667333333),
						new GPSReading(38.7598202566667, -9.16820919333333),
						new GPSReading(38.7598209933333, -9.16819311333333),
						new GPSReading(38.7598178933333, -9.16817831666667),
						new GPSReading(38.7598138100000, -9.16816640333333),
						new GPSReading(38.7598121766667, -9.16815757000000),
						new GPSReading(38.7598123733333, -9.16814732333334),
						new GPSReading(38.7598114633333, -9.16813465666667),
						new GPSReading(38.7598079666667, -9.16811927666667),
						new GPSReading(38.7598038166667, -9.16810401000000),
						new GPSReading(38.7598008166667, -9.16809358000000),
						new GPSReading(38.7597962500000, -9.16808403333334),
						new GPSReading(38.7597896300000, -9.16807447000000),
						new GPSReading(38.7597828833333, -9.16806072666667),
						new GPSReading(38.7597776533333, -9.16804554666667),
						new GPSReading(38.7597715666667, -9.16803348666667),
						new GPSReading(38.7597649866667, -9.16801545333333),
						new GPSReading(38.7597580366667, -9.16799837333333),
						new GPSReading(38.7597542566667, -9.16797934000000),
						new GPSReading(38.7597538066667, -9.16796432000000)},
					new GPSReading[]{
						new GPSReading(38.7597269300000, -9.16792418000000),
						new GPSReading(38.7597352033333, -9.16790595000000),
						new GPSReading(38.7597491633333, -9.16788932666667),
						new GPSReading(38.7597631800000, -9.16786943666667),
						new GPSReading(38.7597750566667, -9.16785154000000),
						new GPSReading(38.7597845533333, -9.16783567666667),
						new GPSReading(38.7597924833333, -9.16782701666667),
						new GPSReading(38.7597985633333, -9.16782273000000),
						new GPSReading(38.7597910666667, -9.16781117000000),
						new GPSReading(38.7597827033333, -9.16779504333333),
						new GPSReading(38.7597771200000, -9.16776951666667),
						new GPSReading(38.7597791866667, -9.16774894666667),
						new GPSReading(38.7597815466667, -9.16772997000000),
						new GPSReading(38.7597803333333, -9.16771311000000),
						new GPSReading(38.7597828666667, -9.16768990000000),
						new GPSReading(38.7597861033333, -9.16766329666667),
						new GPSReading(38.7597916800000, -9.16764638333333),
						new GPSReading(38.7597980100000, -9.16763789666667),
						new GPSReading(38.7598036433333, -9.16763475333333),
						new GPSReading(38.7598079400000, -9.16762739000000),
						new GPSReading(38.7598058233333, -9.16761659000000),
						new GPSReading(38.7598093600000, -9.16760553666667),
						new GPSReading(38.7598140700000, -9.16758773333334),
						new GPSReading(38.7598306133333, -9.16756314666667),
						new GPSReading(38.7598428666667, -9.16753501666667),
						new GPSReading(38.7598574566667, -9.16751020333333),
						new GPSReading(38.7598676066667, -9.16749072000000),
						new GPSReading(38.7598707766667, -9.16749266333333),
						new GPSReading(38.7598687833333, -9.16749894333333),
						new GPSReading(38.7598681833333, -9.16750906000000),
						new GPSReading(38.7598744766667, -9.16749921666667),
						new GPSReading(38.7598839000000, -9.16748287000000),
						new GPSReading(38.7598945500000, -9.16746644000000),
						new GPSReading(38.7599058166667, -9.16745055666667)},
					new GPSReading[]{
						new GPSReading(38.7599116333333, -9.16738489666667),
						new GPSReading(38.7599134566667, -9.16737220000000),
						new GPSReading(38.7599077866667, -9.16735972000000),
						new GPSReading(38.7599171666667, -9.16736025333333),
						new GPSReading(38.7599344800000, -9.16736456333334),
						new GPSReading(38.7599574800000, -9.16737299333333),
						new GPSReading(38.7599698633333, -9.16737423000000),
						new GPSReading(38.7599823633333, -9.16737582666667),
						new GPSReading(38.7599933766667, -9.16737896000000),
						new GPSReading(38.7600013933333, -9.16738700666667),
						new GPSReading(38.7599988466667, -9.16737999333333),
						new GPSReading(38.7599955333333, -9.16736559333333),
						new GPSReading(38.7599915666667, -9.16734552666667),
						new GPSReading(38.7600099100000, -9.16735341333333),
						new GPSReading(38.7600279200000, -9.16737057000000),
						new GPSReading(38.7600454166667, -9.16738826000000),
						new GPSReading(38.7600490433333, -9.16739033000000),
						new GPSReading(38.7600513266667, -9.16738820666667),
						new GPSReading(38.7600516033333, -9.16738501000000)}};
	}

	/**
	 * @return
	 */
	public void addSimpleReadings() {
		expectedReadings = getSimpleGPSReadings();

		// Add them to the segment
		for(GPSReading r :  expectedReadings)
			seg.add(r);
	}

	/**
	 * @return
	 */
	private GPSReading[] getSimpleGPSReadings() {
		return new GPSReading[]{
				new GPSReading(123456D, 33.234D, 9.234D, 0.333D, 0D), 
				new GPSReading(123457D, 33.233D, 9.233D, 0.353D, 0D),
				new GPSReading(123458D, 33.234D, 9.235D, 0.233D, 0D)};
	}
}
