package MeshAlgorithms;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;


import StatisticData.AngleDataDistribution;
import Tools.MeshTools;
import Tools.VectorTools;

import CalculateData.CalculateAngleData;
import CalculateData.CalculateDistanceData;
import IO.ProcessedData;


/**
 * 
 */

/**
 * @author Piotr Bugaj
 * @date June 7, 2010
 */
public class ContourLinesCreator {

	/**The factors used for equality of point orientation**/
	private double epsilonFactor = 20;
	private double epsilonAngle = 1;

	/**Starting frame the vertex clusters will be created from**/
	private int frameNumber;
	/**The total number of frames in the animation**/
	private int totalFrames;
	
	/**The data  set that  describes how  each vertex  moves  in space  over a
	 * period of time**/
	private float [][][] motionTrails;

	/**An inverse table giving  the vertex index in  the 'motion trails' array
	 * for each vertex. This dataset is only to be stored for one frame as the
	 * indexes of the other frames  can be figured out by following allong the
	 * motions trails within the 'motion trails' array**/
	private Hashtable<String, Integer> vertexToIndex;

	/**Array storing the angle for each pair of vertexes  in the neighbourhood
	 * for each vertex.  For memory  convenience, the array will store boolean
	 * valeus, indicating whether  or not the angle between vertexes is  below
	 * or a above a certain threshold.**/
	/**Array = [vertex number][angle between i and (i+1...n)]**/
	@SuppressWarnings("unused")
	private boolean [][] neighbouringAngles_N;
	@SuppressWarnings("unused")
	private boolean [][] neighbouringAngles_M;

	/**The frame where the current vertex cluster are being found in**/
	private int frame_N;
	private int frame_L;

	/**The data containing the location of all vertexes in all  time frames**/
	private ProcessedData [] processedData;

	/**Class used as a helper tool for analysing the mesh**/
	private MeshTools mTools;
	private VectorTools vectorTools;


	/**The minimal size of a cluster**/
	private final int minCluster = 20;

	/**--------------------------------------------------------------------**/
	/**Variables used for comparing the orientation of vertexes between meshes
	 * in frame i and frame i+1**/
	/**--------------------------------------------------------------------**/

	/**The neighbouring vertexes belonging  to each vertex in frame  i**/
	private Hashtable<String, float[][]> vertexNeighbours_N;
	/**The edges belonging to  the mesh in  frame i**/
	private Hashtable<String, float[][]> edges_N;

	/**The faces belonging to  the mesh in  frame i**/
	int [][]faces_N;
	/**The vertexes belonging to  the mesh in  frame i**/
	float [][] vertexes_N;	

	/**Variables controlling how the equalities will calculated.
	 * Whether legnth, angle, or plance comparison will be used.**/
	private final boolean lengthCalc = true;
	private final boolean planeCalc = false;
	private final boolean angleCalc = true;
	
	
	/**Whether  or not  the  angles should be pre-
	 * calculated and stored in physical memory**/
	private boolean precalculateAngles = false;
	
	/**Variable   indicating   whether   or   not   the
	 * calculated angle data has already been stored**/
	private static boolean angleDataCalculated = false;
	
	
	/**Whether  or not  the  angles should be pre-
	 * calculated and stored in physical memory**/
	private boolean precalculateDistances =  false;
	
	/**Variable   indicating   whether   or   not   the
	 * calculated angle data has already been stored**/
	private static boolean distanceDataCalculated = false;
	
	/**Name of the file where the precalculated
	 * angle values will be stored**/
	private String precalcAngFileName = "temp/angData";
	/**Name of the file where the precalculated
	 * distance values will be stored**/
	private String precalcDisFileName = "temp/disData";
	
	/**Minimal distance allowed between vertex at two
	 * different frames.  Vertexes too close will not
	 * be allowed in the creation of the surfaces.**/
	private float dist;
	/**The maximum number of frames identical
	 * sub-mesh  surfaces  will  found for**/
	private int trailDistance;
	
	/**Class constructor. Takes in the data of all meshes, including the array
	 * describing the movement in space of each  vertex and the current  frame
	 * the animation is at**/
	public ContourLinesCreator(ProcessedData [] processedData, 
			float[][][] motionTrails, int currentFrame) {

		System.out.println("Creating contour lines for frame: " + currentFrame);
		
		this.frameNumber = currentFrame;
		this.processedData = processedData;
		this.motionTrails = motionTrails;
		this.totalFrames = processedData.length;
		
		mTools = new MeshTools();
		vectorTools = new VectorTools();
		

		/**Calculate angle difference and distances  for each vertex
		 * within the mesh objects if needed for statistical data**/
		if(precalculateAngles) {
			if(!angleDataCalculated) {
				CalculateAngleData cad =
					new CalculateAngleData(processedData);
				try {
					cad.precalculateAngles(precalcAngFileName);
				} catch (IOException e) {
					System.out.println("Failed to write to " +
						"angle data file.");
					System.exit(1);
					e.printStackTrace();
				}

				AngleDataDistribution add = new AngleDataDistribution(processedData, motionTrails);
				int [] p = add.getAngleDiffDistr(cad, precalcAngFileName, 0, 10, 0.1);
				for(int i = 0; i < p.length; i++) {
					System.out.println(p[i] + " ");
				}
				angleDataCalculated = true;
				
			}
		}

		if(precalculateDistances) {
			if(!distanceDataCalculated) {
				CalculateDistanceData cdd =
					new CalculateDistanceData(processedData, precalcDisFileName);
				try {
					cdd.precalculateDistance();
				} catch (IOException e) {
					System.out.println("Failed to write to " +
						"distance data file.");
					System.exit(1);
					e.printStackTrace();
				}

				try {
					@SuppressWarnings("unused")
					Hashtable<String, Float> f  = cdd.getDistanceData(precalcAngFileName,0);
					f  = cdd.getDistanceData(precalcAngFileName, 1);
					f  = cdd.getDistanceData(precalcAngFileName, 3);
					f  = cdd.getDistanceData(precalcAngFileName, 2);
					
				} catch (IOException e) {
					System.out.println("Failed to read " +
					"distance data from file.");
					e.printStackTrace();
				}
				distanceDataCalculated = true;
			}
		}
	}
	
	/**Initialize the variables needed for comparing frame i and frame i+1**/
	private void initRequiredVariables(int frame_vars_to_initialize) {

		frame_N = frame_vars_to_initialize;
		frame_L = frame_vars_to_initialize-1;
		if(frame_N == 0) {
			frame_L = totalFrames-1;
		}

		/**First find all the edges existing within the current frame.
		 * These edges will be required to calculate the neighbouring
		 * faces for each face**/
		vertexes_N = processedData[frame_N].getFloatData(0);
		faces_N = processedData[frame_N].getIntData(2);
		edges_N = mTools.findAllEdges(vertexes_N, faces_N);
		
		/**Now using the edges, construct the vertex neighbours**/
		vertexNeighbours_N = mTools.getVertexNeighbours(edges_N);

		/**Initialize  the hashtable  for  accessing  the
		 * motionTrails by vertex at the current frame**/
		vertexToIndex = new Hashtable<String, Integer>();
		for(int i = 0; i < motionTrails.length; i++) {
			vertexToIndex.put(mTools.vForm(motionTrails[i][frame_N]), i);
		}

	}

	/**Find contour lines created from the motion trails of the vertexes**/
	/**Params:
	 * dist: the distance between the vertex points allowed when creating the
	 * motion effects. Hence the motion effect won't appear for vertexes too
	 * close to the mesh.
	 * **/
	public Vector<VertexCluster> getContourLines(
			int trailDistance, float dist) {
		this.dist = dist;
		this.trailDistance = trailDistance;
		
		/**The algorithm will work as follows: It will start at one vertex and
		 * visit   other  vertexes  from  that  vertex  and mark these visited 
		 * vertexes as being  part of the  same vertex cluster.  Vertexes  are
		 * part of the same cluster if the surface grid they form appears both
		 * in the current and previous time frame.

		 * Once  such  cluster  is  found, all  the vertexes belonging to this
		 * cluster  will be  checked of  and the algorithm will start at a new
		 * vertex  that has  not yet  been checked  of, to  create a different
		 * cluster.

		 * Once  all the  clusters are found,  the algorithm will  recursively
		 * operate on each cluster alone, grouping vertexes within one cluster
		 * into more seperate sub-clusters that contain vertexes that form the
		 * same identical surfaces within  the last three time frames,  and so
		 * on.**/

		/**Vector containing  all the vertex clusters.  More specifically,  it
		 * contains all the vertex clusters for  each frame.  A vertex cluster
		 * for  frame i is  a cluster of vertexes that create a grid  of lines
		 * that appear in the same  similar orientation  with  respect to each
		 * other for  the last  i frames.  Here the term orientation refers to
		 * how vertexes are oriented relative to each other, by distance,plane
		 * and 3D angle measurements. **/
		Vector<VertexCluster> vertexClusters = new Vector<VertexCluster>();

		/**The  initial set of vertexes  the first  set of  clusters  will  be
		 * created from.  This will  include all the vertexes within the given
		 * mesh at the given frame**/
		VertexCluster cluster_zero = new VertexCluster(0, frameNumber);
		cluster_zero.addVertexes(processedData[frameNumber].getFloatData(0));
		vertexClusters.add(cluster_zero);

		/**Using  iteration, get vertex clusters  ('c_i') for frame i from the
		 * vector 'vertexClusters'. Then create vertex clusters ('c_i_plus_1')
		 * for  frame i + 1  using  clusters  'c_i'.   Finally   add  clusters
		 * 'c_i_plus_1'  to the  vector 'vectorClusters',  thus  updating  the
		 * vector.**/

		/**Variable keeping track of the
		 * number of the  last  clusters
		 * added    to   the   vector**/
		int lastNumberOfClusters = 1;

		/**TODO: last touched**/
		for(int i = 0; i < this.trailDistance; i++) {

			/**Get the vertex clusters for frame i**/
			VertexCluster [] vertexClusters_i =
				getVertexClustersAtLevel_i(
						i, lastNumberOfClusters, vertexClusters);

			/**Create the vertex cluster for frame
			 * i+1 given  cluster  for frame  i**/
			VertexCluster [] vertexCluster_i_plus_1 =
				createVertexClusters(vertexClusters_i, frameNumber, i+1);

			/**Update the vertex of clusters**/
			addVertexClusters(vertexCluster_i_plus_1, vertexClusters);

			/**Store   the  size  of  the  last  number
			 * of clusters added as  useful information
			 * for  the   next  upcoming   iteration**/
			lastNumberOfClusters = vertexCluster_i_plus_1.length;
			
			
			if(frameNumber == 0) {
				frameNumber = this.totalFrames-1;
			} else {
				frameNumber--;
			}
		}

		return vertexClusters;
	}

	/**Given vertex clusters 'vertexClusters_i' for frame i, create the vertex
	 * clusters for frame i+1**/
	private VertexCluster [] createVertexClusters(
			VertexCluster[] vertexClusters_i,
			int mesh_frame_i,
			int nextFrameLevel) {

		/**Initialize the required variables**/
		initRequiredVariables(mesh_frame_i);

		/**TODO: Change this so that an array of clusters is returned**/
		Vector<VertexCluster> retCluster =  new Vector<VertexCluster>();

		/**Iterate through each cluster, creating sub-clusters**/
		for(int i_cluster = 0; i_cluster < vertexClusters_i.length; i_cluster++) {

			VertexCluster cluster_i =  vertexClusters_i[i_cluster];
			
			float [][]cluster_i_vertexes = cluster_i.getVertexes();

			Vector<String> cluster_i_vertexes_vector = new Vector<String>();
			for(int i = 0; i < cluster_i_vertexes.length; i++){
				float [] tempVertex = cluster_i_vertexes[i];
				
				cluster_i_vertexes_vector.add(mTools.vForm(tempVertex));
			}
			
			
			/**Create an array that will keep track of
			 * which vertex the algorithm went over**/
			Hashtable<String, Boolean> markedVertexes = new Hashtable<String, Boolean>();
			for(int j = 0; j < cluster_i_vertexes.length; j++) {
				markedVertexes.put(mTools.vForm(cluster_i_vertexes[j]), false);
			}

			/**Now iterate  through each  vertex in cluster_i  and
			 * group corresponding vertexes into sub-clusters. **/
			int countt = 0;
			for(int j=0; j < cluster_i_vertexes.length; j++){
				
				/**Check that the vertex being looked at is not too close to
				 * the corresponding vertex on the mesh**/
				int sI = this.vertexToIndex.get(mTools.vForm(cluster_i_vertexes[j]));
				float [] originalVertex = motionTrails[sI][frame_L];
				if(vectorTools.distance(originalVertex, cluster_i_vertexes[j]) < dist) {
					continue;
				}
				
				VertexCluster temp = createCluster(
						cluster_i_vertexes[j],
						markedVertexes,
						nextFrameLevel,
						cluster_i_vertexes_vector);

				/**Check that the return  cluster is of size  at least greater
				 * then minCluster. Otherwise disregard the cluster and unmark
				 * the vertexes that were marked within that cluster**/
				if(temp != null) {
					countt++;
					retCluster.add(temp);
				}

			}
		}

		VertexCluster [] retArray = new VertexCluster[retCluster.size()];
		Enumeration<VertexCluster> tempIter = retCluster.elements();
		int counter = 0;
		while(tempIter.hasMoreElements()) {
			VertexCluster nextCluster = tempIter.nextElement();
			retArray[counter] = nextCluster;
			counter++;
		}
		
		return retArray;
	}

	
	/**Given vertex 'start', find the connecting vertexes, that together
	 * combined form a grid of line that appear in the previous frame as
	 * well**/
	private VertexCluster createCluster(
			float [] startVertex,
			Hashtable<String, Boolean> markedVertexes,
			int nextFrameLevel,
			Vector<String> cluster_i_vertexes_vector){

		/**Before creating a cluster from this vertex check
		 * that  it  is  not  already part  of a cluster**/
		if(markedVertexes.get(mTools.vForm(startVertex))){
			return null;
		}

		/**The  resulting vertex  cluster that  will be created at the  end of
		 * this function**/
		VertexCluster vc = new VertexCluster(nextFrameLevel, frame_L);

		/**Linked list containing the edges of the vertex cluster that are yet
		 * to be checked and possibly expanded**/
		LinkedList <float[]> clusterBoundary = new LinkedList<float[]>();

		/**Vector containing all the vertexes that have been gone  over by the
		 * algorithm and are part of this current cluster being made**/
		Hashtable<String, float[]> iteratedVertexes = new Hashtable<String, float[]>();

		/**Add  the starting  vertex to the list as it is part of the starting
		 * boundary**/
		mTools.addVertexToTable(iteratedVertexes, startVertex);
		
		/**Go through all possible pair of vertexes that
		 * touch  the starting  vertex, 'startVertex'**/
		float [][]tempNeighbours = vertexNeighbours_N.get(mTools.vForm(startVertex));
		
		/**TODO: while the boundary is being expanded make sure the starting vertex is not reached
		 * as it is not part of the boundary.**/

		float [][][] possiblePairs =
			new float[((tempNeighbours.length-1)*tempNeighbours.length)/2][2][3];
		int possiblePairsCounter = 0;
		for(int i = 0; i < tempNeighbours.length; i++) {
			for(int j = (i+1); j < tempNeighbours.length; j++) {

				/**Check whether  the points i, j  and startVertex are aligned
				 * in the same orientation in both the ith and  (i+1)th frame.
				 * Here  the orientation  is dependent on the distance between
				 * the   points  defined   by  the   cartesian  norm  and  the
				 * 2-Dimensional angle**/
				
				/**Now find the same information for vertexes in the next frame**/
				int i1 = vertexToIndex.get(mTools.vForm(tempNeighbours[i]));
				int i2 = vertexToIndex.get(mTools.vForm(tempNeighbours[j]));
				int i3 = vertexToIndex.get(mTools.vForm(startVertex));

				float [] tempNeighboursi_M = motionTrails[i1][frame_L];
				float [] tempNeighboursj_M = motionTrails[i2][frame_L];
				float [] startVertex_M = motionTrails[i3][frame_L];

				
				/**Calculate the difference in distances**/
				boolean dist1DiffComp = true;
				boolean dist2DiffComp = true;
				if(this.lengthCalc) {
					/**Distance between i and start vertex at the ith frame**/
					float tempDistance1_N = vectorTools.distance(tempNeighbours[i], startVertex);
					/**Distance between j and start vertex at the ith frame**/
					float tempDistance2_N = vectorTools.distance(tempNeighbours[j], startVertex);
					
					/**Distance between i and start vertex at the i+1th frame**/
					float tempDistance1_M = vectorTools.distance(tempNeighboursi_M, startVertex_M);
					/**Distance between j and start vertex at the i+1th frame**/
					float tempDistance2_M = vectorTools.distance(tempNeighboursj_M, startVertex_M);
					
					dist1DiffComp = (Math.abs(tempDistance1_M - tempDistance1_N)< ((tempDistance1_M + tempDistance1_N)/epsilonFactor));
					dist2DiffComp = (Math.abs(tempDistance2_M - tempDistance2_N) < ((tempDistance2_M + tempDistance2_N)/epsilonFactor));
				}
				
				
				/**Calculate the difference in angles**/
				boolean angleDiffComp = true;
				if(this.angleCalc) {
					float angle_N = vectorTools.ang(
							tempNeighbours[i],
							startVertex,
							tempNeighbours[j]);
					float angle_M = vectorTools.ang(
							tempNeighboursi_M,
							startVertex_M,
							tempNeighboursj_M);
				
					angleDiffComp= (Math.abs(angle_M-angle_N) < epsilonAngle);
				}

				/**Check for equalities**/
				if(angleDiffComp  && dist1DiffComp  && dist2DiffComp) {
					possiblePairs[possiblePairsCounter] =
						new float[][]{tempNeighbours[j],tempNeighbours[i]};
					possiblePairsCounter++;
				}
			}	
		}

		/**Choose a pair of points and expand a cluster from those
		 * points.If the cluster is not expanded, continuously try
		 * choosig the next pair**/
		pairLoop: while(possiblePairsCounter > 0) {
			possiblePairsCounter--;
			float [] vertex_a = possiblePairs[possiblePairsCounter][0];
			float [] vertex_b = possiblePairs[possiblePairsCounter][1];

			int i1 = vertexToIndex.get(mTools.vForm(vertex_a));
			int i2 = vertexToIndex.get(mTools.vForm(vertex_b));
			int i3 = vertexToIndex.get(mTools.vForm(startVertex));

			float [] vertex_a_p = motionTrails[i1][frame_L];
			float [] vertex_b_p = motionTrails[i2][frame_L];
			float [] startVertex_p = motionTrails[i3][frame_L];
			vc.addEdge(vertex_a_p, startVertex_p);
			vc.addEdge(vertex_b_p, startVertex_p);
			
			clusterBoundary.add(startVertex);
			clusterBoundary.add(vertex_a);
			clusterBoundary.add(vertex_b);

			mTools.addVertexToTable(iteratedVertexes, startVertex);
			mTools.addVertexToTable(iteratedVertexes, vertex_a);
			mTools.addVertexToTable(iteratedVertexes, vertex_b);

			
			/**Now constantly  expand the  cluster boundary  until no new vertexes
			 * are added**/
			while(!clusterBoundary.isEmpty()) {

				/**Take out a vertex at  the  boundary of the  cluster and  expand
				 * from that vertex, marking any  new vertexes  visited along  the
				 * way. Also, when new vertexes are found, add them to the cluster
				 * boundary and store them with the rest of  the vertexes  visited
				 * so far in the vector 'iteratedVertexes' **/
				expandBoundary(clusterBoundary.pop(),
						vc,
						markedVertexes,
						clusterBoundary,
						iteratedVertexes,
						cluster_i_vertexes_vector);
			}
			

			/**Check that the created cluster is of size greater than three.
			 * Otherwise it failed to expand  for the  given three  vertexes
			 * so undo the marked vertex and try to expand using a different
			 * pair of points.**/
			if(iteratedVertexes.size() < minCluster) {
				if(iteratedVertexes.size() >= 3) {
					break;
				}
				
				iteratedVertexes.clear();
				clusterBoundary.clear();
				vc.removeAllEdges();
				
				mTools.addVertexToTable(iteratedVertexes, startVertex);
				
			} else {
				break pairLoop;
			}
		}

		/**Make sure the start vertex is marked**/
		markedVertexes.put(mTools.vForm(startVertex), 
				true);

		/**Don't mark the vertexes as  a cluster of three  vertexes can not be
		 * described perfectly in terms of 3Dimensional orientation  and hence
		 * its impossible to say  whether the  vertexes in this cluster can be
		 * part of another larger cluster**/
		if(iteratedVertexes.size() <= 3) {
			return null;
		}

		Enumeration<float[]> clusterEnum = iteratedVertexes.elements();
		float [][] tempVertexes = new float[iteratedVertexes.size()][3];
		int i = 0;
		while(clusterEnum.hasMoreElements()) {
			float []tempElement = clusterEnum.nextElement();

			int tempIndex =
				this.vertexToIndex.get(mTools.vForm(tempElement));
			float[] prevElement = motionTrails[tempIndex][frame_L];
			
			/**TODO: last touched**/
			tempVertexes[i] = prevElement;
			i++;
			/**Mark the iterated vertexes. Note, the start vertex might
			 * get marked twice**/
			markedVertexes.put(mTools.vForm(tempElement), true);
		}

		vc.addVertexes(tempVertexes);
		return vc;
	}

	/**Given a cluster of at least  three vertexes,  expand given the vertexes
	 * at the boundaries**/
	/**Note that the starting  cluster only  has three  vertexes which
	 * are not  oriented relative to each other by a straight angle**/
	private int expandBoundary(float[] boundaryStartVertex,
			VertexCluster vc,
			Hashtable<String, Boolean> markedVertexes,
			LinkedList<float[]> clusterBoundary,
			Hashtable<String, float[]> iteratedVertexes,
			Vector<String> cluster_i_vertexes_vector) {

		/**Check that the vertex being looked at is not too close to
		 * the corresponding vertex on the mesh**/
		int sI = this.vertexToIndex.get(mTools.vForm(boundaryStartVertex));
		float [] originalBoundaryStartVertex = motionTrails[sI][frame_L];
		if(vectorTools.distance(originalBoundaryStartVertex, boundaryStartVertex) < dist) {
			return 0;
		}
		
		/**First find to  vertexes part of the cluster that, together,  with
		 * the  boudary  starting  vertex,  form  an  angle,  as oppose to a
		 * straight line. For convenience, first check if these two vertexes
		 * are  neighbours  of the boundary start vertex, as the possibility
		 * of an angle is much greater.**/
		boolean firstSearch = false;
		float [][] tempNeighbours =
			vertexNeighbours_N.get(mTools.vForm(boundaryStartVertex)
			);
		float [] vertex_a = null;
		float [] vertex_b = null;

		topLoop1: for(int i = 0; i < tempNeighbours.length; i++) {
			vertex_a = tempNeighbours[i];
			for(int j = (i+1); j < tempNeighbours.length; j++) {
				vertex_b = tempNeighbours[j];

				/**First check that the vertexes are part of the cluster**/
				if(iteratedVertexes.containsKey(mTools.vForm(vertex_a)) &&
						iteratedVertexes.containsKey(mTools.vForm(vertex_b))) {

					/**Check that the angle is correct between the points**/
					float tempAngle =
						vectorTools.ang(vertex_a, boundaryStartVertex, vertex_b);
					if(tempAngle <= 175) {
						firstSearch = true;
						break topLoop1;
					}
				}

				vertex_b = null;
			}
			vertex_a = null;
		}

		/**If no vertices have been  found in the  neighbourhood of  the start
		 * vertex, search for a chain of two  vertexes belonging to a cluster,
		 * with the chain starting at the boundary start vertex**/
		/**Note: in  this worst case, the method might  become computationally
		 * expensive if all chain of two vertexes have to be checked**/
		if(!firstSearch) { /**START**/
			topLoop2:	for(int i = 0; i < tempNeighbours.length; i++) {
				vertex_a = tempNeighbours[i];

				/**Check that the vertex is part of the cluster**/
				if(iteratedVertexes.containsKey(mTools.vForm(vertex_a))){

					/**Next look for the second vertex in the chain**/
					float [][]firstVertexNeighbours = 
						vertexNeighbours_N.get(mTools.vForm(vertex_a));
					for(int j = 0; j < firstVertexNeighbours.length; j++) {
						vertex_b = firstVertexNeighbours[j];

						if(iteratedVertexes.containsKey(mTools.vForm(vertex_b))
								&&
								((vertex_b[0] != boundaryStartVertex[0]) ||
								 (vertex_b[1] != boundaryStartVertex[1]) ||
						   		 (vertex_b[2] != boundaryStartVertex[2]))
						){
							/**Check that the angle is correct between the points**/
							float tempAngle =
								vectorTools.ang(boundaryStartVertex, vertex_a, vertex_b);
							if(tempAngle < 170) {
								break topLoop2;
							}
						}

						vertex_b = null;
					}
				}

				vertex_a = null;
			}
		}/**END**/

		/**Return if no vertexes are found**/
		if((vertex_a == null) || (vertex_b == null)) {
			return 0;
		}


		/**How  find  possible  vertexes  not  part of  the cluster  that  are
		 * connected to the boundary start vertex**/
		float [][]  newVertexes = new float[tempNeighbours.length][];
		int counter = 0;

		for(int i = 0; i < tempNeighbours.length; i++) {
			newVertexes[counter] = tempNeighbours[i];

			/**Add a possible vertex from the boundary for expansion**/
			if(!iteratedVertexes.containsKey(mTools.vForm(newVertexes[counter]))
					&& 
				cluster_i_vertexes_vector.contains(mTools.vForm(newVertexes[counter]))
					
			) {
				counter++;

			/**If a neighbouring  vertex already  belongs to the  cluster, the
		     * edge to that following vertex must still be stored**/
			} else {
				
				int i1 = vertexToIndex.get(mTools.vForm(newVertexes[counter]));
				int i2 = vertexToIndex.get(mTools.vForm(boundaryStartVertex));

				float [] newVertexes_p = motionTrails[i1][frame_L];
				float [] boundaryStartVertex_p = motionTrails[i2][frame_L];

				vc.addEdge(newVertexes_p, boundaryStartVertex_p);

				mTools.addVertexToTable(iteratedVertexes, boundaryStartVertex);
				mTools.addVertexToTable(iteratedVertexes, newVertexes[counter]);
			}
		}


		/**If no new vertexes are found, return**/
		if(counter == 0) {
			return 0;
		}

		/**Now iterate through all the new neighbours**/
		for(int i = counter-1; i >= 0; i--) {
			
			/**Now find these corresponding vertexes in the next frame**/
			int i1 = vertexToIndex.get(mTools.vForm(vertex_a));
			int i2 = vertexToIndex.get(mTools.vForm(vertex_b));
			int i3 = vertexToIndex.get(mTools.vForm(boundaryStartVertex));
			int i4 = vertexToIndex.get(mTools.vForm(newVertexes[i]));;

			float [] vertex_a_M = motionTrails[i1][frame_L];
			float [] vertex_b_M = motionTrails[i2][frame_L];
			float [] boundaryStartVertex_M = motionTrails[i3][frame_L];
			float [] newVertex_M = motionTrails[i4][frame_L];

			/**Now check if the 4th point is oriented relative to the three points
			 * the same way in both frames**/
			
			boolean lengthDiffComp1 = true;
			boolean lengthDiffComp2 = true;
			if(this.lengthCalc) {
				float L1_N = vectorTools.distance(boundaryStartVertex, newVertexes[i]);
				float L1_M = vectorTools.distance(boundaryStartVertex_M, newVertex_M);
	
				float L2_N = vectorTools.distance(vertex_a, newVertexes[i]);
				float L2_M = vectorTools.distance(vertex_a_M, newVertex_M);
	
				lengthDiffComp1 = (Math.abs(L1_N - L1_M) < ((L1_N + L1_M)/epsilonFactor));
				lengthDiffComp2 = (Math.abs(L2_N - L2_M) < ((L2_N + L2_M)/epsilonFactor));
			}

			boolean angleDiffComp = true;
			if(this.angleCalc) {
				float A_N = vectorTools.ang(vertex_a, boundaryStartVertex, newVertexes[i]);;
				float A_M = vectorTools.ang(vertex_a_M, boundaryStartVertex_M, newVertex_M);
				
				angleDiffComp = (Math.abs(A_N - A_M) < epsilonAngle);
			}

			boolean planeDiffComp = true;
			if(this.planeCalc) {
				float plane_Side_N = vectorTools.dot(vectorTools.cross(vectorTools.sub(vertex_a, boundaryStartVertex),
						vectorTools.sub(vertex_b, boundaryStartVertex)), 
						newVertexes[i]);
	
				float plane_Side_M = vectorTools.dot(vectorTools.cross(vectorTools.sub(vertex_a_M, boundaryStartVertex_M), 
						vectorTools.sub(vertex_b_M, boundaryStartVertex_M)),
						newVertex_M);
				
				planeDiffComp = (  ((plane_Side_N < 0) && (plane_Side_M < 0)) ||
						((plane_Side_N > 0) && (plane_Side_M > 0)) ||
						(plane_Side_N == plane_Side_M));
			}

			/**Check equality of orientation**/
			if(angleDiffComp &&
					lengthDiffComp1 &&
					lengthDiffComp2 &&
					planeDiffComp) {

				/**Add the vertex  to the  cluster.   If vertex  is
				 * unmarked, add to the cluster boundary as well**/
				if(!markedVertexes.get(mTools.vForm(newVertexes[i]))) {
					clusterBoundary.push(newVertexes[i]);
				}

				mTools.addVertexToTable(iteratedVertexes, newVertexes[i]);
				mTools.addVertexToTable(iteratedVertexes, boundaryStartVertex);
				
				vc.addEdge(boundaryStartVertex_M, newVertex_M);
			}
		}
		return 0;
	}

	/**Given a vector of vertex cluster, find all the clusters in that  vector
	 * that contain vertexes belonging to the specified frame level**/
	private VertexCluster [] getVertexClustersAtLevel_i(
			int frameLevel,
			int numberOfClusters,
			Vector<VertexCluster> vertexClusters) {

		VertexCluster [] clustersAtFrameLevel =
			new VertexCluster[numberOfClusters];
		int counter = 0;
		for(int i = 0; i < vertexClusters.size(); i++) {
			
			VertexCluster temp = vertexClusters.get(i);

			if(temp.getFrameLevel() == frameLevel) {
				clustersAtFrameLevel[counter++] = temp;
			}
		}
		return clustersAtFrameLevel;
	}

	/**Update the vector of clusters by adding the vertex clusters for the new
	 * frame i+1**/
	private void addVertexClusters(VertexCluster [] vertexCluster_i_plus_1,
			Vector<VertexCluster> vertexClusters){
		
		for(int i = 0; i < vertexCluster_i_plus_1.length; i++) {
			vertexClusters.add(vertexCluster_i_plus_1[i]);
		}
	}
}
