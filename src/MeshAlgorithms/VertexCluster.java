package MeshAlgorithms;
import java.util.Hashtable;

import Tools.MeshTools;


/**
 * 
 */

/**
 * @author Piotr Bugaj
 * @date May 30, 2010
 */
public class VertexCluster {

	/**Vertexes belonging to this cluster**/
	private float[][]vertexes;

	/**Edges corresponding the the vertexes**/
	private Hashtable<String, float[][]> edges;

	/**The time frame these vertexes belong to relative  to the  current frame
	 * these clusters are being made for**/
	private int frameLevel;
	
	/**The frame these vertexes belong to**/
	private int frameNumber;
	
	private MeshTools mTools = new MeshTools();
	
	/**Class contructor for storing edges and vertexes at frame: frameLevel**/
	public VertexCluster(int frameLevel, int frameNumber) {
		this.frameLevel = frameLevel;
		this.frameNumber = frameNumber;
		
		edges = new Hashtable<String, float[][]>();
	}

	/**Add a new edge to the cluster**/
	public void addEdge(float v1[], float v2[]) {
		if(!edges.containsKey(mTools.edgeXYForm(new float[][]{v1, v2}))
			&&
			!edges.containsKey(mTools.edgeXYForm(new float[][]{v2, v1}))) {
		
			edges.put(mTools.edgeXYForm(new float[][]{v1, v2})
			, new float[][]{v1, v2});
		}
	}
	
	/**Clear the edges belonging to this cluster**/
	public void removeAllEdges() {
		edges = new Hashtable<String, float[][]>();
	}

	/**Add the vertexes to the cluster**/
	public void addVertexes(float [][] vertexes) {
		this.vertexes = vertexes;
	}
	
	/**Get the frame level the edges and vertexes within the cluster belong to.**/
	public int getFrameLevel() {
		return frameLevel;
	}
	
	/**Get the frame the edges and vertexes within the cluster belong to.**/
	public int getFrame() {
		return frameNumber;
	}
	
	/**Get the vertexes belonging to this current cluster**/
	public float[][] getVertexes() {
		return vertexes;
	}
	
	/**Get the edges belonging to this current cluster**/
	public Hashtable<String, float[][]> getEdges() {
		return edges;
	}
}
