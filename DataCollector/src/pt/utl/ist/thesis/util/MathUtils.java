package pt.utl.ist.thesis.util;

public class MathUtils {

	/**
	 * Computes the modulo operation correctly, even
	 * for a negative 'x' number. (Referenced in 
	 * 'http://stackoverflow.com/questions/90238/whats-the-syntax-for-mod-in-java')
	 * 
	 * @param x LHS operand (the number to be divided) 
	 * @param y RHS operand (the number to divide by)
	 * @return The result of the operation (remainder of the division)
	 */
	public static int altMod(int x, int y) {
	    int result = x % y;
	    if (result < 0) {
	        result += y;
	    }
	    
	    return result;
	}
	/**
	 * Calculates an unweighed mean of the given
	 * values.
	 * 
	 * @param values The vales to be averaged.
	 * @return		 The valued of the mean.
	 */
	public static double plainAverage(double[] values){
		// Sum all the values
		double result = sum(values);
		
		// Divide by divisor
		result /= values.length;
		
		return result;
	}

	/**
	 * @param values
	 * @return 
	 */
	public static double sum(double[] values) {
		double sum = 0;
		for(double v : values)
			sum += v;
		
		return sum;
	}
	
	public static double norm(double[] values) {
		double[] tmp = values.clone();
		for (int i = 0; i < tmp.length; i++)
			tmp[i] = Math.pow(tmp[i], 2);
		
		return Math.sqrt(sum(tmp));
	}
}
