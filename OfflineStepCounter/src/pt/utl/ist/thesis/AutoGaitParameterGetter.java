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
	 * @return
	 */
	public static double[][] getTargetSamples() {
		return new double[][]{
				new double[]{2.679921626, 0.002655365},
				new double[]{3.626991292, 0.009799563},
				new double[]{2.952559515, 0.492466972},
				new double[]{1.866101445, 0.718606316},
				new double[]{2.077592182, 0.729621975},
				new double[]{2.157401543, 0.553268604},
				new double[]{1.47428935, 0.919055119},
				new double[]{1.417697489, 0.825549635},
				new double[]{2.745556234, 0.739789713},
				new double[]{1.474330887, 0.793213836},
				new double[]{2.193053717, 0.592814308},
				new double[]{2.278682507, 0.562825422},
				new double[]{1.625319699, 0.927603804},
				new double[]{1.570066752, 0.868989011},
				new double[]{1.838950193, 0.663124839},
				new double[]{1.783135402, 0.614150915},
				new double[]{1.613057088, 0.937469847},
				new double[]{1.700981789, 0.720577547},
				new double[]{1.8859211, 0.624086452},
				new double[]{1.479343979, 0.831634212},
				new double[]{1.649064973, 0.514767258},
				new double[]{1.598747328, 1.038456648},
				new double[]{2.007309718, 0.783212228},
				new double[]{1.375478819, 1.200001287},
				new double[]{1.733751412, 0.69136052},
				new double[]{1.597532009, 0.458818436},
				new double[]{1.512087701, 0.860871613},
				new double[]{1.122964332, 1.130558316},
				new double[]{1.702005372, 0.615563445},
				new double[]{1.612295211, 0.982498964},
				new double[]{1.638380634, 0.646958801},
				new double[]{1.56620607, 0.193413465},
				new double[]{1.692456316, 0.903129994},
				new double[]{1.752401182, 0.757413001},
				new double[]{1.928432094, 0.590716882},
				new double[]{1.631047931, 0.788835071},
				new double[]{1.530699029, 0.616945759},
				new double[]{1.38907849, 0.859128507},
				new double[]{1.582146293, 0.986252646},
				new double[]{1.856753631, 0.605655752},
				new double[]{1.65631992, 0.516932946},
				new double[]{2.02425113, 0.085785935},
				new double[]{2.29735857, 0.565851918},
				new double[]{1.914051223, 0.40811498},
				new double[]{1.523294948, 0.871471147},
				new double[]{2.405976347, 0.567085354},
				new double[]{1.601146097, 0.557186442},
				new double[]{1.964329263, 0.57361598},
				new double[]{1.509292204, 0.595253514},
				new double[]{2.04070991, 0.556480314},
				new double[]{1.971550609, 0.50934208},
				new double[]{1.884685375, 0.503067219},
				new double[]{1.439356927, 0.464786382},
				new double[]{1.401932981, 0.683963425},
				new double[]{1.45671731, 0.873439189},
				new double[]{1.381201529, 0.568288669},
				new double[]{1.773394356, 0.614537333},
				new double[]{1.783982556, 0.625562438},
				new double[]{1.949508101, 0.466075666},
				new double[]{1.25468402, 0.606438543},
				new double[]{1.382255763, 0.747906799},
				new double[]{1.242460627, 0.607329364},
				new double[]{1.524241166, 0.762105051},
				new double[]{1.75868184, 0.563299861},
				new double[]{1.368208145, 0.508055758}};
	}

}
