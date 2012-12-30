/**
 * 
 */
package StatisticData;

import java.io.IOException;
import java.util.Hashtable;

import Tools.MeshTools;


import CalculateData.CalculateAngleData;
import IO.ProcessedData;

/**
 * @author Piotr Bugaj
 * @data June 21, 2010
 */

/**Class used for giving the distribution of
 * difference in angles values for a mesh**/
public class AngleDataDistribution {

	private ProcessedData [] processedData;
	private float[][][] motionTrails;
	
	private MeshTools mTools;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public AngleDataDistribution(ProcessedData [] processedData, float[][][] motionTrails) {
		this.processedData = processedData;
		this.motionTrails = motionTrails;

		mTools = new MeshTools();
	}
	
	/**Given the class for accessing the angle data in physical memory,
	 * calculate the average distribution of each angle between frame i
	 * and i+1, for i going between sFrame and eFrame - 1**/
	/** @params: cad: the  class  for accessing the
	 *                angle data in physical memory
	 *           sFrame: start frame of angle data to be compared
	 *           eFrame: end frame of angle data to be compared
	 *           distSize: the size of distribution intervals.
	 *                     value should divide the value 360
	 * **/
	public int[] getAngleDiffDistr(
			CalculateAngleData cad,
			String precalcAngFileName,
			int sFrame, int eFrame, double distSize) {

		/**Array for storing the distribution valuess**/
		int [] distributions = new int[(int)(360.0/distSize)];
		for(int g = 0; g < distributions.length; g++) {
			distributions[g] = 0;
		}
		
		
		System.out.println(distributions.length);
		System.out.println(360.0/distSize);

		/**Iterate through the frames, calculating the difference
		 * in angles between every two pair of touching frames**/
		for(int i = sFrame; i < eFrame; i++) {
		
			/**Get angle data for frame i and i + 1**/
			Hashtable<String, Float> set1 = null;
			Hashtable<String, Float> set2 = null;
			try {
				set1  =
					/**TODO: change the default value**/
					cad.getAngleData(precalcAngFileName,i);
				set2  =
					cad.getAngleData(precalcAngFileName,i+1);
			} catch (IOException e) {
				System.out.println("Failed to read angle data");
				System.exit(1);
				e.printStackTrace();
			}
			
			/**Create a vertex to index correspondance**/
			Hashtable<String, Integer> vertexToIndex =
				new Hashtable<String, Integer>();
			for(int h = 0; h < motionTrails.length; h++) {
				vertexToIndex.put(mTools.vForm(motionTrails[h][0]), h);
			}
			
			/**Get the vertex neighbours for  constructing the
			 * angles to be used for quering the hashtables**/
			float [][] tempVertexes_N = processedData[i].getFloatData(0);
			int [][] tempFaces_N = processedData[i].getIntData(2);
			
			Hashtable<String, float[][]> tempEdges_N =
				mTools.findAllEdges(tempVertexes_N, tempFaces_N);
			
			Hashtable<String, float[][]> tempVertexNeighbours_N =
				mTools.getVertexNeighbours(tempEdges_N);

			/**Now iterate through each vertex, iterating through each of  the
			 * vertex's  neighbours and  comparing  all the possible angles**/
			for(int j = 0; j < tempVertexes_N.length; j++) {
				double angDiff = 0;
				
				float [] tempVertex_N = tempVertexes_N[j];
				float [][] tempNeigh_N =
					tempVertexNeighbours_N.get(mTools.vForm(tempVertex_N));

				for(int z = 0; z < tempNeigh_N.length; z++) {
					for(int g = (z+1); g < tempNeigh_N.length; g++) {
						
						float [] leftV_N = tempNeigh_N[z];
						float [] rightV_N = tempNeigh_N[g];

						int i1 = vertexToIndex.get(mTools.vForm(leftV_N));
						int i2 = vertexToIndex.get(mTools.vForm(rightV_N));
						int i3 = vertexToIndex.get(mTools.vForm(tempVertex_N));

						float [] leftV_M = motionTrails[i1][1];
						float [] rightV_M = motionTrails[i2][1];

						float ang1;
						if(set1.containsKey(
								mTools.vForm(leftV_N) +" "+
								j +" "+
								mTools.vForm(rightV_N))) {
							
							ang1 = set1.get(
								mTools.vForm(leftV_N) +" "+
								j +" "+
								mTools.vForm(rightV_N));
							
						} else {
							System.out.println(mTools.vForm(rightV_N) +" "+
									j +" "+
									mTools.vForm(leftV_N));
							
							ang1 = set1.get(
									mTools.vForm(rightV_N) +" "+
									j +" "+
									mTools.vForm(leftV_N));
						}

						float ang2;
						if(set2.containsKey(
								mTools.vForm(leftV_M) +" "+
								i3 +" "+
								mTools.vForm(rightV_M))) {
							
							ang2 = set2.get(
								mTools.vForm(leftV_M) +" "+
								i3 +" "+
								mTools.vForm(rightV_M));
							
						} else {
							
							ang2 = set2.get(
									mTools.vForm(rightV_M) +" "+
									i3 +" "+
									mTools.vForm(leftV_M));
						}
						
						angDiff += (Math.abs(ang1 - ang2));
					}
				}
				
				int arrayIndex = (int)((angDiff/(tempNeigh_N.length*distSize)));

				int temp = distributions[arrayIndex];
				distributions[arrayIndex] = temp+1;
			}
			
		}
		return distributions;
	}
}
