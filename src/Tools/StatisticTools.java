/**
 * 
 */
package Tools;


/**
 * @author Piotr Bugaj
 *
 */
public class StatisticTools {

	private VectorTools vT = new VectorTools();

	public StatisticTools() {
		
	}
	
    /**------------------------------------------------------------------**/
    /**Description: Given two  a set  of  distances  between
     *              two frames, find the average distance**/
    /**@Params:
     * distances: the distances between all vertexes between the two frames
     * **/
    /**------------------------------------------------------------------**/
    /**Return Value:
     * 
     * float value: average distance between the two frames
     * **/
    /**------------------------------------------------------------------**/
    public float getAvgFrameVertexDistance(
    		float []distances) {
    	int n = distances.length;
    	float sum = 0.0f;
    	for(int i = 0; i < n; i++) {
    		sum += distances[i];
    	}
    	
    	return sum/(n + 0.0f);
    }
    
    /**Get the  distance values  between
     * the vertexes within two frames**/
    public float [] getFrameVertexDistances (
    		float[][] vertexesN,
    		float[][] vertexesM) {
    	
    	float [] distances = new float[vertexesN.length];
    	for(int i = 0; i < distances.length; i++) {
    		float d = vT.distanceQuick(vertexesN[i], vertexesM[i]);
    		distances[i] = d;
    	}
    	
    	return distances;
    }
    
    public float getFrameDistanceStandardDeviation(
    		float avg, float[]distances) {
    	float sum = 0.0f;
    	
    	for(int i = 0; i < distances.length; i++) {
    		sum += Math.pow(distances[i] - avg, 2);
    	}
    	sum /= distances.length;
    	
    	return (float) Math.sqrt(sum);
    }
}
