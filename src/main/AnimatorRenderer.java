package main;

import javax.media.opengl.GL;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLAutoDrawable;

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
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Scrollbar;
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

import Effects.ShadeLines;
import IO.ObjectReader;
import IO.ProcessedData;

import MeshAlgorithms.ConstructedLines;
import MeshAlgorithms.ContourLinesCreator;
import MeshAlgorithms.VertexCluster;

import Splines.BezierSpline;
import Splines.TangentControlledBezierSegmentedSpline;


import Tools.MeshTools;
import Tools.StatisticTools;
import Tools.VectorTools;

import WeightFunctions.ALineWeightFilter;


/**
 * @author Piotr Bugaj
 * @date May 21, 2010
 */

public class AnimatorRenderer extends GUI implements GLEventListener, KeyListener {

	/**TODO: add normal for lines**/

	/**------------Main data needed to run the animation-------------------**/
	/**Motion trails for a set of vertexes**/
	private float [][][] motionTrails;

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

	/**Tools for dealing with vectors**/
	private VectorTools vectorTools = new VectorTools();

	/**Tools for dealing with statistic data**/
	private StatisticTools sTools = new StatisticTools();

	/**Tool for computing various information about the mesh**/
	private MeshTools mTools;
	/**--------------------------------------------------------------------**/


	/**------------Control variables for Line Effect-----------------------**/
	/**Variable stating whether or not the interpolated values
	 * for the motion trails should be stored when computed**/
	private final boolean storedLinesComputation = true;
	private boolean computedLinesYet[] = null;
	private float [][][][] storedLinesInterpolation = null;
	/**--------------------------------------------------------------------**/


	/**------------Control variables for Composed Line Effect--------------**/
	/**Variable stating whether or not the calculated
	 * composed lines should be stored.**/
	private final boolean storedComposedLinesComputation = true;
	private boolean computedComposedLinesYet[] = null;
	private float [][][][][][] storedComposedLinesData1 = null;

	/**Variable   indicating   whether  or  not
	 * the line bases have yet been computed**/
	private boolean basesComputed = false;

	/**The composed lines bases**/
	private Vector<int [][]> composedLineBases = null;
	/**--------------------------------------------------------------------**/


	/**------------Control variables for contour faces effect--------------**/
	/**Variable stating whether or not the faces for contour
	 * surfaces should be stored when computed**/
	private final boolean storedFacesComputation = true;
	private boolean computedFacesYet[] = null;
	private float [][][][][] storedFacesData1 = null;
	private int [][] storedFacesData2 = null;
	private Hashtable<String, float[]>
	boundaryVertexes = new Hashtable<String, float[]>();
	/**--------------------------------------------------------------------**/


	private byte [][][][] boundaryVertexesMarkers;
	private final boolean storedMovingFacesComputation = true;
	private boolean computedMovingFacesYet[] = null;


	/**--------------------------------------------------------------------**/
	/**                         DATA HELPER FUNCTIONS                      **/
	/**--------------------------------------------------------------------**/
	/**Reset all the data stored for contour faces**/
	private void resetContourFacesEffectVar() {
		computedFacesYet = null;
		storedFacesData1 = null;
		storedFacesData2 = null;

	}

	/**Reset all the data stored for moving faces**/
	private void resetMovingFacesEffectVar() {
		computedMovingFacesYet = null;

		boundaryVertexesMarkers = null;
		computedMovingFacesYet = null;
	}

	/**Reset all the data stored for line computations**/
	private void resetLineEffectVar() {
		computedLinesYet = null;
		storedLinesInterpolation = null;
	}

	/**Reset all the data stored for line computations**/

	private void resetConstructedLineEffectVar() {
		computedComposedLinesYet = null;
		storedComposedLinesData1 = null;

		/**Variable   indicating   whether  or  not
		 * the line bases have yet been computed**/
		basesComputed = false;

		/**The composed lines bases**/
		composedLineBases = null;
	}

	/**Set up the required data structues for animating lines**/
	private void setLineEffectVar() {
		storedLinesInterpolation = new float[numObjectFiles][][][] ;

		computedLinesYet = new boolean[numObjectFiles];
		for(int i = 0; i < numObjectFiles; i++) {
			computedLinesYet[i] = false;
		}
	}

	/**Set up the required data structues
	 * for animating constructed lines**/
	private void setConstructedLineEffectVar() {
		storedComposedLinesData1 = new float[numObjectFiles][][][][][];

		computedComposedLinesYet = new boolean[numObjectFiles];
		for(int i = 0; i < numObjectFiles; i++) {
			computedComposedLinesYet[i] = false;
		}
	}

	/**Set up the required data structues for surface tracking**/
	private void setContourFacesEffectVar() {
		storedFacesData1 = new float[numObjectFiles][][][][];
		storedFacesData2 = new int[numObjectFiles][];

		computedFacesYet = new boolean[numObjectFiles];
		for(int i = 0; i < numObjectFiles; i++) {
			computedFacesYet[i] = false;
		}
	}

	/**Set up the required data structues for moving surfaces effect**/
	private void setMovingFacesEffectVar() {
		boundaryVertexesMarkers =
			new byte[numObjectFiles]
			         [movingFacesTrailLengthBarScroller][][];

		computedMovingFacesYet = new boolean[numObjectFiles];
		for(int i = 0; i < numObjectFiles; i++) {
			computedMovingFacesYet[i] = false;
		}
	}

	/**Return an array of length maxTrailCount of values,
	 * corresponding  to  a line trail   at  vertex  j**/
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


	/**--------------------------------------------------------------------**/
	/**                           RENDERING FUNCTIONS                      **/
	/**--------------------------------------------------------------------**/
	/**Initialize and set up lights and material for the animation**/
	private void initLightMaterial() {

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
	}
	
	/**Return the corresponding colour in float vector format**/
	private float [] setUpColour(String colour) {
		float [] currentColour = new float[3];
		if(colour.equals("BLACK")) {
			currentColour = new float[]{0.0f, 0.0f, 0.0f};
		} else if(colour.equals("RED")) {
			currentColour = new float[]{1.0f, 0.0f, 0.0f};
		} else if(colour.equals("GREEN")) {
			currentColour = new float[]{0.0f, 1.0f, 0.0f};
		} else if(colour.equals("BLUE")) {
			currentColour = new float[]{0.0f, 0.0f, 1.0f};
		} else if(colour.equals("WHITE")) {
			currentColour = new float[]{1.0f, 1.0f, 1.0f};
		} else if(colour.equals("NAVY")) {
			currentColour = new float[]{0.098f, 0.48627f, 0.53725f};
		} else if(colour.equals("LIGHT PURPLE")) {
			currentColour = new float[]{231f/255f, 109f/255f, 208f/255f};
		} else if(colour.equals("BABY BLUE")) {
			currentColour = new float[]{182f/255f, 217f/255f, 222f/255f};
		} else if(colour.equals("FADED GREEN")) {
			currentColour = new float[]{112f/255f, 165f/255f, 137f/255f};
		} else if(colour.equals("GOLDEN YELLOW")) {
			currentColour = new float[]{240f/255f, 197f/255f, 10f/255f};
		} else if(colour.equals("PLATINUM")) {
			currentColour = new float[]{204f/255f, 198f/255f, 173f/255f};
		} else if(colour.equals("YELLOW")) {
			currentColour = new float[]{247f/255f, 252f/255f, 69f/255f};
		} else if(colour.equals("DARK BROWN")) {
			currentColour = new float[]{79f/255f, 73f/255f, 54f/255f};
		}

		return currentColour;
	}

	/**Main rendering function. Draws everything in JOGL**/
	/**This display is updated as the animation plays**/
	public void display(GLAutoDrawable gLDrawable) {

		/**----------------------------------------------------------------**/
		/**Reset  the  frame   number   to
		 * zero once it reaches the top**/
		if(objectNumber >= numObjectFiles) {
			objectNumber = 0;
		}

		/**Initialize JOGL**/
		gl = gLDrawable.getGL();

		/**Initialize the lights and materials**/
		initLightMaterial();
		/**----------------------------------------------------------------**/


		/**----------------------------------------------------------------**/
		/**@MAIN RENDERING START**/
		gl.glPushMatrix();

		/**@BACKGROUND RENDERING START**/
		gl.glPushMatrix();
		drawBackground();
		gl.glPopMatrix();
		/**@BACKGROUND RENDERING END**/

		/**@ANIMATION RENDERING START**/
		gl.glPushMatrix();
		/**Initialize the animation orientation**/
		gl.glTranslatef(-xMovementScroller*0.05f,
				-0.5f -yMovementScroller*0.05f,
				-2.5f + (-zMovementScroller*0.05f));

		/**Rotate the animation into position**/
		gl.glRotatef(-xRotationScroller, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(-yRotationScroller, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(zRotationScroller, 0.0f, 0.0f, 1.0f);

		/**Retrieve the required arrays for the object number**/
		float [][] pointValues =  processedData[objectNumber].getFloatData(0);
		int [][] triangleSurfaces = processedData[objectNumber].getIntData(2);
		float [][] normalValues = processedData[objectNumber].getFloatData(1);

		/**Case where constructed lines will be made,
		 * composed  from  smaller  motion  trails**/
		if(motionEffect.equals("Constructed Lines")) {
			drawConstructedLines(
					triangleSurfaces,
					pointValues,
					normalValues);

			/**Draw the mesh**/
			if(meshVisibility.equals("ON")) {
				drawMesh(triangleSurfaces, normalValues, pointValues);
			}
		}

		/**Case where moving faces are being drawn**/
		if(motionEffect.equals("Moving Faces")) {
			/**Draw the mesh**/
			if(meshVisibility.equals("ON")) {
				drawMesh(triangleSurfaces, normalValues, pointValues);
			}

			drawMovingMeshComponents(normalValues);
		}

		/**Case where motion sensors are being drawn**/
		if(motionEffect.equals("Motion Sensors")) {
			drawMotionSensorComponents(normalValues);
		}

		/**Case where lines are being drawn**/
		if(motionEffect.equals("Lines")) {

			drawLines(normalValues);

			/**Draw the mesh**/
			if(meshVisibility.equals("ON")) {
				drawMesh(triangleSurfaces, normalValues, pointValues);
			}
		}

		/**Case where contour lines are being drawn**/
		if(motionEffect.equals("Surface Tracking")) {

			/**Draw the mesh**/
			if(meshVisibility.equals("ON")) {
				drawMesh(triangleSurfaces, normalValues, pointValues);
			}

			drawMotionEffectSurfaces(triangleSurfaces);
		}

		gl.glPopMatrix();
		/**@ANIMATION RENDERING END**/

		gl.glPopMatrix();
		/**@MAIN RENDERING END**/
		/**----------------------------------------------------------------**/


		/**----------------------------------------------------------------**/
		/**If computation is to be stored for Bezier spline interpolation,
		 * mark these  values as  being stored for  the  current  frame**/
		if((storedLinesComputation && (interpType.equals("Bezier") ||
				interpType.equals("TCBS SPLINE"))) &&
				motionEffect.equals("Lines")) {
			computedLinesYet[objectNumber] = true;
		}

		/**Alternate  to the next  frame
		 * according to the set speed**/
		speedIntervalCounter++;
		if(speedScroller == 0) {
			/**Do nothing**/
		}
		else if(speedIntervalCounter >=
			(20-speedScroller)) {
			objectNumber++;
			speedIntervalCounter = 0;
		}
		/**----------------------------------------------------------------**/
	}

	ShadeLines shadeLines = null;
	private void drawShadeLines() {
		if(shadeLines == null) {
			shadeLines = new ShadeLines();
		}
		
	}
	
	
	/**Draw the motion effect: moving components**/
	/**---------------------------------------------------**/
	/**params: 
	 *
	 * normalValues:     corresponding normals for each
	 *                   vertex**/
	/**---------------------------------------------------**/
	private void drawMovingMeshComponents(float [][] normalValues) {

		/**Set up the effect colour**/
		float []effectColour = this.setUpColour(movingFacesColour);

		/**Tool used for figuring out the distance
		 * for   comparison   between  vertexes**/
		StatisticTools sT = new StatisticTools();

		/**Variable keeping track for which time frame
		 * the  motion  effect is being created for**/
		int cur = objectNumber;
		int prevFrame = objectNumber;

		/**The  direction  of motion  described
		 * by the current and previous frame**/
		float [] initialTrailDirection = null;

		/**The vertexes at the boundaries of the moving surfaces**/
		byte [][][] boundaryVertexesMarkers_objectNum = null;

		/**Get the boundary  vertexes if they
		 * are already stored and computed**/
		if(storedMovingFacesComputation &&
				computedMovingFacesYet[objectNumber] &&
				motionEffect.equals("Moving Faces")){
			boundaryVertexesMarkers_objectNum =
				boundaryVertexesMarkers[objectNumber];
		} else {
			boundaryVertexesMarkers_objectNum =
				new byte[movingFacesTrailLengthBarScroller][][];

			System.out.println("Creating effect: " +
					"Moving Faces, for frame: " + objectNumber);
		}

		/**Create the motion effect for multiple previous time frames**/
		for(int i = 0; i < movingFacesTrailLengthBarScroller-1; i++) {

			/**------------------------------------------------------------**/
			cur = prevFrame;
			prevFrame -= 1;   
			if(prevFrame < 0) {
				prevFrame = this.numObjectFiles - 1;
			}

			/**Get the vertexes for the current and previous time frame**/
			float [][] vertexesCur =
				processedData[cur].getFloatData(0);
			float [][] vertexesPrev =
				processedData[prevFrame].getFloatData(0);

			int [][] faces = processedData[prevFrame].getIntData(2);

			/**Get average  and standard  deviation needed
			 * for comparing distances between vertexes**/
			float [] diffs = sT.getFrameVertexDistances(
					vertexesCur, vertexesPrev);
			float avg = sT.getAvgFrameVertexDistance(diffs);
			float sD = sT.getFrameDistanceStandardDeviation(
					avg, diffs);
			/**------------------------------------------------------------**/


			/**------------------------------------------------------------**/
			/**Find the faces to be displayed**/
			Vector<int[]> displayedFacesVector = new Vector<int[]>();
			Vector<float[]> directionVector = new Vector<float[]>();
			for(int j = 0; j < faces.length; j++) {

				float []nextTrailDirection = new float[3];

				/**Find the initial direction of the motion (with respect
				 * to the position of the  previous frame after the  very
				 * first one) and  ensure the rest  of the motion effects
				 * are rendered only if they too follow that direction**/
				if(i == 0) {
					initialTrailDirection =
						vectorTools.sub(motionTrails[faces[j][1]-1][cur],
								motionTrails[faces[j][1]-1][prevFrame]);

					nextTrailDirection = initialTrailDirection; 
				} else {
					nextTrailDirection =
						vectorTools.sub(motionTrails[faces[j][1]-1][cur],
								motionTrails[faces[j][1]-1][prevFrame]);   

					if(vectorTools.dot(
							nextTrailDirection,
							initialTrailDirection) < 0) {
						continue;
					}
				}

				/**Also checked that the face moved a significant
				 * distance  from  one time frame to the other**/
				if(!(vectorTools.distanceQuick(
						vertexesCur[faces[j][1]-1],
						vertexesPrev[faces[j][1]-1]) > avg +(sD*0.5f)))  {
					continue;
				}
				directionVector.add(nextTrailDirection);
				displayedFacesVector.add(faces[j]);
			}

			int [][] displayedFaces = new int[displayedFacesVector.size()][];
			displayedFacesVector.toArray(displayedFaces);
			/**------------------------------------------------------------**/


			/**Compute the vertexes at the boundaries if not yet computed**/
			if(!storedMovingFacesComputation ||
					(storedMovingFacesComputation &&
							!computedMovingFacesYet[objectNumber])) {

				/**----------------------------------------**/
				/**Find the boundary of these faces**/
				/**----------------------------------------**/
				Hashtable<String, float [][]> boundaryEdges;
				/**@START**/

				/**Find the edges making up the displayed faces**/
				/**@START**/
				Hashtable<String, float[][]>  edges =
					mTools.findAllEdges(vertexesPrev, displayedFaces);

				/**@END**/

				/**@START**/
				/**Get a  list  of edges  and  the
				 * faces that are touching them**/
				Hashtable<String, int[]> touchingFaces =
					mTools.getTouchingFaces(
							edges, displayedFaces, vertexesPrev);
				/**@END**/

				/**Go  through the  edges and  collect the ones  that
				 * only   one face  touching  it  ( boundary face)**/
				/**@START**/
				boundaryEdges = new Hashtable<String, float [][]>();

				Enumeration<float[][]> edgesEnum =
					edges.elements();
				while(edgesEnum.hasMoreElements()) {
					float [][] temporaryEdge =
						edgesEnum.nextElement();

					/**Get the touching faces for this edge**/
					String n1 = mTools.edgeXYForm(
							new float[][]{
									temporaryEdge[0],temporaryEdge[1]});
					String n2 = mTools.edgeXYForm(
							new float[][]{
									temporaryEdge[1],temporaryEdge[0]});

					int []temporaryTouchingFaces = 
						touchingFaces.get(n1);
					int []temporaryTouchingFaces2 = 
						touchingFaces.get(n2);

					if(temporaryTouchingFaces !=
						null && temporaryTouchingFaces.length == 1) {
						boundaryEdges.put(n1, temporaryEdge);
					}

					/**Get the touching faces for this edge**/
					else if(temporaryTouchingFaces2 !=
						null && temporaryTouchingFaces2.length == 1) {
						boundaryEdges.put(n2, temporaryEdge);
					}
				}
				/**@END**/

				/**@END**/

				/**Find and store the vertexes making up the boundary edges**/
				Enumeration<float[][]> boundaryEnum =
					boundaryEdges.elements();
				Hashtable<String, float[]>
				tempBoundaryVertexes = new Hashtable<String, float[]>();

				while(boundaryEnum.hasMoreElements()) {
					float[][]tempEdge = boundaryEnum.nextElement();
					float[] v1 = tempEdge[0];
					float[] v2 = tempEdge[1];
					tempBoundaryVertexes.put(mTools.vForm(v1), v1);
					tempBoundaryVertexes.put(mTools.vForm(v2), v2);
				}
				byte [][] boundaryVertexesMarker_i_j =
					new byte[displayedFaces.length][3];
				for(int j = 0; j < displayedFaces.length; j++) {
					int [] tempFace = displayedFaces[j];

					float [] v1 = vertexesPrev[tempFace[0]-1];
					float [] v2 = vertexesPrev[tempFace[1]-1];
					float [] v3 = vertexesPrev[tempFace[2]-1];

					/**Here,  one indicates that alpha value will
					 * not be  affected. Zero indicates that  the
					 * alpha at the given vertex will be zero.**/

					/**This   will   force the   boundary of   the   mesh
					 * surfaces   to   blend   into the   background   of
					 * the animation thus creating a smoothing effect.**/
					if(tempBoundaryVertexes.containsKey(mTools.vForm(v1))) {
						boundaryVertexesMarker_i_j[j][0] = 0;
					} else {
						boundaryVertexesMarker_i_j[j][0] = 1;
					}

					if(tempBoundaryVertexes.containsKey(mTools.vForm(v2))) {
						boundaryVertexesMarker_i_j[j][1] = 0;
					} else {
						boundaryVertexesMarker_i_j[j][1] = 1;
					}

					if(tempBoundaryVertexes.containsKey(mTools.vForm(v3))) {
						boundaryVertexesMarker_i_j[j][2] = 0;
					} else {
						boundaryVertexesMarker_i_j[j][2] = 1;
					}

					boundaryVertexesMarkers[objectNumber][i] =
						boundaryVertexesMarker_i_j;

				}

			}
			boundaryVertexesMarkers_objectNum[i] =
				boundaryVertexesMarkers[objectNumber][i];

			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glBegin(GL.GL_TRIANGLES);
			for(int j = 0; j <displayedFaces.length; j++) {

				float[] direction_j = directionVector.get(j);

				/**Get the initial direction for each vertex**/
				if(i == 0) {
					initialTrailDirection =
						vectorTools.sub(motionTrails[faces[j][1]-1][cur],
								motionTrails[faces[j][1]-1][prevFrame]);
				}

				/**------------------------------------------------------------**/
				/**Decrease the alpha as  the motion effect are
				 * drawn for further and further time frames**/

				/**Effect   floats   towards   ( positive
				 * value) or away from the moving mesh**/
				float w = spreadEffectBarScroller;

				gl.glNormal3f(
						normalValues[displayedFaces[j][0]-1][0],
						normalValues[displayedFaces[j][0]-1][1],
						normalValues[displayedFaces[j][0]-1][2]);
				gl.glColor4f(
						effectColour[0]*((i+2.0f)/
								(movingFacesTrailLengthBarScroller+1.0f)),
								effectColour[1]*((i+2.0f)/
										(movingFacesTrailLengthBarScroller+1.0f)),
										effectColour[2]*((i+2.0f)/
												(movingFacesTrailLengthBarScroller+1.0f)),

												(1.0f -
														(i+1.0f)/(movingFacesTrailLengthBarScroller+1.0f))*
														boundaryVertexesMarkers_objectNum[i][j][0]);
				gl.glVertex3d(
						vertexesPrev[displayedFaces[j][0]-1][0] +
						(direction_j[0]/w)*(i+1.0f),
						vertexesPrev[displayedFaces[j][0]-1][1] +
						(direction_j[1]/w)*(i+1.0f),
						vertexesPrev[displayedFaces[j][0]-1][2] +
						(direction_j[2]/w)*(i+1.0f));

				gl.glNormal3f(
						normalValues[displayedFaces[j][1]-1][0],
						normalValues[displayedFaces[j][1]-1][1],
						normalValues[displayedFaces[j][1]-1][2]);
				gl.glColor4f(
						effectColour[0]*((i+2.0f)/
								(movingFacesTrailLengthBarScroller+1.0f)),
								effectColour[1]*((i+2.0f)/
										(movingFacesTrailLengthBarScroller+1.0f)),
										effectColour[2]*((i+2.0f)/
												(movingFacesTrailLengthBarScroller+1.0f)),
												(1.0f -
														(i+1.0f)/(movingFacesTrailLengthBarScroller+1.0f))*
														boundaryVertexesMarkers_objectNum[i][j][1]);
				gl.glVertex3d(
						vertexesPrev[displayedFaces[j][1]-1][0] +
						(direction_j[0]/w),
						vertexesPrev[displayedFaces[j][1]-1][1] +
						(direction_j[1]/w)*(i+1.0f),
						vertexesPrev[displayedFaces[j][1]-1][2] +
						(direction_j[2]/w)*(i+1.0f));

				gl.glNormal3f(
						normalValues[displayedFaces[j][2]-1][0],
						normalValues[displayedFaces[j][2]-1][1],
						normalValues[displayedFaces[j][2]-1][2]);
				gl.glColor4f(
						effectColour[0]*((i+2.0f)/
								(movingFacesTrailLengthBarScroller+1.0f)),
								effectColour[1]*((i+2.0f)/
										(movingFacesTrailLengthBarScroller+1.0f)),
										effectColour[2]*((i+2.0f)/
												(movingFacesTrailLengthBarScroller+1.0f)),
												(1.0f -
														(i+1.0f)/(movingFacesTrailLengthBarScroller+1.0f))*
														boundaryVertexesMarkers_objectNum[i][j][2]);
				gl.glVertex3d(
						vertexesPrev[displayedFaces[j][2]-1][0] +
						(direction_j[0]/w)*(i+1.0f),
						vertexesPrev[displayedFaces[j][2]-1][1] +
						(direction_j[1]/w)*(i+1.0f),
						vertexesPrev[displayedFaces[j][2]-1][2] +
						(direction_j[2]/w)*(i+1.0f));
			}

			gl.glEnd();
			gl.glDisable(GL.GL_BLEND);
		}

		computedMovingFacesYet[objectNumber] = true;
	}

	/**Draw the motion effect: motion sensor**/
	/**---------------------------------------------------**/
	/**params: 
	 *
	 * normalValues:     corresponding normals for each
	 *                   vertex**/
	/**---------------------------------------------------**/
	private void drawMotionSensorComponents(float [][] normalValues) {

		/**Set up the effect colour**/
		float [] effectColour = this.setUpColour(motionSensorColour);
		float [] meshCol = this.setUpColour(meshColour);


		/**Tool used for figuring out the distance
		 * for   comparison   between  vertexes**/
		StatisticTools sT = new StatisticTools();

		/**Variable keeping track for which time frame
		 * the  motion  effect is being created for**/
		int cur = objectNumber;
		int prevFrame = objectNumber;

		/**Find   the   average   distance  between
		 * vertexes for i number of time frames.**/
		for(int i = 0; i < 1; i++) {

			/**------------------------------------------------------------**/
			cur = prevFrame;
			prevFrame -= 1;   
			if(prevFrame < 0) {
				prevFrame = this.numObjectFiles - 1;
			}

			/**Get the vertexes for the current and previous time frame**/
			float [][] vertexesCur =
				processedData[cur].getFloatData(0);
			float [][] vertexesPrev =
				processedData[prevFrame].getFloatData(0);

			int [][] faces = processedData[cur].getIntData(2);

			/**Get average  and standard  deviation needed
			 * for comparing distances between vertexes**/
			float [] diffs1 = sT.getFrameVertexDistances(
					vertexesCur, vertexesPrev);
			float avg = sT.getAvgFrameVertexDistance(diffs1);
			float sD = sT.getFrameDistanceStandardDeviation(
					avg, diffs1);


			cur = prevFrame;
			prevFrame -= 1;   
			if(prevFrame < 0) {
				prevFrame = this.numObjectFiles - 1;
			}

			/**Get the vertexes for the current and previous time frame**/
			vertexesCur =
				processedData[cur].getFloatData(0);
			vertexesPrev =
				processedData[prevFrame].getFloatData(0);

			/**Get average  and standard  deviation needed
			 * for comparing distances between vertexes**/
			float [] diffs2 = sT.getFrameVertexDistances(
					vertexesCur, vertexesPrev);
			float avg2 = sT.getAvgFrameVertexDistance(diffs1);
			float sD2 = sT.getFrameDistanceStandardDeviation(
					avg2, diffs2);
			/**------------------------------------------------------------**/


			/**Draw the vertex with specific colour
			 * or  alpha based  on their  motion**/
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glBegin(GL.GL_TRIANGLES);
			for(int j = 0; j <faces.length; j++) {

				float alpha1;
				float alpha2;
				float alpha3;
				float alpha1b;
				float alpha2b;
				float alpha3b;
				alpha1 = motionEffectHelper(diffs1, diffs2,
						sD, sD2, avg, avg2, faces, j, 0);

				alpha2 = motionEffectHelper(diffs1, diffs2,
						sD, sD2, avg, avg2, faces, j, 1);

				alpha3 = motionEffectHelper(diffs1, diffs2,
						sD, sD2, avg, avg2, faces, j, 2);

				if(alphaEffect.equals("ON")) {
					alpha1b = alpha1;
					alpha2b = alpha2;
					alpha3b = alpha3;
				} else {
					alpha1b = 1.0f;
					alpha2b = 1.0f;
					alpha3b = 1.0f;
				}

				float colDiff1 = effectColour[0] - meshCol[0];
				float colDiff2 = effectColour[1] - meshCol[1];
				float colDiff3 = effectColour[2] - meshCol[2];
				gl.glNormal3f(
						normalValues[faces[j][0]-1][0],
						normalValues[faces[j][0]-1][1],
						normalValues[faces[j][0]-1][2]);
				gl.glColor4f(
						meshCol[0] + colDiff1*(1.0f-alpha1),
						meshCol[1] + colDiff2*(1.0f-alpha1),
						meshCol[2] + colDiff3*(1.0f-alpha1),
						alpha1b);
				gl.glVertex3d(
						vertexesPrev[faces[j][0]-1][0],
						vertexesPrev[faces[j][0]-1][1],
						vertexesPrev[faces[j][0]-1][2]);

				gl.glNormal3f(
						normalValues[faces[j][1]-1][0],
						normalValues[faces[j][1]-1][1],
						normalValues[faces[j][1]-1][2]);
				gl.glColor4f(
						meshCol[0] + colDiff1*(1.0f-alpha2),
						meshCol[1] + colDiff2*(1.0f-alpha2),
						meshCol[2] + colDiff3*(1.0f-alpha2),
						alpha2b);
				gl.glVertex3d(
						vertexesPrev[faces[j][1]-1][0],
						vertexesPrev[faces[j][1]-1][1],
						vertexesPrev[faces[j][1]-1][2]);

				gl.glNormal3f(
						normalValues[faces[j][2]-1][0],
						normalValues[faces[j][2]-1][1],
						normalValues[faces[j][2]-1][2]);
				gl.glColor4f(
						meshCol[0] + colDiff1*(1.0f-alpha3),
						meshCol[1] + colDiff2*(1.0f-alpha3),
						meshCol[2] + colDiff3*(1.0f-alpha3),
						alpha3b);
				gl.glVertex3d(
						vertexesPrev[faces[j][2]-1][0],
						vertexesPrev[faces[j][2]-1][1],
						vertexesPrev[faces[j][2]-1][2]);
			}

			gl.glEnd();
			gl.glDisable(GL.GL_BLEND);
		}
	}

	/**Helper function for the method: drawMotionSensorComponents**/
	private float motionEffectHelper(float [] diffs1, float [] diffs2,
			float sD, float sD2, float avg, float avg2,
			int [][]faces, int j, int v) {

		float alpha1 = 1.0f - 
		((diffs1[faces[j][v]-1])/
				(3*sD)*(1.0f/2.0f)) - 

				((diffs2[faces[j][v]-1])/
						(3*sD2)*(1.0f/2.0f));

		return alpha1;
	}
	
	/**Draw the background for the animtion**/
	/**Draw a background behind the animation**/
	private void drawBackground() {
		gl.glBegin(GL.GL_POLYGON);

		gl.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);

		gl.glNormal3f(0.0f, 0.0f, 1.0f);
		gl.glVertex3d(-20,-20, -10);
		gl.glVertex3d(-20,20, -10);
		gl.glVertex3d(20,20, -10);
		gl.glVertex3d(20,-20, -10);

		gl.glEnd();
	}

	/**Draw the motion effect: constructed lines**/
	/**---------------------------------------------------**/
	/**params: 
	 *
	 * triangleSurfaces: triangles making up the  mesh
	 * pointValues:      corresponding vertexes making
	 *                   up the triangles
	 * normalValues:     corresponding normals for each
	 *                   vertex**/
	/**---------------------------------------------------**/
	private void drawConstructedLines(
			int [][] triangleSurfaces,
			float [][] pointValues,
			float [][] normalValues) {

		/**Set up the line colours**/
		float []effectColour = this.setUpColour(constructedLineColour);

		gl.glColor3f(effectColour[0], effectColour[1], effectColour[2]);

		float [][][][][] frame_i_e_i_m_edges = null;
		if(!storedComposedLinesComputation ||
				(storedComposedLinesComputation &&
						!computedComposedLinesYet[objectNumber])) {

			System.out.println("Constructing lines for frame: " +
					objectNumber);

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
						pointValues);

				/**Get the bases of the composed lines and save them**/
				composedLineBases =
					cL.getConstructedLinesBase(baseSize);

				/**Indicate that the bases have already been computed**/
				basesComputed = true;
			}

			/**Get a correspondance between vertexes and the index**/
			Hashtable<String, Integer> vertexToIndex =
				mTools.createVertexToIndexCorrespondance(pointValues);

			/**Given motion trails  and faces,  find the  faces that  face
			 * the same direction of the motion starting from that face**/
			Hashtable<String, Boolean> trailingFaces =
				mTools.getTrailingFaces(
						triangleSurfaces,
						normalValues,
						pointValues,
						motionTrails,
						numObjectFiles,
						objectNumber);

			/**Given the current line bases, truncate each base based with
			 * the new bases being just made up of trailing faces**/
			Vector<int[][]> trailingBases =
				mTools.getTrailingBases(
						composedLineBases,
						trailingFaces,
						pointValues,
						triangleSurfaces);

			Enumeration<int[][]>trailingBasesEnum =
				trailingBases.elements();
			int yCount = 0;
			frame_i_e_i_m_edges =
				new float[trailingBases.size()][][][][];

			/**Construct the data required for creating
			 * the  lines from  each truncated  base**/
			while(trailingBasesEnum.hasMoreElements()) {

				if(!(yCount%(101-lineDensity) == 0)) {
					trailingBasesEnum.nextElement();
					yCount++;
					continue;
				}

				/**Get the base faces**/
				int [][] trailingdBaseFaces =
					trailingBasesEnum.nextElement();

				/**Get the edges forming this base of faces**/
				Hashtable<String, float[][]> baseEdges =
					mTools.findAllEdges(pointValues, trailingdBaseFaces);

				/**Get the list of touching faces for this base**/
				Hashtable<String, int[]> touchingBaseFaces =
					mTools.getTouchingFaces(
							baseEdges, trailingdBaseFaces, pointValues);

				/**Go  through the  edges and  collect the ones  that only
				 * have one face touching it (boundary edge)**/
				Hashtable<String, float [][]> baseBoundaryEdges =
					mTools.getBoundaryEdges(baseEdges, touchingBaseFaces);

				/**
                Test tt= new Test();
                float[][][] pp = new float[baseBoundaryEdges.size()][][];
                Enumeration<float[][]> hjk = baseBoundaryEdges.elements();
                int rty = 0;
                while(hjk.hasMoreElements()) {
                    float opop[][] = hjk.nextElement();
                    pp[rty++] = opop;
                }
                tt.doTest9(pp, "Test 1 ");
				 **/

				/**Get   the  weight  function  to  be
				 * used when constructing the lines**/
				String tempWeightFunction = "";
				if(this.weightFunctionChooser.equals("None")) {
					tempWeightFunction = ALineWeightFilter.NONE;
				} else if(this.weightFunctionChooser.equals("Log Base 2")) {
					tempWeightFunction = ALineWeightFilter.LN;
				} else if(this.weightFunctionChooser.equals("Power")){
					tempWeightFunction = ALineWeightFilter.POW;
				}

				/**Using the base boundary edges, get all the other edges
				 * for  each time frame required for constructing the
				 * entire line.**/
				float[][][][] e_i_m_edges =
					mTools.getEdgesFromBase(
							baseBoundaryEdges,
							motionTrails,
							vertexToIndex,
							constructedTrailLengthBarScroller,
							objectNumber,
							numObjectFiles,
							tempWeightFunction);

				/**Check that the line is long enough to be
				 * stored based on the standard deviation of
				 * all vertexes between all two pairing frames**/
				int prevFrame = objectNumber;

				float[][][][] renderedEdges =
					new float[e_i_m_edges.length][][][];
				int counter = 0;
				renderedEdges[counter++] = e_i_m_edges[0];
				for(int i = 1; i < e_i_m_edges.length; i++) {

					/**Get the distance between two
					 * frames and the standard deviation**/
					prevFrame = prevFrame-1;
					if(prevFrame <= 0) {
						prevFrame = numObjectFiles-1;
					}
					float[] tempDistances =
						sTools.getFrameVertexDistances(pointValues, 
								processedData[prevFrame].getFloatData(0));
					float tempAvgFrameDis =
						sTools.getAvgFrameVertexDistance(tempDistances);
					float sD =
						sTools.getFrameDistanceStandardDeviation(
								tempAvgFrameDis, tempDistances);
					float requiredLength = tempAvgFrameDis - (sD*0.1f);

					if(vectorTools.distance(
							e_i_m_edges[0][0][0],
							e_i_m_edges[i][0][0]) >
					requiredLength) {
						renderedEdges[counter++] = e_i_m_edges[i];
					}
				}

				if(counter < 2) {
					continue;
				} else {
					e_i_m_edges = new float[counter][][][];
					for(int u = 0; u < counter; u++) {
						e_i_m_edges[u] = renderedEdges[u];
					}
				}


				/**Collect point for each side edge
				 * of the line for interpolation**/
				int pointsPerControlPoint = 5;
				float[][][][] interpolated_e_i_m_edges =
					new float[e_i_m_edges.length*pointsPerControlPoint]
					          [e_i_m_edges[0].length][2][3];

				for(int r = 0; r < e_i_m_edges[0].length; r++) {
					float [][][] sideFace =
						new float[2][e_i_m_edges.length][];

					for(int e = 0; e < e_i_m_edges.length; e++) {
						sideFace[0][e] = e_i_m_edges[e][r][0];
						sideFace[1][e] = e_i_m_edges[e][r][1];
					}

					/**Initialize the interpolator**/
					BezierSpline bSpline = new BezierSpline();

					/**Interpolate points**/
					float[][] interpolatedTrails1 = 
						bSpline.interpolateBezier(
								"EVEN",
								e_i_m_edges.length*pointsPerControlPoint, 
								sideFace[0]);
					float[][] interpolatedTrails2 = 
						bSpline.interpolateBezier(
								"EVEN",
								e_i_m_edges.length*pointsPerControlPoint, 
								sideFace[1]);

					for(int e = 0; e < interpolatedTrails1.length; e++) {
						interpolated_e_i_m_edges[e][r][0] =
							interpolatedTrails1[e];
						interpolated_e_i_m_edges[e][r][1] =
							interpolatedTrails2[e];
					}
				}

				/**Store the computed edges**/
				frame_i_e_i_m_edges[yCount++] = interpolated_e_i_m_edges;
			}

			storedComposedLinesData1[objectNumber] = frame_i_e_i_m_edges;
			computedComposedLinesYet[objectNumber] = true;
		} else {
			frame_i_e_i_m_edges = storedComposedLinesData1[objectNumber];
		}


		/**Render the composed line from the resulting edges**/
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glDepthMask(false);
		for(int q = 0; q < frame_i_e_i_m_edges.length; q++) {

			if(!(q%(101-lineDensity) == 0)) {
				continue;
			}
			float [][][][] e_i_m_edges = frame_i_e_i_m_edges[q];
			if(e_i_m_edges == null) {
				continue;
			}

			/**Variable used to blend in the colour of the lines**/
			float alpha = 1.0f;
			float i1 = 1.2f/(e_i_m_edges.length);

			/**Iterate through each base i**/
			int n = e_i_m_edges.length;
			for(int k = 0; k < n-2; k++) {



				gl.glBegin(GL.GL_TRIANGLES);
				/**Iterate through each edge**/
				for(int g = 0; g < e_i_m_edges[k].length; g++) {

					float [] nn =
						vectorTools.cross(
								vectorTools.sub(
										e_i_m_edges[k][g][0],
										e_i_m_edges[k][g][1]), 
										vectorTools.sub(
												e_i_m_edges[k+1][g][1],
												e_i_m_edges[k][g][1]));

					gl.glNormal3d(
							nn[0],
							nn[1],
							nn[2]);
					alpha = 1.0f - (i1*k);
					gl.glColor4f(
							effectColour[0],
							effectColour[1],
							effectColour[2], alpha);
					gl.glVertex3f(
							e_i_m_edges[k][g][0][0],
							e_i_m_edges[k][g][0][1],
							e_i_m_edges[k][g][0][2]);

					gl.glNormal3d(
							nn[0],
							nn[1],
							nn[2]);
					alpha = 1.0f - (i1*k);
					gl.glColor4f(
							effectColour[0],
							effectColour[1],
							effectColour[2], alpha);
					gl.glVertex3f(
							e_i_m_edges[k][g][1][0],
							e_i_m_edges[k][g][1][1],
							e_i_m_edges[k][g][1][2]);

					gl.glNormal3d(
							nn[0],
							nn[1],
							nn[2]);
					alpha = 1.0f - (i1*(k+1));
					gl.glColor4f(
							effectColour[0],
							effectColour[1],
							effectColour[2], alpha);
					gl.glVertex3f(
							e_i_m_edges[k+1][g][1][0],
							e_i_m_edges[k+1][g][1][1],
							e_i_m_edges[k+1][g][1][2]);

					gl.glNormal3d(
							nn[0],
							nn[1],
							nn[2]);
					alpha = 1.0f - (i1*(k+1));
					gl.glColor4f(
							effectColour[0],
							effectColour[1],
							effectColour[2], alpha);
					gl.glVertex3f(
							e_i_m_edges[k+1][g][1][0],
							e_i_m_edges[k+1][g][1][1],
							e_i_m_edges[k+1][g][1][2]);

					gl.glNormal3d(
							nn[0],
							nn[1],
							nn[2]);
					alpha = 1.0f - (i1*(k+1));
					gl.glColor4f(
							effectColour[0],
							effectColour[1],
							effectColour[2], alpha);
					gl.glVertex3f(
							e_i_m_edges[k+1][g][0][0],
							e_i_m_edges[k+1][g][0][1],
							e_i_m_edges[k+1][g][0][2]);

					gl.glNormal3d(
							nn[0],
							nn[1],
							nn[2]);
					alpha = 1.0f - (i1*(k));
					gl.glColor4f(
							effectColour[0],
							effectColour[1],
							effectColour[2], alpha);
					gl.glVertex3f(
							e_i_m_edges[k][g][0][0],
							e_i_m_edges[k][g][0][1],
							e_i_m_edges[k][g][0][2]);
				}
			}

			for(int g = 0; g < e_i_m_edges[n-2].length; g++) {
				alpha = 1.0f - (i1*(n-2));
				gl.glColor4f(
						effectColour[0],
						effectColour[1],
						effectColour[2], alpha);
				gl.glVertex3f(
						e_i_m_edges[n-2][g][0][0],
						e_i_m_edges[n-2][g][0][1],
						e_i_m_edges[n-2][g][0][2]);
				alpha = 1.0f - (i1*(n-2));
				gl.glColor4f(
						effectColour[0],
						effectColour[1],
						effectColour[2], alpha);
				gl.glVertex3f(
						e_i_m_edges[n-2][g][1][0],
						e_i_m_edges[n-2][g][1][1],
						e_i_m_edges[n-2][g][1][2]);
				alpha = 1.0f - (i1*(n-1));
				gl.glColor4f(
						effectColour[0],
						effectColour[1],
						effectColour[2], alpha);
				gl.glVertex3f(
						e_i_m_edges[n-1][g][1][0],
						e_i_m_edges[n-1][g][1][1],
						e_i_m_edges[n-1][g][1][2]);
			}
			gl.glEnd();	
		}
		gl.glDepthMask(true);
		gl.glDisable(GL.GL_BLEND);
	}

	/**Draw the motion effect: constructed lines**/
	/**---------------------------------------------------**/
	/**params: 
	 *
	 * normalValues:     corresponding normals for each
	 *                   vertex**/
	/**---------------------------------------------------**/
	private void drawLines(float [][] normalValues) {
		/**Set up the line colours**/
		float []effectColour = this.setUpColour(lineColour);
		gl.glColor3f(effectColour[0], effectColour[1], effectColour[2]);

		float i1 = 1.2f/trailLengthBarScroller;

		/**Initialize the storage for keeping interpolated value if needed**/
		if(storedLinesComputation && !computedLinesYet[objectNumber] &&
				motionEffect.equals("Lines") &&
				(interpType.equals("Bezier") ||
						interpType.equals("TCBS SPLINE"))){	
			storedLinesInterpolation[objectNumber] =
				new float[motionTrails.length][][];
		}

		/**Store  the  distance  of  each  motion  trail  if  its
		 * length will relate to the alpha value to be choosen**/
		float avg = 0.0f;
		float sD = 0.0f;
		float [] dist_j = null;
		if(alphaEffect2.equals("Length")) {
			/**Calculate the distances of all the trails**/
			dist_j =new float[motionTrails.length];
			for(int j = 0; j < motionTrails.length; j++) {
				float dist_j_i = 0.0f;
				for(int i = objectNumber; i > 0; i--) {
					dist_j_i += vectorTools.distanceQuick(
							motionTrails[j][i], motionTrails[j][i-1]);
				}
				dist_j[j] = dist_j_i;
			}
			avg = sTools.getAvgFrameVertexDistance(dist_j);
			sD = sTools.getFrameDistanceStandardDeviation(avg, dist_j);
		}

		for(int j = 0; j < motionTrails.length; j++) {

			if(!(j%(101-lineDensity) == 0)) {
				continue;
			}

			/**Find the direction of the motion trail vs the normal of the
			 * corresponding vertex**/
			float [] tempNormal  = normalValues[j];
			int tempPrevFrame = objectNumber-1;
			if(objectNumber == 0) {
				tempPrevFrame = (numObjectFiles-1);
			}

			/**Ensure the motion trails is at the back of the moving face**/
			float [] tempTrailDirection =
				vectorTools.sub(motionTrails[j][tempPrevFrame],
						motionTrails[j][objectNumber]);
			float whichSide = vectorTools.dot(tempNormal, tempTrailDirection);
			/**Don't render the line if its not facing the direction of motion**/
			if(whichSide < 0) {
				continue;
			}

			float angle = vectorTools.ang(
					motionTrails[j][tempPrevFrame],
					motionTrails[j][objectNumber],
					vectorTools.add(
							tempNormal,
							motionTrails[j][objectNumber]));

			/**Variable keeping track of the trail length**/
			int maxTrailCount = trailLengthBarScroller;
			int vertexTrailCount = trailLengthBarScroller;

			/**Variable used to blend in the colour of the lines**/
			float alpha = 1.0f;

			/**Case where no interpolation is used**/
			if(interpType.equals("None") && motionEffect.equals("Lines")) {
				gl.glEnable(GL.GL_BLEND);
				gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
				gl.glDepthMask(false);

				gl.glBegin(GL.GL_LINE_STRIP);

				int counter = 0;
				for(int i = objectNumber; i >= 0; i--) {
					if(vertexTrailCount <= 0) {
						break;
					}

					float alphaVal = 0.0f;
					if(alphaEffect2.equals("Frame")) {
						alphaVal = alpha*(1.0f- (angle/180.0f));
					} else if(alphaEffect2.equals("Length")) {
						alphaVal = 
							1.0f - (dist_j[j]/(3*sD));
					} else {
						alphaVal = 1.0f;
					}
					float [][] tempNormals = processedData[i].getFloatData(1);
					gl.glNormal3f(tempNormals[j][0],
							tempNormals[j][1],
							tempNormals[j][2]);

					/**Set the line colour. It will slowly blend into the
					 * background as it streches**/
					gl.glColor4f(
							effectColour[0],
							effectColour[1],
							effectColour[2], alphaVal);

					float w = 5.0f;
					float [] direction_i_j = new float[]{0.0f, 0.0f, 0.0f};
					if(i != objectNumber) {
						direction_i_j =
						vectorTools.sub(motionTrails[j][i],
								motionTrails[j][i+1]);
					}
					gl.glVertex3f(motionTrails[j][i][0] +
							(direction_i_j[0]/w)*(counter+1.0f),
							motionTrails[j][i][1] +
							(direction_i_j[1]/w)*(counter+1.0f),
							motionTrails[j][i][2] +
							(direction_i_j[2]/w)*(counter+1.0f));

					alpha -= i1;
					vertexTrailCount--;
					counter++;
				}

				for(int i = numObjectFiles-1; i > objectNumber; i--) {
					if(vertexTrailCount <= 0) {
						break;
					}

					float alphaVal = 0.0f;
					if(alphaEffect2.equals("Frame")) {
						alphaVal = alpha*(1.0f- (angle/180.0f));
					} else if(alphaEffect2.equals("Length")) {
						alphaVal = 
							1.0f - (dist_j[j]/(3*sD));
					} else {
						alphaVal = 1.0f;
					}
					float [][] tempNormals = processedData[i].getFloatData(1);
					gl.glNormal3f(tempNormals[j][0],
							tempNormals[j][1],
							tempNormals[j][2]);

					/**Set the line colour. It will slowly blend into the
					 * background as it streches**/
					gl.glColor4f(
							effectColour[0],
							effectColour[1],
							effectColour[2], alphaVal);
					float w = -5.0f;
					float [] direction_i_j = new float[]{0.0f, 0.0f, 0.0f};
					if(i == numObjectFiles-1) {
						direction_i_j =
						vectorTools.sub(motionTrails[j][i],
								motionTrails[j][0]);
					}else {
						direction_i_j =
							vectorTools.sub(motionTrails[j][i],
									motionTrails[j][i+1]);
					}
					
					gl.glVertex3f(motionTrails[j][i][0] +
							(direction_i_j[0]/w)*(counter+1.0f),
							motionTrails[j][i][1] +
							(direction_i_j[1]/w)*(counter+1.0f),
							motionTrails[j][i][2] +
							(direction_i_j[2]/w)*(counter+1.0f));
					alpha -= i1;
					vertexTrailCount--;
					counter++;
				}
				gl.glEnd();
				gl.glDepthMask(true);
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
				if(!computedLinesYet[objectNumber] &&storedLinesComputation) {

					/**Initialize the interpolator**/
					BezierSpline bSpline = new BezierSpline();

					float [][] controlPoints =
						getControlPoints(maxTrailCount, j);

					/**Interpolate points**/
					interpolatedTrails = 
						bSpline.interpolateBezier(
								"EVEN",
								maxTrailCount*pointsPerControlPoint,
								controlPoints);

					storedLinesInterpolation[objectNumber][j] =
						interpolatedTrails;
				}

				/**Get the interpolated points if already stored**/
				if(storedLinesComputation) {
					interpolatedTrails =
						storedLinesInterpolation[objectNumber][j];
				} else {
					BezierSpline bSpline = new BezierSpline();
					float [][] controlPoints =
						getControlPoints(maxTrailCount, j);
					interpolatedTrails = 
						bSpline.interpolateBezier(
								"EVEN",
								maxTrailCount*pointsPerControlPoint,
								controlPoints);
				}

				/**Plot the interpolated points**/
				gl.glEnable(GL.GL_BLEND);
				gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
				gl.glDepthMask(false);
				alpha = 1;

				angle = vectorTools.ang(
						motionTrails[j][tempPrevFrame],
						motionTrails[j][objectNumber],
						vectorTools.add(
								tempNormal,
								motionTrails[j][objectNumber]));

				gl.glBegin(GL.GL_LINE_STRIP);

				for(int i = 0; i < interpolatedTrails.length ; i++) {
					/**Set the line colour. It will slowly blend into the
					 * background as it streches**/
					float alphaVal = 0.0f;
					if(alphaEffect2.equals("Frame")) {
						alphaVal = alpha*(1.0f- (angle/180.0f));
					} else if(alphaEffect2.equals("Length")) {
						alphaVal = 
							1.0f - (dist_j[j]/(3*sD));
					} else {
						alphaVal = 1.0f;
					}

					gl.glColor4f(
							effectColour[0],
							effectColour[1],
							effectColour[2], alphaVal);
					alpha -= i1/pointsPerControlPoint;

					float [][] tempNormals =
						processedData[i/pointsPerControlPoint].
						getFloatData(1);
					gl.glNormal3f(tempNormals[j][0],
							tempNormals[j][1],
							tempNormals[j][2]);

					float w = spreadEffectBarScroller;
					float [] direction_i_j = new float[]{0.0f, 0.0f, 0.0f};
					if(i > 3) {
						direction_i_j =
						vectorTools.sub(interpolatedTrails[i],
								interpolatedTrails[i-3]);
					}
					
					gl.glVertex3f(interpolatedTrails[i][0] +
							(direction_i_j[0]/w)*(i+1.0f),
							interpolatedTrails[i][1] +
							(direction_i_j[1]/w)*(i+1.0f),
							interpolatedTrails[i][2] +
							(direction_i_j[2]/w)*(i+1.0f));
				}
				gl.glEnd();
				gl.glDepthMask(true);
				gl.glDisable(GL.GL_BLEND);
			}

			/**Case  where   line   being   drawn   has
			 * Segmented Bezier Spline interpolation**/
			if(interpType.equals("TCBS SPLINE") &&
					motionEffect.equals("Lines")) {

				float [][]interpolatedTrails = null;

				/**Number of  points to   be   used   between   the
				 * control   points    in   the   Bezier    Splines
				 * interpolating the segments in the TCBS spline**/
				int perControlPoint = 3;

				/**Check that at least three points are available**/
				if(maxTrailCount < 3) {
					continue;
				}

				/**If computation is to be stored, store these
				 * interpolated value for the given frame**/
				if(!computedLinesYet[objectNumber] &&storedLinesComputation) {

					/**Initialize the interpolator**/
					TangentControlledBezierSegmentedSpline tcbsSpline =
						new TangentControlledBezierSegmentedSpline();

					float [][]controlPoints =
						getControlPoints(maxTrailCount, j);

					interpolatedTrails = 
						tcbsSpline.interpolate(
								controlPoints, 0.5f, perControlPoint);

					storedLinesInterpolation[objectNumber][j] =
						interpolatedTrails;
				}

				/**Get the interpolated points if already stored**/
				if(storedLinesComputation) {
					interpolatedTrails =
						storedLinesInterpolation[objectNumber][j];
				} else {
					TangentControlledBezierSegmentedSpline tcbsSpline =
						new TangentControlledBezierSegmentedSpline();
					float [][] controlPoints =
						getControlPoints(maxTrailCount, j);
					interpolatedTrails = 
						tcbsSpline.interpolate(
								controlPoints, 0.5f, perControlPoint);
				}


				/**Plot the interpolated points**/
				gl.glEnable(GL.GL_BLEND);
				gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
				gl.glDepthMask(false);
				alpha = 1;

				gl.glBegin(GL.GL_LINE_STRIP);

				for(int i = 0; i < interpolatedTrails.length ; i++) {
					/**Set the line colour. It will slowly blend
					 * into  the  background as  it  streches**/
					float alphaVal = 0.0f;
					if(alphaEffect2.equals("Frame")) {
						alphaVal = alpha*(1.0f- (angle/180.0f));
					} else if(alphaEffect2.equals("Length")) {
						alphaVal = 
							1.0f - (dist_j[j]/(3*sD));
					} else {
						alphaVal = 1.0f;
					}


					gl.glColor4f(
							effectColour[0],
							effectColour[1],
							effectColour[2], alphaVal);

					/**Each  Bezier  spline  in the  TCBS spline
					 * uses 4 points. However 3 was choosen here
					 * to  decrease the  alpha value  faster.**/
					alpha -= i1/(3*perControlPoint);

					gl.glVertex3f(interpolatedTrails[i][0],
							interpolatedTrails[i][1],
							interpolatedTrails[i][2]);
				}
				gl.glEnd();
				gl.glDepthMask(true);
				gl.glDisable(GL.GL_BLEND);
			}
		}
	}

	/**Draw the motion effect: surface tracking**/
	/**---------------------------------------------------**/
	/**params: 
	 *
	 * triangleSurfaces: triangles making up the  mesh**/
	/**---------------------------------------------------**/
	private void drawMotionEffectSurfaces(
			int [][] triangleSurfaces) {

		/**Set up the effect colours**/
		float []effectColour = this.setUpColour(afterImageColour);
		gl.glColor3f(effectColour[0], effectColour[1], effectColour[2]);

		/**Faces created and to be displayed for each contour surface**/
		float [][][][] clustersToDisplay = null;

		/**Array corresponding  to the structure above,  keeping
		 * track of which frame level each cluster belongs to**/
		int [] clustersToDisplayFrameLevels = null;

		/**Get the faces if they are already stored and computed**/
		if(storedFacesComputation && computedFacesYet[objectNumber] &&
				motionEffect.equals("Surface Tracking")){
			clustersToDisplay = storedFacesData1[objectNumber];
			clustersToDisplayFrameLevels = storedFacesData2[objectNumber];
		}

		/**Create   the   object  for creating  the  contour  lines. The
		 * object  will  take  the   processedData  (all  the   required
		 * 4D  information),  and  the  motion  trails  (the  data   set
		 * describing how each  vertex moves  in 3D  space over time)**/
		int lastFrameNum;
		int contourClusterCounter = 0;
		lastFrameNum = objectNumber - 1;
		if(objectNumber == 0) {
			lastFrameNum = numObjectFiles-1;
		}

		if(!storedFacesComputation ||
				(storedFacesComputation && !computedFacesYet[objectNumber])) {

			ContourLinesCreator clc =
				new ContourLinesCreator(
						processedData,
						motionTrails,
						objectNumber);

			/**Get the created clusters**/
			Vector<VertexCluster> returnedClusters =
				clc.getContourLines(
						surfaceTrackingTrailLengthBarScroller, 0.03f);
			VertexCluster [] clusters_ret =
				new VertexCluster[returnedClusters.size()];
			returnedClusters.toArray(clusters_ret);

			/**Iterate   through   the  clusters  and  store
			 * them  in  display  format.   Skip  the  first
			 * cluster as it simply contains the vertexes**/
			clustersToDisplay =
				new float[clusters_ret.length-1][][][];
			clustersToDisplayFrameLevels =
				new int[clusters_ret.length-1];
			for(int i = 1; i < clusters_ret.length; i++) {

				/**Get the cluster**/
				VertexCluster cluster_i = clusters_ret[i];

				/**Find the depth/level this cluster is at**/
				int clusterFrameLevel = cluster_i.getFrameLevel();

				/**Get the cluster vertexes**/
				float [][] clusterVertexes = cluster_i.getVertexes();

				/**Get the cluster edges**/
				Hashtable<String, float[][]> clusterEdges =
					cluster_i.getEdges(); 

				/**Varaiable that will store the vertexes
				 * corresponding to the  faces created**/
				Hashtable<String, float[]>
				faceVertexesHashtable = new Hashtable<String, float[]>();

				/**Get the  neighbouring vertexes for
				 * each vertex making up a cluster**/
				Hashtable<String, float[][]> clusterVertexNeighbours =
					mTools.getVertexNeighbours(clusterEdges);

				/**Get the cluster faces in the form float[][]**/
				Hashtable<String, float[][]>  clusterFaces =
					mTools.getComposedFaces(
							clusterEdges,
							clusterVertexNeighbours,
							clusterVertexes,
							faceVertexesHashtable);

				/**Get the cluster faces in array form: float [][][]**/
				float [][][] clusterFacesArrayForm =
					new float[clusterFaces.size()][][];
				int fcount = 0;
				Enumeration<float[][]> tempFaceEnum = clusterFaces.elements();
				while(tempFaceEnum.hasMoreElements()){
					float [][] tempFace = tempFaceEnum.nextElement();
					clusterFacesArrayForm[fcount++] = tempFace;
				}

				/**----------------------------------------**/
				/**Find the boundary of these faces**/
				/**----------------------------------------**/
				Hashtable<String, float [][]> boundaryEdges;
				/**@START**/

				/**Get the corresponding vertexes making up the faces**/
				/**@START**/
				float[][] faceVertexes =
					new float[faceVertexesHashtable.size()][];
				Enumeration<float[]> enumm =
					faceVertexesHashtable.elements();
				int fVerCount = 0;
				while(enumm.hasMoreElements()) {
					float[] tempFaceVertex = enumm.nextElement();
					faceVertexes[fVerCount++] = tempFaceVertex;
				}
				/**@END**/

				/**Get the cluster faces in the form int[][]**/
				/**@START**/
				/**Create a vertex to index correspondance
				 * for   the  cluster   face   vertexes**/
				Hashtable <String, Integer> vertexToIndex =
					mTools.createVertexToIndexCorrespondance(faceVertexes);
				int [][]faceIndexes =
					new int[clusterFacesArrayForm.length][3];
				for(int y = 0; y < clusterFacesArrayForm.length; y++){
					float [][] face_y = clusterFacesArrayForm[y];

					int tempVertexIndex1 =
						vertexToIndex.get(mTools.vForm(face_y[0]));
					int tempVertexIndex2 =
						vertexToIndex.get(mTools.vForm(face_y[1]));
					int tempVertexIndex3 =
						vertexToIndex.get(mTools.vForm(face_y[2]));

					/**Add one to each index as that is how the face
					 * indexes  are  originally  constructed and the
					 * algorithms expect this form**/
					faceIndexes[y] =
						new int []{tempVertexIndex1+1,
							tempVertexIndex2+1,
							tempVertexIndex3+1};
				}
				/**@END**/

				/**@START**/
				/**Get a  list  of edges  and  the
				 * faces that are touching them**/
				Hashtable<String, int[]> touchingFaces =
					mTools.getTouchingFaces(
							clusterEdges, faceIndexes, faceVertexes);
				/**@END**/

				/**Go  through the  edges and  collect the ones  that
				 * only   one face  touching  it  ( boundary face)**/
				/**@START**/
				boundaryEdges = new Hashtable<String, float [][]>();

				Enumeration<float[][]> clusterEdgesEnum =
					clusterEdges.elements();
				while(clusterEdgesEnum.hasMoreElements()) {
					float [][] temporaryEdge =
						clusterEdgesEnum.nextElement();

					/**Get the touching faces for this edge**/
					String n1 = mTools.edgeXYForm(
							new float[][]{
									temporaryEdge[0],temporaryEdge[1]});
					String n2 = mTools.edgeXYForm(
							new float[][]{
									temporaryEdge[1],temporaryEdge[0]});

					int []temporaryTouchingFaces = 
						touchingFaces.get(n1);
					int []temporaryTouchingFaces2 = 
						touchingFaces.get(n2);

					if(temporaryTouchingFaces !=
						null && temporaryTouchingFaces.length == 1) {
						boundaryEdges.put(n1, temporaryEdge);
					}

					/**Get the touching faces for this edge**/
					else if(temporaryTouchingFaces2 !=
						null && temporaryTouchingFaces2.length == 1) {
						boundaryEdges.put(n2, temporaryEdge);
					}
				}
				/**@END**/

				/**@END**/

				/**Find and store the vertexes making up the boundary edges**/
				Enumeration<float[][]> boundaryEnum =
					boundaryEdges.elements();

				while(boundaryEnum.hasMoreElements()) {
					float[][]tempEdge = boundaryEnum.nextElement();
					float[] v1 = tempEdge[0];
					float[] v2 = tempEdge[1];
					boundaryVertexes.put(objectNumber + " " +
							mTools.vForm(v1), v1);
					boundaryVertexes.put(objectNumber + " " +
							mTools.vForm(v2), v2);
				}


				/**Store the calculated faces**/
				clustersToDisplay[contourClusterCounter] =
					clusterFacesArrayForm;
				clustersToDisplayFrameLevels[contourClusterCounter] =
					clusterFrameLevel;
				contourClusterCounter++;
			}

			if(storedFacesComputation && (!computedFacesYet[objectNumber])) {
				storedFacesData1[objectNumber] = clustersToDisplay;
				storedFacesData2[objectNumber] = clustersToDisplayFrameLevels;
				computedFacesYet[objectNumber] = true;
			}
		}

		/**Store the required points and normals for  working
		 * with the image affects at the different frames.**/
		float[][] tempPoints  = processedData[lastFrameNum].getFloatData(0); 
		float [][] tempNormal = processedData[lastFrameNum].getFloatData(1);

		/**Create a face to index correspondance required
		 * for   calculating   normals  for each point**/
		Hashtable<String, Integer>  faceToIndex =
			mTools.createFacesToIndexCorrespondance(
					tempPoints, triangleSurfaces);

		/**Iterate through the clusters**/
		for(int k = 0; k < clustersToDisplay.length; k++) {

			/**Get the frame level of the cluster**/
			/**TODO: finish the use of this!!**/
			int tempFrameLevel = clustersToDisplayFrameLevels[k];
			float alphaModifier =
				(surfaceTrackingTrailLengthBarScroller-tempFrameLevel+1.0f)/
				(surfaceTrackingTrailLengthBarScroller+0.0f)*0.9f;

			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

			gl.glBegin(GL.GL_TRIANGLES);
			/**Iterate through the faces**/
			for(int j = 0; j< clustersToDisplay[k].length; j++) {

				/**Get  the face  index for calculating
				 * the normal for the given triangle**/
				float[] v1 = clustersToDisplay[k][j][0];
				float[] v2 = clustersToDisplay[k][j][1];
				float[] v3 = clustersToDisplay[k][j][2];

				Integer faceIndex =
					faceToIndex.get(mTools.getCentroid(v1, v2, v3));
				/**Display each face**/
				for(int p = 0; p < clustersToDisplay[k][j].length; p++) {
					if(faceIndex != null) {
						gl.glNormal3f(
								tempNormal[triangleSurfaces[faceIndex][p]-1][0],
								tempNormal[triangleSurfaces[faceIndex][p]-1][1],
								tempNormal[triangleSurfaces[faceIndex][p]-1][2]);
					}

					if(this.boundaryVertexes.containsKey(
							objectNumber +" "+
							mTools.vForm(clustersToDisplay[k][j][p]))) {
						gl.glColor4f(effectColour[0], 
								effectColour[1], 
								effectColour[2], 0);

					} else {
						gl.glColor4f(effectColour[0], 
								effectColour[1], 
								effectColour[2], 1*alphaModifier);
					}
					gl.glVertex3f(
							clustersToDisplay[k][j][p][0],
							clustersToDisplay[k][j][p][1],
							clustersToDisplay[k][j][p][2]
					);
				}
			}
			gl.glEnd();
			gl.glDisable(GL.GL_BLEND);
		}
	}

	/**Draw the mesh**/
	/**---------------------------------------------------**/
	/**params: 
	 *
	 * triangleSurfaces: triangles making up the  mesh
	 * pointValues:      corresponding vertexes making
	 *                   up the triangles
	 * normalValues:     corresponding normals for each
	 *                   vertex**/
	/**---------------------------------------------------**/
	private void drawMesh(
			int[][] triangleSurfaces,
			float[][] normalValues,
			float[][] pointValues) {

		gl.glBegin(GL.GL_TRIANGLES);
		float[]currentColour = setUpColour(meshColour);
		for(int i = 0; i < triangleSurfaces.length; i++) {

			gl.glColor4f(
					currentColour[0],
					currentColour[1],
					currentColour[2], 1.0f);

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
	}


	/**--------------------------------------------------------------------**/
	/**                             GUI FUNCTIONS                          **/
	/**--------------------------------------------------------------------**/
	/**Close the GUI and Mesh Display**/
	private void exit(){
		animator.stop();
		frame.dispose();
		System.exit(0);
	}

	/**Initialize all the GUI components**/
	protected void initGUI() {

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
		mainTopBox.setLayout(new GridLayout(1, 4));

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

		constructedTailLengthBox = new Container();
		constructedTailLengthBox.setSize(frameWidth, 20);
		constructedTailLengthBox.setLayout(new FlowLayout());

		surfaceTrackingTailLengthBox = new Container();
		surfaceTrackingTailLengthBox.setSize(frameWidth, 20);
		surfaceTrackingTailLengthBox.setLayout(new FlowLayout());

		movingFacesTailLengthBox = new Container();
		movingFacesTailLengthBox.setSize(frameWidth, 20);
		movingFacesTailLengthBox.setLayout(new FlowLayout());

		interpTypeBox = new Container();
		interpTypeBox.setSize(frameWidth, 20);
		interpTypeBox.setLayout(new FlowLayout());

		alphaEffectBox = new Container();
		alphaEffectBox.setSize(frameWidth, 20);
		alphaEffectBox.setLayout(new FlowLayout());

		alphaEffectBox2 = new Container();
		alphaEffectBox2.setSize(frameWidth, 20);
		alphaEffectBox2.setLayout(new FlowLayout());

		motionEffectBox = new Container();
		motionEffectBox.setSize(frameWidth, 20);
		motionEffectBox.setLayout(new FlowLayout());

		meshColourBox = new Container();
		meshColourBox.setSize(frameWidth, 20);
		meshColourBox.setLayout(new FlowLayout());

		motionSensorColourBox = new Container();
		motionSensorColourBox.setSize(frameWidth, 20);
		motionSensorColourBox.setLayout(new FlowLayout());

		constructedLineColourBox = new Container();
		constructedLineColourBox.setSize(frameWidth, 20);
		constructedLineColourBox.setLayout(new FlowLayout());

		lineColourBox = new Container();
		lineColourBox.setSize(frameWidth, 20);
		lineColourBox.setLayout(new FlowLayout());

		movingFacesColourBox = new Container();
		movingFacesColourBox.setSize(frameWidth, 20);
		movingFacesColourBox.setLayout(new FlowLayout());

		afterImageColourBox = new Container();
		afterImageColourBox.setSize(frameWidth, 20);
		afterImageColourBox.setLayout(new FlowLayout());

		weightFunctionBox = new Container();
		weightFunctionBox.setSize(frameWidth, 20);
		weightFunctionBox.setLayout(new FlowLayout());

		baseSizeBox = new Container();
		baseSizeBox.setSize(frameWidth, 20);
		baseSizeBox.setLayout(new FlowLayout());

		lineDensityBox = new Container();
		lineDensityBox.setSize(frameWidth, 20);
		lineDensityBox.setLayout(new FlowLayout());

		spreadEffectBox = new Container();
		spreadEffectBox.setSize(frameWidth, 20);
		spreadEffectBox.setLayout(new FlowLayout());

		/**Label for the controls**/
		Label rotationText = new Label("Rotation");
		Label movementText = new Label("Movement");
		Label speedText = new Label("Speed");

		Label trailLengthText = new Label("Length");
		Label constructedTrailLengthText = new Label("Trail Length");
		Label surfaceTrackingTrailLengthText = new Label("Trail Length");
		Label movingFacesTrailLengthText = new Label("Trail Length");
		Label interpTypeText = new Label("Interpolation");
		Label motionEffectText = new Label("Motion Effect");
		Label meshColourText = new Label("Mesh Colour");
		Label constructedLineColourText = new Label("Line Colour");
		Label lineColourText = new Label("Line Colour");
		Label sensorColourText = new Label("Motion Colour");
		Label afterImageColourText = new Label("Surface Colour");
		Label movingFacesColourText = new Label("Surface Colour");
		Label weightFunctionText = new Label("Weight Function");
		Label baseSizeText = new Label("Base Size");
		Label lineDensityText = new Label("Density");
		/**TODO: fix**/
		Label spreadEffectText = new Label("");
		Label alphaEffectText = new Label("Alpha Effect");
		Label alphaEffectText2 = new Label("Alpha Effect");

		/**Set up the properties pop-up**/
		propertiesText.addMouseListener(
				new MouseListener(){

					public void mouseClicked(MouseEvent arg0) {

						/**Set up the listener for closing the pop-up menu**/
						mainTopPropertiesBox = new Container();
						mainTopPropertiesBox.addMouseListener(
								new MouseListener() {

									public void mouseClicked(MouseEvent arg10) {
										mainTopBox.removeAll();
										mainTopBox.add(motionEffectBox);
										mainTopBox.add(propertiesText);
										mainTopBox.add(meshColourBox);

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
							mainTopPropertiesBox.setLayout(
									new GridLayout(1, 6));

							mainTopPropertiesBox.add(tailLengthBox);
							mainTopPropertiesBox.add(lineDensityBox);
							mainTopPropertiesBox.add(interpTypeBox);
							mainTopPropertiesBox.add(spreadEffectBox);
							mainTopPropertiesBox.add(lineColourBox);
							mainTopPropertiesBox.add(alphaEffectBox2);

							mainTopBox.removeAll();
							mainTopBox.setSize(frameWidth, 40);
							mainTopBox.setLayout(new GridLayout(1, 1));
							mainTopBox.add(mainTopPropertiesBox);

							mainTopBox.repaint();
							frame.repaint();
							frame.setVisible(true);
						}

						if(motionEffect.equals("Motion Sensors")) {

							mainTopPropertiesBox.setSize(frameWidth, 40);
							mainTopPropertiesBox.setLayout(
									new GridLayout(1, 2));

							mainTopPropertiesBox.add(motionSensorColourBox);
							mainTopPropertiesBox.add(alphaEffectBox);

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
							mainTopPropertiesBox.setLayout(
									new GridLayout(1, 4));

							mainTopPropertiesBox.add(
									constructedTailLengthBox);
							mainTopPropertiesBox.add(lineDensityBox);
							mainTopPropertiesBox.add(baseSizeBox);
							mainTopPropertiesBox.add(weightFunctionBox);
							mainTopPropertiesBox.add(
									constructedLineColourBox);

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
							mainTopPropertiesBox.setLayout(
									new GridLayout(1, 2));

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

						if(motionEffect.equals("Moving Faces")) {
							mainTopPropertiesBox.setSize(frameWidth, 40);
							mainTopPropertiesBox.setLayout(
									new GridLayout(1, 3));

							mainTopPropertiesBox.add(movingFacesTailLengthBox);
							mainTopPropertiesBox.add(spreadEffectBox);
							mainTopPropertiesBox.add(movingFacesColourBox);

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
						propertiesText.setFont(
								new Font("monospaced", Font.BOLD, 16));
					}
					public void mouseExited(MouseEvent arg0) {
						propertiesText.setFont(
								new Font("monospaced", Font.BOLD, 15));
					}
					public void mousePressed(MouseEvent arg0) {
					}
					public void mouseReleased(MouseEvent arg0) {
					}
				});

		rotationText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		movementText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		speedText.setFont(
				new Font("monospaced", Font.BOLD, 15));

		trailLengthText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		constructedTrailLengthText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		surfaceTrackingTrailLengthText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		movingFacesTrailLengthText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		interpTypeText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		motionEffectText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		meshColourText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		lineColourText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		constructedLineColourText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		afterImageColourText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		movingFacesColourText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		weightFunctionText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		baseSizeText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		lineDensityText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		spreadEffectText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		sensorColourText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		alphaEffectText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		alphaEffectText2.setFont(
				new Font("monospaced", Font.BOLD, 15));

		propertiesText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		propertiesText.setForeground(Color.MAGENTA);

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

		Label constructedTrailLengthLabel = new Label("vx");
		constructedTrailLengthLabel.setFont(
				new Font("monospaced", Font.BOLD, 9));
		constructedTrailLengthLabel.setAlignment(Label.CENTER);

		Label surfaceTrackingTrailLengthLabel = new Label("f");
		constructedTrailLengthLabel.setFont(
				new Font("monospaced", Font.BOLD, 9));
		surfaceTrackingTrailLengthLabel.setAlignment(Label.CENTER);

		Label movingFacesTrailLengthLabel = new Label("f");
		movingFacesTrailLengthLabel.setFont(
				new Font("monospaced", Font.BOLD, 9));
		movingFacesTrailLengthLabel.setAlignment(Label.CENTER);

		Label baseSizeLabel = new Label("R");
		baseSizeLabel.setFont(new Font("monospaced", Font.BOLD, 9));
		baseSizeLabel.setAlignment(Label.CENTER);

		Label lineDensityLabel = new Label("D");
		lineDensityLabel.setFont(new Font("monospaced", Font.BOLD, 9));
		lineDensityLabel.setAlignment(Label.CENTER);

		String [] colours = new String[]{"DARK BROWN", "BLACK", "RED",
				"GREEN", "BLUE", "WHITE", "LIGHT PURPLE", "NAVY",
				"BABY BLUE", "FADED GREEN"};

		/**Set up the GUI for controlling the colour of motion trails**/
		lineColourChooser = new Choice();
		for(int i = 0; i < colours.length; i++) {
			lineColourChooser.add(colours[i]);
		}
		lineColourChooser.addItemListener(
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						lineColour = lineColourChooser.getSelectedItem();
					}
				});

		/**Set up  the  GUI for  controlling  the
		 * colour of the motion sensor effects**/
		motionSensorColourChooser = new Choice();
		for(int i = 0; i < colours.length; i++) {
			motionSensorColourChooser.add(colours[i]);
		}
		motionSensorColourChooser.addItemListener(
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						motionSensorColour =
							motionSensorColourChooser.getSelectedItem();
					}
				});

		/**Set up the GUI for controlling the
		 * colour of moving mesh  surfaces**/
		movingFacesColourChooser = new Choice();
		for(int i = 0; i < colours.length; i++) {
			movingFacesColourChooser.add(colours[i]);
		}
		movingFacesColourChooser.addItemListener(
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						movingFacesColour = movingFacesColourChooser.getSelectedItem();
					}
				});

		/**Set up the GUI for controlling the colour of constructed lines**/
		constructedLineColourChooser = new Choice();
		for(int i = 0; i < colours.length; i++) {
			constructedLineColourChooser.add(colours[i]);
		}
		constructedLineColourChooser.addItemListener(
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						constructedLineColour =
							constructedLineColourChooser.getSelectedItem();
					}
				});

		/**Set up the GUI for controlling the colour of the mesh**/
		meshColourChooser = new Choice();
		for(int i = 0; i < colours.length; i++) {
			meshColourChooser.add(colours[i]);
		}
		meshColourChooser.addItemListener(
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						meshColour = meshColourChooser.getSelectedItem();
					}
				});

		/**Set up the GUI for controlling the
		 * colour of after effect surfaces**/
		afterImageColourChooser = new Choice();
		for(int i = 0; i < colours.length; i++) {
			afterImageColourChooser.add(colours[i]);
		}
		afterImageColourChooser.addItemListener(
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						afterImageColour =
							afterImageColourChooser.getSelectedItem();
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

						/**Additionally, if interpolated values
						 * are stored, they have to be restored
						 * when  the  trail  length  changes**/
						resetLineEffectVar();


						/**Initialize the storage for containing
						 * the interpolated points if  needed**/
						if(storedLinesComputation &&
								motionEffect.equals("Lines")) {
							setLineEffectVar();
						}
					}
				});

		/**Set up the GUI for controlling the visibility of the mesh**/
		meshVisibilityChooser = new Choice();
		meshVisibilityChooser.add("ON");
		meshVisibilityChooser.add("OFF");
		meshVisibilityChooser.addItemListener(
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						meshVisibility =
							meshVisibilityChooser.getSelectedItem();
					}
				});

		/**Set  up the  GUI for  controlling  the
		 * alpha effect for the motion sensors**/
		alphaEffectChooser = new Choice();
		alphaEffectChooser.add("ON");
		alphaEffectChooser.add("OFF");
		alphaEffectChooser.addItemListener(
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						alphaEffect =
							alphaEffectChooser.getSelectedItem();
					}
				});

		/**Set  up the  GUI for  controlling  the
		 * alpha effect for the motion sensors**/
		alphaEffectChooser2 = new Choice();
		alphaEffectChooser2.add("Frame");
		alphaEffectChooser2.add("Length");
		alphaEffectChooser2.add("None");
		alphaEffectChooser2.addItemListener(
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						alphaEffect2 =
							alphaEffectChooser2.getSelectedItem();
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
						weightFunctionEffect =
							motionEffectChooser.getSelectedItem();
						resetConstructedLineEffectVar();

						/**Initialize the storage for containing
						 * the interpolated points if  needed**/
						if(storedComposedLinesComputation &&
								motionEffect.equals("Constructed Lines")) {
							setConstructedLineEffectVar();
						}
					}
				});

		/**Set up the GUI for controlling the motion effect**/
		motionEffectChooser = new Choice();
		motionEffectChooser.add("Lines");
		motionEffectChooser.add("Surface Tracking");
		motionEffectChooser.add("Constructed Lines");
		motionEffectChooser.add("Moving Faces");
		motionEffectChooser.add("Motion Sensors");
		motionEffectChooser.addItemListener(
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						motionEffect = motionEffectChooser.getSelectedItem();

						/**Additionally, if interpolated values
						 * are stored, they have to be restored
						 * when  the  trail  length  changes**/
						if(storedLinesComputation &&
								motionEffect.equals("Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();
							resetMovingFacesEffectVar();

							setLineEffectVar();
						}

						if(storedFacesComputation &&
								motionEffect.equals("Surface Tracking")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();
							resetMovingFacesEffectVar();

							setContourFacesEffectVar();
						}

						if(storedComposedLinesComputation &&
								motionEffect.equals("Constructed Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();
							resetMovingFacesEffectVar();

							setConstructedLineEffectVar();
						}

						if(storedComposedLinesComputation &&
								motionEffect.equals("Moving Faces")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();
							resetMovingFacesEffectVar();

							setMovingFacesEffectVar();
						}
					}
				});

		/**Set up the GUI for controlling the
		 * base size of a constructed line**/
		Container baseSizeTextContainer = new Container();
		baseSizeTextContainer.setLayout(new BorderLayout());
		baseSizeTextComponent = new TextField("" + 4);
		baseSizeTextComponent.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = baseSizeTextComponent.getText();
						try{
							int temp2 = Integer.parseInt(temp);
							baseSize = temp2;
							baseSizeScroller.setValue(temp2);
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}

						/**Additionally, if interpolated values
						 * are stored, they have to be restored
						 * when  the  trail  length  changes**/
						if(storedComposedLinesComputation &&
								motionEffect.equals("Constructed Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setConstructedLineEffectVar();
						}
					}
				});
		baseSizeTextContainer.add(baseSizeTextComponent, BorderLayout.CENTER);
		baseSizeTextContainer.add(baseSizeLabel, BorderLayout.SOUTH);

		baseSizeScroller = new Scrollbar(Scrollbar.VERTICAL, 4, 0, 0, 11);
		baseSizeScroller.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						baseSize = e.getValue();
						baseSizeTextComponent.setText("" + e.getValue());

						/**Additionally, if interpolated values
						 * are stored, they have to be restored
						 * when  the  trail  length  changes**/
						if(storedComposedLinesComputation &&
								motionEffect.equals("Constructed Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setConstructedLineEffectVar();
						}
					}
				}
		);

		/**Set up the GUI for controlling the
		 * line densities**/
		Container lineDensityTextContainer = new Container();
		lineDensityTextContainer.setLayout(new BorderLayout());
		lineDensityTextComponent = new TextField("" + 99);
		lineDensityTextComponent.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = lineDensityTextComponent.getText();
						try{
							int temp2 = Integer.parseInt(temp);
							lineDensity = temp2;
							lineDensityScroller.setValue(temp2);
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}

						/**Additionally, if interpolated values
						 * are stored, they have to be restored
						 * when  the  trail  length  changes**/
						if(storedLinesComputation &&
								motionEffect.equals("Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setLineEffectVar();
						}

						if(storedLinesComputation &&
								motionEffect.equals("Constructed Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setConstructedLineEffectVar();
						}
					}
				});
		lineDensityTextContainer.add(
				lineDensityTextComponent, BorderLayout.CENTER);
		lineDensityTextContainer.add(lineDensityLabel, BorderLayout.SOUTH);

		lineDensityScroller = new Scrollbar(Scrollbar.VERTICAL, 99, 0, 1,101);
		lineDensityScroller.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						lineDensity = e.getValue();
						lineDensityTextComponent.setText("" + e.getValue());

						/**Additionally, if interpolated values
						 * are stored, they have to be restored
						 * when  the  trail  length  changes**/
						if(storedLinesComputation &&
								motionEffect.equals("Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setLineEffectVar();
						}
						if(storedLinesComputation &&
								motionEffect.equals("Constructed Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setConstructedLineEffectVar();
						}
					}
				}
		);

		/**Set up the GUI for controlling the motion trail line length**/
		Container trailLengthTextContainer = new Container();
		trailLengthTextContainer.setLayout(new BorderLayout());
		trailLengthTextComponent = new TextField("" + 4);
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

						/**Additionally, if interpolated values
						 * are stored, they have to be restored
						 * when  the  trail  length  changes**/
						if(storedLinesComputation &&
								motionEffect.equals("Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setLineEffectVar();
						}

						if(storedFacesComputation &&
								motionEffect.equals("Surface Tracking")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setContourFacesEffectVar();
						}

						if(storedComposedLinesComputation &&
								motionEffect.equals("Constructed Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setConstructedLineEffectVar();
						}
					}
				});
		trailLengthTextContainer.add(
				trailLengthTextComponent, BorderLayout.CENTER);
		trailLengthTextContainer.add(trailLengthLabel, BorderLayout.SOUTH);

		trailLengthBar = new Scrollbar(Scrollbar.VERTICAL, 4, 0, 0, 11);
		trailLengthBar.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						trailLengthBarScroller = e.getValue();
						trailLengthTextComponent.setText("" + e.getValue());

						/**Additionally, if interpolated values
						 * are stored, they have to be restored
						 * when  the  trail  length  changes**/
						if(storedLinesComputation &&
								motionEffect.equals("Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setLineEffectVar();
						}

						if(storedFacesComputation &&
								motionEffect.equals("Surface Tracking")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setContourFacesEffectVar();
						}

						if(storedComposedLinesComputation &&
								motionEffect.equals("Constructed Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setConstructedLineEffectVar();
						}
					}
				}
		);

		/**Set up the GUI for controlling the spread
		 * effect  for  the moving  faces  effect**/
		Container spreadEffectTextContainer = new Container();
		spreadEffectTextContainer.setLayout(new BorderLayout());
		spreadEffectTextComponent = new TextField("" + 5);
		spreadEffectTextComponent.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = spreadEffectTextComponent.getText();
						try{
							int temp2 = Integer.parseInt(temp);
							spreadEffectBarScroller = temp2;
							spreadEffectBar.setValue(temp2);
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		spreadEffectTextContainer.add(
				spreadEffectTextComponent, BorderLayout.CENTER);
		spreadEffectTextContainer.add(trailLengthLabel, BorderLayout.SOUTH);

		spreadEffectBar = new Scrollbar(Scrollbar.VERTICAL, 5, 0, -100, 100);
		spreadEffectBar.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						spreadEffectBarScroller = e.getValue();
						spreadEffectTextComponent.setText("" + e.getValue());
					}
				}
		);

		/**Set up the GUI for controlling the motion trail line length**/
		Container constructedTrailLengthTextContainer = new Container();
		constructedTrailLengthTextContainer.setLayout(new BorderLayout());
		constructedTrailLengthTextComponent = new TextField("" + 3);
		constructedTrailLengthTextComponent.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp =
							constructedTrailLengthTextComponent.getText();
						try{
							int temp2 = Integer.parseInt(temp);
							constructedTrailLengthBarScroller = temp2;
							constructedTrailLengthBar.setValue(temp2);
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}

						/**Additionally, if interpolated values
						 * are stored, they have to be restored
						 * when  the  trail  length  changes**/
						if(storedComposedLinesComputation &&
								motionEffect.equals("Constructed Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setConstructedLineEffectVar();
						}
					}
				});
		constructedTrailLengthTextContainer.
		add(constructedTrailLengthTextComponent, BorderLayout.CENTER);
		constructedTrailLengthTextContainer.
		add(constructedTrailLengthLabel, BorderLayout.SOUTH);

		constructedTrailLengthBar =
			new Scrollbar(Scrollbar.VERTICAL, 3, 0, 0, 11);
		constructedTrailLengthBar.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						constructedTrailLengthBarScroller = e.getValue();
						constructedTrailLengthTextComponent.
						setText("" + e.getValue());

						/**Additionally, if interpolated values
						 * are stored, they have to be restored
						 * when  the  trail  length  changes**/
						if(storedComposedLinesComputation &&
								motionEffect.equals("Constructed Lines")) {
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setConstructedLineEffectVar();
						}
					}
				}
		);

		/**Set up  the GUI for  controlling the
		 * trail length for surface tracking**/
		Container surfaceTrackingLengthTextContainer = new Container();
		surfaceTrackingLengthTextContainer.setLayout(new BorderLayout());
		surfaceTrackingTrailLengthTextComponent = new TextField("" + 3);
		surfaceTrackingTrailLengthTextComponent.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp =
							surfaceTrackingTrailLengthTextComponent.getText();
						try{
							int temp2 = Integer.parseInt(temp);
							surfaceTrackingTrailLengthBarScroller = temp2;
							surfaceTrackingTrailLengthBar.setValue(temp2);
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}

						/**Additionally, if interpolated values
						 * are stored, they have to be restored
						 * when  the  trail  length  changes**/
						if(storedComposedLinesComputation &&
								motionEffect.equals("Constructed Lines")) {
							/**TODO**/
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setConstructedLineEffectVar();
						}
					}
				});
		surfaceTrackingLengthTextContainer.
		add(surfaceTrackingTrailLengthTextComponent, BorderLayout.CENTER);
		surfaceTrackingLengthTextContainer.
		add(surfaceTrackingTrailLengthLabel, BorderLayout.SOUTH);

		surfaceTrackingTrailLengthBar = new Scrollbar(
				Scrollbar.VERTICAL, 3, 0, 0, 11);
		surfaceTrackingTrailLengthBar.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						surfaceTrackingTrailLengthBarScroller =
							e.getValue();
						surfaceTrackingTrailLengthTextComponent.
						setText("" + e.getValue());

						/**Additionally, if interpolated values
						 * are stored, they have to be restored
						 * when the  trail  length   changes**/
						if(storedComposedLinesComputation &&
								motionEffect.equals("Constructed Lines")) {
							/**TODO:**/
							resetLineEffectVar();
							resetConstructedLineEffectVar();
							resetContourFacesEffectVar();

							setConstructedLineEffectVar();
						}
					}
				}
		);

		/**Set up  the GUI for  controlling the
		 * trail length for the moving faces**/
		Container movingFacesLengthTextContainer = new Container();
		movingFacesLengthTextContainer.setLayout(new BorderLayout());
		movingFacesTrailLengthTextComponent = new TextField("" + 3);
		movingFacesTrailLengthTextComponent.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp =
							movingFacesTrailLengthTextComponent.getText();
						try{
							int temp2 = Integer.parseInt(temp);
							movingFacesTrailLengthBarScroller = temp2;
							movingFacesTrailLengthBar.setValue(temp2);
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		movingFacesLengthTextContainer.
		add(movingFacesTrailLengthTextComponent, BorderLayout.CENTER);
		movingFacesLengthTextContainer.
		add(movingFacesTrailLengthLabel, BorderLayout.SOUTH);

		movingFacesTrailLengthBar = new Scrollbar(
				Scrollbar.VERTICAL, 3, 0, 0, 11);
		movingFacesTrailLengthBar.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						movingFacesTrailLengthBarScroller =
							e.getValue();
						movingFacesTrailLengthTextComponent.
						setText("" + e.getValue());
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
		zRotationText = new TextField("347");
		zRotationText.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = zRotationText.getText();
						try{
							float temp2 = Float.parseFloat(temp);
							if((temp2 <= 179) && (temp2 > 0)) {
								zRotationScroller = -temp2;
								zRotation.setValue(-((int)temp2));
							} else if(((temp2 <= 360) && (temp2 >= 180)) ||
									(temp2==0)) {
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
		yRotationText = new TextField("322");
		yRotationText.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = yRotationText.getText();
						try{
							float temp2 = Float.parseFloat(temp);
							if((temp2 <= 179) && (temp2 > 0)) {
								yRotationScroller = -temp2;
								yRotation.setValue(-((int)temp2));
							} else if(((temp2 <= 360) && (temp2 >= 180)) ||
									(temp2==0)) {
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
		xRotationText = new TextField("34");
		xRotationText.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = xRotationText.getText();
						try{
							float temp2 = Float.parseFloat(temp);
							if((temp2 <= 179) && (temp2 > 0)) {
								xRotationScroller = -temp2;
								xRotation.setValue(-((int)temp2));
							} else if(((temp2 <= 360) && (temp2 >= 180)) ||
									(temp2==0)) {
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
		zAxisMovementTextContainer.add(zAxisMovementText,BorderLayout.CENTER);
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
		yAxisMovementTextContainer.add(yAxisMovementText,BorderLayout.CENTER);
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
								xAxisMovement.
								setValue(((int)xMovementScroller));
							}
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		xAxisMovementTextContainer.add(xAxisMovementText,BorderLayout.CENTER);
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

		xRotation = new Scrollbar(Scrollbar.VERTICAL, -34, 0, -180, 182);
		xRotation.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						xRotationScroller = e.getValue();
						if((e.getValue() >= 0)  && (e.getValue() <= 180)) {
							xRotationText.setText("" +
									(360 + ((-1)*e.getValue())));
						} else if((e.getValue() >= -179)  &&
								(e.getValue() < 0)){
							xRotationText.setText("" + -1*e.getValue());
						} else if (e.getValue() == 181){
							xRotationText.setText("" + 179);
						} else if (e.getValue() == -180){
							xRotationText.setText("" + 180);
						}
					}
				}
		);

		yRotation = new Scrollbar(Scrollbar.VERTICAL, 360-322, 0, -180, 182);
		yRotation.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						yRotationScroller = e.getValue();
						if((e.getValue() >= 0)  && (e.getValue() <= 180)) {
							yRotationText.setText("" +
									(360 + ((-1)*e.getValue())));
						} else if((e.getValue() >= -179)  &&
								(e.getValue() < 0)){
							yRotationText.setText("" + -1*e.getValue());
						} else if (e.getValue() == 181){
							yRotationText.setText("" + 179);
						} else if (e.getValue() == -180){
							yRotationText.setText("" + 180);
						}
					}
				}
		);

		zRotation = new Scrollbar(Scrollbar.VERTICAL, 360-347, 0, -180, 182);
		zRotation.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						zRotationScroller = e.getValue();
						if((e.getValue() >= 0)  && (e.getValue() <= 180)) {
							zRotationText.setText("" +
									(360 + ((-1)*e.getValue())));
						} else if((e.getValue() >= -179)  &&
								(e.getValue() < 0)){
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


		motionSensorColourBox.add(sensorColourText);
		motionSensorColourBox.add(motionSensorColourChooser);


		afterImageColourBox.add(afterImageColourText);
		afterImageColourBox.add(afterImageColourChooser);


		movingFacesColourBox.add(movingFacesColourText);
		movingFacesColourBox.add(movingFacesColourChooser);


		constructedLineColourBox.add(constructedLineColourText);
		constructedLineColourBox.add(constructedLineColourChooser);


		meshColourBox.add(meshColourText);
		meshColourBox.add(meshColourChooser);
		meshColourBox.add(meshVisibilityChooser);


		alphaEffectBox.add(alphaEffectText);
		alphaEffectBox.add(alphaEffectChooser);


		alphaEffectBox2.add(alphaEffectText2);
		alphaEffectBox2.add(alphaEffectChooser2);


		weightFunctionBox.add(weightFunctionText);
		weightFunctionBox.add(weightFunctionChooser);


		motionEffectBox.add(motionEffectText);
		motionEffectBox.add(motionEffectChooser);


		interpTypeBox.add(interpTypeText);
		interpTypeBox.add(interpTypeChooser);


		constructedTailLengthBox.add(constructedTrailLengthText);
		constructedTailLengthBox.add(constructedTrailLengthTextContainer);
		constructedTailLengthBox.add(constructedTrailLengthBar);


		movingFacesTailLengthBox.add(movingFacesTrailLengthText);
		movingFacesTailLengthBox.add(movingFacesLengthTextContainer);
		movingFacesTailLengthBox.add(movingFacesTrailLengthBar);


		spreadEffectBox.add(spreadEffectText);
		spreadEffectBox.add(spreadEffectTextContainer);
		spreadEffectBox.add(spreadEffectBar);


		tailLengthBox.add(trailLengthText);
		tailLengthBox.add(trailLengthTextContainer);
		tailLengthBox.add(trailLengthBar);


		speedBox.add(speedText);
		speedBox.add(speedTextContainer);
		speedBox.add(speedBar);


		baseSizeBox.add(baseSizeText);
		baseSizeBox.add(baseSizeTextContainer);
		baseSizeBox.add(baseSizeScroller);


		lineDensityBox.add(lineDensityText);
		lineDensityBox.add(lineDensityTextContainer);
		lineDensityBox.add(lineDensityScroller);


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
		mainTopBox.add(meshColourBox);

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

	/**Useless function required for interface implementation**/
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

	/**Useless function required for interface implementation**/
	public void displayChanged(GLAutoDrawable gLDrawable, 
			boolean modeChanged, boolean deviceChanged) {
	}

	/**Useless function required for interface implementation**/
	public void reshape(GLAutoDrawable gLDrawable,
			int x,int y, int width, int height) {
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

	/**Useless function required for interface implementation**/
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			exit();
		}
	}

	/**Useless function required for interface implementation**/
	public void keyReleased(KeyEvent e) {

	}

	/**Useless function required for interface implementation**/
	public void keyTyped(KeyEvent e) {
	}


	/**--------------------------------------------------------------------**/
	/**                             IO FUNCTIONS                           **/
	/**--------------------------------------------------------------------**/
	/**Read in all the object files from a directory
	 * as described in the provided property file**/
	private void readObjects() {
		Properties metaFile = new Properties();
		try {
			metaFile.load(new FileInputStream
					("models/meta.properties"));
		} catch (FileNotFoundException e) {
			System.out.println(
			"Failed to read property file for object files");
			System.exit(1);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(
			"Failed to read property file for object files");
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
		motionTrails = mTools.getVertexTrails(
				"FRONT_BACK", processedData, numObjectFiles);

		/**Create the dataset for storing repetetive computations**/
		if(storedLinesComputation &&
				motionEffect.equals("Lines")) {
			this.setLineEffectVar();
		}

		if(storedFacesComputation &&
				motionEffect.equals("Surface Tracking")) {
			this.setContourFacesEffectVar();
		}

		if(storedMovingFacesComputation &&
				motionEffect.equals("Moving Faces")) {
			this.setMovingFacesEffectVar();
		}

		if(storedComposedLinesComputation &&
				motionEffect.equals("Constructed Lines")) {
			this.setConstructedLineEffectVar();
		}
	}


	/**--------------------------------------------------------------------**/
	/**                             MAIN FUNCTIONS                         **/
	/**--------------------------------------------------------------------**/
	/**Class Constructor**/
	public AnimatorRenderer() {
	}

	public static void main(String[] args) {
		AnimatorRenderer bd = new AnimatorRenderer();

		bd.readObjects();
		bd.initGUI();
	}
}
