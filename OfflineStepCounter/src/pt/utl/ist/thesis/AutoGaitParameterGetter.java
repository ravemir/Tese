package pt.utl.ist.thesis;

import pt.utl.ist.thesis.signalprocessor.AutoGaitModel;

public class AutoGaitParameterGetter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double[][] samples = getTargetSamples();
		AutoGaitModel agm = new AutoGaitModel(samples);
		
		System.out.println("Alpha: " + agm.getAlpha() + "; Beta: " + agm.getBeta() + "; Fit: " + agm.getRegressionFit());
	}

	/**
	 * Returns the AutoGait Segment Samples from the experiment days
	 * @return
	 */
	public static double[][] getTargetSamples() {
		return new double[][]{ // Day one
				new double[]{1.487875778538276, 0.0037525314489665782},
				new double[]{1.6962020865703045, 0.01378063552323991},
				new double[]{1.61850668875021, 0.7288802430525394},
				new double[]{1.63054069215787, 0.7284040398779201},
				new double[]{1.577332334540963, 0.7880442433805924},
				new double[]{1.6459825712103557, 0.7023010640574631},
				new double[]{1.6364979829148993, 0.6454720110505687},
				new double[]{1.5971215091329127, 0.47210135229423617},
				new double[]{1.6298721508165466, 0.941066841905269},
				new double[]{1.6344143635869486, 0.5884772895053819},
				new double[]{1.6045658836298085, 0.6739620328699341},
				new double[]{1.4969450442550487, 1.038456647676609},
				new double[]{1.543340357828495, 0.8906821650204483},
				new double[]{1.6407253088665903, 0.709567387715049},
				new double[]{1.6758488242117255, 0.5351027028108359},
				new double[]{1.6236008965644437, 0.17959821744362356},
				new double[]{1.6019984429109726, 0.9213762550528898},
				new double[]{1.556910112134288, 0.910616265782361},
				new double[]{1.554935547587889, 0.5904792004001078},
				new double[]{1.543423558110132, 0.9799884956808492},
				new double[]{1.5327841386813073, 0.709636590088186},
				new double[]{1.5700537145050177, 0.6274524558172356},
				new double[]{1.5271382897650327, 0.7363958632297674},
				new double[]{1.7038406036021398, 0.9862526462245895},
				new double[]{1.5448256143238588, 0.5821464779118788},
				new double[]{1.7122687641275558, 0.5838927569302123},
				new double[]{1.6573802612247241, 0.49058417010324007},
				new double[]{1.6204278919092714, 0.7499655273946835},
				new double[]{1.6362202210011492, 0.09650917654365453},
				new double[]{1.564062560722058, 0.19320053580596833},
				new double[]{1.5921973409898271, 0.7738987672134684},
				new double[]{1.656670356778535, 0.7331427907553735},
				new double[]{1.5632202966516697, 0.6390375819069313},
				new double[]{1.5092922037228578, 0.5952535140147025},
				new double[]{1.4843895393998268, 0.6608203728399342},
				new double[]{1.4628922419418509, 0.5942324272310044},
				new double[]{1.4759438023429803, 0.5701428478297633},
				new double[]{1.4405587417207155, 0.46478638158423585},
				new double[]{1.3912732227084115, 0.6818284038423337},
				new double[]{1.3954705496867499, 0.6651419151324189},
				new double[]{1.4083705215436193, 0.6767206369238812},
				new double[]{1.4115612744880357, 0.5084461806028211},
				new double[]{1.3978518022979365, 0.5671660435200692},
				new double[]{1.361754150500972, 0.568958192532028},
				new double[]{1.3090808239074987, 0.49627294149966317},
				new double[]{1.3470389604530009, 0.6221371929707274},
				new double[]{1.221777271963858, 0.7721805993077753},
				new double[]{1.3197943551522724, 0.2846109230573198},
				new double[]{1.3682081447826442, 0.5080557579751437},
				// Day two
				new double[]{0.8163266933049054, 0.097726483135715},
				new double[]{1.133216802156658, 2.244744085469776},
				new double[]{1.2139221762950994, 1.4715046461774144},
				new double[]{1.4070067798989132, 1.342156216882707},
				new double[]{1.7917100881931902, 0.8177119713840822},
				new double[]{1.7977572825272723, 0.7557391720363466},
				new double[]{1.8615086312075424, 0.8836467797090907},
				new double[]{1.9717614329699278, 0.9358969427201888},
				new double[]{2.0472010252468023, 0.7368508725069998},
				new double[]{2.0073699589103935, 0.7750916969257318},
				new double[]{1.9921177821954585, 0.9194320267653815},
				new double[]{1.9716336681174351, 0.8310522588765271},
				new double[]{1.9559772661259203, 0.8130437631960404},
				new double[]{1.9450489443976522, 0.8792463339609877},
				new double[]{2.0717476225198985, 0.8507463118721785},
				new double[]{2.028795159336385, 0.8996955824717717},
				new double[]{1.978785578619199, 0.681914302399727},
				new double[]{1.9372653050421185, 0.7861580285270477},
				new double[]{1.9876977639373876, 0.7731830236110357},
				new double[]{2.0008222718294664, 0.5786883459838214},
				new double[]{1.9523897710620766, 0.7866328935112686},
				new double[]{1.9491354267265273, 0.864844447503905},
				new double[]{2.026573162554481, 0.9006795846686001},
				new double[]{2.1632539962523194, 0.9183743749535367},
				new double[]{2.297239795838833, 1.072745426327978},
				new double[]{2.1545372828625338, 0.8858271239554556},
				new double[]{2.1299646561495496, 0.8730704260307889},
				new double[]{2.122539989299111, 0.818004721528944},
				new double[]{2.8832142851731257, 1.5092736967564542},
				new double[]{2.8736436104849914, 1.6165786086384661},
				new double[]{3.0006756453343635, 1.3512938552842146},
				new double[]{2.914092030123529, 1.3104963963956466},
				new double[]{2.7335121195278185, 1.6546429643810132},
				new double[]{2.8845188250805496, 1.083717409244435},
				new double[]{2.959930898573475, 1.4999737999121285},
				new double[]{0.9125148787230557, 2.4007830473108793},
				new double[]{1.4101046037408655, 1.0545687004175994},
				new double[]{1.9378976906098087, 0.8555805145455189},
				new double[]{1.9313913078502256, 0.7704985618053042},
				new double[]{1.9580467339676721, 0.8418324598270844},
				new double[]{1.950363810568448, 0.7516225359944498},
				new double[]{1.8942901626288098, 0.8130902767318662},
				new double[]{1.8547263456506193, 0.8542909967711516},
				new double[]{1.8767507546379791, 0.8264138820599634},
				new double[]{1.9050503113130777, 0.7210967650266171},
				new double[]{1.8769848568320198, 0.7121830186760367},
				new double[]{1.8836437689292218, 0.8342433813774046},
				new double[]{1.8312838781285494, 0.6626056122968484},
				new double[]{1.8402195413444171, 0.7987886447633616},
				new double[]{1.7755933489477107, 0.7171559255824531},
				new double[]{1.7486184238814426, 0.7624703006062749},
				new double[]{1.7677036530481685, 0.23714640350742605},
				new double[]{1.8941492288630357, 0.9044789121510642},
				new double[]{1.7792917688962864, 0.8391441956206858},
				new double[]{1.6872717105014214, 0.6393211885908149},
				new double[]{1.7160470174595315, 0.7790793124581654},
				new double[]{1.7112100732481614, 0.7408464947204241},
				new double[]{1.6775732994796617, 0.7113994617615121},
				new double[]{1.674004989513301, 0.552486138382214},
				new double[]{1.0300341307676986, 2.1312206987557216},
				new double[]{1.0607273330725369, 1.7269870208884324},
				new double[]{1.4072403524844406, 1.4749112835243556},
				new double[]{2.149885840957437, 0.8049222764152484},
				new double[]{2.1586062087678632, 0.893034498378051},
				new double[]{2.1053506609773556, 0.8244900917403096},
				new double[]{2.225747750972165, 0.808632182739276},
				new double[]{2.073540053407163, 0.6367370665373999},
				new double[]{2.1216352574600013, 0.787223646862398},
				new double[]{2.1482592086759893, 0.8242086750062615},
				new double[]{2.109155463319719, 0.7478988286242597},
				new double[]{2.001082792378853, 1.0370857604600632},
				new double[]{1.967129163859209, 0.8980947732349631},
				new double[]{1.9565707760686908, 0.7379455375048998},
				new double[]{2.011720290674799, 0.9315793168826731},
				new double[]{2.1972641092381098, 0.6381305859805115},
				new double[]{2.0440538626102125, 0.8854732052109293},
				new double[]{1.8997699036482516, 0.9622870916656773},
				new double[]{2.0629756043760006, 0.8340578212723261},
				new double[]{1.974964858063534, 0.9896142678776243},
				new double[]{1.9756888279261806, 0.7815658082903019},
				new double[]{1.9891416239181372, 0.8487671847393017},
				new double[]{2.100023875223888, 0.6312501671951274},
				new double[]{2.1036455116779598, 0.8035282271362695},
				new double[]{2.053232020756978, 0.6921255483587239},
				new double[]{1.9715020209095553, 0.5238191599949309}};
	}

}
