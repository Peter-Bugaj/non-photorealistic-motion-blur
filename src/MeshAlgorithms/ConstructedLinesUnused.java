/**
 * 
 */
package MeshAlgorithms;


import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import CalculateData.CalculateDistanceData;
import IO.ProcessedData;
import StatisticData.DistanceDataDistribution;
import Tools.MeshTools;

/**
 * @author Piotr Bugaj
 *
 */
public class ConstructedLinesUnused {

	private MeshTools mTools;

	private int frameNum;
	private float[][][] motionTrails;
	private float [][] vertexes;
	private int [][] faces;
	
	private float epsilon;
	private float [] params = null;
	private String weightFunction;

	/**Whether  or not  the  angles should be pre-
	 * calculated and stored in physical memory**/
	private boolean precalculateDistances = false;
	
	/**Variable   indicating   whether   or   not   the
	 * calculated angle data has already been stored**/
	private static boolean distanceDataCalculated = false;
	
	/**Name of the file where the precalculated
	 * distance values will be stored**/
	private String precalcDisFileName = "temp/disData";
	
	public static void main(String[] args) {

	}

	public ConstructedLinesUnused(
			ProcessedData [] processedData,
			int frameNum,
			float[][][] motionTrails,
			float [][] vertexes,
			int [][] faces) {

		this.vertexes = vertexes;
		this.faces = faces;
		this.motionTrails = motionTrails;
		this.frameNum = frameNum;

		mTools = new MeshTools();
		
		/**Precalculate the distance between all pair of points**/
		if(precalculateDistances) {
			if(!distanceDataCalculated) {
				
				CalculateDistanceData cdd =
					new CalculateDistanceData(processedData, precalcDisFileName);
				/**
				try {
					cdd.precalculateDistance();
				} catch (IOException e) {
					System.out.println("Failed to write to " +
						"distance data file.");
					System.exit(1);
					e.printStackTrace();
				}
				 **/

				DistanceDataDistribution ddd = new DistanceDataDistribution(processedData);
				int [] distributedData = ddd.getDistanceDistr(cdd, precalcDisFileName, 0.001);
				for(int i = 0; i < distributedData.length; i++) {
					System.out.println(distributedData[i]);
					if(distributedData[i] == 0) {
						break;
					}
				}
				distanceDataCalculated = true;
				System.exit(1);
			}
		}
	}

	/**------------------------------------------------------------------**/
	/**Given  motion  trails,  construct  larger
	 * lines from trails that are close together
	 * Param 1: The motion trails
	 * 
	 * Param 2: Vertexes existing within the mesh
	 * 
	 * Param 3: Faces making up the mesh
	 * 
	 * Param 4: The neighbouring vertexes for each vertex
	 * 
	 * Param 5: The current frame number the motion
	 *          trails will be starting of from
	 *          
	 * Param 6: The length of the longest composed line
	 * 
	 * Param 7: The weight function to be used on the
	 *          line difference  at for each segment
	 * 
	 * Param 8: Parameters required for the weight function
	 * 
	 * Param 9: The   difference   used  in  the  comparisons  to
	 *          determine whether lines are close together or not
	 *          when being constructed into one line.         
	 *          **/
	/**------------------------------------------------------------------**/
	/**Return value: array:
	 * [line number][vertexes at level zero][vertex coordinates] **/
	/**------------------------------------------------------------------**/
	public float [][][] getConstructedLines(
			int lineLength,
			String weightFunction,
			float [] params, float epsilon) {

		this.epsilon = epsilon;
		this.params = params;
		this.weightFunction = weightFunction;
		
		/**Create  a  hashtable  for keeping  track
		 * of which faces have been gone over**/
		Hashtable<String, Boolean> markedVertexes =
			new Hashtable<String, Boolean>();
		for(int i = 0; i < faces.length; i++) {
			float [][] tV = getTriVertexes(faces[i]);
			markedVertexes.put(mTools.getCentroid(
					tV[0], tV[1], tV[2]), false);
		}

		/**Vector storing the constructed lines**/
		Vector<int[][]> constructedLines = new Vector<int[][]>();

		/**Create a table for neighbouring faces
		 * and a face to index correspondance**/
		Hashtable<String, float[][]>  edges =
			mTools.findAllEdges(vertexes, faces);
		Hashtable<String, int[]> touchingFaces =
			mTools.getTouchingFaces(edges, faces, vertexes);

		int[][] faceNeighbours =
			mTools.getFaceNeighbours(vertexes, faces, touchingFaces);
		Hashtable<String, Integer> faceToIndexCorrespondance =
			mTools.createFacesToIndexCorrespondance(vertexes, faces);


		/**Construct lines of length line length and finally down to zero**/
		for(int j = lineLength; j >= 2; j--) {

			System.out.println(j);
			/**Iterate   through  each   face and  construct
			 * lines of size three or greater of length j**/
			for(int i = 0; i < faces.length; i++) {

				/**Current line being constructed, starting at this vertex**/
				Vector<int[]> currentLine =
					new Vector<int[]>();

				/**The  boundary  of  which  triangles  the
				 * line is currently being expanded from**/
				LinkedList <int[]> triangularBoundary =
					new LinkedList<int[]>();

				/**Add the traingles if the lines  are similar
				 * and the triangle has not yet been marked**/
				triangleAppender(
						faces[i], j,
						triangularBoundary,
						markedVertexes,
						currentLine);

				/**Expand this line if possible**/
				while(!triangularBoundary.isEmpty()) {

					/**Get the next triangle from the boundary**/
					int[]nextTriangle = triangularBoundary.pop();
					float [][] triV = getTriVertexes(nextTriangle);

					/**Get the triangles index**/
					int triIndex = faceToIndexCorrespondance.get(
							mTools.getCentroid(triV[0], triV[1], triV[2]));

					/**Get the face neighbours of that triangle**/
					int [] tempFaceNeighbours = faceNeighbours[triIndex];
					for(int e = 0; e < tempFaceNeighbours.length; e++) {
						int nextNeighbours = tempFaceNeighbours[e];

						triangleAppender(faces[nextNeighbours], j,
								triangularBoundary,
								markedVertexes,
								currentLine);
					}
				}

				/**Add a line if one has been created**/
				if(currentLine.size() > 0) {
					System.out.println(currentLine.size());
					int [][] lineArrayForm = new int[currentLine.size()][];
					currentLine.toArray( lineArrayForm);
					constructedLines.add(lineArrayForm);
				}
			}
		}
		return null;
	}

	private float [][] getTriVertexes(int []triangle) {
		float [] r1 = vertexes[triangle[0]-1];
		float [] r2 = vertexes[triangle[1]-1];
		float [] r3 = vertexes[triangle[2]-1];

		return new float[][]{r1, r2, r3};
	}

	private void triangleAppender(
			int[] face_i,
			int lineLength,
			LinkedList <int[]> triangularBoundary,
			Hashtable<String, Boolean> markedVertexes,
			Vector<int[]> currentLine) {

		/**Get the vertexes corresponding to the triangle**/
		float [][] triV = getTriVertexes(face_i);

		/**Check if this triangle has been marked**/
		if(!markedVertexes.get(mTools.getCentroid(triV[0], triV[1], triV[2]))) {

			/**Get the indexes of the triagle vertexes**/
			int i1 = face_i[0]-1;
			int i2 = face_i[1]-1;
			int i3 = face_i[2]-1;

			/**Construct  the  three  lines  using  the  motion
			 * trails for the vertexes defining the triangle**/
			float [][] line1 = new float[lineLength][];
			float [][] line2 = new float[lineLength][];
			float [][] line3 = new float[lineLength][];

			int vertexTrailCount = lineLength;
			for(int j = frameNum; j >= 0; j--) {
				if(vertexTrailCount <= 0) {
					break;
				}
				line1[vertexTrailCount-1] = motionTrails[i1][j];
				line2[vertexTrailCount-1] = motionTrails[i2][j];
				line3[vertexTrailCount-1] = motionTrails[i3][j];
				vertexTrailCount--;
			}

			for(int j = motionTrails[0].length-1; j > frameNum; j--) {
				if(vertexTrailCount <= 0) {
					break;
				}	
				line1[vertexTrailCount-1] = motionTrails[i1][j];
				line2[vertexTrailCount-1] = motionTrails[i2][j];
				line3[vertexTrailCount-1] = motionTrails[i3][j];
				vertexTrailCount--;
			}

			/**Check the similarity of the lines**/
			float diff1 = mTools.getLineDifference(line1, line2,
					params, this.weightFunction);
			float diff2 = mTools.getLineDifference(line1, line3,
					params, this.weightFunction);
			float diff3= mTools.getLineDifference(line2, line3,
					params, this.weightFunction);

			if((diff1 < epsilon) && (diff2 < epsilon) && (diff3 < epsilon)) {
				currentLine.add(face_i);
				triangularBoundary.add(face_i);
			}
			markedVertexes.put(mTools.getCentroid(
					triV[0], triV[1], triV[2]), true);
		}
	}
}
