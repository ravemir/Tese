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
	
	/**
	 * 
	 * (Adapted from version presented in  
	 * 'http://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi')
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return
	 */
	public static Double distFrom(double lat1, double lng1, double lat2, double lng2) {
	    double earthRadius = 6369000; // in meters
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    return new Double(dist);
    }
	/**
	 * @param angle
	 * @return
	 */
	public static double rangeTo180(Double angle) {
		double tmp = angle + 180;
		return (angle < 180? angle : -180 + (angle-180));
	}
	/**
	 * @param lat2
	 * @param long2
	 * @param lat1
	 * @param long1
	 */
	public static double calculateHeadingChange(Double lat2, Double long2, Double lat1,
			Double long1) {
		double dLon = (long2-long1);
		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1)*Math.sin(lat2) - 
				Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
		return Math.toDegrees((Math.atan2(y, x)));
	}
	
	public static double headingChangeFromBearings(double b1, double b2){
		double diff = b2 - b1;
		
		// If the difference between b2 and b1 is bigger than 180º...
		if(Math.abs(diff) > 180)
			// ...return the angle to origin of both, 
			// multiplied by a coefficient that denotes 
			// the direction
			return (b1 > b2? 1 : -1) *
					(smallerAngleToOrigin(b1)+
					smallerAngleToOrigin(b2));
		else
			return diff;
	}
	
	/**
	 * Determines the closest distance in degrees  
	 * that a given angle is from the origin.
	 * 
	 * @param angle	The angle in question.
	 * @return		The distance from the origin.
	 */
	public static double smallerAngleToOrigin(double angle){
		double degreesTo360 = 360-angle;
		
		// If this difference is smaller than 180º...
		if(degreesTo360 < 180)
			// ...then it is the intended value
			return degreesTo360;
		else
			// ...if not, then the angle itself is closest
			return angle;
	}
	
	private final static double EPSILON = 0.00001;


	/**
	 * Returns true if two doubles are considered equal.  Tests if the absolute
	 * difference between two doubles has a difference less then .00001.   This
	 * should be fine when comparing prices, because prices have a precision of
	 * .001.(referenced from 
	 * 'http://stackoverflow.com/questions/356807/java-double-comparison-epsilon')
	 *
	 * @param a double to compare.
	 * @param b double to compare.
	 * @return true true if two doubles are considered equal.
	 */
	public static boolean equalsDouble(double a, double b){
	    return a == b ? true : Math.abs(a - b) < EPSILON;
	}


	/**
	 * Returns true if two doubles are considered equal. Tests if the absolute
	 * difference between the two doubles has a difference less then a given
	 * double (epsilon). Determining the given epsilon is highly dependant on the
	 * precision of the doubles that are being compared. (referenced from 
	 * 'http://stackoverflow.com/questions/356807/java-double-comparison-epsilon')
	 *
	 * @param a double to compare.
	 * @param b double to compare
	 * @param epsilon double which is compared to the absolute difference of two
	 * doubles to determine if they are equal.
	 * @return true if a is considered equal to b.
	 */
	public static boolean equalsDouble(double a, double b, double epsilon){
	    return a == b ? true : Math.abs(a - b) < epsilon;
	}
}
