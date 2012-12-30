package Tools;
/**
 * 
 */

/**
 * @author Piotr Bugaj
 * @date June 7, 2010
 */
public class VectorTools {

	/**A class containing vector operations**/
	public VectorTools() {
		
	}

	/**Subtract two vertexes**/
	public float [] sub(float [] v1, float []v2) {
		float [] ans = new float[]{v1[0] - v2[0],
				                   v1[1] - v2[1],
				                   v1[2] - v2[2]};
		return ans;
	}
	
	/**Multiply a vertex by a scalar**/
	public float [] mult(float s, float []v2) {
		float [] ans = new float[]{
				s*v2[0],
				s*v2[1],
				s*v2[2]};
		return ans;
	}
	
	/**Add two vertexes**/
	public float [] add(float [] v1, float []v2) {
		float [] ans = new float[]{v1[0] + v2[0],
				                   v1[1] + v2[1],
				                   v1[2] + v2[2]};
		return ans;
	}
	
	/**Calculate the distance between two vertexes**/
	public float distance(float [] v1, float []v2) {
		return (float) Math.sqrt(
				Math.pow(v2[2]-v1[2], 2) +
				Math.pow(v2[1]-v1[1], 2) +
				Math.pow(v2[0]-v1[0], 2)
				);
	}
		
	/**Calculate the distance between two vertexes by
	 * finding the average distance between the point
	 * in each dimension. Useful if the distance is
	 * required to compare values, but where the exact
	 * value of the distance is not required**/
	public float distanceQuick(float [] v1, float []v2) {
		return (Math.abs((v2[2]-v1[2])) +
				Math.abs((v2[1]-v1[1])) +
				Math.abs((v2[0]-v1[0])));
	}
	
	/**Calculate the cross product between two vertexes**/
	public float [] cross(float [] v1, float []v2) {
		
		float [] cross = new float []{(v1[1]*v2[2] - v1[2]*v2[1]),
		                              (v1[2]*v2[0] - v1[0]*v2[2]),
		                              (v1[0]*v2[1] - v1[1]*v2[0])};
		
		return cross;
	}
	
	/**Calculate the angle at vertex 'c' between vertex 'a' and 'b'**/
	public float ang(float [] a, float [] c, float [] b) {
		float [] v1 = new float[]{a[0]-c[0], a[1]-c[1], a[2]-c[2]};
		float [] v2 = new float[]{b[0]-c[0], b[1]-c[1], b[2]-c[2]};

		double angle = Math.acos(
				dot(v1, v2)/(norm(v1)*norm(v2))
			);
		
		return (float) ((180*angle)/Math.PI);
	}
	
	/**Calculate the dot product between two vectors**/
	public float dot(float [] v1, float [] v2) {
		return v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2];
	}
	
	/**Calculate the norm of a vector**/
	public float norm(float [] v1) {
		return (float) Math.sqrt(
			Math.pow(v1[0], 2) + Math.pow(v1[1], 2) + Math.pow(v1[2], 2)
		);
	}
}
