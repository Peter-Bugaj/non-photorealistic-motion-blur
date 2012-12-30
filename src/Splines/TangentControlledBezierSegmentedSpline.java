package Splines;


/**
 * @author Piotr Bugaj
 *
 */
public class TangentControlledBezierSegmentedSpline {

	public TangentControlledBezierSegmentedSpline() {
		
	}
	
	public float[][] interpolate(float[][] controlPoints, float tangentFactor, int k) {
		
		/**
		System.out.print("x: ");
		for(int i = 0; i < controlPoints.length; i++) {
			System.out.print(controlPoints[i][0] + " ");
		}
		System.out.print("\n");
		System.out.print("y: ");
		for(int i = 0; i < controlPoints.length; i++) {
			System.out.print(controlPoints[i][1] + " ");
		}
		System.out.print("\n");
		System.out.print("z: ");
		for(int i = 0; i < controlPoints.length; i++) {
			System.out.print(controlPoints[i][2] + " ");
		}
		System.out.print("\n");
		**/
		
		/**Array containing the interpolated points**/
		float [][] interpolatedPoints = new float[(k*4) + (controlPoints.length-2)*((4*k)-1)][3];
		
		float[][][] tValues = new float[controlPoints.length][2][3];
		int n = controlPoints.length-1;
		
		/**Create T1 and Tn**/
		float[][] pair1 = new float[2][3];
		pair1[0] = new float[]{ 
				controlPoints[1][0] + (controlPoints[1][0] - controlPoints[0][0])*tangentFactor/2,
				controlPoints[1][1] + (controlPoints[1][1] - controlPoints[0][1])*tangentFactor/2,
				controlPoints[1][2] + (controlPoints[1][2] - controlPoints[0][2])*tangentFactor/2};
		pair1[1] = new float[]{ 
				controlPoints[1][0] + (controlPoints[1][0] - controlPoints[0][0])*tangentFactor/2,
				controlPoints[1][1] + (controlPoints[1][1] - controlPoints[0][1])*tangentFactor/2,
				controlPoints[1][2] + (controlPoints[1][2] - controlPoints[0][2])*tangentFactor/2};
		
		float[][] pairn = new float[2][3];
		pairn[0] = new float[]{ 
				controlPoints[n][0] - (controlPoints[n][0] - controlPoints[n-1][0])*tangentFactor/2,
				controlPoints[n][1] - (controlPoints[n][1] - controlPoints[n-1][1])*tangentFactor/2,
				controlPoints[n][2] - (controlPoints[n][2] - controlPoints[n-1][2])*tangentFactor/2};
		pairn[1] = new float[]{ 
				controlPoints[n][0] - (controlPoints[n][0] - controlPoints[n-1][0])*tangentFactor/2,
				controlPoints[n][1] - (controlPoints[n][1] - controlPoints[n-1][1])*tangentFactor/2,
				controlPoints[n][2] - (controlPoints[n][2] - controlPoints[n-1][2])*tangentFactor/2};
		
		tValues[0] = pair1;
		tValues[n] = pairn;
		
		/**Create each Ti**/
		for(int i = 1; i <= n-1; i++) {
			float [][] pairi = new float[2][3];
			pairi[0] = new float[]{ 
					controlPoints[i][0] - (controlPoints[i+1][0] - controlPoints[i-1][0])*tangentFactor/2,
					
					controlPoints[i][1] - (controlPoints[i+1][1] - controlPoints[i-1][1])*tangentFactor/2,
	                
					controlPoints[i][2] - (controlPoints[i+1][2] - controlPoints[i-1][2])*tangentFactor/2
			};
			pairi[1] = new float[]{ 
					controlPoints[i][0] + (controlPoints[i+1][0] - controlPoints[i-1][0])*tangentFactor/2,
					
					controlPoints[i][1] + (controlPoints[i+1][1] - controlPoints[i-1][1])*tangentFactor/2,
	                
					controlPoints[i][2] + (controlPoints[i+1][2] - controlPoints[i-1][2])*tangentFactor/2
			};
			tValues[i] = pairi;
		}
		
		/**
		System.out.print("\nx: ");
		for(int i = 0; i < tValues.length; i++) {
			System.out.print(tValues[i][0][0] + "|" + tValues[i][1][0]+ " ");
		}
		System.out.print("\n");
		System.out.print("y: ");
		for(int i = 0; i < tValues.length; i++) {
			System.out.print(tValues[i][0][1] + "|" + tValues[i][1][1]+ " ");
		}
		System.out.print("\n");
		System.out.print("z: ");
		for(int i = 0; i < tValues.length; i++) {
			System.out.print(tValues[i][0][2] + "|" + tValues[i][1][2]+ " ");
		}
		System.out.print("\n\n");
		**/
		
		/**Create the interpolated points between each value Ti and Ti-1**/
		BezierSpline bSpline = new BezierSpline();
		int z = 0;
		for(int i = 0; i <= n-1; i++) {
			float [] xi = controlPoints[i];
			float [] xiplus1 = controlPoints[i+1];
			float [] Ti_1 = tValues[i][1];
			float [] Tiplus1_0 = tValues[i+1][0];
			
			float [][] quadSet = new float[][]{xi, Ti_1, Tiplus1_0, xiplus1};
			
			float [][]tempInterpPoints = bSpline.interpolateBezier("EVEN", k*quadSet.length, quadSet);
			
			for(int j = 0; j < tempInterpPoints.length; j++) {
				interpolatedPoints[z] = tempInterpPoints[j];
				z++;
			}
			z--;
		}
		
		return interpolatedPoints;
	}
	
}
