package main;

import javax.media.opengl.GL;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.GLCanvas;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import IO.ObjectReader;
import IO.ProcessedData;
import MeshAlgorithms.ConstructedLines;
import MeshAlgorithms.ContourLinesCreator;
import MeshAlgorithms.LineCreator;
import MeshAlgorithms.VertexCluster;
import Splines.BezierSpline;
import Splines.TangentControlledBezierSegmentedSpline;
import Tools.MeshTools;
import Tools.VectorTools;
import WeightFunctions.ALineWeightFilter;

import com.sun.opengl.util.Animator;

/**
 * @author Piotr Bugaj
 * @date May 21, 2010
 */
public class MeshRenderer implements GLEventListener, KeyListener {
	/**Motion trails for a set of vertexes**/
	private float [][][] motionTrails;

	/**TODO: add control for density**/
	/**TODO: add control for line thickness**/
	/**TODO: add normal for lines**/
	/**TODO: add control for using alpha on lines**/
	/**TODO: add control for the mesh: on/of plus colour**/
	
	/**The number of object files to be read in to the animation**/
	private int numObjectFiles;

	/**The array storing all the data from each object file**/
	private ProcessedData [] processedData;

	/**Array storing the name of the object files**/
	private String [] objectDatafiles;

	/**Physical location of the model**/
	private String modelLocation;

	/**The   variable  that  will  allow   for   the
	 * alternation between different object files**/
	private int objectNumber = 0;

	/**Tools for dealing with vector**/
	private VectorTools vectorTools = new VectorTools();

	/**Variable stating whether or not the interpolated values
	 * for the motion trails should be stored when computed**/
	private final boolean storedLinesComputation = true;
	private boolean computedLinesYet[];
	private float [][][][] storedLinesInterpolation;

	/**Variable stating whether or not the calculated
	 * composed lines should be stored.**/
	private final boolean storedComposedLinesComputation = true;
	private boolean computedComposedLinesYet[];
	private float [][][][][][] storedComposedLinesData;
	private boolean basesComputed = false;
	
	/**Variable stating whether or not the faces for contour
	 * surfaces should be stored when computed**/
	private final boolean storedFacesComputation = true;
	private boolean computedFacesYet[];
	private float [][][][][] storedFacesData;

	/**Tool for computing various information about the mesh**/
	MeshTools mTools;

	/**Main GL variables**/
	private GLU glu = new GLU();
	private GLCanvas canvas = new GLCanvas();
	private Frame frame;
	private Animator animator = new Animator(canvas); 
	private GL gl;

	/**Gui tools for controlling the rotation and movement
	 * interaction, as well as speed and line trail length**/
	private Container mainBottomBox;
	private Container mainTopBox;
	private Container mainTopPropertiesBox;
	private Container movementBox;
	private Container rotationBox;
	private Container speedBox;
	private Container tailLengthBox;
	private Container interpTypeBox;
	private Container motionEffectBox;
	private Container lineColourBox;
	private Container afterImageColourBox;
	private Container weightFunctionBox;

	private Scrollbar xAxisMovement;
	private static float xMovementScroller = 0;
	private TextComponent xAxisMovementText;

	private Scrollbar yAxisMovement;
	private static float yMovementScroller = 0;
	private TextComponent yAxisMovementText;

	private Scrollbar zAxisMovement;
	private static float zMovementScroller = 0;
	private TextComponent zAxisMovementText;

	private Scrollbar xRotation;
	private static float xRotationScroller = 0;
	private TextComponent xRotationText;

	private Scrollbar yRotation;
	private static float yRotationScroller = 0;
	private TextComponent yRotationText;

	private Scrollbar zRotation;
	private static float zRotationScroller = 0;
	private TextComponent zRotationText;

	/**Variable describing the dimension of the GUI**/
	private final int frameWidth = 980;
	private final int frameHeight = 570;

	/**Label used for pop-up meny box**/
	Label propertiesText = new Label("Properties");

	/**GUI for controlling the speed**/
	private Scrollbar speedBar;
	private static float speedScroller = 19;
	private TextComponent speedTextComponent;
	private int speedIntervalCounter = 0;

	/**GUI for controlling the length of the motion trails**/
	private Scrollbar trailLengthBar;
	private static int trailLengthBarScroller = 10;
	private TextComponent trailLengthTextComponent;

	/**GUI for controlling the interpolation of the line**/
	private Choice interpTypeChooser;
	private String interpType = "None";

	/**GUI for controlling the motion effect**/
	private Choice motionEffectChooser;
	private String motionEffect = "Lines";

	/**GUI for controlling the colour of lines**/
	private Choice lineColourChooser;
	private String lineColour = "BLACK";

	/**GUI for controlling the colour of the after images**/
	private Choice afterImageColourChooser;

	/**GUI for controlling the weight function**/
	private Choice weightFunctionChooser;
	@SuppressWarnings({ "unused"})
	private String weightFunctionEffect = "None";

	/**This display is updated as the animation plays**/
	public void display(GLAutoDrawable gLDrawable) {

		/**Reset the frame number to zero once it reaches the top**/
		if(objectNumber >= numObjectFiles) {
			objectNumber = 0;
		}

		/**Initialize JOGL**/
		gl = gLDrawable.getGL();

		/**Initialize the lighting affect**/
		float[] lightDiffuse = {0.8f, 0.8f, 0.8f, 1.0f};
		float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
		float[] lightSpecular = {0.5f, 0.5f, 0.5f, 1.0f};
		float[] lightPosition= {0.0f, 0.0f, 5.0f, 1.0f};

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, lightSpecular, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightPosition, 0);

		/**Turn on the lights**/
		gl.glEnable(GL.GL_LIGHT0);
		gl.glEnable(GL.GL_LIGHTING);

		/**Initialize the shade model and material**/
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glEnable(GL.GL_COLOR_MATERIAL);

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		gl.glPushMatrix();


		/**Draw a background**/
		gl.glPushMatrix();
		gl.glBegin(GL.GL_POLYGON);

		gl.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);

		gl.glNormal3f(0.0f, 0.0f, 1.0f);
		gl.glVertex3d(-20,-20, -10);
		gl.glVertex3d(-20,20, -10);
		gl.glVertex3d(20,20, -10);
		gl.glVertex3d(20,-20, -10);

		gl.glEnd();
		gl.glPopMatrix();

		gl.glPushMatrix();

		/**Draw the mesh object**/
		gl.glTranslatef(-xMovementScroller*0.05f,
				-0.5f -yMovementScroller*0.05f,
				-2.5f + (-zMovementScroller*0.05f));

		/**Rotate the display into position**/
		gl.glRotatef(-xRotationScroller, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(-yRotationScroller, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(zRotationScroller, 0.0f, 0.0f, 1.0f);

		gl.glBegin(GL.GL_TRIANGLES);

		/**Retrieve the require arrays for the object number**/
		float [][] pointValues = processedData[objectNumber].getFloatData(0);
		int [][] triangleSurfaces = processedData[objectNumber].getIntData(2);
		float [][] normalValues = processedData[objectNumber].getFloatData(1);	

		for(int i = 0; i < triangleSurfaces.length; i++) {
			gl.glColor4f(0.4f, 0.4f, 1.0f, 1.0f);

			gl.glNormal3f(
					normalValues[triangleSurfaces[i][0]-1][0],
					normalValues[triangleSurfaces[i][0]-1][1],
					normalValues[triangleSurfaces[i][0]-1][2]);
			gl.glVertex3d(
					pointValues[triangleSurfaces[i][0]-1][0],
					pointValues[triangleSurfaces[i][0]-1][1],
					pointValues[triangleSurfaces[i][0]-1][2]);

			gl.glNormal3f(
					normalValues[triangleSurfaces[i][1]-1][0],
					normalValues[triangleSurfaces[i][1]-1][1],
					normalValues[triangleSurfaces[i][1]-1][2]);
			gl.glVertex3d(
					pointValues[triangleSurfaces[i][1]-1][0],
					pointValues[triangleSurfaces[i][1]-1][1],
					pointValues[triangleSurfaces[i][1]-1][2]);

			gl.glNormal3f(
					normalValues[triangleSurfaces[i][2]-1][0],
					normalValues[triangleSurfaces[i][2]-1][1],
					normalValues[triangleSurfaces[i][2]-1][2]);
			gl.glVertex3d(
					pointValues[triangleSurfaces[i][2]-1][0],
					pointValues[triangleSurfaces[i][2]-1][1],
					pointValues[triangleSurfaces[i][2]-1][2]);
		}
		gl.glEnd();


		/**Draw the motion lines**/

		/**Set up the line colours**/
		float [] currentColour = new float[3];
		if(lineColour.equals("BLACK")) {
			currentColour = new float[]{0.0f, 0.0f, 0.0f};
		} else if(lineColour.equals("RED")) {
			currentColour = new float[]{1.0f, 0.0f, 0.0f};
		} else if(lineColour.equals("GREEN")) {
			currentColour = new float[]{0.0f, 1.0f, 0.0f};
		} else if(lineColour.equals("BLUE")) {
			currentColour = new float[]{0.0f, 0.0f, 1.0f};
		} else if(lineColour.equals("WHITE")) {
			currentColour = new float[]{1.0f, 1.0f, 1.0f};
		} else if(lineColour.equals("NAVY")) {
			currentColour = new float[]{0.098f, 0.48627f, 0.53725f};
		} else if(lineColour.equals("LIGHT PURPLE")) {
			currentColour = new float[]{231f/255f, 109f/255f, 208f/255f};
		} else if(lineColour.equals("BABY BLUE")) {
			currentColour = new float[]{182f/255f, 217f/255f, 222f/255f};
		} else if(lineColour.equals("FADED GREEN")) {
			currentColour = new float[]{112f/255f, 165f/255f, 137f/255f};
		} else if(lineColour.equals("GOLDEN YELLOW")) {
			currentColour = new float[]{240f/255f, 197f/255f, 10f/255f};
		} else if(lineColour.equals("PLATINUM")) {
			currentColour = new float[]{204f/255f, 198f/255f, 173f/255f};
		} else if(lineColour.equals("YELLOW")) {
			currentColour = new float[]{247f/255f, 252f/255f, 69f/255f};
		} else if(lineColour.equals("DARK BROWN")) {
			currentColour = new float[]{79f/255f, 73f/255f, 54f/255f};
		}        

		gl.glColor3f(currentColour[0], currentColour[1], currentColour[2]);

		float i1 = 1.2f/trailLengthBarScroller;

		/**Case where constructed lines will be made,
		 * composed  from  smaller  motion  trails**/
		if(motionEffect.equals("Constructed Lines")) {

			/**Compute the triangular bases that
			 * will be used within  each mess**/
			if(!basesComputed) {

				/**Create correspondance between face and index**/
				Hashtable<String, Integer> faceToIndex =
					mTools.createFacesToIndexCorrespondance(
							pointValues, triangleSurfaces);

				/**Get the edges existing within the mesh**/
				Hashtable<String, float[][]> edges =
					mTools.findAllEdges(pointValues, triangleSurfaces);

				/**Get the touching faces for each edge**/
				Hashtable<String, int[]> touchingFaces =
					mTools.getTouchingFaces(
							edges, triangleSurfaces, pointValues);

				/**Get the neighbouring faces for each face**/
				int[][]  faceNeighbours =
					mTools.getFaceNeighbours(
							pointValues, triangleSurfaces, touchingFaces);

				/**Create the constructed lines**/
				ConstructedLines cL = new ConstructedLines(
						faceNeighbours,
						faceToIndex,
						triangleSurfaces, 
						pointValues,
						trailingFaces);
			}
			
			float [][][][][] frame_i_e_i_m_edges = null;
			if(!storedComposedLinesComputation ||
					(storedComposedLinesComputation &&
							!computedComposedLinesYet[objectNumber])) {

				/**Given motion trails  and faces,  find the  faces that  face
				 * the same direction of the motion starting from that face**/
				Hashtable<String, Boolean> trailingFaces =
					mTools.getTrailingFaces(triangleSurfaces, normalValues,
							pointValues,
							motionTrails,
							numObjectFiles,objectNumber);

				System.out.println(trailingFaces.size());
				/**Create correspondance between face and index**/
				Hashtable<String, Integer> faceToIndex =
					mTools.createFacesToIndexCorrespondance(
							pointValues, triangleSurfaces);

				/**Get the edges existing within the mesh**/
				Hashtable<String, float[][]> edges =
					mTools.findAllEdges(pointValues, triangleSurfaces);

				/**Get the touching faces for each edge**/
				Hashtable<String, int[]> touchingFaces =
					mTools.getTouchingFaces(
							edges, triangleSurfaces, pointValues);

				/**Get the neighbouring faces for each face**/
				int[][]  faceNeighbours =
					mTools.getFaceNeighbours(
							pointValues, triangleSurfaces, touchingFaces);

				/**Create the constructed lines**/
				ConstructedLines cL = new ConstructedLines(
						faceNeighbours,
						faceToIndex,
						triangleSurfaces, 
						pointValues,
						trailingFaces);

				/**Get a correspondance between vertexes and the index**/
				Hashtable<String, Integer> vertexToIndex =
					mTools.createVertexToIndexCorrespondance(pointValues);

				/**Get the base of the composed lines**/
				Vector<int [][]> composedLineBases =
					cL.getConstructedLinesBase(2);
				
				Enumeration<int[][]>composedLineBasesEnum =
					composedLineBases.elements();

				int yCount = 0;
				frame_i_e_i_m_edges = new float[composedLineBases.size()][][][][];

				while(composedLineBasesEnum.hasMoreElements()) {

					/**Get the base faces**/
					int [][] composedBaseFaces =
						composedLineBasesEnum.nextElement();

					/**Get the edges forming this base of faces**/
					Hashtable<String, float[][]> baseEdges =
						mTools.findAllEdges(pointValues, composedBaseFaces);

					/**Get the list of touching faces for this base**/
					Hashtable<String, int[]> touchingBaseFaces =
						mTools.getTouchingFaces(
								baseEdges, composedBaseFaces, pointValues);

					/**Go  through the  edges and  collect the ones  that only
					 * have one face touching it (boundary edge)**/
					Hashtable<String, float [][]> baseBoundaryEdges =
						mTools.getBoundaryEdges(baseEdges, touchingBaseFaces);

					/**Using the base boundary edges, get all the other edges
					 * for  each frame required for constructing the line.**/
					float[][][][] e_i_m_edges =
						mTools.getEdgesFromBase(
								baseBoundaryEdges,
								motionTrails,
								vertexToIndex,
								trailLengthBarScroller,
								objectNumber,
								numObjectFiles,
								ALineWeightFilter.POW);

					frame_i_e_i_m_edges[yCount++] = e_i_m_edges;
					
				}

				storedComposedLinesData[objectNumber] = frame_i_e_i_m_edges;
				computedComposedLinesYet[objectNumber] = true;
			} else {
				frame_i_e_i_m_edges = storedComposedLinesData[objectNumber];
			}


			/**Render the composed line from the resulting edges**/
			for(int q = 0; q < frame_i_e_i_m_edges.length; q++) {

				if(!(q%20 == 0)) {
					continue;
				}
				
				float [][][][] e_i_m_edges = frame_i_e_i_m_edges[q];

				/**Iterate through each base i**/
				int n = e_i_m_edges.length;
				for(int k = 0; k < n-2; k++) {

					/**Iterate through each edge**/
					for(int g = 0; g < e_i_m_edges[k].length-1; g++) {

						gl.glBegin(GL.GL_TRIANGLES);

						gl.glVertex3d(
								e_i_m_edges[k][g][0][0],
								e_i_m_edges[k][g][0][1],
								e_i_m_edges[k][g][0][2]);

						gl.glVertex3d(
								e_i_m_edges[k][g][1][0],
								e_i_m_edges[k][g][1][1],
								e_i_m_edges[k][g][1][2]);

						gl.glVertex3d(
								e_i_m_edges[k+1][g][1][0],
								e_i_m_edges[k+1][g][1][1],
								e_i_m_edges[k+1][g][1][2]);
						gl.glEnd();	
						
						
						gl.glBegin(GL.GL_TRIANGLES);
						
						gl.glVertex3d(
								e_i_m_edges[k+1][g][1][0],
								e_i_m_edges[k+1][g][1][1],
								e_i_m_edges[k+1][g][1][2]);
						
						gl.glVertex3d(
								e_i_m_edges[k+1][g][0][0],
								e_i_m_edges[k+1][g][0][1],
								e_i_m_edges[k+1][g][0][2]);

						gl.glVertex3d(
								e_i_m_edges[k][g][0][0],
								e_i_m_edges[k][g][0][1],
								e_i_m_edges[k][g][0][2]);
						
						gl.glEnd();	
					}
				}

				for(int g = 0; g < e_i_m_edges[n-2].length-1; g++) {
					gl.glBegin(GL.GL_TRIANGLES);

					gl.glVertex3d(
							e_i_m_edges[n-2][g][0][0],
							e_i_m_edges[n-2][g][0][1],
							e_i_m_edges[n-2][g][0][2]);

					gl.glVertex3d(
							e_i_m_edges[n-2][g][1][0],
							e_i_m_edges[n-2][g][1][1],
							e_i_m_edges[n-2][g][1][2]);

					gl.glVertex3d(
							e_i_m_edges[n-1][g][1][0],
							e_i_m_edges[n-1][g][1][1],
							e_i_m_edges[n-1][g][1][2]);

					gl.glEnd();	
				}
			}
		}


		/**Case where contour lines are being drawn**/
		if(motionEffect.equals("Surface Tracking")) {

			/**Faces created and to be displayed for each contour surface**/
			float [][][][] contourFaces = null;

			/**Get the faces if they are already stored and computed**/
			if(storedFacesComputation && computedFacesYet[objectNumber] &&
					motionEffect.equals("Surface Tracking")){
				contourFaces = storedFacesData[objectNumber];
			}

			/**Create the object for creating the contour lines. The object will take the
			 * processedData (all the required 4D  information),  and the motion trails (
			 * the data set describing how each  vertex moves  in 3D  space over time)**/
			int lastFrameNum;
			int contourClusterCounter = 0;
			lastFrameNum = objectNumber - 1;
			if(objectNumber == 0) {
				lastFrameNum = numObjectFiles-1;
			}

			if(!storedFacesComputation || (storedFacesComputation && !computedFacesYet[objectNumber])) {

				ContourLinesCreator clc =
					new ContourLinesCreator(processedData, motionTrails, lastFrameNum);

				/**Get the created clusters**/
				Vector<VertexCluster> returnedClusters = clc.getContourLines();
				VertexCluster [] clusters_ret = new VertexCluster[returnedClusters.size()];
				returnedClusters.toArray(clusters_ret);

				/**Iterate through the clusters and store them in display format**/
				/**Skip the first cluster as it simply contains the vertexes**/
				contourFaces = new float[clusters_ret.length-1][][][];
				for(int i = 1; i < clusters_ret.length; i++) {

					/**Get the cluster**/
					VertexCluster cluster_i = clusters_ret[i];

					/**Get the cluster vertexes**/
					float [][] clusterVertexes = cluster_i.getVertexes();

					/**Get the cluster edges**/
					Hashtable<String, float[][]> clusterEdges = cluster_i.getEdges(); 

					/**Varaiable that will store the vertexes corresponding to
					 * the faces created**/
					Hashtable<String, float[]> faceVertexesHashtable = new Hashtable<String, float[]>();

					/**Get the neighbouring vertexes for each vertex making up a cluster**/
					Hashtable<String, float[][]> clusterVertexNeighbours = mTools.getVertexNeighbours(clusterEdges);

					/**Get the cluster faces in the form float[][]**/
					Hashtable<String, float[][]>  clusterFaces =
						mTools.getComposedFaces(clusterEdges, clusterVertexNeighbours, clusterVertexes, faceVertexesHashtable);

					/**Get the cluster faces in array form: int [][]**/
					float [][][] clusterFacesArrayForm = new float[clusterFaces.size()][][];
					int fcount = 0;
					Enumeration<float[][]> tempFaceEnum = clusterFaces.elements();
					while(tempFaceEnum.hasMoreElements()){
						float [][] tempFace = tempFaceEnum.nextElement();

						clusterFacesArrayForm[fcount++] = tempFace;
					}

					/**Smoothen the edges of the mesh cluster parts**/
					boolean smoothCutEdges = false;
					if(smoothCutEdges) {

						/**Get the corresponding vertexes making up the faces**/
						float[][] faceVertexes = new float[faceVertexesHashtable.size()][];
						Enumeration<float[]> enumm = faceVertexesHashtable.elements();
						int fVerCount = 0;
						while(enumm.hasMoreElements()) {
							float[] tempFaceVertex = enumm.nextElement();
							faceVertexes[fVerCount++] = tempFaceVertex;
						}

						/**Create a vertex to index correspondance for face vertexes**/
						Hashtable <String, Integer> vertexToIndex = mTools.createVertexToIndexCorrespondance(faceVertexes);

						/**Get the cluster faces in the form int[][]**/
						int [][]faceIndexes = new int[clusterFacesArrayForm.length][3];
						for(int y = 0; y < clusterFacesArrayForm.length; y++) {
							float [][] face_y = clusterFacesArrayForm[y];

							int tempVertexIndex1 = vertexToIndex.get(mTools.vForm(face_y[0]));
							int tempVertexIndex2 = vertexToIndex.get(mTools.vForm(face_y[1]));
							int tempVertexIndex3 = vertexToIndex.get(mTools.vForm(face_y[2]));

							/**Add one to each index as that is how the face
							 * indexes are originally constructed and the
							 * algorithms expect this form**/
							faceIndexes[y] =
								new int []{tempVertexIndex1+1, tempVertexIndex2+1, tempVertexIndex3+1};
						}

						/**Initialize the table for accessing the vertexes within faces in the form int[][]**/
						Hashtable <String, int[]> faceVertexToIndex =
							mTools.createVertexToIndexCorrespondance(clusterFacesArrayForm);

						/**Get a list of edges and the faces that are touching them**/
						Hashtable<String, int[]> touchingFaces =
							mTools.getTouchingFaces(clusterEdges, faceIndexes, faceVertexes);

						/**Go  through the  edges and  collect the ones  that only
						 * have one face touching it (boundary face)**/
						Hashtable<String, float [][]> boundaryEdges =
							new Hashtable<String, float [][]>();
							Enumeration<float[][]> clusterEdgesEnum = clusterEdges.elements();
							while(clusterEdgesEnum.hasMoreElements()) {
								float [][] temporaryEdge = clusterEdgesEnum.nextElement();

								/**Get the touching faces for this edge**/
								String n1 = mTools.edgeXYForm(
										new float[][]{temporaryEdge[0],temporaryEdge[1]});
								String n2 = mTools.edgeXYForm(
										new float[][]{temporaryEdge[1],temporaryEdge[0]});

								int []temporaryTouchingFaces = 
									touchingFaces.get(n1);
								int []temporaryTouchingFaces2 = 
									touchingFaces.get(n2);

								if(temporaryTouchingFaces != null && temporaryTouchingFaces.length == 1) {
									boundaryEdges.put(n1, temporaryEdge);
								}

								/**Get the touching faces for this edge**/
								else if(temporaryTouchingFaces2 != null && temporaryTouchingFaces2.length == 1) {
									boundaryEdges.put(n2, temporaryEdge);
								}

							}

							/**Get the boundary edges in array form**/
							float [][][] boundaryEdgesArray = new float[boundaryEdges.size()][][];
							int boundaryCounter = 0;
							Enumeration<float[][]> enum3 = boundaryEdges.elements();
							while(enum3.hasMoreElements()) {
								boundaryEdgesArray[boundaryCounter] = enum3.nextElement();
								boundaryCounter++;
							}

							/**Get  the  vertexes  neighbours for
							 * each vertex making up the edges**/
							Hashtable<String, float[][]> boundaryVertexNeighbours =
								mTools.getVertexNeighbours(boundaryEdges);

							/**Get the boundary lines**/
							LineCreator lineCreator = new LineCreator();
							Vector<float [][]> boundaryLines =
								lineCreator.getBoundaryPaths(boundaryEdgesArray, boundaryVertexNeighbours);

							/**Iterate through the lines**/
							Enumeration<float[][]> boundaryLinesEnum = boundaryLines.elements();
							while(boundaryLinesEnum.hasMoreElements()) {

								/**Get a corresponding line**/
								float[][] line = boundaryLinesEnum.nextElement();

								/**Interpolate the lines to get additional points**/
								BezierSpline bSpline = new BezierSpline();
								int length = line.length;
								int subLength = 6;

								/**Here  the number 6 is chosen as the  length of each
								 * segmented  within   the   boundary   line   to   be
								 * interpolated individually**/
								for(int z  = 0; z < (length - (length%subLength)); z++) {

									float[][] interpControlPoints = new float[subLength][];
									for(int w = 0; w < subLength; w++) {
										interpControlPoints[w] = line[z++];
									}

									/**Interpolate the points**/
									float [][]interpPoints = bSpline.interpolateBezier("EVEN", subLength, interpControlPoints);

									/**Replace the original points with the interpolated points**/
									for(int w = 0; w < interpControlPoints.length; w++) {
										int [] tempIndex = faceVertexToIndex.get(mTools.vForm(interpControlPoints[w]));

										clusterFacesArrayForm[tempIndex[0]][tempIndex[1]][0] = interpPoints[w][0];
										clusterFacesArrayForm[tempIndex[0]][tempIndex[1]][1] = interpPoints[w][1];
										clusterFacesArrayForm[tempIndex[0]][tempIndex[1]][2] = interpPoints[w][2];
									}
								}	

								for(int z  = (length - (length%subLength)); z < length; z++) {

									float[][] interpControlPoints = new float[length%subLength][];
									for(int w = 0; w < length%subLength; w++) {
										interpControlPoints[w] = line[z++];
									}

									/**Interpolate the points**/
									float [][]interpPoints = bSpline.interpolateBezier("EVEN", subLength, interpControlPoints);

									/**Replace the original points with the interpolated points**/
									for(int w = 0; w < interpControlPoints.length; w++) {
										int [] tempIndex = faceVertexToIndex.get(mTools.vForm(interpControlPoints[w]));

										clusterFacesArrayForm[tempIndex[0]][tempIndex[1]][0] = interpPoints[w][0];
										clusterFacesArrayForm[tempIndex[0]][tempIndex[1]][1] = interpPoints[w][1];
										clusterFacesArrayForm[tempIndex[0]][tempIndex[1]][2] = interpPoints[w][2];
									}
								}
							}
					}

					/**Store the calculated faces**/
					contourFaces[contourClusterCounter] = clusterFacesArrayForm;
					contourClusterCounter++;
				}

				if(storedFacesComputation && (!computedFacesYet[objectNumber])) {
					storedFacesData[objectNumber] = contourFaces;
					computedFacesYet[objectNumber] = true;
				}
			}

			/**Store the required points and normals for  working
			 * with the image affects at the different frames.**/
			float[][] tempPoints  = processedData[lastFrameNum].getFloatData(0); 
			float [][] tempNormal = processedData[lastFrameNum].getFloatData(1);

			/**Create a face to index correspondance required for calculating normals for each point**/
			Hashtable<String, Integer>  faceToIndex =
				mTools.createFacesToIndexCorrespondance(tempPoints, triangleSurfaces);

			/**Iterate through the clusters**/
			for(int k = 0; k < contourFaces.length; k++) {
				gl.glBegin(GL.GL_TRIANGLES);
				/**Iterate through the faces**/
				for(int j = 0; j< contourFaces[k].length; j++) {

					/**Get  the face  index for calculating the normal for the
					 * given triangle**/
					float[] v1 = contourFaces[k][j][0];
					float[] v2 = contourFaces[k][j][1];
					float[] v3 = contourFaces[k][j][2];

					Integer faceIndex = faceToIndex.get(mTools.getCentroid(v1, v2, v3));
					/**Display each face**/
					for(int p = 0; p < contourFaces[k][j].length; p++) {	
						if(faceIndex != null) {
							gl.glNormal3f(
									tempNormal[triangleSurfaces[faceIndex][p]-1][0],
									tempNormal[triangleSurfaces[faceIndex][p]-1][1],
									tempNormal[triangleSurfaces[faceIndex][p]-1][2]);
						}

						gl.glVertex3f(
								contourFaces[k][j][p][0],
								contourFaces[k][j][p][1],
								contourFaces[k][j][p][2]
						);
					}
				}
				gl.glEnd();
			}
		}

		/**Case where lines are being drawn**/
		if(motionEffect.equals("Lines")) {

			/**Initialize the storage for keeping interpolated value if needed**/
			if(storedLinesComputation && !computedLinesYet[objectNumber] &&
					motionEffect.equals("Lines") &&
					(interpType.equals("Bezier") || interpType.equals("TCBS SPLINE"))){	
				storedLinesInterpolation[objectNumber] = new float[motionTrails.length][][];
			}

			for(int j = 0; j < motionTrails.length; j++) {

				if(!(j%2 == 0)) {
					//continue;
				}

				/**Find the direction of the motion trail vs the normal of the
				 * corresponding vertex**/
				float [] tempNormal  = normalValues[j];
				int tempPrevFrame = objectNumber-1;
				if(objectNumber == 0) {
					tempPrevFrame = (numObjectFiles-1);
				}

				/**Ensure the motion trails is at the back of the moving face**/
				float [] tempTrailDirection = vectorTools.sub(motionTrails[j][objectNumber],
						motionTrails[j][tempPrevFrame]);
				float whichSide = vectorTools.dot(tempNormal, tempTrailDirection);

				if(whichSide < 0) {
					continue;
				}

				/**Variable keeping track of the trail length**/
				int maxTrailCount = trailLengthBarScroller;
				int vertexTrailCount = trailLengthBarScroller;

				/**Variable used to blend in the colour of the lines**/
				float alpha = 1.0f;

				/**Case where no interpolation is used**/
				if(interpType.equals("None") && motionEffect.equals("Lines")) {
					gl.glEnable(GL.GL_BLEND);
					gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

					gl.glBegin(GL.GL_LINE_STRIP);
					for(int i = objectNumber; i >= 0; i--) {
						if(vertexTrailCount <= 0) {
							break;
						}
						/**Set the line colour. It will slowly blend into the
						 * background as it streches**/
						gl.glColor4f(currentColour[0], currentColour[1], currentColour[2], alpha);
						gl.glVertex3f(motionTrails[j][i][0],
								motionTrails[j][i][1],
								motionTrails[j][i][2]);

						alpha -= i1;
						vertexTrailCount--;
					}

					for(int i = numObjectFiles-1; i > objectNumber; i--) {
						if(vertexTrailCount <= 0) {
							break;
						}	
						/**Set the line colour. It will slowly blend into the
						 * background as it streches**/
						gl.glColor4f(currentColour[0], currentColour[1], currentColour[2], alpha);
						gl.glVertex3f(motionTrails[j][i][0],
								motionTrails[j][i][1],
								motionTrails[j][i][2]);
						alpha -= i1;
						vertexTrailCount--;
					}
					gl.glEnd();

					gl.glDisable(GL.GL_BLEND);
				}

				/**Case where line being drawn has Bezier Spline interpolation**/
				if(interpType.equals("Bezier") && motionEffect.equals("Lines")) {

					if(trailLengthBarScroller < 2) {
						continue;
					}

					float [][]interpolatedTrails = null;
					int pointsPerControlPoint = 5;

					/**If computation is to be stored, store these
					 * interpolated value for the given frame**/
					if(!computedLinesYet[objectNumber] && storedLinesComputation) {

						/**Initialize the interpolator**/
						BezierSpline bSpline = new BezierSpline();

						float [][] controlPoints = getControlPoints(maxTrailCount, j);

						/**Interpolate points**/
						interpolatedTrails = 
							bSpline.interpolateBezier("EVEN", maxTrailCount*pointsPerControlPoint, controlPoints);

						storedLinesInterpolation[objectNumber][j] = interpolatedTrails;
					}

					/**Get the interpolated points if already stored**/
					if(storedLinesComputation) {
						interpolatedTrails = storedLinesInterpolation[objectNumber][j];
					} else {
						BezierSpline bSpline = new BezierSpline();
						float [][] controlPoints = getControlPoints(maxTrailCount, j);
						interpolatedTrails = 
							bSpline.interpolateBezier("EVEN", maxTrailCount*pointsPerControlPoint, controlPoints);
					}

					/**Plot the interpolated points**/
					gl.glEnable(GL.GL_BLEND);
					gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
					alpha = 1;

					gl.glBegin(GL.GL_LINE_STRIP);

					for(int i = 0; i < interpolatedTrails.length ; i++) {
						/**Set the line colour. It will slowly blend into the
						 * background as it streches**/
						gl.glColor4f(currentColour[0], currentColour[1], currentColour[2], alpha);
						alpha -= i1/pointsPerControlPoint;

						gl.glVertex3f(interpolatedTrails[i][0],
								interpolatedTrails[i][1],
								interpolatedTrails[i][2]);
					}
					gl.glEnd();
					gl.glDisable(GL.GL_BLEND);
				}

				/**Case where line being drawn has Segmented Bezier Spline interpolation**/
				if(interpType.equals("TCBS SPLINE") && motionEffect.equals("Lines")) {

					float [][]interpolatedTrails = null;

					/**Number of points to be used between the control points in the Bezier Splines
					 * interpolating the segments in the TCBS spline**/
					int perControlPoint = 3;

					/**Check that at least three points are available**/
					if(maxTrailCount < 3) {
						continue;
					}

					/**If computation is to be stored, store these
					 * interpolated value for the given frame**/
					if(!computedLinesYet[objectNumber] && storedLinesComputation) {

						/**Initialize the interpolator**/
						TangentControlledBezierSegmentedSpline tcbsSpline = new TangentControlledBezierSegmentedSpline();

						float [][]controlPoints = getControlPoints(maxTrailCount, j);

						interpolatedTrails = 
							tcbsSpline.interpolate(controlPoints, 0.5f, perControlPoint);

						storedLinesInterpolation[objectNumber][j] = interpolatedTrails;
					}

					/**Get the interpolated points if already stored**/
					if(storedLinesComputation) {
						interpolatedTrails = storedLinesInterpolation[objectNumber][j];
					} else {
						TangentControlledBezierSegmentedSpline tcbsSpline = new TangentControlledBezierSegmentedSpline();
						float [][] controlPoints = getControlPoints(maxTrailCount, j);
						interpolatedTrails = 
							tcbsSpline.interpolate(controlPoints, 0.5f, perControlPoint);
					}


					/**Plot the interpolated points**/
					gl.glEnable(GL.GL_BLEND);
					gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
					alpha = 1;

					gl.glBegin(GL.GL_LINE_STRIP);

					for(int i = 0; i < interpolatedTrails.length ; i++) {
						/**Set the line colour. It will slowly blend into the
						 * background as it streches**/
						gl.glColor4f(currentColour[0], currentColour[1], currentColour[2], alpha);

						/**Each Bezier spline in the TCBS spline uses 4 points. However 3 was choosen here
						 * to decrease the alpha value faster.**/
						alpha -= i1/(3*perControlPoint);

						gl.glVertex3f(interpolatedTrails[i][0],
								interpolatedTrails[i][1],
								interpolatedTrails[i][2]);
					}
					gl.glEnd();
					gl.glDisable(GL.GL_BLEND);
				}
			}
		}


		/**If computation is to be stored for Bezier spline interpolation,
		 * mark these values as being stored for the current frame**/
		if((storedLinesComputation && (interpType.equals("Bezier") || interpType.equals("TCBS SPLINE"))) &&
				motionEffect.equals("Lines")) {
			computedLinesYet[objectNumber] = true;
		}

		/**ALternate to the next frame according to the set speed**/
		speedIntervalCounter++;
		if(speedScroller == 0) {
			/**Do nothing**/
		}
		else if(speedIntervalCounter >= (20-speedScroller)) {
			objectNumber++;
			speedIntervalCounter = 0;
		}

		gl.glPopMatrix();
		gl.glPopMatrix();
	}

	/**Compute the control points for interpolation, given the maximum length of the trail, the current length of the trail, and the frame number j**/
	private float[][] getControlPoints(int maxTrailCount, int j) {

		int vertexTrailCount = maxTrailCount;

		/**Create the control points**/
		float [][]controlPoints;
		if(numObjectFiles > maxTrailCount) {
			controlPoints = new float[maxTrailCount][3];
		} else {
			controlPoints = new float[numObjectFiles][3];
		}

		int p = 0;
		for(int i = objectNumber; i >= 0; i--) {
			if(vertexTrailCount <= 0) {
				break;
			}

			controlPoints[p] = motionTrails[j][i];
			vertexTrailCount--;
			p++;
		}
		for(int i = numObjectFiles-1; i > objectNumber; i--) {
			if(vertexTrailCount <= 0) {
				break;
			}
			controlPoints[p] = motionTrails[j][i];
			vertexTrailCount--;
			p++;
		}

		return controlPoints;
	}

	/**Close the GUI and Mesh Display**/
	private void exit(){
		animator.stop();
		frame.dispose();
		System.exit(0);
	}

	/**Initialize all the GUI components**/
	private void initGUI() {

		/**Set up the main frame and canvas**/
		frame = new Frame("Motion Effect Renderer");
		frame.setSize(frameWidth, frameHeight);
		frame.setLayout(new BorderLayout());

		canvas.addGLEventListener(this);
		canvas.setSize(frameWidth, frameHeight-40);
		frame.add(canvas, BorderLayout.CENTER);

		/**Set up the GUI containers**/
		mainBottomBox = new Container();
		mainBottomBox.setSize(frameWidth, 40);
		mainBottomBox.setLayout(new GridLayout(1, 4));

		mainTopBox = new Container();
		mainTopBox.setSize(frameWidth, 40);
		mainTopBox.setLayout(new GridLayout(1, 2));

		movementBox = new Container();
		movementBox.setSize(frameWidth, 20);
		movementBox.setLayout(new FlowLayout());

		rotationBox = new Container();
		rotationBox.setSize(frameWidth, 20);
		rotationBox.setLayout(new FlowLayout());

		speedBox = new Container();
		speedBox.setSize(frameWidth, 20);
		speedBox.setLayout(new FlowLayout());

		tailLengthBox = new Container();
		tailLengthBox.setSize(frameWidth, 20);
		tailLengthBox.setLayout(new FlowLayout());		

		interpTypeBox = new Container();
		interpTypeBox.setSize(frameWidth, 20);
		interpTypeBox.setLayout(new FlowLayout());	

		motionEffectBox = new Container();
		motionEffectBox.setSize(frameWidth, 20);
		motionEffectBox.setLayout(new FlowLayout());	

		lineColourBox = new Container();
		lineColourBox.setSize(frameWidth, 20);
		lineColourBox.setLayout(new FlowLayout());	

		afterImageColourBox = new Container();
		afterImageColourBox.setSize(frameWidth, 20);
		afterImageColourBox.setLayout(new FlowLayout());	

		weightFunctionBox = new Container();
		weightFunctionBox.setSize(frameWidth, 20);
		weightFunctionBox.setLayout(new FlowLayout());	

		/**Label for the controls**/
		Label rotationText = new Label("Rotation");
		Label movementText = new Label("Movement");
		Label speedText = new Label("Speed");

		Label trailLengthText = new Label("Trail Length");
		Label interpTypeText = new Label("Interpolation Type");
		Label motionEffectText = new Label("Motion Effect");
		Label lineColourText = new Label("Line Colour");
		Label afterImageColourText = new Label("Surface Colour");
		Label weightFunctionText = new Label("Weight Function");


		/**Set up the properties pop-up**/
		propertiesText.addMouseListener(        	
				new MouseListener(){

					public void mouseClicked(MouseEvent arg0) {

						/**Set up the listener for closing the pop-up menu**/
						mainTopPropertiesBox = new Container();
						mainTopPropertiesBox.addMouseListener(new MouseListener() {

							public void mouseClicked(MouseEvent arg10) {
								mainTopBox.removeAll();
								mainTopBox.add(motionEffectBox);		
								mainTopBox.add(propertiesText);	

								mainTopBox.repaint();
								frame.repaint();
								frame.setVisible(true);
							}

							public void mouseEntered(MouseEvent arg10) {

							}
							public void mouseExited(MouseEvent arg10) {

							}
							public void mousePressed(MouseEvent arg10) {
							}
							public void mouseReleased(MouseEvent arg10) {
							}
						});

						if(motionEffect.equals("Lines")) {

							mainTopPropertiesBox.setSize(frameWidth, 40);
							mainTopPropertiesBox.setLayout(new GridLayout(1, 3));

							mainTopPropertiesBox.add(tailLengthBox);
							mainTopPropertiesBox.add(interpTypeBox);
							mainTopPropertiesBox.add(lineColourBox);

							mainTopBox.removeAll();
							mainTopBox.setSize(frameWidth, 40);
							mainTopBox.setLayout(new GridLayout(1, 1));
							mainTopBox.add(mainTopPropertiesBox);

							mainTopBox.repaint();
							frame.repaint();
							frame.setVisible(true);
						}

						if(motionEffect.equals("Constructed Lines")) {

							mainTopPropertiesBox.setSize(frameWidth, 40);
							mainTopPropertiesBox.setLayout(new GridLayout(1, 4));

							mainTopPropertiesBox.add(tailLengthBox);
							mainTopPropertiesBox.add(interpTypeBox);
							mainTopPropertiesBox.add(lineColourBox);

							mainTopBox.removeAll();
							mainTopBox.setSize(frameWidth, 40);
							mainTopBox.setLayout(new GridLayout(1, 1));
							mainTopBox.add(mainTopPropertiesBox);

							mainTopBox.repaint();
							frame.repaint();
							frame.setVisible(true);
						}
						if(motionEffect.equals("Surface Tracking")) {
							mainTopPropertiesBox.setSize(frameWidth, 40);
							mainTopPropertiesBox.setLayout(new GridLayout(1, 2));

							mainTopPropertiesBox.add(tailLengthBox);
							mainTopPropertiesBox.add(afterImageColourBox);

							mainTopBox.removeAll();
							mainTopBox.setSize(frameWidth, 40);
							mainTopBox.setLayout(new GridLayout(1, 1));
							mainTopBox.add(mainTopPropertiesBox);

							mainTopBox.repaint();
							frame.repaint();
							frame.setVisible(true);
						}
					}

					public void mouseEntered(MouseEvent arg0) {
						propertiesText.setFont(new Font("monospaced", Font.BOLD, 16));
					}
					public void mouseExited(MouseEvent arg0) {
						propertiesText.setFont(new Font("monospaced", Font.BOLD, 15));
					}
					public void mousePressed(MouseEvent arg0) {
					}
					public void mouseReleased(MouseEvent arg0) {
					}
				});

		rotationText.setFont(new Font("monospaced", Font.BOLD, 15));
		movementText.setFont(new Font("monospaced", Font.BOLD, 15));
		speedText.setFont(new Font("monospaced", Font.BOLD, 15));

		trailLengthText.setFont(new Font("monospaced", Font.BOLD, 15));
		interpTypeText.setFont(new Font("monospaced", Font.BOLD, 15));
		motionEffectText.setFont(new Font("monospaced", Font.BOLD, 15));
		lineColourText.setFont(new Font("monospaced", Font.BOLD, 15));
		afterImageColourText.setFont(new Font("monospaced", Font.BOLD, 15));
		weightFunctionText.setFont(new Font("monospaced", Font.BOLD, 15));

		propertiesText.setFont(new Font("monospaced", Font.BOLD, 15));

		Label xLabel = new Label("x");
		xLabel.setFont(new Font("monospaced", Font.BOLD, 9));
		xLabel.setAlignment(Label.CENTER);
		Label yLabel = new Label("y");
		yLabel.setFont(new Font("monospaced", Font.BOLD, 9));
		yLabel.setAlignment(Label.CENTER);
		Label zLabel = new Label("z");
		zLabel.setFont(new Font("monospaced", Font.BOLD, 9));
		zLabel.setAlignment(Label.CENTER);
		Label xLabel2 = new Label("x");
		xLabel2.setFont(new Font("monospaced", Font.BOLD, 9));
		xLabel2.setAlignment(Label.CENTER);
		Label yLabel2 = new Label("y");
		yLabel2.setFont(new Font("monospaced", Font.BOLD, 9));
		yLabel2.setAlignment(Label.CENTER);
		Label zLabel2 = new Label("z");
		zLabel2.setFont(new Font("monospaced", Font.BOLD, 9));
		zLabel2.setAlignment(Label.CENTER);

		Label speedLabel = new Label("f/s");
		speedLabel.setFont(new Font("monospaced", Font.BOLD, 9));
		speedLabel.setAlignment(Label.CENTER);

		Label trailLengthLabel = new Label("vx");
		trailLengthLabel.setFont(new Font("monospaced", Font.BOLD, 9));
		trailLengthLabel.setAlignment(Label.CENTER);


		/**Set up the GUI for controlling the colour of motion trails**/
		lineColourChooser = new Choice();
		lineColourChooser.add("BLACK");
		lineColourChooser.add("RED");
		lineColourChooser.add("GREEN");
		lineColourChooser.add("BLUE");
		lineColourChooser.add("WHITE");
		lineColourChooser.add("LIGHT PURPLE");
		lineColourChooser.add("NAVY");
		lineColourChooser.add("BABY BLUE");
		lineColourChooser.add("FADED GREEN");
		lineColourChooser.add("GOLDEN YELLOW");
		lineColourChooser.add("PLATINUM");
		lineColourChooser.add("DARK BROWN");
		lineColourChooser.add("YELLOW");
		lineColourChooser.addItemListener(        	
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						lineColour = lineColourChooser.getSelectedItem();
					}
				});

		/**Set up the GUI for controlling the colour of after effect surfaces**/
		afterImageColourChooser = new Choice();
		afterImageColourChooser.add("BLACK");
		afterImageColourChooser.add("RED");
		afterImageColourChooser.add("GREEN");
		afterImageColourChooser.add("BLUE");
		afterImageColourChooser.add("WHITE");
		afterImageColourChooser.add("LIGHT PURPLE");
		afterImageColourChooser.add("NAVY");
		afterImageColourChooser.add("BABY BLUE");
		afterImageColourChooser.add("FADED GREEN");
		afterImageColourChooser.add("GOLDEN YELLOW");
		afterImageColourChooser.add("PLATINUM");
		afterImageColourChooser.add("DARK BROWN");
		afterImageColourChooser.add("YELLOW");
		afterImageColourChooser.addItemListener(        	
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						lineColour = afterImageColourChooser.getSelectedItem();
					}
				});

		/**Set up the GUI for controlling the interpolation type**/
		interpTypeChooser = new Choice();
		interpTypeChooser.add("None");
		interpTypeChooser.add("Bezier");
		interpTypeChooser.add("TCBS SPLINE");
		interpTypeChooser.addItemListener(        	
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						interpType = interpTypeChooser.getSelectedItem();

						/**Additionally, if interpolated values are stored,
						 * they have to be restored when the trail length changes**/
						for(int i = 0; i < numObjectFiles; i++) {
							computedLinesYet[i] = false;
						}

						/**Initialize the stored for containing the interpolated points if needed**/
						if(storedLinesComputation) {
							storedLinesInterpolation = new float[numObjectFiles][][][];
						}
					}
				});


		/**Set up the GUI for controlling the weight function**/
		weightFunctionChooser = new Choice();
		weightFunctionChooser.add("None");
		weightFunctionChooser.add("Log Base 2");
		weightFunctionChooser.add("Power");
		weightFunctionChooser.addItemListener(        	
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						weightFunctionEffect = motionEffectChooser.getSelectedItem();
					}
				});

		/**Set up the GUI for controlling the motion effect**/
		motionEffectChooser = new Choice();
		motionEffectChooser.add("Lines");
		motionEffectChooser.add("Surface Tracking");
		motionEffectChooser.add("Constructed Lines");
		motionEffectChooser.addItemListener(        	
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						motionEffect = motionEffectChooser.getSelectedItem();
						
						/**Additionally, if interpolated values are stored,
						 * they have to be restored when the trail length changes**/

						/**Initialize the stored for containing the interpolated points if needed**/
						if(storedLinesComputation) {
							for(int i = 0; i < numObjectFiles; i++) {
								computedLinesYet[i] = false;       
							}
							storedLinesInterpolation = new float[numObjectFiles][][][];
						}
						
						if(storedFacesComputation) {
							for(int i = 0; i < numObjectFiles; i++) {
								computedFacesYet[i] = false;       
							}
							storedFacesData = new float[numObjectFiles][][][][];
						}
						
						if(storedComposedLinesComputation) {
							for(int i = 0; i < numObjectFiles; i++) {
								computedComposedLinesYet[i] = false;       
							}
							storedComposedLinesData = new float[numObjectFiles][][][][][];
						}
					}
				});


		/**Set up the GUI for controlling the motion trail line length**/
		Container trailLengthTextContainer = new Container();
		trailLengthTextContainer.setLayout(new BorderLayout());
		trailLengthTextComponent = new TextField("" + 10);
		trailLengthTextComponent.addTextListener(        	
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = trailLengthTextComponent.getText();
						try{
							int temp2 = Integer.parseInt(temp);
							trailLengthBarScroller = temp2;
							trailLengthBar.setValue(temp2);
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}

						/**Additionally, if interpolated values are stored,
						 * they have to be restored when the trail length changes**/

						/**Initialize the stored for containing the interpolated points if needed**/
						if(storedLinesComputation) {
							for(int i = 0; i < numObjectFiles; i++) {
								computedLinesYet[i] = false;       
							}
							storedLinesInterpolation = new float[numObjectFiles][][][];
						}
						
						if(storedFacesComputation) {
							for(int i = 0; i < numObjectFiles; i++) {
								computedFacesYet[i] = false;       
							}
							storedFacesData = new float[numObjectFiles][][][][];
						}
						
						if(storedComposedLinesComputation) {
							for(int i = 0; i < numObjectFiles; i++) {
								computedComposedLinesYet[i] = false;       
							}
							storedComposedLinesData = new float[numObjectFiles][][][][][];
						}
					}
				});
		trailLengthTextContainer.add(trailLengthTextComponent, BorderLayout.CENTER);
		trailLengthTextContainer.add(trailLengthLabel, BorderLayout.SOUTH);

		trailLengthBar = new Scrollbar(Scrollbar.VERTICAL, 10, 0, 0, 11);
		trailLengthBar.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						trailLengthBarScroller = e.getValue();
						trailLengthTextComponent.setText("" + e.getValue());

						/**Additionally, if interpolated values are stored,
						 * they have to be restored when the trail length changes**/
						for(int i = 0; i < numObjectFiles; i++) {
							computedLinesYet[i] = false;
						}

						/**Initialize the stored for containing the interpolated points if needed**/
						if(storedLinesComputation) {
							storedLinesInterpolation = new float[numObjectFiles][][][];
						}
					}
				}
		);

		/**Set up the GUI for controlling speed**/
		Container speedTextContainer = new Container();
		speedTextContainer.setLayout(new BorderLayout());
		speedTextComponent = new TextField("" + 19);
		speedTextComponent.addTextListener(        	
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = speedTextComponent.getText();
						try{
							int temp2 = Integer.parseInt(temp);
							speedScroller = temp2;
							speedBar.setValue(temp2);
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		speedTextContainer.add(speedTextComponent, BorderLayout.CENTER);
		speedTextContainer.add(speedLabel, BorderLayout.SOUTH);

		speedBar = new Scrollbar(Scrollbar.VERTICAL, 19, 0, 0, 21);
		speedBar.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						speedScroller = e.getValue();
						speedTextComponent.setText("" + e.getValue());
					}
				}
		);

		/**Text and text fields for the controls**/
		Container zRotationTextContainer = new Container();
		zRotationTextContainer.setLayout(new BorderLayout());
		zRotationText = new TextField("360");
		zRotationText.addTextListener(        	
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = zRotationText.getText();
						try{
							float temp2 = Float.parseFloat(temp);
							if((temp2 <= 179) && (temp2 > 0)) {
								zRotationScroller = -temp2;
								zRotation.setValue(-((int)temp2));
							} else if(((temp2 <= 360) && (temp2 >= 180)) || (temp2==0)) {
								zRotationScroller = -temp2;
								zRotation.setValue(-((int)temp2) + 360);
							}

						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		zRotationTextContainer.add(zRotationText, BorderLayout.CENTER);
		zRotationTextContainer.add(zLabel, BorderLayout.SOUTH);

		Container yRotationTextContainer = new Container();
		yRotationTextContainer.setLayout(new BorderLayout());
		yRotationText = new TextField("360");
		yRotationText.addTextListener(            	
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = yRotationText.getText();
						try{
							float temp2 = Float.parseFloat(temp);
							if((temp2 <= 179) && (temp2 > 0)) {
								yRotationScroller = -temp2;
								yRotation.setValue(-((int)temp2));
							} else if(((temp2 <= 360) && (temp2 >= 180)) || (temp2==0)) {
								yRotationScroller = -temp2;
								yRotation.setValue(-((int)temp2) + 360);
							}

						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		yRotationTextContainer.add(yRotationText, BorderLayout.CENTER);
		yRotationTextContainer.add(yLabel, BorderLayout.SOUTH);

		Container xRotationTextContainer = new Container();
		xRotationTextContainer.setLayout(new BorderLayout());
		xRotationText = new TextField("360");
		xRotationText.addTextListener(            	
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = xRotationText.getText();
						try{
							float temp2 = Float.parseFloat(temp);
							if((temp2 <= 179) && (temp2 > 0)) {
								xRotationScroller = -temp2;
								xRotation.setValue(-((int)temp2));
							} else if(((temp2 <= 360) && (temp2 >= 180)) || (temp2==0)) {
								xRotationScroller = -temp2;
								xRotation.setValue(-((int)temp2) + 360);
							}

						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		xRotationTextContainer.add(xRotationText, BorderLayout.CENTER);
		xRotationTextContainer.add(xLabel, BorderLayout.SOUTH);

		Container zAxisMovementTextContainer = new Container();
		zAxisMovementTextContainer.setLayout(new BorderLayout());
		zAxisMovementText = new TextField("0");
		zAxisMovementText.addTextListener(            	
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = zAxisMovementText.getText();
						try{
							zMovementScroller = -(Float.parseFloat(temp));
							zAxisMovement.setValue(((int)zMovementScroller));
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		zAxisMovementTextContainer.add(zAxisMovementText, BorderLayout.CENTER);
		zAxisMovementTextContainer.add(zLabel2, BorderLayout.SOUTH);

		Container yAxisMovementTextContainer = new Container(); 
		yAxisMovementTextContainer.setLayout(new BorderLayout());
		yAxisMovementText = new TextField("0");
		yAxisMovementText.addTextListener(            	
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = yAxisMovementText.getText();
						try{
							yMovementScroller = -(Float.parseFloat(temp));
							yAxisMovement.setValue(((int)yMovementScroller));
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		yAxisMovementTextContainer.add(yAxisMovementText, BorderLayout.CENTER);
		yAxisMovementTextContainer.add(yLabel2, BorderLayout.SOUTH);

		Container xAxisMovementTextContainer = new Container();
		xAxisMovementTextContainer.setLayout(new BorderLayout());
		xAxisMovementText = new TextField("0");
		xAxisMovementText.addTextListener(            	
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = xAxisMovementText.getText();
						try{
							float temp2 = -(Float.parseFloat(temp));
							if(temp2 <= 50 || temp2 >= -50) {
								xMovementScroller = temp2;
								xAxisMovement.setValue(((int)xMovementScroller));
							}
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		xAxisMovementTextContainer.add(xAxisMovementText, BorderLayout.CENTER);
		xAxisMovementTextContainer.add(xLabel2, BorderLayout.SOUTH);

		/**Set up the scrollbars**/
		xAxisMovement = new Scrollbar(Scrollbar.VERTICAL, 0, 0, -50, 50);
		xAxisMovement.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						xMovementScroller = e.getValue();
						xAxisMovementText.setText("" + -1*e.getValue());
					}
				}
		);

		yAxisMovement = new Scrollbar(Scrollbar.VERTICAL, 0, 0, -50, 50);
		yAxisMovement.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						yMovementScroller = e.getValue();
						yAxisMovementText.setText("" + -1*e.getValue());
					}
				}
		);

		zAxisMovement = new Scrollbar(Scrollbar.VERTICAL, 0, 0, -50, 50);
		zAxisMovement.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						zMovementScroller = e.getValue();
						zAxisMovementText.setText("" + -1*e.getValue());
					}
				}
		);

		xRotation = new Scrollbar(Scrollbar.VERTICAL, 0, 0, -180, 182);
		xRotation.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						xRotationScroller = e.getValue();
						if((e.getValue() >= 0)  && (e.getValue() <= 180)) {
							xRotationText.setText("" + (360 + ((-1)*e.getValue())));
						} else if((e.getValue() >= -179)  && (e.getValue() < 0)){
							xRotationText.setText("" + -1*e.getValue());
						} else if (e.getValue() == 181){
							xRotationText.setText("" + 179);
						} else if (e.getValue() == -180){
							xRotationText.setText("" + 180);
						}

					}
				}
		);

		yRotation = new Scrollbar(Scrollbar.VERTICAL, 0, 0, -180, 182);
		yRotation.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						yRotationScroller = e.getValue();
						if((e.getValue() >= 0)  && (e.getValue() <= 180)) {
							yRotationText.setText("" + (360 + ((-1)*e.getValue())));
						} else if((e.getValue() >= -179)  && (e.getValue() < 0)){
							yRotationText.setText("" + -1*e.getValue());
						} else if (e.getValue() == 181){
							yRotationText.setText("" + 179);
						} else if (e.getValue() == -180){
							yRotationText.setText("" + 180);
						}

					}
				}
		);

		zRotation = new Scrollbar(Scrollbar.VERTICAL, 0, 0, -180, 182);
		zRotation.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						zRotationScroller = e.getValue();
						if((e.getValue() >= 0)  && (e.getValue() <= 180)) {
							zRotationText.setText("" + (360 + ((-1)*e.getValue())));
						} else if((e.getValue() >= -179)  && (e.getValue() < 0)){
							zRotationText.setText("" + -1*e.getValue());
						} else if (e.getValue() == 181){
							zRotationText.setText("" + 179);
						} else if (e.getValue() == -180){
							zRotationText.setText("" + 180);
						}

					}
				}
		);



		/**Add together all the created GUI components**/
		lineColourBox.add(lineColourText);
		lineColourBox.add(lineColourChooser);


		afterImageColourBox.add(afterImageColourText);
		afterImageColourBox.add(afterImageColourChooser);


		weightFunctionBox.add(weightFunctionText);
		weightFunctionBox.add(weightFunctionChooser);


		motionEffectBox.add(motionEffectText);
		motionEffectBox.add(motionEffectChooser);



		interpTypeBox.add(interpTypeText);
		interpTypeBox.add(interpTypeChooser);



		tailLengthBox.add(trailLengthText);

		tailLengthBox.add(trailLengthTextContainer);
		tailLengthBox.add(trailLengthBar);



		speedBox.add(speedText);

		speedBox.add(speedTextContainer);
		speedBox.add(speedBar);



		movementBox.add(movementText);

		movementBox.add(xAxisMovementTextContainer);
		movementBox.add(xAxisMovement);

		movementBox.add(yAxisMovementTextContainer);
		movementBox.add(yAxisMovement);

		movementBox.add(zAxisMovementTextContainer);
		movementBox.add(zAxisMovement);



		rotationBox.add(rotationText);

		rotationBox.add(xRotationTextContainer);
		rotationBox.add(xRotation);

		rotationBox.add(yRotationTextContainer);
		rotationBox.add(yRotation);

		rotationBox.add(zRotationTextContainer);
		rotationBox.add(zRotation);



		mainBottomBox.add(movementBox);
		mainBottomBox.add(rotationBox);
		mainBottomBox.add(speedBox);



		mainTopBox.add(motionEffectBox);		
		mainTopBox.add(propertiesText);	



		frame.add(mainTopBox, BorderLayout.NORTH);
		frame.add(mainBottomBox, BorderLayout.SOUTH);

		/**Finalize the display**/
		frame.setUndecorated(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		frame.setVisible(true);
		animator.start();
		canvas.requestFocus();	
	}

	/**Read in all the object files from a directory
	 * as described in the provided property file**/
	private void readObjects() {
		Properties metaFile = new Properties();
		try {
			metaFile.load(new FileInputStream
					("models/meta.properties"));
		} catch (FileNotFoundException e) {
			System.out.println("Failed to read property file for object files");
			System.exit(1);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Failed to read property file for object files");
			System.exit(1);
			e.printStackTrace();
		}

		/**Get the number of object files to read**/
		numObjectFiles = Integer.parseInt(metaFile.getProperty("totalFiles"));
		processedData = new ProcessedData[numObjectFiles];

		/**Get the name of the input files**/
		modelLocation = metaFile.getProperty("modelLocation");
		objectDatafiles = new String [numObjectFiles];
		for(int i = 0; i < numObjectFiles; i++) {
			String temp = "0";
			if(i > 8) {
				temp="";
			}
			objectDatafiles[i] = modelLocation+ "-" + temp + (i+1) + ".obj";
		}

		/**Create and initialize the file reader**/
		ObjectReader []surfaceReaders = new ObjectReader[numObjectFiles];

		/**Get the points for each object file**/
		for(int i = 0; i < numObjectFiles; i++) {
			surfaceReaders[i] = new ObjectReader(objectDatafiles[i]);
			surfaceReaders[i].start();
			try {
				surfaceReaders[i].join();
			} catch (InterruptedException e) {
				System.out.println("Failed to read file: " + (i+1));
				e.printStackTrace();
			}
		}
		for(int i = 0; i < numObjectFiles; i++) {
			try {
				surfaceReaders[i].join();
			} catch (InterruptedException e) {
				System.out.println("Readeing failed for file: " + (i+1));
				e.printStackTrace();
			}
			processedData[i] = surfaceReaders[i].getProcessedData();
		}

		/**Initialize the mesh tools**/
		mTools = new MeshTools();

		/**Store the trail of each vertex**/
		motionTrails = mTools.getVertexTrails(processedData, numObjectFiles);

		/**Create the dataset for storing repetetive computations**/
		if(storedLinesComputation) {
			storedLinesInterpolation = new float[numObjectFiles][][][];

			computedLinesYet = new boolean[numObjectFiles];
			for(int i = 0; i < numObjectFiles; i++) {
				computedLinesYet[i] = false;
			}
		}

		if(storedFacesComputation) {
			storedFacesData = new float[numObjectFiles][][][][];

			computedFacesYet = new boolean[numObjectFiles];
			for(int i = 0; i < numObjectFiles; i++) {
				computedFacesYet[i] = false;
			}
		}

		if(storedComposedLinesComputation) {
			storedComposedLinesData = new float[numObjectFiles][][][][][];

			computedComposedLinesYet = new boolean[numObjectFiles];
			for(int i = 0; i < numObjectFiles; i++) {
				computedComposedLinesYet[i] = false;
			}
		}
		
	}

	public void displayChanged(GLAutoDrawable gLDrawable, 
			boolean modeChanged, boolean deviceChanged) {
	}

	public void init(GLAutoDrawable gLDrawable) {
		GL gl = gLDrawable.getGL();
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		gLDrawable.addKeyListener(this);
	}

	public void reshape(GLAutoDrawable gLDrawable, int x,int y, int width, int height) {
		GL gl = gLDrawable.getGL();
		if(height <= 0) {
			height = 1;
		}

		float h = (float)width / (float)height;
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(50.0f, h, 1.0, 1000.0);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			exit();
		}
	}

	public void keyReleased(KeyEvent e) {

	}

	public void keyTyped(KeyEvent e) {
	}

	/**Class Constructor**/
	public MeshRenderer() {
		/**

		float [][] testPoints = new float[3][3];
		testPoints[0] = new float[]{2.0f, 1.34f, 3454f};
		testPoints[1] = new float[]{2.0f, 1.34f, 0.43f};
		testPoints[2] = new float[]{223.0f, 12.354f, 5.34f};

		int [][] testFaces = new int[2][3];
		testFaces[0] = new int[]{1,2,3};
		testFaces[1] = new int[]{2,1,3};

		Hashtable<String, float[][]> edges = findAllEdges(testPoints, testFaces);
		System.out.println(edges.size());
		Enumeration<float[][]> enumEdges = edges.elements();
		while(enumEdges.hasMoreElements()) {
			float[][] tempPair = enumEdges.nextElement();
			System.out.println(tempPair[0][0] +" "+
					           tempPair[0][1] +" "+
					           tempPair[0][2] +"|"+
					           tempPair[1][0] +" "+
					           tempPair[1][1] +" "+
					           tempPair[1][2]);
		}

		Hashtable<String, float[][]> neighbours = getVertexNeighbours(edges);
		for(int i = 0; i < neighbours.size(); i++) {

			float [][] vertexNeighbours = neighbours.get(
					testPoints[i][0] +" "+
                    testPoints[i][1] +" "+
                    testPoints[i][2]);

			System.out.print("\n\n");
			System.out.println(testPoints[i][0] +" "+
                    testPoints[i][1] +" "+
                    testPoints[i][2]);
			System.out.print("||");
			for(int j = 0; j < vertexNeighbours.length; j++) {
				System.out.print(vertexNeighbours[j][0] +" "+
						         vertexNeighbours[j][1] +" "+
						         vertexNeighbours[j][2]);
				System.out.print("||");
			}
			System.out.print("\n");
		}

		System.out.println(getTouchingFaces(findAllEdges(testPoints, testFaces), testFaces, testPoints).size());
		 **/
	}
	public static void main(String[] args) {
		MeshRenderer bd = new MeshRenderer();

		bd.readObjects();
		bd.initGUI();
	}
}
