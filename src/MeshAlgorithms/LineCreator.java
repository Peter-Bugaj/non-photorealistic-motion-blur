package MeshAlgorithms;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import Tools.MeshTools;


/**
 * 
 */

/**
 * @author Piotr Bugaj
 * @date June 12, 2010
 */
public class LineCreator {

	/**The edges store that the lines will be created from**/
	private float[][][] edges;

	/**The  neighbouring  vertexes  required  when contructing  a line  from a
	 * given vertex**/
	private Hashtable<String, float[][]> vertexNeighbours;

	/**Variable keeping track of which edges have  been marked and are part of
	 * the line while all the edges are being iterated through**/
	private Hashtable<String, Boolean> markedEdges;

	/**Tools for dealing with the mesh**/
	private MeshTools mTools;


	public LineCreator() {
		mTools = new MeshTools();
	}

	/**
	 * Description:  Given the  queue, pop  the next  vertex and  look at  the
	 * neighbouring  vertexes from  that queue.   If a neighbour is  valid for
	 * incrementing the line, add that vertex to the line, onto the queue, and
	 * mark the  corresponding  edge to  that vertex  as  being  marked by the
	 * algorithm.
	 * 
	 * Input:
	 * 		edgeEndPoint: The queue  containing the  newly
	 *                    added vertexes forming the line.
	 *                    
	 *      line: array  containing the  vertexes  forming
	 *            the line
	 *            
	 *      counter: size of the line
	 * 		
	 * **/
	private int expandLine(LinkedList<float[][]> edgeEndPoint,
			float[][] line,
			int counter) {

		/**Pop the  next  edge.   Note that only one
		 * edge is expected to exist on the stack**/
		float[][] edge = edgeEndPoint.pop();

		/**get the possible vertexes from where the line might be expanded
		 * from. Note that based on how the line is  constructed, the line
		 * will be expanded from only one vertex.  Hence only one ew  edge
		 * will always be added onto the queue.**/
		float [] v1 = edge[0];
		float [] v2 = edge[1];


		float[][]neighbours1 = vertexNeighbours.get(mTools.vForm(v1));
		float[][]neighbours2 = vertexNeighbours.get(mTools.vForm(v2));

		/**Each vertex  along a  line can  either have  two neighbours,  or be
		 * marking a point where two or  more faces touch each other.  For the
		 * latter case, the number of vertex neighbours for such a vertex must
		 * be even and greater than two.**/
		@SuppressWarnings("unused")
		boolean tipEdge_v1 = false;

		@SuppressWarnings("unused")
		boolean tipEdge_v2 = false;

		/**Ensure that the number of vertexes stored in the neighbourhood of a
		 * vertex is even since a vertex  might either lie on a line  (contain
		 * two  neighbours)  or  might exist  on the tip  of  a triangle  that
		 * touches two or  more other triangles ( an  even number of  vertexes
		 * greater than two).**/
		if((neighbours1.length > 2) && (neighbours1.length%2 == 0)) {
			tipEdge_v1 = true;
		} else if((neighbours1.length > 2) && (neighbours1.length%2 != 0)) {
			System.out.println("Invalid line being created");
			System.exit(1);
		}

		if((neighbours2.length > 2) && (neighbours2.length%2 == 0)) {
			tipEdge_v2 = true;
		} else if((neighbours2.length > 2) && (neighbours2.length%2 != 0)) {
			System.out.println("Invalid line being created");
			System.exit(1);
		}

		for(int i = 0; i < neighbours1.length; i++) {

			float [] v_neigh = neighbours1[i];
			//if(!tipEdge_v1) {
			
			/**Ensure edge is not marked and that the
			 * neighbouring vertex does not equal  to
			 * the vertex making up the current  edge
			 * being expanded**/
			if(!containsEdge(v1, v_neigh)
					&& 
					((v_neigh[0] != v2[0]) ||
							(v_neigh[1] != v2[1]) ||
							(v_neigh[2] != v2[2]))) {

				/**Mark the new edge, place into the line
				 * array, and push it  onto the  queue**/
				markedEdges.put(mTools.edgeXYForm(new float[][]{v1, v_neigh}), true);
				line[counter++] = v_neigh;
				edgeEndPoint.push(new float[][]{v1, v_neigh});

				/**Return as no additional node should be added**/
				return 0;
			} 

			/**If the edge exist as a tip touching another triangle,  the next
			 * vertex must exist along the same triangle**/
			//} else {
				/**Note:  It is better to  interpolate a  line going from  one
				 * triangle to the next  as it doesnt really  matter how  that
				 * tip point is dealt with.**/
			//}
		}

		for(int i = 0; i < neighbours2.length; i++) {

			float [] v_neigh = neighbours2[i];
			//if(!tipEdge_v2) {
			
			/**Ensure edge is not marked and that the
			 * neighbouring vertex does not equal  to
			 * the vertex making up the current  edge
			 * being expanded**/
			if(!containsEdge(v2, v_neigh)
					&& 
					((v_neigh[0] != v1[0]) ||
							(v_neigh[1] != v1[1]) ||
							(v_neigh[2] != v1[2]))) {

				/**Mark the new edge, place into the line
				 * array, and push it  onto the  queue**/
				markedEdges.put(mTools.edgeXYForm(new float[][]{v2, v_neigh}), true);
				line[counter++] = v_neigh;
				edgeEndPoint.push(new float[][]{v2, v_neigh});

				/**Return as no additional node should be added**/
				return 0;
			} 

			/**If the edge exist as a tip touching another triangle,  the next
			 * vertex must exist along the same triangle**/
			//} else {
				/**Note:  It is better to  interpolate a  line going from  one
				 * triangle to the next  as it doesnt really  matter how  that
				 * tip point is dealt with.**/
			//}
		}

		return 0;
	}

	/**Check whether the hashtable contains an edge in the edgeXYForm**/
	public Boolean checkMarked(float [][] thisEdge) {
		if(markedEdges.containsKey(mTools.edgeXYForm(new float[][]{thisEdge[0], thisEdge[1]}))){
			if(markedEdges.get(mTools.edgeXYForm(new float[][]{thisEdge[0], thisEdge[1]}))) {
				return true;
			}
		}

		if(markedEdges.containsKey(mTools.edgeXYForm(new float[][]{thisEdge[1], thisEdge[0]}))) {
			if(markedEdges.get(mTools.edgeXYForm(new float[][]{thisEdge[1], thisEdge[0]}))) {
				return true;
			}
		}

		return false;
	}

	/**Check if marked edges contains the edge in the form: edgeXYForm**/
	public Boolean containsEdge(float[] v1, float[]v2) {
		if(!markedEdges.contains(mTools.edgeXYForm(new float[][]{v1, v2})) 
				&&
				!markedEdges.contains(mTools.edgeXYForm(new float[][]{v2, v1})) ){
			return false;
		} else {
			return true;
		}
	}

	/**Given a set of edges and  the corresponding  list of  vertex neighbours
	 * for the vertexes making up the edges, return the line(s) that the edges
	 * create**/
	public Vector<float[][]> getBoundaryPaths(float[][][] edges,
			Hashtable<String, float[][]> vertexNeighbours) {

		/**Store the input edges**/
		this.edges = edges;
		/**Store the vertex neighbours**/
		this.vertexNeighbours = vertexNeighbours;

		/**Variable keeping track of which edges have been marked**/
		markedEdges = new Hashtable<String, Boolean>();
		for(int i = 0; i < this.edges.length; i++) {
			markedEdges.put(mTools.edgeXYForm(new float[][]{edges[i][0], edges[i][1]}), false);
		}

		/**Vector for storing the lines**/
		Vector<float[][]> lines = new Vector<float[][]>();

		/**Iterate through the edges and store the lines**/
		for(int i = 0; i < this.edges.length; i++) {
			
			/**Skip edges that have been iterated by  the
			 * algorithm and already belong to some other
			 * line**/
			if(!checkMarked(edges[i])) {

				/**Initialize a new line, adding a new unmarked edge**/
				float[][] line = new  float[edges.length][];
				int counter = 0;
				line[counter++] = new float[]{this.edges[i][0][0],
						this.edges[i][0][1], this.edges[i][0][2]};
				line[counter++] = new float[]{this.edges[i][1][0],
						this.edges[i][1][1], this.edges[i][1][2]};
				
				/**Mark the new edge**/
				markedEdges.put(mTools.edgeXYForm(
						new float[][]{edges[i][0], edges[i][1]}), true);

				/**Variable containing the current endpoint
				 * of   the   path   being   constructed**/
				LinkedList<float[][]> edgeEndPoint =
					new LinkedList<float[][]>();
				edgeEndPoint.add(edges[i]);

				expandLine(edgeEndPoint, line, counter);

				/**Truncate the line array and add
				 * the  line  if  its non-empty**/
				float [][] lineTrunc = new float[counter][];
				for(int u = 0; u < counter; u++) {
					lineTrunc[u] = new float[]{line[u][0],
							line[u][1], line[u][2]};
				}
				if(lineTrunc.length > 0) {
					lines.add(lineTrunc);
				}
			}
		}

		return lines;
	}
}
