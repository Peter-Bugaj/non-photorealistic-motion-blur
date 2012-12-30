package Testing;

import java.util.Enumeration;
import java.util.Hashtable;

import Tools.MeshTools;


/**
 * 
 */

/**
 * @author Piotr Bugaj
 * @date June 9, 2010
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Test t = new Test();
		t.doTest1();
	}
	
	MeshTools mTools;
	
	public Test() {
		mTools = new MeshTools();
	}

	/**Create edges defined by vertexes and construct the following triangles
	 * that the edges form. Output should be instantaneous.**/
	public void doTest1() {
		
		Hashtable<String, float[][]> edges = new Hashtable<String, float[][]>();
		
		float v1[] = new float[]{12, 11, -8};
		float v2[] = new float[]{9, 5, 77};
		float v3[] = new float[]{8, 2, 8};
		float v4[] = new float[]{8, 9, 1};
		float v5[] = new float[]{2, 11, 22};
		float v6[] = new float[]{4, 44, 1};
		float v7[] = new float[]{7, 4, 9};
		float v8[] = new float[]{7, 17, 18};
		float v9[] = new float[]{13, 6, 2};
		float v10[] = new float[]{1, 10, 11};
		float v11[] = new float[]{60, 6, 17};
		float v12[] = new float[]{14, 5, 5};
		float v13[] = new float[]{9, 9, 0};
		float v14[] = new float[]{17, 26, 22};
		float v15[] = new float[]{4, 55, 5};
		float v16[] = new float[]{6, 32, 9};
		float v17[] = new float[]{44, 11, 8};
		float v18[] = new float[]{3, 20, 9};
		float v19[] = new float[]{2, 4, 6};
		float v20[] = new float[]{14, 30, 8};
		float v21[] = new float[]{3, 9, 12};
		float v22[] = new float[]{1, 2, 6};
		
		float vertexes[][] = new float [][]{new float[]{12, 11, -8},
		new float[]{9, 5, 77},
		new float[]{8, 2, 8},
		new float[]{8, 9, 1},
		new float[]{2, 11, 22},
		new float[]{4, 44, 1},
		new float[]{7, 4, 9},
		new float[]{7, 17, 18},
		new float[]{13, 6, 2},
		new float[]{1, 10, 11},
		new float[]{60, 6, 17},
		new float[]{14, 5, 5},
		new float[]{9, 9, 0},
		new float[]{17, 26, 22},
		new float[]{4, 55, 5},
		new float[]{6, 32, 9},
		new float[]{44, 11, 8},
		new float[]{3, 20, 9},
		new float[]{2, 4, 6},
		new float[]{14, 30, 8},
		new float[]{3, 9, 12},
		new float[]{1, 2, 6}};
		
		
		edges.put(new String((v1[0]+v2[0]) +" "+
				             (v1[1]+v2[1]) +" "+
				             (v1[2]+v2[2])
				                ), new float [][]{v1, v2});
		edges.put(new String(
				(v2[0]+v8[0]) +" "+
	             (v2[1]+v8[1]) +" "+
	             (v2[2]+v8[2])
	                ), new float [][]{v2, v8});
		edges.put(new String(
				(v2[0]+v5[0]) +" "+
	             (v2[1]+v5[1]) +" "+
	             (v2[2]+v5[2])
	                ), new float [][]{v2, v5});
		edges.put(new String(
				(v6[0]+v5[0]) +" "+
	             (v6[1]+v5[1]) +" "+
	             (v6[2]+v5[2])
	                ), new float [][]{v6, v5});
		edges.put(new String(
				(v3[0]+v4[0]) +" "+
	             (v3[1]+v4[1]) +" "+
	             (v3[2]+v4[2])
	                ), new float [][]{v3, v4});
		edges.put(new String(
				(v6[0]+v4[0]) +" "+
	             (v6[1]+v4[1]) +" "+
	             (v6[2]+v4[2])
	                ), new float [][]{v6, v4});
		edges.put(new String(
				(v7[0]+v4[0]) +" "+
	             (v7[1]+v4[1]) +" "+
	             (v7[2]+v4[2])
	                ), new float [][]{v7, v4});
		edges.put(new String(
				(v9[0]+v5[0]) +" "+
	             (v9[1]+v5[1]) +" "+
	             (v9[2]+v5[2])
	                ), new float [][]{v9, v5});
		edges.put(new String(
				(v8[0]+v5[0]) +" "+
	             (v8[1]+v5[1]) +" "+
	             (v8[2]+v5[2])
	                ), new float [][]{v8, v5});
		edges.put(new String(
				(v6[0]+v7[0]) +" "+
	             (v6[1]+v7[1]) +" "+
	             (v6[2]+v7[2])
	                ), new float [][]{v6, v7});
		edges.put(new String(
				(v6[0]+v10[0]) +" "+
	             (v6[1]+v10[1]) +" "+
	             (v6[2]+v10[2])
	                ), new float [][]{v6, v10});
		edges.put(new String(
				(v6[0]+v9[0]) +" "+
	             (v6[1]+v9[1]) +" "+
	             (v6[2]+v9[2])
	                ), new float [][]{v6, v9});
		edges.put(new String(
				(v9[0]+v10[0]) +" "+
	             (v9[1]+v10[1]) +" "+
	             (v9[2]+v10[2])
	                ), new float [][]{v9, v10});
		edges.put(new String(
				(v9[0]+v13[0]) +" "+
	             (v9[1]+v13[1]) +" "+
	             (v9[2]+v13[2])
	                ), new float [][]{v9, v13});
		edges.put(new String(
				(v10[0]+v13[0]) +" "+
	             (v10[1]+v13[1]) +" "+
	             (v10[2]+v13[2])
	                ), new float [][]{v10, v13});
		edges.put(new String(
				(v15[0]+v12[0]) +" "+
	             (v15[1]+v12[1]) +" "+
	             (v15[2]+v12[2])
	                ), new float [][]{v15, v12});
		edges.put(new String(
				(v5[0]+v12[0]) +" "+
	             (v5[1]+v12[1]) +" "+
	             (v5[2]+v12[2])
	                ), new float [][]{v5, v12});
		edges.put(new String(
				(v11[0]+v12[0]) +" "+
	             (v11[1]+v12[1]) +" "+
	             (v11[2]+v12[2])
	                ), new float [][]{v11, v12});
		edges.put(new String(
				(v11[0]+v8[0]) +" "+
	             (v11[1]+v8[1]) +" "+
	             (v11[2]+v8[2])
	                ), new float [][]{v11, v8});
		edges.put(new String(
				(v8[0]+v12[0]) +" "+
	             (v8[1]+v12[1]) +" "+
	             (v8[2]+v12[2])
	                ), new float [][]{v8, v12});
		edges.put(new String(
				(v22[0]+v21[0]) +" "+
	             (v22[1]+v21[1]) +" "+
	             (v22[2]+v21[2])
	                ), new float [][]{v22, v21});
		edges.put(new String(
				(v18[0]+v21[0]) +" "+
	             (v18[1]+v21[1]) +" "+
	             (v18[2]+v21[2])
	                ), new float [][]{v18, v21});
		edges.put(new String(
				(v20[0]+v21[0]) +" "+
	             (v20[1]+v21[1]) +" "+
	             (v20[2]+v21[2])
	                ), new float [][]{v20, v21});
		edges.put(new String(
				(v18[0]+v20[0]) +" "+
	             (v18[1]+v20[1]) +" "+
	             (v18[2]+v20[2])
	                ), new float [][]{v18, v20});
		edges.put(new String(
				(v19[0]+v20[0]) +" "+
	             (v19[1]+v20[1]) +" "+
	             (v19[2]+v20[2])
	                ), new float [][]{v19, v20});
		edges.put(new String(
				(v18[0]+v19[0]) +" "+
	             (v18[1]+v19[1]) +" "+
	             (v18[2]+v19[2])
	                ), new float [][]{v18, v19});
		edges.put(new String(
				(v18[0]+v16[0]) +" "+
	             (v18[1]+v16[1]) +" "+
	             (v18[2]+v16[2])
	                ), new float [][]{v18, v16});
		edges.put(new String(
				(v19[0]+v16[0]) +" "+
	             (v19[1]+v16[1]) +" "+
	             (v19[2]+v16[2])
	                ), new float [][]{v19, v16});
		edges.put(new String(
				(v18[0]+v15[0]) +" "+
	             (v18[1]+v15[1]) +" "+
	             (v18[2]+v15[2])
	                ), new float [][]{v18, v15});
		edges.put(new String(
				(v19[0]+v17[0]) +" "+
	             (v19[1]+v17[1]) +" "+
	             (v19[2]+v17[2])
	                ), new float [][]{v19, v17});
		edges.put(new String(
				(v16[0]+v15[0]) +" "+
	             (v16[1]+v15[1]) +" "+
	             (v16[2]+v15[2])
	                ), new float [][]{v16, v15});
		edges.put(new String(
				(v16[0]+v17[0]) +" "+
	             (v16[1]+v17[1]) +" "+
	             (v16[2]+v17[2])
	                ), new float [][]{v16, v17});
		edges.put(new String(
				(v11[0]+v15[0]) +" "+
	             (v11[1]+v15[1]) +" "+
	             (v11[2]+v15[2])
	                ), new float [][]{v11, v15});
		edges.put(new String(
				(v14[0]+v15[0]) +" "+
	             (v14[1]+v15[1]) +" "+
	             (v14[2]+v15[2])
	                ), new float [][]{v14, v15});
		edges.put(new String(
				(v14[0]+v11[0]) +" "+
	             (v14[1]+v11[1]) +" "+
	             (v14[2]+v11[2])
	                ), new float [][]{v14, v11});
		edges.put(new String(
				(v16[0]+v12[0]) +" "+
	             (v16[1]+v12[1]) +" "+
	             (v16[2]+v12[2])
	                ), new float [][]{v16, v12});
		edges.put(new String(
				(v16[0]+v13[0]) +" "+
	             (v16[1]+v13[1]) +" "+
	             (v16[2]+v13[2])
	                ), new float [][]{v16, v13});
		edges.put(new String(
				(v16[0]+v9[0]) +" "+
	             (v16[1]+v9[1]) +" "+
	             (v16[2]+v9[2])
	                ), new float [][]{v16, v9});
		edges.put(new String(
				(v9[0]+v12[0]) +" "+
	             (v9[1]+v12[1]) +" "+
	             (v9[2]+v12[2])
	                ), new float [][]{v9, v12});
		edges.put(new String(
				(v17[0]+v13[0]) +" "+
	             (v17[1]+v13[1]) +" "+
	             (v17[2]+v13[2])
	                ), new float [][]{v17, v13});
		
		MeshTools mTools = new MeshTools();
		
		/**Check that correct neighbours are stored for each vertex given the edges**/
		Hashtable<String, float[][]> neighb = mTools.getVertexNeighbours(edges);

		for(int i = 0; i < vertexes.length; i++) {

			System.out.println();
			System.out.print(vertexes[i][0] +" "+vertexes[i][1] +" "+vertexes[i][2] +" " + "££");
			
			float ne[][] = neighb.get(new String(vertexes[i][0] +" "+
					vertexes[i][1] +" "+vertexes[i][2]));
			
			for(int j = 0; j < ne.length; j++) {
				System.out.print(ne[j][0] + " " + ne[j][1] + " " +ne[j][2] + "~~");
			}
		}
		/**
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		Vector<float[][]>  faces = mTools.getComposedFaces(edges, neighb, vertexes);
		float [][][] facesArray = new float[faces.size()][3][3];
		faces.toArray(facesArray);
		
		for(int i = 0; i < facesArray.length; i++) {

			System.out.println();
			System.out.print(facesArray[i][0][0] +" "+facesArray[i][0][1] +" "+facesArray[i][0][2] +" " + "££");
			System.out.print(facesArray[i][1][0] +" "+facesArray[i][1][1] +" "+facesArray[i][1][2] +" " + "££");
			System.out.print(facesArray[i][2][0] +" "+facesArray[i][2][1] +" "+facesArray[i][2][2] +" " + "££");
		}
		**/
	}

	public Boolean doTest9(float[][][] base_i, String name) {

			Hashtable<String, Integer> h = new Hashtable<String, Integer>();
			
			for(int j = 0; j< base_i.length; j++) {
				float [][]edge_j  = base_i[j];
				h.put(mTools.vForm(edge_j[1]), 0);
				h.put(mTools.vForm(edge_j[0]), 0);
			}
			
			for(int j = 0; j< base_i.length; j++) {
				float [][]edge_j  = base_i[j];
				int y = h.get(mTools.vForm(edge_j[1]));
				int yy = y+1;
				h.put(mTools.vForm(edge_j[1]), yy);
				
				int y1 = h.get(mTools.vForm(edge_j[0]));
				int yy1 = y1+1;
				h.put(mTools.vForm(edge_j[0]), yy1);
				
			}
			
			Enumeration<Integer> bn = h.elements();
			while(bn.hasMoreElements()) {
				int y = bn.nextElement();
				if(y!=2) {
					System.out.println(name+base_i.length + " "+y);
					System.exit(1);
					return false;
					
				}
			}
		
		
		return true;
	}
	
	public Boolean doTest8(float[][][][] e_i_m_edges) {
		
		/**Iterate through each base of the constructed line**/
		for(int i = 0; i<e_i_m_edges.length; i++) {
			float[][][] base_i = e_i_m_edges[i];

			doTest9(base_i, "Test 2  ");

		}
		
		return true;
	}
	
	public Boolean doTest7(Hashtable<String, float[][]> clusterVertexNeighbours) {
		Enumeration <float[][]>ii =  clusterVertexNeighbours.elements();
		while(ii.hasMoreElements()) {
			float[][] nem = ii.nextElement();
			
			if(nem == null) {
				return false;
			}

		}
		return true;
	}
	
	public Boolean doTest6(float[][] line, Hashtable<String, int[]> faceVertexToIndex ) {
		for(int i = 0; i < line.length; i++) {
			if(!faceVertexToIndex.containsKey(new String(line[i][0] +" "+
					line[i][1] +" "+
					line[i][2]))) {
				return false;
			}
			if(faceVertexToIndex.get(new String(line[i][0] +" "+
					line[i][1] +" "+
					line[i][2])) == null) {
				return false;
			}
		}
		return true;
	}
	
	public Boolean doTest5(Hashtable<String, float[][]> boundaryVertexNeighbours, Hashtable<String, int[]> faceVertexToIndex ) {
		Enumeration<float[][]> rr= boundaryVertexNeighbours.elements();
		while(rr.hasMoreElements()) {
			float[][] nem = rr.nextElement();
			
			for(int i = 0; i < nem.length; i++) {
				if(!faceVertexToIndex.containsKey(new String(nem[i][0] +" "+
						nem[i][1] +" "+
						nem[i][2]))) {
					return false;
				}
				if(faceVertexToIndex.get(new String(nem[i][0] +" "+
						nem[i][1] +" "+
						nem[i][2])) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	public Boolean doTest3(float [][][] boundaryEdgesArray, Hashtable <String, int[]> faceVertexToIndex) {
		for(int i = 0; i < boundaryEdgesArray.length; i++) {
			for(int j = 0; j < boundaryEdgesArray[i].length; j++) {
				if(!faceVertexToIndex.containsKey(new String(boundaryEdgesArray[i][j][0] +" "+
						boundaryEdgesArray[i][j][1] +" "+
						boundaryEdgesArray[i][j][2]))) {
					return false;
				}
				if(faceVertexToIndex.get(new String(boundaryEdgesArray[i][j][0] +" "+
						boundaryEdgesArray[i][j][1] +" "+
						boundaryEdgesArray[i][j][2])) == null) {
					return false;
				}

			}
		}
		return true;
	}
	
	public Boolean doTest2(Hashtable <String, int[]> faceVertexToIndex, float[][][] clusterFacesArrayForm){
		
		for(int i = 0; i < clusterFacesArrayForm.length; i++) {
			for(int j= 0; j < clusterFacesArrayForm[i].length; j++) {
				if(!faceVertexToIndex.containsKey(new String(clusterFacesArrayForm[i][j][0] +" "+
						                     clusterFacesArrayForm[i][j][1] +" "+
						                     clusterFacesArrayForm[i][j][2]))) {
					return false;
				}
				if(faceVertexToIndex.get(new String(clusterFacesArrayForm[i][j][0] +" "+
	                     clusterFacesArrayForm[i][j][1] +" "+
	                     clusterFacesArrayForm[i][j][2])) == null) {
					return false;
				}
			}
		}
		return true;
	}
}