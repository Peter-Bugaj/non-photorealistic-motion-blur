/**
 * 
 */
package MeshAlgorithms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import Tools.MeshTools;

/**
 * @author Piotr Bugaj
 * @date June 27, 2010
 */
public class ConstructedLines {

	/**The neighbouring faces for each face**/
	private int[][] faceNeighbours;
	/**Correspondance between face and index**/
	private Hashtable<String, Integer> faceToIndex;
	
	/**Faces belonging to the mesh at a given frame**/
	private int[][]faces;
	/**Vertexes belonging to the mesh at a given frame**/
	private float[][]vertexes;
	
	/**Tools for working with the mesh data**/
	private MeshTools mTools;

	
	/**If  trailingFaces is  null, all  faces
	 * will be used when creating the base**/
	public ConstructedLines(
			int[][] faceNeighbours,
			Hashtable<String, Integer> faceToIndex,
			int[][]faces, 
			float[][]vertexes) {
		
		this.faceNeighbours = faceNeighbours;
		this.faceToIndex = faceToIndex;
		
		this.faces = faces;
		this.vertexes = vertexes;
		
		mTools = new MeshTools();
	}
	
	/**Construct composed  lines with the  size
	 * of the bases starting at: maxBaseSize**/
	public Vector<int [][]> getConstructedLinesBase(int maxBaseSize) {
		
		/**Initialize the vector for storing
		 * the base of each composed line**/
		Vector<int [][]> composedLines = new Vector<int[][]>();
		
		/**Initialize the vector for storing which faces have been marked**/
		Hashtable<String, Boolean> marked = new Hashtable<String, Boolean>();
		
		/**Loop  start: construct all lines
		 * with base of size n to size 1**/
		for(int i = maxBaseSize; i >= maxBaseSize/**0**/; i--) {
			
			/**Iterate through faces. If face unmarked and
			 * its a trailing face go into if statement**/
			for(int h = 0; h < faces.length; h++) {

				int [] face_h = faces[h];
				float [] v1 = vertexes[face_h[0]-1];
				float [] v2 = vertexes[face_h[1]-1];
				float [] v3 = vertexes[face_h[2]-1];
				
				/**Construct  a  line of  base equal
				 * to size n. If unable, continue**/
				if(!marked.containsKey(mTools.getCentroid(v1, v2, v3))) {
					int [][] base_i = constructBase(face_h, i, marked);
					
					if(base_i != null) {
						composedLines.add(base_i);
					}
				}
			}
		}/**Loop End**/
		
		return composedLines;
	}
	
	/**Given the triangle: triangle, try to expand the bondary to the  radius:
	 * size. If boundary is successfully expanded, mark the vertexes that have
	 * been overlapped by the expansion.**/
	public int [][] constructBase(
			int[] triangle,
			int size,
			Hashtable<String, Boolean> marked) {
		
		/**Initialize a table  for keeping track
		 * of the triangles added to the base**/
		Hashtable<String, int[]> baseVertexes = new Hashtable<String, int[]>();
		
		/**Initialize linked list for keeping track
		 * of the boundary of the expanding base**/
		LinkedList<int []> boundary = new LinkedList<int[]>();
		
		/**Initialize the counter  for keeping track of
		 * how far the boundary has been expanded to**/
		int counter = 0;

		boundary.add(triangle);
		baseVertexes.put(triangle[0] +" "+ triangle[1] +" "+ triangle[2],
				triangle);
		
		/**Expand the boundaries up to the given radius**/
		while(counter < size) {
			
			/**Return null if the boundary  failed
			 * to be expanded to radius: counter**/
			if(!expandToNextLevel(boundary, baseVertexes, marked)) {
				return null;
			}
			counter++;
		}
		
		/**Store the base, if created, in an array, and
		 * mark the vertexes found for that base.**/
		Enumeration<int[]> enumm = baseVertexes.elements();
		int [][] retArray = new int [baseVertexes.size()][];
		int tempCounter = 0;
		
		while(enumm.hasMoreElements()) {
			int [] tempFace = enumm.nextElement();
			
			retArray[tempCounter] = tempFace;
			tempCounter++;
			
			float [] v1 = vertexes[tempFace[0]-1];
			float [] v2 = vertexes[tempFace[1]-1];
			float [] v3 = vertexes[tempFace[2]-1];
			
			marked.put(mTools.getCentroid(v1, v2, v3), true);
		}
		
		return retArray;
	}
	
	/**Expand the boundary at the given radius.  Store the vertexes overlapped
	 * by  the boundary.   Don't  expand  over faces that have been marked.**/
	public Boolean expandToNextLevel(
			LinkedList<int []> boundary,
			Hashtable<String, int[]> baseVertexes,
			Hashtable<String, Boolean> marked) {
		
		/**Get  the  size  of  the  currrent  boundary so  that  it  can
		 * be  used  as  a  counter  to  make sure  the boundary is only
		 * expanded from the faces at boundary at the current radius.**/
		int curBoundarySize = boundary.size();
		
		/**Go through all the boundary faces**/
		for(int i = 0; i < curBoundarySize; i++) {
			/**Get a boundary face**/
			int[] boundFace = boundary.pop();

			float [] v1 = vertexes[boundFace[0]-1];
			float [] v2 = vertexes[boundFace[1]-1];
			float [] v3 = vertexes[boundFace[2]-1];

			/**Get the neighbours for the boundary face and expand from it**/
			int boundFaceIndex =
				faceToIndex.get(mTools.getCentroid(v1, v2, v3));

			int[] boundFaceNeighs =
				faceNeighbours[boundFaceIndex];

			for(int d = 0; d < boundFaceNeighs.length; d++) {
				int face_d_index = boundFaceNeighs[d]; 

				if(face_d_index == -1) {
					continue;
				}
				
				int [] tempFace = faces[face_d_index];
				
				float [] r1 = vertexes[tempFace[0]-1];
				float [] r2 = vertexes[tempFace[1]-1];
				float [] r3 = vertexes[tempFace[2]-1];
				
				/**Construct  a  line of  base equal
				 * to size n. If unable, continue**/
				if(!marked.containsKey(mTools.getCentroid(r1, r2, r3))) {
					
					/**Dont add a vertex that is already part of the base**/
					if(!baseVertexes.containsKey(new String(
							tempFace[0] +" "+
							tempFace[1] +" "+
							tempFace[2]))) {

						boundary.add(tempFace);
						baseVertexes.put(new String(
								tempFace[0] +" "+
								tempFace[1] +" "+
								tempFace[2]),
								tempFace);
					}
				} else {
					return false;
				}
				
			}	
		}
		return true;
	}
}
