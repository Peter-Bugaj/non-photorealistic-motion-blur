package IO;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * @author Piotr Bugaj
 * @date May 21, 2010
 */
public class ObjectReader extends Thread{

	/**Tools for reading in the surface files**/
	private FileInputStream surfaceInputStream;
	private InputStreamReader surfaceStreamReader;
	private BufferedReader surfaceReader;
	
	/**Variables for storing and reading through the surface data**/
	private int numberOfVertexes = 0;
	private int numberOfFaces = 0;
	private int numberOfNormals = 0;
	private int numberOfTextures = 0;

	/**The array storing the 3D surface points**/
	float [][] surfacePoints;
	/**The array storing the 3D face points**/
	int [][] facePoints;
	/**The array storing the normal points**/
	float [][] normalPoints;
	/**The array storing the texture values for each face**/
	float [][] texturePoints;
	
	/**Array storing the process data for each data array**/
	private ProcessedData processedData;
	
	private boolean facePointsPresent = false;
	private boolean surfacePointsPresent = false;
	private boolean texturePointsPresent = false;
	
	static float f = 0.0f;
	
	/**The name of the file being read**/
	private String fileName;
	
	public static void main(String[] args) {
	}

	public ObjectReader(String fileName) {
		//f+=0.02f;
		this.fileName = fileName;
	}

	public void run() {
		System.out.println("Starting file reader: " + fileName);
		readFile(fileName);
		System.gc();
		
		readInData();
		System.gc();
		
		storeData();
		System.gc();
		
		closeFile();
		System.gc();
	}
	
	public ProcessedData getProcessedData() {
		return processedData;
	}
	
	/**Initialize the input reader from the file**/
	private void readFile(String fileName) {
		System.out.println("    Opening in file data");
		try {
			surfaceInputStream = new FileInputStream(fileName);
			surfaceStreamReader = new InputStreamReader(surfaceInputStream);
			surfaceReader = new BufferedReader(surfaceStreamReader);
			
			surfaceReader.mark(1000);
		} catch (FileNotFoundException e) {
			System.out.print("    Failed to open object file\n");
			System.exit(1);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.print("    Failed to open object file\n");
			System.exit(1);
			e.printStackTrace();
		}
	}
	
	/**Get the required information about the surface points to be stored**/
	private void readInData() {
		System.out.println("    Reading in data");
		try {
			surfaceReader.reset();
		} catch (IOException e) {
			System.out.print("    Failed to read input file\n");
			System.exit(1);
			e.printStackTrace();
		}

		String probeString = "";

		try {
			probeString = surfaceReader.readLine();
			while(probeString.indexOf("obj format") < 0 && probeString != null) {
				probeString = surfaceReader.readLine();
			}
		} catch (IOException e) {
			System.out.print("    Failed to read input file\n");
			System.exit(1);
			e.printStackTrace();
		}
		String [] formatInfo = probeString.split("( )");
		System.out.println(probeString);
		
		/**Read  in  the quantities  of
		 * each type of point stored**/
		for (int i = 0; i< formatInfo.length; i++) {
			if(formatInfo[i].indexOf("vertices") > -1) {
				surfacePointsPresent = true;

				numberOfVertexes = Integer.parseInt(formatInfo[i-1]);
				numberOfNormals = numberOfVertexes;
				System.out.println("Number of vertexes: " + numberOfVertexes);
				System.out.println("Number of normals: " + numberOfNormals);
			} else if(formatInfo[i].indexOf("triangles") > -1) {
				facePointsPresent = true;
				
				numberOfFaces = Integer.parseInt(formatInfo[i-1]);
				System.out.println("Number of faces: " + numberOfFaces);
			} else if(formatInfo[i].indexOf("texture") > -1) {
				texturePointsPresent = true;
				
				numberOfFaces = Integer.parseInt(formatInfo[i-1]);
				System.out.println("Number of textures: " + numberOfTextures);
			}  
		}

		if(texturePointsPresent) {
			processedData = new ProcessedData(
				new String[] {"vertexes", "normals", "faces", "textures"}, 
				new String[]{"FLOAT32", "FLOAT32", "INT32", "FLOAT32"}, 4);
		} else {
			processedData = new ProcessedData(
					new String[] {"vertexes", "normals", "faces"}, 
					new String[]{"FLOAT32", "FLOAT32", "INT32"}, 3);			
		}
	
	}
	
	/**Store all the surface and face points into an array for OPENGL input**/
	private void storeData() {

		System.out.print("    Storing data\n");
		if(facePointsPresent) {
			facePoints = new int [numberOfFaces][3];
			normalPoints = new float[numberOfNormals][3];
		}
		
		if(surfacePointsPresent) {
			surfacePoints = new float[numberOfVertexes][3];
		}
		
		if(texturePointsPresent) {
			texturePoints = new float[numberOfTextures][3];
		}
		
		String probeString = "";
		int facePointCounter = 0;
		int surfacePointCounter = 0;
		int normalPointCounter = 0;
		int texturePointCounter = 0;
		
		try {
			while ((probeString = surfaceReader.readLine()) != null){

				/**Store a normal point**/
				if (probeString.indexOf("vn") > -1) {
					try {
						String [] temp  = probeString.split("( )( )*");
						normalPoints[normalPointCounter] = new float[]{
							Float.parseFloat(temp[1]),
							Float.parseFloat(temp[2]),
							Float.parseFloat(temp[3])};
					} catch (NumberFormatException e) {
						System.out.println("    Failed to read normal point, number: " + normalPointCounter);
						normalPoints[normalPointCounter] = new float[]{0.0f, 0.0f, 0.0f};
					}
					normalPointCounter++;
				} 
				/**Store a vertex point**/
				else if(probeString.indexOf('v') > -1) {
					try {
						String [] temp  = probeString.split("( )( )*");
						surfacePoints[surfacePointCounter] = new float[]{
							Float.parseFloat(temp[1])+f,
							Float.parseFloat(temp[2])+f,
							Float.parseFloat(temp[3])+(f*0.4f)};
					} catch (NumberFormatException e) {
						System.out.println("    Failed to read surface point, number: " + surfacePointCounter);
						surfacePoints[surfacePointCounter] = new float[]{0.0f, 0.0f, 0.0f};
					}
					surfacePointCounter++;
				/**Store a face point**/	
				} else if(probeString.indexOf('f') > -1) {
					try {
						String [] temp  = probeString.split("( )( )*");
						String [] temp1a = temp[1].split("//");
						String [] temp1b = temp[2].split("//");
						String [] temp1c = temp[3].split("//");
						facePoints[facePointCounter] = new int[]{
							Integer.parseInt(temp1a[0]),
							Integer.parseInt(temp1b[0]),
							Integer.parseInt(temp1c[0])};
					} catch (NumberFormatException e) {
						System.out.println("    Failed to read face point, number: " + facePointCounter);
						facePoints[facePointCounter] = new int[]{0,0,0};
					}
					facePointCounter++;
				/**Store a texture point**/
				} else if(probeString.indexOf('t') > -1){
					try {
						String [] temp  = probeString.split("( )( )*");
						texturePoints[texturePointCounter] = new float[]{
							Float.parseFloat(temp[1]),
							Float.parseFloat(temp[2]),
							Float.parseFloat(temp[3])};
					} catch (NumberFormatException e) {
						System.out.println("    Failed to read texture point, number: " + texturePointCounter);
						texturePoints[texturePointCounter] = new float[]{0.0f, 0.0f, 0.0f};
					}
					texturePointCounter++;
				}
			}
		} catch (IOException e1) {
			System.out.print("    Failed to read store data\n");
			e1.printStackTrace();
		}
		
		/**Check that the correct number of points has been stored**/
		if((facePointCounter != facePoints.length) || 
		   (surfacePointCounter != surfacePoints.length) ||
		   (normalPointCounter != normalPoints.length)) {
			System.out.println("    Missing number of points\n");
			System.exit(1);
		}
		
		/**Store the resulting data in an object**/
		if(surfacePointsPresent) {
			processedData.setData(0, surfacePoints);
			System.out.println("    Stored " + surfacePoints.length + " vertex points");
			processedData.setData(1, normalPoints);
			System.out.println("    Stored " + surfacePoints.length + " normal points");
		}
		if(facePointsPresent) {
			processedData.setData(2, facePoints);
			System.out.println("    Stored " + facePoints.length + " face points");
		}
		if(texturePointsPresent) {
			processedData.setData(3, texturePoints);
			System.out.println("    Stored " + texturePoints.length + " texture points");
		}
	}

	private void closeFile() {
		try {
			surfaceReader.close();
			surfaceStreamReader.close();
			surfaceInputStream.close();
		} catch (IOException e) {
			System.out.println("    Failed to close surface data file");
			System.exit(1);
			e.printStackTrace();
		}
		
	}
}
