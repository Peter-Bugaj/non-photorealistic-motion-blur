/**
 * 
 */
package StatisticData;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import CalculateData.CalculateDistanceData;
import IO.ProcessedData;

/**
 * @author Piotr Bugaj
 * @data June 21, 2010
 */

/**Class used for giving the distribution of
 * difference in angles values for a mesh**/
public class DistanceDataDistribution {

	private ProcessedData [] processedData;

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public DistanceDataDistribution(ProcessedData [] processedData) {
		this.processedData = processedData;

	}
	
	/**Calculate  the   distribution  of  all  distances
	 * between pairs of joined vectors for all frames**/
	/**Params:
	 * cdd: The class holding access  to the calculated  data in
	 *      physical memory. Can also be used to read that data.
	 *      
	 * precalcDisFileName: Name of the file where the data
	 *                     is  stored in  physical  memory
	 *                     
	 * disSize: Distance between the values
	 *          distributing    the    data
	 * **/
	public int[] getDistanceDistr(
			CalculateDistanceData cdd,
			String precalcDisFileName,
			double distSize) {

		/**Array for storing the distribution valuess**/
		/**Maximum value expected is 10**/
		int [] distributions = new int[(int)(10.0/distSize)];
		for(int g = 0; g < distributions.length; g++) {
			distributions[g] = 0;
		}

		for(int i = 0; i < processedData.length; i++) {
			
			Hashtable<String, Float> distances_i = null;
			try {
				distances_i = cdd.getDistanceData(precalcDisFileName, i);
			} catch (IOException e) {
				System.out.println("Failed to read " +
						"distance data for frame: " + i);
				System.exit(1);
				e.printStackTrace();
			}
			
			Enumeration<Float> enumm = distances_i.elements();
			while(enumm.hasMoreElements()) {
				float tempDistance = enumm.nextElement();
				
				int arrayIndex = (int) (tempDistance/distSize);
				int tempCount = distributions[arrayIndex];
				
				distributions[arrayIndex] = tempCount+1;
			}
		}
		
		return distributions;
	}
}
