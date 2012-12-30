package Splines;
/**
 * 
 */

/**
 * @author Piotr Bugaj
 *
 */
public class BezierSpline {

	/**Factorial coefficients**/
	private float[][] nchoosei = new float[][]{
		{1, 1, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}, 
		{1, 2, 1, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
		{1, 3, 3, 1, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
		{1, 4, 6, 4, 1, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
		{1, 5, 10, 10, 5, 1, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
		{1, 6, 15, 20, 15, 6, 1, 0.0f, 0.0f, 0.0f, 0.0f},
		{1, 7, 21, 35, 35, 21, 7, 1, 0.0f, 0.0f, 0.0f},
		{1, 8, 28, 56, 70, 56, 28, 8, 1, 0.0f, 0.0f},
		{1, 9 , 36, 84, 126, 126, 84, 36, 9, 1, 0.0f},
		{1, 10, 45, 120, 210, 252, 210, 120, 45, 10, 1},
	};

	/**The interval [0, 1] is divided evenly for the value of t**/
	private final String EVEN = "EVEN";
	
	/**Class constructor**/
	public BezierSpline() {
		
	}
	
	/**Interpolate n points using Bezier Spline and control points: points**/
	public float[][] interpolateBezier(String distribution, int n, float[][]points) {
		int numPoints = points.length;
		
		float [] xPoints = new float[numPoints];
		float [] yPoints = new float[numPoints];
		float [] zPoints = new float[numPoints];
		
		float [][] interpolatedPoints = new float[n][3];
		
		for(int i = 0; i < xPoints.length; i++) {
			xPoints[i] = points[i][0];
		}
		for(int i = 0; i < yPoints.length; i++) {
			yPoints[i] = points[i][1];
		}
		for(int i = 0; i < zPoints.length; i++) {
			zPoints[i] = points[i][2];
		}
		
		/**Case where interval for t is evenly divided**/
		if(distribution.equals(EVEN)) {
			for(int i = 0; i < n; i++) {
				float tx = (i + 0.0f)/(n-1.0f);
				float ptx = computeSpline(tx, xPoints);
				
				float ty = (i + 0.0f)/(n-1.0f);
				float pty = computeSpline(ty, yPoints);
				
				float tz = (i + 0.0f)/(n-1.0f);
				float ptz = computeSpline(tz, zPoints);
				
				interpolatedPoints[i] = new float[]{ptx, pty, ptz};
			}
			
			/**
			System.out.println("xpoints");
			for(int i = 0; i < xPoints.length; i++) {
				System.out.print(" " + xPoints[i]);
			}
			System.out.println("\n");
			for(int i = 0; i < interpolatedPoints.length; i++) {
				System.out.print(" " + interpolatedPoints[i][0]);
			}
			
			System.out.println("\n");
			System.out.println("zpoints");
			for(int i = 0; i < yPoints.length; i++) {
				System.out.print(" " + yPoints[i]);
			}	
			System.out.println("\n");
			for(int i = 0; i < interpolatedPoints.length; i++) {
				System.out.print(" " + interpolatedPoints[i][1]);
			}
			
			System.out.println("\n");
			System.out.println("zpoints");
			for(int i = 0; i < zPoints.length; i++) {
				System.out.print(" " + zPoints[i]);
			}
			System.out.println("\n");
			for(int i = 0; i < interpolatedPoints.length; i++) {
				System.out.print(" " + interpolatedPoints[i][2]);
			}
			System.out.println("\n");
			**/
		}
		
		return interpolatedPoints;
	}
	
	/**Compute the Bezier spline none-recursively up to order n =20**/
	private float computeSpline(float t, float [] p){
		int N = p.length-1;
		float sum = 0;
		
		for(int i = 0; i <= N; i++){
			sum += nchoosei[N-1][i] * Math.pow(1-t, N-i) * Math.pow(t, i) * p[i];
		}
		return sum;
	}
}
