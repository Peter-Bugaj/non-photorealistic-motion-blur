/**
 * 
 */
package main;

import java.awt.Choice;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.TextComponent;

import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.Animator;

/**
 * @author Piotr Bugaj
 *
 */
public class GUI {

	/**------------Main GL variables---------------------------------------**/
	protected GLU glu = new GLU();
	protected GLCanvas canvas = new GLCanvas();
	protected Frame frame;
	protected Animator animator = new Animator(canvas); 
	protected GL gl;
	/**--------------------------------------------------------------------**/
	
	
	/**------------GUI-----------------------------------------------------**/
	protected Container mainBottomBox;
	protected Container mainTopBox;
	protected Container mainTopPropertiesBox;
	protected Container movementBox;
	protected Container rotationBox;
	protected Container speedBox;
	protected Container tailLengthBox;
	protected Container constructedTailLengthBox;
	protected Container surfaceTrackingTailLengthBox;
	protected Container movingFacesTailLengthBox;
	protected Container interpTypeBox;
	protected Container motionEffectBox;
	protected Container lineColourBox;
	protected Container constructedLineColourBox;
	protected Container afterImageColourBox;
	protected Container movingFacesColourBox;
	protected Container weightFunctionBox;
	protected Container meshColourBox;
	protected Container baseSizeBox;
	protected Container lineDensityBox;
	protected Container spreadEffectBox;
	protected Container alphaEffectBox;
	protected Container alphaEffectBox2;
	protected Container motionSensorColourBox;
	
	/**GUI for controlling the x movement of the mesh**/
	protected Scrollbar xAxisMovement;
	protected static float xMovementScroller = 0;
	protected TextComponent xAxisMovementText;
	
	/**GUI for controlling the y movement of the mesh**/
	protected Scrollbar yAxisMovement;
	protected static float yMovementScroller = 0;
	protected TextComponent yAxisMovementText;

	/**GUI for controlling the z movement of the mesh**/
	protected Scrollbar zAxisMovement;
	protected static float zMovementScroller = 0;
	protected TextComponent zAxisMovementText;

	/**GUI for controlling the x rotation of the mesh**/
	protected Scrollbar xRotation;
	protected static float xRotationScroller = 326;
	protected TextComponent xRotationText;

	/**GUI for controlling the y rotation of the mesh**/
	protected Scrollbar yRotation;
	protected static float yRotationScroller = 38;
	protected TextComponent yRotationText;

	/**GUI for controlling the z rotation of the mesh**/
	protected Scrollbar zRotation;
	protected static float zRotationScroller = 13;
	protected TextComponent zRotationText;

	/**Variable describing the dimension of the GUI**/
	protected final int frameWidth = 980;
	protected final int frameHeight = 570;

	/**Label used for pop-up meny box**/
	Label propertiesText = new Label("Properties");

	/**GUI for controlling the speed of animation**/
	protected Scrollbar speedBar;
	protected static float speedScroller = 19;
	protected TextComponent speedTextComponent;
	protected int speedIntervalCounter = 0;

	/**GUI   for   controlling   the  spread
	 * effect for the moving faces effect**/
	protected Scrollbar spreadEffectBar;
	protected float spreadEffectBarScroller = 5;
	protected TextComponent spreadEffectTextComponent;
	
	/**GUI  for  controlling  the  length  of  the
	 * motion trails for moving surfaces effect**/
	protected Scrollbar movingFacesTrailLengthBar;
	protected int movingFacesTrailLengthBarScroller = 4;
	protected TextComponent movingFacesTrailLengthTextComponent;
	
	/**GUI for controlling the length of the motion trails**/
	protected Scrollbar trailLengthBar;
	protected static int trailLengthBarScroller = 4;
	protected TextComponent trailLengthTextComponent;

	/**GUI for controlling the length of the constructed trails**/
	protected Scrollbar constructedTrailLengthBar;
	protected static int constructedTrailLengthBarScroller = 3;
	protected TextComponent constructedTrailLengthTextComponent;

	/**GUI for controlling the length of the constructed trails**/
	protected Scrollbar surfaceTrackingTrailLengthBar;
	protected static int surfaceTrackingTrailLengthBarScroller = 1;
	protected TextComponent surfaceTrackingTrailLengthTextComponent;
	
	/**GUI for controlling the interpolation of singular lines**/
	protected Choice interpTypeChooser;
	protected String interpType = "None";

	/**GUI for controlling wheather the mesh is visible or not**/
	protected Choice meshVisibilityChooser;
	protected String meshVisibility = "ON";
	
	/**GUI for controlling whether the alpha effect
	 * will be used for the motion  sensor effect**/
	protected Choice alphaEffectChooser;
	protected String alphaEffect = "ON";
	
	/**GUI for  controlling how the  alpha effect
	 * will be used for various motion effects**/
	protected Choice alphaEffectChooser2;
	protected String alphaEffect2 = "Frame";
	
	/**GUI for controlling the motion effect**/
	protected Choice motionEffectChooser;
	protected String motionEffect = "Lines";

	/**GUI for controlling the colour of single lines**/
	protected Choice lineColourChooser;
	protected String lineColour = "DARK BROWN";

	/**GUI for controlling the colour the mesh**/
	protected Choice meshColourChooser;
	protected String meshColour = "DARK BROWN";

	/**GUI for controlling the colour of the motion sensors**/
	protected Choice motionSensorColourChooser;
	protected String motionSensorColour = "DARK BROWN";
	
	/**GUI for controlling the colour of the after images**/
	protected Choice constructedLineColourChooser;
	protected String constructedLineColour = "DARK BROWN";
	
	/**GUI for controlling the colour of the after images**/
	protected Choice movingFacesColourChooser;
	protected String movingFacesColour = "DARK BROWN";
	
	/**GUI for controlling the colour of the after images**/
	protected Choice afterImageColourChooser;
	protected String afterImageColour = "DARK BROWN";
	
	/**GUI for controlling the base of the constructed lines**/
	protected Scrollbar baseSizeScroller;
	protected static int baseSize = 4;
	protected TextComponent baseSizeTextComponent;

	protected Scrollbar lineDensityScroller;
	protected static int lineDensity = 99;
	protected TextComponent lineDensityTextComponent;

	/**GUI for controlling the  weight function
	 * of how the composed lines are created**/
	protected Choice weightFunctionChooser;

	protected String weightFunctionEffect = "None";
	/**--------------------------------------------------------------------**/
}
