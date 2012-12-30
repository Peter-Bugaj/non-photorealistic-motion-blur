package CalculateData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

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
public class CalculateAngleData {

	/**Variable indicating whether or not the
	 * data is already stored on hard disk**/
	private final boolean dataStored = true;

	/**Tools  for reading  in the precalculated
	 * file for stored distance values**/
	private FileInputStream angleDataInputStream;
	private InputStreamReader angleDataStreamReader;
	private BufferedReader angleDataReader;

	/**The data containing the location of all vertexes in all  time frames**/
	private ProcessedData [] processedData;

	/**Class used as a helper tool for analysing the mesh**/
	private MeshTools mTools;
	private VectorTools vectorTools;

	/**Flags for indicating  the start and end of a file, the start and end of
	 * a frame, and the start and the end of a vertex.**/
	private String startTag = "START";
	private String endTag = "END";
	private String sFrameTag = "SFRAME";
	private String eFrameTag = "EFRAME";
	private String sVertexTag = "SVERTEX";
	private String eVertexTag = "EVERTEX";

	public CalculateAngleData(
			ProcessedData [] processedData) {

		System.out.println("\nCalculating stored angle value data");

		this.processedData = processedData;

		mTools = new MeshTools();
		vectorTools = new VectorTools();
	}

	/**Using  the processed data,  precompute the angle for all sets  of three
	 * vertices
	 * @throws IOException **/
	public void precalculateAngles(String precalcAngFileName) throws IOException {

		if(!dataStored) {
			System.out.println("    Storing calculated angle data");

			/**Create the data file and initialize the writers**/
			BufferedWriter bWriter = null;
			try {
				bWriter = new BufferedWriter(new FileWriter(precalcAngFileName));
			} catch (IOException e1) {
				System.out.println("Failed to create file for " +
				"storing angle data.");
				System.exit(1);
				e1.printStackTrace();
			}

			/**Write the FLAG for indicating start of file**/
			bWriter.write(this.startTag + " " + "blank");
			bWriter.newLine();


			/**Iterate through each frame, calculating
			 * and   storing   the   required  data**/
			for(int i = 0; i < processedData.length; i++) {

				System.out.println("   Writing calculated angles for frame: " + i);

				/**Write the FLAG for indicating start of a new frame**/
				bWriter.write(this.sFrameTag + " " + i);
				bWriter.newLine();

				/**First find all the edges existing within the current frame.
				 * These edges will be required to calculate the neighbouring
				 * faces for each face**/
				float [][] tempVertexes = processedData[i].getFloatData(0);
				int [][] tempFaces = processedData[i].getIntData(2);
				Hashtable<String, float[][]> tempEdges =
					mTools.findAllEdges(tempVertexes, tempFaces);

				/**Now using the edges, construct the vertex neighbours**/
				Hashtable<String, float[][]> tempVertexNeighbours =
					mTools.getVertexNeighbours(tempEdges);

				/**Now iterate through each vertex, iterating through each of  the
				 * vertex's  neighbours and  storing  all the  possible  angles**/
				for(int j = 0; j < tempVertexes.length; j++) {

					/**Write the FLAG for indicating
					 * the  start of a new vertex**/
					bWriter.write(this.sVertexTag + " " + j);
					bWriter.newLine();

					float [] tempVertex = tempVertexes[j];
					float [][] tempNeigh =
						tempVertexNeighbours.get(mTools.vForm(tempVertex));

					for(int z = 0; z < tempNeigh.length; z++) {
						for(int g = (z+1); g < tempNeigh.length; g++) {
							float [] leftV = tempNeigh[z];
							float [] rightV = tempNeigh[g];

							float zjgAng =
								vectorTools.ang(leftV, tempVertex, rightV);

							/**Write the calculated angle**/
							bWriter.write(
									mTools.vForm(leftV) +" "+
									mTools.vForm(rightV) +" "+
									zjgAng);
							bWriter.newLine();
						}
					}

					/**Write the end tag for the vertex**/
					bWriter.write(this.eVertexTag + " " + "blank");
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
		}

		/**Initialize the readers for reading in the stored data**/
		try {
			angleDataInputStream = new FileInputStream(precalcAngFileName);
			angleDataStreamReader = new InputStreamReader(angleDataInputStream);
			angleDataReader = new BufferedReader(angleDataStreamReader);
		} catch (FileNotFoundException e) {
			System.out.print("Failed to read stored " +
			"data file for angle values\n");
			System.exit(1);
			e.printStackTrace();
		}

		System.out.println("    Finished storing calculated angle data");
	}

	/**Get the required angle data from physical memory
	 * @throws IOException **/
	public Hashtable<String, Float> getAngleData(
			String precalcAngFileName, 
			int frame_i) throws IOException {

		Hashtable<String, Float> angleData =
			new Hashtable<String, Float>();

		System.out.println("    Reading angle data for frame: " + frame_i);

		String probeString = "null null";
		String [] stringParts = probeString.split("( )");

		/**Move the pointer within the file to the data
		 * corresponding to the  given frame  number**/
		while(!(stringParts[0].equals(sFrameTag)) ||
				!(stringParts[1].equals(frame_i + ""))) {

			/**Reset the buffer if the end is reached**/
			if(stringParts[0].equals(endTag)) {
				angleDataInputStream.close();
				angleDataStreamReader.close();
				angleDataReader.close();

				angleDataInputStream =
					new FileInputStream(precalcAngFileName);
				angleDataStreamReader =
					new InputStreamReader(angleDataInputStream);
				angleDataReader =
					new BufferedReader(angleDataStreamReader);
			}
			probeString = angleDataReader.readLine();
			stringParts = probeString.split("( )");
		}

		/**Now store the vertexes**/

		/**Move the file reader pointer as its
		 * currently on the start frame tag**/
		int vertexNum = 0;

		while(!stringParts[0].equals(eFrameTag)) {

			if(stringParts[0].equals(this.sVertexTag)) {
				vertexNum = Integer.parseInt(stringParts[1]);

				probeString = angleDataReader.readLine();
				stringParts = probeString.split("( )");

			} else if(stringParts[0].equals(this.eVertexTag)) {
				probeString = angleDataReader.readLine();
				stringParts = probeString.split("( )");

			} else if (stringParts.length > 2) {
				
				angleData.put(new String(
						stringParts[0] +" "+
						stringParts[1] +" "+
						stringParts[2] +" "+
						vertexNum +" "+
						stringParts[3] +" "+
						stringParts[4] +" "+
						stringParts[5]), Float.parseFloat(stringParts[6]));
				
				probeString = angleDataReader.readLine();
				stringParts = probeString.split("( )");
			} else {
				probeString = angleDataReader.readLine();
				stringParts = probeString.split("( )");
			}
		}

		System.out.println("    Finished reading angle " +
				"data for frame: " + frame_i);
		return angleData;
	}
}
