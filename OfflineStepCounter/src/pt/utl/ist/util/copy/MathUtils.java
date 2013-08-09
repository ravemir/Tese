package pt.utl.ist.util.copy;

import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

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
			tmp[i] = pow(tmp[i], 2);
		
		return sqrt(sum(tmp));
	}
	
	/**
	 * Returns the approximate distance between two
	 * decimal degree coordinates in meters.
	 * (Adapted from version presented in  
	 * 'http://stackoverflow.com/a/3694416/1293116')
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return
	 */
	public static Double distFrom(double lat1, double lng1, double lat2, double lng2) {
	    double dLat = toRadians(lat2-lat1);
	    double dLng = toRadians(lng2-lng1);
	    double a = sin(dLat/2) * sin(dLat/2) +
	               cos(toRadians(lat1)) * cos(toRadians(lat2)) *
	               sin(dLng/2) * sin(dLng/2);
	    double c = 2 * atan2(sqrt(a), sqrt(1-a));
	    double dist = EARTH_RADIUS * c;

	    return new Double(dist);
    }
	
	/**
	 * @param angle
	 * @return
	 */
	public static double rangeTo180(Double angle) {
//		double tmp = angle + 180;
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
		double y = sin(dLon) * cos(lat2);
		double x = cos(lat1)*sin(lat2) - 
				sin(lat1)*cos(lat2)*cos(dLon);
		return toDegrees((atan2(y, x)));
	}
	
	public static double headingChangeFromBearings(double b1, double b2){
		double diff = b2 - b1;
		
		// If the difference between b2 and b1 is bigger than 180º...
		if(abs(diff) > 180)
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
	
	private static final double EPSILON = 0.00001;
	public static final double EARTH_RADIUS = 6369000.0;		// Semi-major axis
	public static final double F = 1 / 298.25257223563;		// Reciprocal of flattening


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
	    return a == b ? true : abs(a - b) < EPSILON;
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
	    return a == b ? true : abs(a - b) < epsilon;
	}
	
	/**
	 * Converts the given Latitude, Longitude and Altitude
	 * coordinates to a X,Y and Z coordinate.
	 * 
	 * @param lat
	 * @param lon
	 * @param alt
	 * @return
	 */
	public static double[] LLAtoXYZ(double lat, double lon, double alt){
		double N = EARTH_RADIUS / (sqrt(1-F*(2-F)*(pow(sin(lat),2))));
		double[] res = new double[]{
			(N+alt)*cos(lat)*cos(lon),
			(N+alt)*cos(lat)*sin(lon),
			(pow((1-F),2)*N+alt)*sin(lat)};
		
		return res;
	}
	
	/**
	 * Converts a set o northing, easting and 
	 * coordinates to Latitude, Longitude and
	 * altitude decimal degree coordinates.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static double[] XYZtoLLA(double x, double y, double z){
		double b = EARTH_RADIUS*(1-F);
		double r = sqrt(pow(x,2) + pow(y,2));
		double e2 = 1 - ((pow(b,2))/(pow(EARTH_RADIUS,2)));
		double e2l = ((pow(EARTH_RADIUS,2))/(pow(b,2))) - 1;
		double theta = atan((EARTH_RADIUS*z)/(b*r));
		double pLx = atan((z+e2l*b*(pow(sin(theta),3)))/(r - e2*EARTH_RADIUS*(pow(cos(theta),3))));
		double N = EARTH_RADIUS / (sqrt(1-F*(2-F)*(pow(sin(pLx),2))));
		double pLy = atan(y/x);
		double h = (r/cos(pLx))-N;
		
		return new double[]{pLx, pLy, h};
	}
	/**
	 * Returns the Atan2 value, placed within
	 * the [0;2Pi[ interval.
	 */
	public static double atan2PositiveRange(double y, double x) {
		double res = Math.atan2(y, x);
		return (res < 0 ? res + 2*Math.PI : res);
	}
}
