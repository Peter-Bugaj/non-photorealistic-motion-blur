package CalculateData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Enumeration;
import java.util.Hashtable;

import IO.ProcessedData;
import Tools.MeshTools;
import Tools.VectorTools;


/**
 * 
 */

/**
 * @author Piotr Bugaj
 * @data June 20, 2010
 */
public class CalculateDistanceData {

	/**Tools  for reading  in the precalculated
	 * file for stored distance values**/
	private FileInputStream disDataInputStream;
	private InputStreamReader disDataStreamReader;
	private BufferedReader disDataReader;
	
	/**The data containing the location of all vertexes in all  time frames**/
	private ProcessedData [] processedData;

	/**Name of file where calculated data will be stored on the hard disk**/
	private String precalcDisFileName;
	
	/**Class used as a helper tool for analysing the mesh**/
	private MeshTools mTools;
	private VectorTools vectorTools;
	
	/**Flags for indicating  the start and end of a file, the start and end of
	 * a frame, and the start and the end of a vertex.**/
	private String startTag = "START";
	private String endTag = "END";
	private String sFrameTag = "SFRAME";
	private String eFrameTag = "EFRAME";
	
	public CalculateDistanceData(
			ProcessedData [] processedData, String precalcDisFileName) {
		
		System.out.println("\nCalculating stored distance value data");

		this.processedData = processedData;
		this.precalcDisFileName = precalcDisFileName;
		
		mTools = new MeshTools();
		vectorTools = new VectorTools();
		
		/**Initialize the readers for reading in the stored data**/
		try {
			disDataInputStream = new FileInputStream(precalcDisFileName);
			disDataStreamReader = new InputStreamReader(disDataInputStream);
			disDataReader = new BufferedReader(disDataStreamReader);
		} catch (FileNotFoundException e) {
			System.out.print("Failed to read stored " +
					"data file for distance values\n");
			System.exit(1);
			e.printStackTrace();
		}
	}

	/**Using  the  processed data,  precompute the
	 * distances  between all  connected  vertexes
	 * @throws IOException **/
	public void precalculateDistance() throws IOException {
	
		System.out.println("    Storing calculated distance data");
		
		/**Create the data file and initialize the writers**/
		BufferedWriter bWriter = null;
		try {
			bWriter = new BufferedWriter(new FileWriter(precalcDisFileName));
		} catch (IOException e1) {
			System.out.println("Failed to create file for " +
				"storing distance data.");
			System.exit(1);
			e1.printStackTrace();
		}

		/**Write the FLAG for indicating start of file**/
		bWriter.write(this.startTag + " " + "blank");
		bWriter.newLine();


		/**Iterate through each frame, calculating
		 * and   storing   the   required  data**/
		for(int i = 0; i < processedData.length; i++) {
			
			System.out.println("   Writing calculated " +
					"distances for frame: " + i);
			
			/**Write the FLAG for indicating start of a new frame**/
			bWriter.write(this.sFrameTag + " " + i);
			bWriter.newLine();

			/**First find all the edges existing within the current frame.**/
			float [][] tempVertexes = processedData[i].getFloatData(0);
			int [][] tempFaces = processedData[i].getIntData(2);
			Hashtable<String, float[][]> tempEdges =
				mTools.findAllEdges(tempVertexes, tempFaces);

			/**Now iterate through each edge, storing the
			 * distance between each connected vertex.**/
			Enumeration<float[][]> edgesEnum = tempEdges.elements();
			while(edgesEnum.hasMoreElements()) {
				float[][]nextEdge = edgesEnum.nextElement();
				
				float tempDis = vectorTools.distance(nextEdge[0],nextEdge[1]);
				
				/**Write the calculated distance**/
				bWriter.write(
						mTools.vForm(nextEdge[0]) +" "+
						mTools.vForm(nextEdge[1]) +" "+
						tempDis);
				bWriter.newLine();
			}

			
			/**Write the end tag for the frame**/
			bWriter.write(this.eFrameTag + " " + "blank");
			bWriter.newLine();
		}
		
		/**Write the FLAG for indicating the END of file**/
		bWriter.write(this.endTag + " " + "blank");
		bWriter.newLine();
		
		/**Close of the buffered writer**/
		bWriter.flush();
		bWriter.close();
		
		System.out.println("    Finished storing calculated distance data");
	}
	
	/**Get the required angle data from physical memory
	 * Return value: Hashtable
	 * Key: a b c d e f
	 * Value: distance between vertex (a, b, c) and (d, e, f)
	 * @throws IOException **/
	public Hashtable<String, Float> getDistanceData(
			String precalcDisFileName,
			int frame_i) throws IOException {
		
		Hashtable<String, Float> disData =
			new Hashtable<String, Float>();
		
		System.out.println("    Reading distance data for frame: " + frame_i);
		
		String probeString = "null null";
		String [] stringParts = probeString.split("( )");

		/**Move the pointer within the file to the data
		 * corresponding to the  given frame  number**/
		while(!(stringParts[0].equals(sFrameTag)) ||
				!(stringParts[1].equals(frame_i + ""))) {

			/**Reset the buffer if the end is reached**/
			if(stringParts[0].equals(endTag)) {
				disDataInputStream.close();
				disDataStreamReader.close();
				disDataReader.close();

				disDataInputStream =
					new FileInputStream(precalcDisFileName);
				disDataStreamReader =
					new InputStreamReader(disDataInputStream);
				disDataReader =
					new BufferedReader(disDataStreamReader);
			}
			probeString = disDataReader.readLine();
			stringParts = probeString.split("( )");
		}

		/**Now store the vertexes**/

		while(!stringParts[0].equals(eFrameTag)) {


			if (stringParts.length > 2) {
				
				disData.put(new String(
						stringParts[0] +" "+
						stringParts[1] +" "+
						stringParts[2] +" "+
						stringParts[3] +" "+
						stringParts[4] +" "+
						stringParts[5]), Float.parseFloat(stringParts[6]));
				
				probeString = disDataReader.readLine();
				stringParts = probeString.split("( )");
			} else {
				probeString = disDataReader.readLine();
				stringParts = probeString.split("( )");
			}
		}

		System.out.println("    Finished reading distance " +
				"data for frame: " + frame_i);
		return disData;
	}
}
