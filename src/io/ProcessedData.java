package IO;
/**
 * 
 */

/**
 * @author Piotr Bugaj
 * @date May 21, 2010
 */
public class ProcessedData {

	/**Description of data stored**/
	private String [] dataTypes;
	
	/**Dimension of data stored**/
	private int numArrays;

	/**Intent of the data**/
	private String [] intent;
	
	/**Arrays  storing the data  values corresponding
	 * to the description of the to be data stored**/
	private Object [] data;
	
	public ProcessedData(String [] intent, String [] dataTypes, int numArrays) {
		this.dataTypes = dataTypes;
		this.numArrays = numArrays;
		this.intent = intent;
		
		data = new Object [numArrays];
	}
	
	/**Stored an integer array**/
	public void setData(int setNumber, int [][] inputData) {
		if(dataTypes[setNumber].equals("INT32")) {
			data[setNumber] = inputData;
		} else {
			System.out.println("Error: Wrong function call\n" +
					"Setting integer array to data type: " +
					dataTypes[setNumber]);
			System.exit(1);
		}
	}

	/**Store a float array**/
	public void setData(int setNumber, float [][] inputData) {
		if(dataTypes[setNumber].equals("FLOAT32")) {
			data[setNumber] = inputData;
		} else {
			System.out.println("Error: Wrong function call\n" +
					"    Setting float array to data type: " +
					dataTypes[setNumber]);
			System.exit(1);
		}
	}
	
	/**Return the requested float data**/
	public float [][] getFloatData(int i) {
		if(dataTypes[i].equals("FLOAT32")) {
			return (float [][])data[i];
		} else {
			System.out.println("Error: Wrong function call\n" +
					"Requesting a float array from data type: " +
					dataTypes[i]);
			System.exit(1);
			return null;
		}
	}
	
	/**Return the requested integer data**/
	public int [][] getIntData(int i) {
		if(dataTypes[i].equals("INT32")) {
			return (int [][])data[i];
		} else {
			System.out.println("Error: Wrong function call\n" +
					"Requesting an integer array from data type: " +
					dataTypes[i]);
			System.exit(1);
			return null;
		}
	}

	/**Return the data description**/
	public String getDataTypes(int i) {
		return dataTypes[i];
	}

	/**Return the number of data arrays stored in the object**/
	public int getNumDataArrays() {
		return numArrays;
	}
	
	/**Return the intent of a given data set**/
	public String getIntent(int i) {
		return intent[i];
	}
}