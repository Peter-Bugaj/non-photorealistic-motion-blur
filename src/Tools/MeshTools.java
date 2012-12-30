package Tools;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;


import IO.ProcessedData;
import WeightFunctions.ALineWeightFilter;
import WeightFunctions.ILineWeightFilter;
import WeightFunctions.LnLineFilter;
import WeightFunctions.PowerLineFilter;

/**
 * 
 */

/**
 * @author Piotr Bugaj
 * @date May 30, 2010
 */
public class MeshTools {

    private VectorTools vT;

    public MeshTools() {
        vT = new VectorTools();
    }

    /**------------------------------------------------------------------**/
    /**Get the faces touching each edge within the mesh.
     * Param 1: edges existing within the mesh
     * Param 2: faces makig up the mesh
     * Param 3: vertexes of each face**/
    /**------------------------------------------------------------------**/
    /**Return values: 
     * Key: a1xb2xc3ya2xb2xc2  - corresponding to edge (a1,b1,c1)-(a2,b2,c2)
     * Value: {f1, f2, ... fk} - corresponding to face numbers touching edge 
     *                           (a1,b1,c1)-(a2,b2,c2)**/
    /**------------------------------------------------------------------**/
    /**Note: the edges are assumed to be unique**/
    /**------------------------------------------------------------------**/
    public Hashtable<String, int[]> getTouchingFaces(
            Hashtable<String, float[][]> edges, 
            int[][]faces, float [][]vertexes) {
        
        /**Array storing the edges and the corresponding faces**/
        Hashtable<String, int[]> touchingFaces = new Hashtable<String, int[]>();

        /**Initiallize the hashtable, assigning each edge
         * an empty integer array as default**/
        Enumeration<float[][]>tempEdges = edges.elements();
        while(tempEdges.hasMoreElements()) {
            float[][] tempVertexes = tempEdges.nextElement();

            touchingFaces.put(edgeXYForm(new float[][]{tempVertexes[0], tempVertexes[1]}),
                        new int[]{});
        }

        /**Go through each face and store the nine coordinate values, three
         * values for the three vertexes making up each face**/
        for(int i = 0; i < faces.length; i++) {
            
            /**Store the face in a temporary location**/
            int [] tempFaces = faces[i];

            float [] v1 = vertexes[tempFaces[0]-1];
            float [] v2 = vertexes[tempFaces[1]-1];
            float [] v3 = vertexes[tempFaces[2]-1];
            
            /**String a1 = edge(v1, v2)**/
            String a1 = edgeXYForm(new float[][]{v1, v2});
            
            /**String a2 = edge(v2, v1)**/
            String a2 = edgeXYForm(new float[][]{v2, v1});

            /**String b1 = edge(v1, v3)**/
            String b1 = edgeXYForm(new float[][]{v1, v3});
            
            /**String b2 = edge(v3, v1)**/
            String b2 = edgeXYForm(new float[][]{v3, v1});

            /**String c1 = edge(v2, v3)**/
            String c1 = edgeXYForm(new float[][]{v2, v3});
            
            /**String c2 = edge(v3, v2)**/
            String c2 = edgeXYForm(new float[][]{v3, v2});           

            
            /**The set of edges is unique. Therefore the set either contains a1
             * or a2, b1 or b2, c1 or c2**/
            
            /**Case where edge a1 exists**/
            if(touchingFaces.containsKey(a1)) {
                int [] temp = touchingFaces.get(a1);

                /**Update the array containing the faces touching this edge**/
                touchingFaceHelper(touchingFaces, temp, a1, i);
            }

            /**Case where edge a2 exists instead of edge a1**/
            if(touchingFaces.containsKey(a2)) {
                int [] temp = touchingFaces.get(a2);
                touchingFaceHelper(touchingFaces, temp, a2, i);
            }

            if(touchingFaces.containsKey(b1)) {
                int [] temp = touchingFaces.get(b1);
                touchingFaceHelper(touchingFaces, temp, b1, i);
            }

            if(touchingFaces.containsKey(b2)) {
                int [] temp = touchingFaces.get(b2);
                touchingFaceHelper(touchingFaces, temp, b2, i);
            }

            if(touchingFaces.containsKey(c1)) {
                int [] temp = touchingFaces.get(c1);
                touchingFaceHelper(touchingFaces, temp, c1, i);
            }

            if(touchingFaces.containsKey(c2)) {
                int [] temp = touchingFaces.get(c2);
                touchingFaceHelper(touchingFaces, temp, c2, i);
            }
        }
        return touchingFaces;
    }

    /**Helper method for the function: getTouchingFaces**/
    private int touchingFaceHelper(
            Hashtable<String, int[]> touchingFaces,
            int [] touchingFacesArray,
            String edgeString,
            int faceNumber) {
        
        int [] newTemp = new int[touchingFacesArray.length + 1];
        for(int j = 0; j < newTemp.length-1; j++) {
            newTemp[j] = touchingFacesArray[j];
        }
        newTemp[newTemp.length-1] = faceNumber;
        touchingFaces.put(new String(edgeString), newTemp);
        
        return 0;
    }
    
    /**Get the faces that are facing in the direction of the motion**/
    public Hashtable<String, Boolean> getTrailingFaces(
            int[][] faces,
            float[][] normalValues,
            float [][]vertexes,
            float[][][] motionTrails,
            int numObjectFiles,
            int objectNumber
        ) {
        
        /**Vector indicating the trailing faces**/
        Hashtable<String, Boolean> trailingFaces = new Hashtable<String, Boolean>();
        
        for(int i = 0; i < faces.length; i++) {
            int [] face_i = faces[i];
            
            /**Get the indexes of the vertexes defining the triangle**/
            int i1 = face_i[0]-1;
            int i2 = face_i[1]-1;
            int i3 = face_i[2]-1;
            
            /**Get the normals at the vertexes**/
            float [] n1 = normalValues[i1];
            float [] n2 = normalValues[i2];
            float [] n3 = normalValues[i3];
            
            int prevFrame = objectNumber-1;
            if(objectNumber == 0) {
                prevFrame = (numObjectFiles-1);
            }

            /**Get the direction of the vertexes**/
            float [] trailDirection1 = vT.sub(motionTrails[i1][objectNumber],
                    motionTrails[i1][prevFrame]);
            
            float [] trailDirection2 = vT.sub(motionTrails[i2][objectNumber],
                    motionTrails[i2][prevFrame]);
            
            float [] trailDirection3 = vT.sub(motionTrails[i3][objectNumber],
                    motionTrails[i3][prevFrame]);
            
            
            
            float whichSide1 = vT.dot(n1, trailDirection1);
            float whichSide2 = vT.dot(n2, trailDirection2);
            float whichSide3 = vT.dot(n3, trailDirection3);
            
            if((whichSide1 >= 0) || (whichSide2 >= 0) || (whichSide3 >= 0)) {
                float [] v1 = vertexes[i1];
                float [] v2 = vertexes[i2];
                float [] v3 = vertexes[i3];

                trailingFaces.put(getCentroid(v1, v2, v3), true);
            }
        }
        
        return trailingFaces;
    }

    /**------------------------------------------------------------------**/
    /**Given base boundary edges, constructed the rest
     * of  the edges  to create  the composed line.**/
    /**
     * Params:
     * 
     * baseBoundaryEdges: the edges at making a part of the  mesh
     *                    surfaces belonging to the current frame.
     *                    Base of the line to be constructed.
     * 
     * motionTrails: The data set storing the location of the vertexes
     *               within  the   mesh  belonging  to  other  frames.
     * 
     * vertexToIndex: A hashtable providing a correspondance
     *                between  each vertex  and  its' index.
     *                
     * lineLength: The length of  the line to  be
     *             created, from 0 to lineLength.
     * 
     * frameNum: The  current frame  the
     *           base of edges exist on.
     *           
     * numObjectFiles: The total number of frames
     *                 making  up  the animation.
     *             
     * controlFunction: function defining how the edges
     *                  of   the  line  will  converge.
     * **/
    /**------------------------------------------------------------------**/
    /**Return value:
     * 
     * array: [base i][edge_i_m][vertex_edge_i_m_a][vertex dimensions]
     * **/
    /**------------------------------------------------------------------**/
    public float [][][][] getEdgesFromBase(
            Hashtable<String, float [][]> baseBoundaryEdges,
            float[][][] motionTrails,
            Hashtable<String, Integer> vertexToIndex,
            int lineLength,
            int frameNum,
            int numObjectFiles,
            String controlFunction) {

        /**Store the base edges in array form**/
        float[][][] baseBoundaryEdgesArray =
            new float[baseBoundaryEdges.size()][][];
        Enumeration<float[][]>bE = baseBoundaryEdges.elements();
        int bCount = 0;
        while(bE.hasMoreElements()) {
            float[][]bENext = bE.nextElement();
            
            baseBoundaryEdgesArray[bCount++] = bENext;
        }
        
        /**Store the edges for the previous frames**/
        /**Note: The number of levels is from 0 to lineLength.
         * Hence array size is lineLength+1.**/
        float[][][][] e_i_m_edges =
            new float[lineLength+1][baseBoundaryEdgesArray.length][][];

        e_i_m_edges[0] = baseBoundaryEdgesArray;

        /**Go through each edge at  the base and  store the
         * corresponding edges at the rest of the levels**/
        for(int edge_m_counter = 0; edge_m_counter < baseBoundaryEdgesArray.length; edge_m_counter++) {
            float [][] edge_0_m = baseBoundaryEdgesArray[edge_m_counter];
            
            float [] v1_0_m = edge_0_m[0];
            float [] v2_0_m = edge_0_m[1];
            
            int i1 = vertexToIndex.get(vForm(v1_0_m));
            int i2 = vertexToIndex.get(vForm(v2_0_m));
            
            /**Start the counter at one as the edges for base 0 are
             * already stored. Also, because of this, trailCount is
             * set to  lineLength as oppose to lineLength + 1 and i
             * starts at frameNum-i.**/
            int trailCount = lineLength;
            int iCounter = 1;
            for(int i = frameNum-1; i >= 0; i--) {
                if(trailCount <= 0) {
                    break;
                }
                float [] v1_i_m = motionTrails[i1][i]; 
                float [] v2_i_m = motionTrails[i2][i]; 
                
                float[][] edge_i_m = new float[][]{v1_i_m, v2_i_m};
                
                /**Store edge m at level iCounter**/
                e_i_m_edges[iCounter][edge_m_counter] = edge_i_m;
                
                iCounter++;
                trailCount--;
            }

            for(int i = numObjectFiles-1; i > frameNum; i--) {
                if(trailCount <= 0) {
                    break;
                }
                
                float [] v1_i_m = motionTrails[i1][i]; 
                float [] v2_i_m = motionTrails[i2][i]; 
                
                float[][] edge_i_m = new float[][]{v1_i_m, v2_i_m};
                
                /**Store edge m at level iCounter**/
                e_i_m_edges[iCounter][edge_m_counter] = edge_i_m;
                
                iCounter++;
                trailCount--;
            }
        }
        
        float [][] centroids = new float[lineLength+1][3];
        /**Find centroids ci for each base i, for i = 1 to n**/
        for(int i = 0; i < centroids.length; i++) {
            
            /**Get the edges at base i**/
            float[][][] base_i_edges = e_i_m_edges[i];
            
            /**Number of edges equals the number of vertexes,
             * given that the edges are acyclic**/
            int numVertexes = base_i_edges.length;
            
            /**Initialize the 3D centroid values**/
            float c1 = 0.0f;
            float c2 = 0.0f;
            float c3 = 0.0f;
    
            /**Iterate  through  edges_i.length  number  of  edges,
             * storing edges_i.length number i fcentroid values.**/
            for(int m = 0; m < numVertexes; m++) {

                c1 += base_i_edges[m][0][0];
                c2 += base_i_edges[m][0][1];
                c3 += base_i_edges[m][0][2];
            }
            
            c1 = c1/numVertexes;
            c2 = c2/numVertexes;
            c3 = c3/numVertexes;

            centroids[i][0] = c1;
            centroids[i][1] = c2;
            centroids[i][2] = c3;
        }
        
        /**Calculate   the    average    distances
         * between each vertex and its centroid**/
        float [] distances = new float[lineLength+1];
        float minDist = 1000;
        for(int i = 0; i < e_i_m_edges.length; i++) {
        	float [][][] edges_i  = e_i_m_edges[i];
        	
        	float tempSum = 0.0f;
        	for(int m = 0; m < edges_i.length; m++) {
        		float[][] edge_i_m = edges_i[m];
        		
        		float [] v1 = edge_i_m[0];
        		float [] v2 = edge_i_m[1];
        		
        		float dist1 = vT.distanceQuick(v1, centroids[i]);
        		float dist2 = vT.distanceQuick(v2, centroids[i]);
        		
        		tempSum += dist1;
        		tempSum += dist2;
        		
        		if(minDist > dist1) {
        			minDist = dist1;
        		}
        		if(minDist > dist2) {
        			minDist = dist2;
        		}
        	}
        	
        	distances[i] = tempSum/(edges_i.length*2);
        }

        /**Get the function values f(i) for domain [0, n], range [1, 0]**/
        ILineWeightFilter lF;
        if(controlFunction.equals(ALineWeightFilter.LN)) {
            lF = new LnLineFilter();
        } else  {
            lF = new PowerLineFilter();
        }

        lF.calculateLineFunction(lineLength+1);
        
        float [] fValues = lF.getLineFunction();
        
        /**Set   each   vertex v accordingly   so that   the points
         * eventually converge to a point at the tip of the line**/
        for(int i = 0; i < e_i_m_edges.length; i++) {
            float [][][] edges_i = e_i_m_edges[i];
            
            for(int m = 0; m < edges_i.length; m++) {
                
                float[][] edge_i_m = edges_i[m];
                
                float []v1 = edge_i_m[0];
                float []v2 = edge_i_m[1];

                float  [] fVector1;
                float  [] fVector2;
                
                fVector1 = vT.sub(v1, centroids[i]);
                fVector2 = vT.sub(v2, centroids[i]);
                
                float tempAvgDis = distances[i];
                float dist1 = vT.distanceQuick(v1, centroids[i]);
                float dist2 = vT.distanceQuick(v2, centroids[i]);
                if(dist1 > tempAvgDis) {
                	float s = tempAvgDis/dist1;
                	fVector1 = vT.mult(s, fVector1);
                }                
                
                if(dist2 > tempAvgDis) {
                	float s = tempAvgDis/dist2;
                	fVector2 = vT.mult(s, fVector2);
                }

                /**TODO: thin lines should not be long but short, whereas wide lines can remain wide. I.e. lines should be more square.**/
                edge_i_m[0] = vT.add(vT.mult(fValues[i], fVector1), centroids[i]);
                edge_i_m[1] = vT.add(vT.mult(fValues[i], fVector2), centroids[i]);
            }			
        }
        return e_i_m_edges;
    }
  
    /**Given trailing faces and composed line bases, return the composed line
     * bases  that  contain at least one  triangle that is a trailing face**/
    public Vector<int[][]> getTrailingBases(
            Vector<int[][]> composedLineBases,
            Hashtable<String,Boolean> trailingFaces,
            float[][]vertexes,
            int[][] faces) {
        
        Vector<int[][]> trailingBases = new Vector<int[][]>();
        
        Enumeration<int[][]> enumm = composedLineBases.elements();
        while(enumm.hasMoreElements()) {
            int[][] base = enumm.nextElement();
            Vector<int[]> newBase = new Vector<int[]>();
            
            /**Itertate     through    the     triangles
             * belonging  in  the  base  and  find   the
             * ones that face the direction of motion**/
            for(int i = 0; i < base.length; i++) {
                int [] face_i = base[i];
                
                float [] v1 = vertexes[face_i[0]-1];
                float [] v2 = vertexes[face_i[1]-1];
                float [] v3 = vertexes[face_i[2]-1];
                
                if(trailingFaces.containsKey(getCentroid(v1, v2, v3))) {
                    newBase.add(face_i);
                }
            }
            if(newBase.size() > 0) {
                int [][] newBaseArrayForm = new int[newBase.size()][];
                newBase.toArray(newBaseArrayForm);
                trailingBases.add(newBaseArrayForm);
            }
        }
        
        
        
        return trailingBases;
    }
    
    /**Given  a set  of triangles  and the corresponding vertexes,  return
     * a set of triangular  bases of a given size.   A triangular base  is
     * made up of triangles, with the resulting shape being a triangles**/
    public float[][] getTriangularBases(int size) {
        
        
        
        return null;
    }

    /**Go   through the  list of  provided edges  and collect  the
     * ones that only have one face touching it (boundary face)**/
    public Hashtable<String, float [][]> getBoundaryEdges(
            Hashtable<String, float[][]> baseEdges,
            Hashtable<String, int[]> touchingBaseFaces) {	
        
        Hashtable<String, float [][]> boundaryEdges =
            new Hashtable<String, float [][]>();
            
        Enumeration<float[][]> baseEdgesEnum = baseEdges.elements();
        while(baseEdgesEnum.hasMoreElements()) {
            float [][] temporaryEdge = baseEdgesEnum.nextElement();
            
            /**Get the touching faces for this edge**/
            String n1 = edgeXYForm(
                    new float[][]{temporaryEdge[0],temporaryEdge[1]});
            String n2 = edgeXYForm(
                    new float[][]{temporaryEdge[1],temporaryEdge[0]});
            
            int []temporaryTouchingFaces = 
                touchingBaseFaces.get(n1);
            int []temporaryTouchingFaces2 = 
                touchingBaseFaces.get(n2);
            
            if(temporaryTouchingFaces != null &&
                    temporaryTouchingFaces.length == 1) {
                boundaryEdges.put(n1, temporaryEdge);
            }
            
            /**Get the touching faces for this edge**/
            else if(temporaryTouchingFaces2 != null &&
                    temporaryTouchingFaces2.length == 1) {
                boundaryEdges.put(n2, temporaryEdge);
            }
        }
        
        return boundaryEdges;
    }

    /**------------------------------------------------------------------**/
    /**Description: Given a set of faces in the form: float[][][],
     * return the vertexes making up those triangles**/
    /**------------------------------------------------------------------**/
    public float[][] getTriangleVertexes(float[][][] triangles) {
    	
    	
    	
    	return null;
    }
    
    /**------------------------------------------------------------------**/
    /**Get all the neighbouring faces for each face.
     * Param 1: vertexes makig up each face
     * Param 2: faces making uup the mesh
     * Param 3: set of edges along with the faces that touch them**/
    /**------------------------------------------------------------------**/
    /**Return values: 
     * [face number][f1, f2, f3] **/
    /**------------------------------------------------------------------**/
    /**Note: each face is only expected to have three neighbours**/
    /**------------------------------------------------------------------**/
    public int [][] getFaceNeighbours(float [][] vertexes,  int [][] faces, 
            Hashtable<String, int[]> touchingFaces) {

        /**Variable containing the face neighbours for each face. Each face is
         * assumed to have at most tree faces**/
        int [][] faceNeighbours = new int[faces.length][3];
        for(int i = 0; i < faceNeighbours.length; i++) {
            
            /**Initialize the values to negative -1.
             * -1 indicating that no neighbour exists**/
            /**Note: three neighbours are expected per triangle.
             * I.e., one  triangle  is attached  to each side**/
            faceNeighbours[i] = new int[]{-1, -1, -1};
        }
        
        /**Go through each face and store the nine coordinate values, three
         * values for the three vertexes making up each face**/
        for(int i = 0; i < faces.length; i++) {
            
            /**Store the face in a temporary location**/
            int [] tempFaces = faces[i];

            float [] v1 = vertexes[tempFaces[0]-1];
            float [] v2 = vertexes[tempFaces[1]-1];
            float [] v3 = vertexes[tempFaces[2]-1];
            
            /**String a1 = edge(v1, v2)**/
            String a1 = edgeXYForm(new float[][]{v1, v2});
            
            /**String a2 = edge(v2, v1)**/
            String a2 = edgeXYForm(new float[][]{v2, v1});

            /**String b1 = edge(v1, v3)**/
            String b1 = edgeXYForm(new float[][]{v1, v3});
            
            /**String b2 = edge(v3, v1)**/
            String b2 = edgeXYForm(new float[][]{v3, v1});

            /**String c1 = edge(v2, v3)**/
            String c1 = edgeXYForm(new float[][]{v2, v3});
            
            /**String c2 = edge(v3, v2)**/
            String c2 = edgeXYForm(new float[][]{v3, v2});        

            /**Once the edges for face i is stored, for each edge finding the
             * faces that are touching it**/
            /**If the face p != i, i.e., the face touching the edge is some other
             * face then the current face i being iterated, add that face number
             * to the array of face neighbours corresponding to face i**/
            if(touchingFaces.containsKey(a1)) {
                
                /**Get the touching faces for edge a1**/
                int [] tempTouchingFaces = touchingFaces.get(a1);		
                
                /**Find the  touching face that  is a neighbour of
                 * the triangle and add it to its neighbourhood**/
                getFaceNeighboursHelper(faceNeighbours, tempTouchingFaces, i);
            }

            if(touchingFaces.containsKey(a2)) {
                int [] tempTouchingFaces = touchingFaces.get(a2);				
                getFaceNeighboursHelper(faceNeighbours, tempTouchingFaces, i);
            }

            if(touchingFaces.containsKey(b1)) {
                int [] tempTouchingFaces = touchingFaces.get(b1);				
                getFaceNeighboursHelper(faceNeighbours, tempTouchingFaces, i);
            }

            if(touchingFaces.containsKey(b2)) {
                int [] tempTouchingFaces = touchingFaces.get(b2);				
                getFaceNeighboursHelper(faceNeighbours, tempTouchingFaces, i);
            }

            if(touchingFaces.containsKey(c1)) {
                int [] tempTouchingFaces = touchingFaces.get(c1);				
                getFaceNeighboursHelper(faceNeighbours, tempTouchingFaces, i);
            }
            
            if(touchingFaces.containsKey(c2)) {
                int [] tempTouchingFaces = touchingFaces.get(c2);				
                getFaceNeighboursHelper(faceNeighbours, tempTouchingFaces, i);
            }

        }

        return faceNeighbours;
    }

    /**Helper function for the method: getFaceNeighbours**/
    private int getFaceNeighboursHelper(
            int [][] faceNeighbours,
            int [] tempTouchingFaces, int i) {
        
        /**Iterate through these faces**/
        for(int p = 0; p < tempTouchingFaces.length; p++) {
            
            /**Case where a face is found touching the edge of face i that is not
             * face i itself**/
            if(tempTouchingFaces[p]!= i) {
                /**Iterate through the current neighbouring faces of face i
                 * until an empty slot is found. Note that only three
                 * space are expected to be filled**/
                for(int z = 0; z < faceNeighbours[i].length; z++) {
                    if(faceNeighbours[i][z] == -1) {
                        faceNeighbours[i][z] = tempTouchingFaces[p];
                        return 0;
                    }
                }
            }
        }
        
        return 0;
    }

    /**------------------------------------------------------------------**/
    /**Get all the neighbouring vertexes for each vertex.
     * Param 1: The edges existing within the mesh**/
    /**------------------------------------------------------------------**/
    /** Return values:
     * KEY: "a b c", VALUE: {vertexes connected to (a, b, c) **/
    /**------------------------------------------------------------------**/
    public Hashtable<String, float[][]> getVertexNeighbours(Hashtable<String,
            float[][]> edges) {

        /**Create the hash table for storing the neighbours**/
        /**KEY: "a b c", VALUE: {vertexes connected to (a, b, c) **/
        Hashtable <String, float[][]>vertexNeighbours =
            new Hashtable <String, float[][]>();
        
        /**Enumerate through the edges. For each edge (v1)-(v2), check
         * if v1 or v2 already exists  within the  hash table.  If one
         * already exists,  check if  that vertex has the other vertex
         * already  as  a  neighbour, i.e., it exists within the array
         * corresponding to that vertex.
         * 
         * Then add the  vertex to  that array depending on whether or
         * not the vertex yet exists within the array**/
        Enumeration<float[][]> enumEdge = edges.elements();
        while(enumEdge.hasMoreElements()) {
            
            float [][]tempEdge = enumEdge.nextElement();
            
            /**Case where tempEdge[0] already exist in the hash table**/
            if(vertexNeighbours.containsKey(vForm(tempEdge[0]))) {

                /**Store the current neighbours for the vertex**/
                float[][] tempNeighbours = vertexNeighbours.get(vForm(tempEdge[0]));
                
                /**Array storing the new set of neighbours with one additional
                 * vertex, tempEdge[1]**/
                int tempLength =  tempNeighbours.length;
                float [][] newNeighbours = new float[tempLength + 1][3];
                
                /**Indicating whether the neighbouring vertex tempEdge[1]
                 * exists for the vertex tempEdge[0]**/
                boolean neighbourExists = false;
                this_loop: for(int i = 0; i < tempLength; i++) {
                    if(tempNeighbours[i] == tempEdge[1]) {
                        neighbourExists = true;
                        break this_loop;
                    }
                    newNeighbours[i] = tempNeighbours[i];
                }
                
                /**At the second vertex if it is not yet a neighbour of the
                 * first vertex**/
                if(!neighbourExists) {
                    newNeighbours[tempLength] = tempEdge[1];
                    vertexNeighbours.put(vForm(tempEdge[0]), newNeighbours);
                }

            /**Case where the first vertex does not yet exist within the hash table**/
            } else if(!vertexNeighbours.containsKey(vForm(tempEdge[0]))) {
                vertexNeighbours.put(vForm(tempEdge[0]), new float[][]{tempEdge[1]});
            }
            
            
            if(vertexNeighbours.containsKey(vForm(tempEdge[1]))) {
                /**Store the current neighbours for the vertex**/
                float[][] tempNeighbours =
                    vertexNeighbours.get(vForm(tempEdge[1]));
                
                /**Array storing the new set of neighbours with one additional
                 * vertex, tempEdge[1]**/
                int tempLength =  tempNeighbours.length;
                float [][] newNeighbours = new float[tempNeighbours.length + 1][3];
                
                /**Indicating whether the neighbouring vertex tempEdge[1]
                 * exists for the vertex tempEdge[0]**/
                boolean neighbourExists = false;
                this_loop: for(int i = 0; i < tempLength; i++) {
                    if(tempNeighbours[i] == tempEdge[0]) {
                        neighbourExists = true;
                        break this_loop;
                    }
                    newNeighbours[i] = tempNeighbours[i];
                }
                if(!neighbourExists) {
                    newNeighbours[tempLength] = tempEdge[0];
                    vertexNeighbours.put(vForm(tempEdge[1]), newNeighbours);
                }
            } else if(!vertexNeighbours.containsKey(vForm(tempEdge[1]))) {
                vertexNeighbours.put(vForm(tempEdge[1]), new float [][]{tempEdge[0]});
            }
            
        }
        
        return vertexNeighbours;
    }

    /**------------------------------------------------------------------**/
    /**Find the difference between two lines
     * corresponding  to the  given  weights
     * 
     * Param 1: First line.
     * Param 2: Second line.
     * Param 3: Weight relating to the difference between the two lines
     * Param 4: Name of the function distributing the weight
     * **/
    /**------------------------------------------------------------------**/
    /**Return value: Difference between the two lines**/
    /**------------------------------------------------------------------**/
    public float getLineDifference(
            float [][] line1, float [][]line2,
            float []params,
            String weightFunction) {

        float diff = 0.0f;
        ILineWeightFilter lf = null;
        float [] weightValues;
        
        /**Get the weighted values if required**/
        if(weightFunction.equals(ALineWeightFilter.LN)) {
            lf = new LnLineFilter();
        } else if(weightFunction.equals(ALineWeightFilter.POW)) {
            lf = new PowerLineFilter();
        }
        
        lf.setParams(params);
        lf.calculateLineFunction(line1.length);
        weightValues = lf.getLineFunction();

        /**Calculate the difference**/
        if(weightFunction.equals(ALineWeightFilter.NONE)) {
            for(int i = 0; i < line1.length; i++) {
                diff += Math.sqrt(
                        Math.pow(line1[i][0] + line2[i][0], 2) +
                        Math.pow(line1[i][1] + line2[i][1], 2) +
                        Math.pow(line1[i][2] + line2[i][2], 2));
            }
        } else {
            for(int i = 0; i < line1.length; i++) {
                diff += (Math.sqrt(
                        Math.pow(line1[i][0] + line2[i][0], 2) +
                        Math.pow(line1[i][1] + line2[i][1], 2) +
                        Math.pow(line1[i][2] + line2[i][2], 2)))
                        *
                        weightValues[i];
            }
        }
        
        /**Return the average**/
        diff = diff/(line1.length + 0.0f);
        return diff;
    }

    /**------------------------------------------------------------------**/
    /**Find all the edges within the mesh.
     * Param 1: the vertexes making up the faces
     * Param 2: the faces making up the mesh**/
    /**------------------------------------------------------------------**/
    /**Return value: Hashtable for finding all the edges.
     * KEY: "(a+d, b+e, c+f)", VALUE: (a,b,c), (d,e,f)**/
    /**------------------------------------------------------------------**/
    public Hashtable<String, float[][]> findAllEdges(float vertexes[][],
            int faces[][]) {

        /**Hashtable for finding all the edges.
         * KEY: "(a+d, b+e, c+f)", VALUE: (a,b,c), (d,e,f)**/
        Hashtable <String, float[][]>edges =
            new Hashtable <String, float[][]>();

        /**Fill up the hash-table**/
        for(int i = 0; i < faces.length; i++) {

            int [] tempFace = faces[i];
            float []v1 = vertexes[tempFace[0]-1];
            float []v2 = vertexes[tempFace[1]-1];
            float []v3 = vertexes[tempFace[2]-1];		

            /**Note that the vertex addition is stored, as oppose  to a string
             * of representing the coordinates of two vertexes.This is because
             * the resulting  string is  shorter to  compare.  Also it is much
             * more work checking  whether the  hash table  contains the  edge
             * (a, b, c) (d e f) or the flipped edge (d e f) (a b c)**/
            if(!edges.containsKey(edgeXYForm(new float[][]{v1, v2}))  &&
                !edges.containsKey(edgeXYForm(new float[][]{v2, v1}))) {
                
                edges.put(edgeXYForm(new float[][]{v1, v2}), 
                            new float[][]{v1.clone(), v2.clone()});
            } 

            if(!edges.containsKey(edgeXYForm(new float[][]{v2, v3})) &&
                    !edges.containsKey(edgeXYForm(new float[][]{v3, v2}))) {
                
                edges.put(edgeXYForm(new float[][]{v2, v3}), 
                        new float[][]{v2.clone(), v3.clone()});
            }			

            if(!edges.containsKey(edgeXYForm(new float[][]{v1, v3})) &&
                    !edges.containsKey(edgeXYForm(new float[][]{v3, v1}))) {
                
                edges.put(edgeXYForm(new float[][]{v1, v3}), 
                        new float[][]{v1.clone(), v3.clone()});
            }
        }

        return edges;
    }

    /**------------------------------------------------------------------**/
    /**Create the motion trails for each face
     * Param 1: the data containing all vertexes and faces
     * Param 2: the number of object files**/
    /**------------------------------------------------------------------**/
    /**Return value: Motion trails corresponding to each face.
    /**------------------------------------------------------------------**/
    public float [][][] getSurfaceTrails(ProcessedData [] processedData,
            int numObjectFiles) {

        int [][] faces = processedData[0].getIntData(2);
        float [][][] faceTrails = new float[faces.length][numObjectFiles][3];
        
        for(int i = 0; i < numObjectFiles; i++) {
            int [][] tempFaces = processedData[i].getIntData(2);
            float [][] tempVertexes = processedData[i].getFloatData(0);
            
            for(int j = 0; j < tempFaces.length; j++) {
                
                int [] tempFace = tempFaces[j];
                float v11 = tempVertexes[tempFace[0]-1][0];
                float v12 = tempVertexes[tempFace[0]-1][1];
                float v13 = tempVertexes[tempFace[0]-1][2];

                float v21 = tempVertexes[tempFace[1]-1][0];
                float v22 = tempVertexes[tempFace[1]-1][1];
                float v23 = tempVertexes[tempFace[1]-1][2];

                float v31 = tempVertexes[tempFace[2]-1][0];
                float v32 = tempVertexes[tempFace[2]-1][1];
                float v33 = tempVertexes[tempFace[2]-1][2];		
                
                float [] center = new float[]{
                        (v11+v21+v31)/3, (v12+v22+v32)/3, (v13+v23+v33)/3};
                
                faceTrails[j][i] = center;
            }
        }
        
        return faceTrails;
    }

    /**------------------------------------------------------------------**/
    /**Create the motion trails for each vertex
     * Param 1: the data containing all vertexes and faces
     * Param 2: the number of object files**/
    /**------------------------------------------------------------------**/
    /**Return value: Motion trails corresponding to each vertex.
    /**------------------------------------------------------------------**/
    public float [][][] getVertexTrails(String direction, ProcessedData [] processedData,
    		int numObjectFiles) {

    	float [][] vertexes = processedData[0].getFloatData(0);
    	int totalPoints = vertexes.length;

    	/**Array storing the trails for each point**/
    	float [][][] trails = new float[totalPoints][numObjectFiles][3];
    	/**Fill up the array**/
    	if(direction.equals("FRONT_BACK")) {
    		for(int j = 0; j < numObjectFiles; j++) {
    			float [][] vertexes_i = processedData[j].getFloatData(0);

    			for(int i = 0; i < totalPoints; i++) {
    				float []tempPoints = vertexes_i[i];
    				trails[i][j] =
    					new float[] {tempPoints[0], tempPoints[1], tempPoints[2]};
    			}
    		}
    	}

    	for(int j = 0; j < numObjectFiles; j++) {
    		float [][] vertexes_i;
    		if(direction.equals("BACK_FRONT")) {
    			vertexes_i = processedData[numObjectFiles-1-j].getFloatData(0);
    		} else {
    			vertexes_i = processedData[j].getFloatData(0);
    		}

    		for(int i = 0; i < totalPoints; i++) {
    			float []tempPoints = vertexes_i[i];
    			trails[i][j] =
    				new float[] {tempPoints[0], tempPoints[1], tempPoints[2]};
    		}
    	}

    	return trails;
    }

    /**------------------------------------------------------------------**/
    /**Given a set of edges, return the triangles that the edges form
     * Param 1: the set of edges
     * Param 2: the set of vertexes**/
    /**------------------------------------------------------------------**/
    /**Return value: the set of faces that are created from those edges
     * 
     * Description: The algorithm will  work as  follows:  It will do a
     * breath  first search  on the  edges and store all the neighbours
     * corresponding to each vertex during the search.   Then given the
     * neighbours for  each vertex,  triangles will be constructed from
     * all  the possible  pair of  vertexes  from each neighbourhood of
     * vertexes.  This algorithm is very similar to just simply storing
     * all the possible neighbouring vertexes for each vertex, and then
     * trying out all the  possible  pairs of  vertexes for each vertex
     * that form a triangle.The only difference is that by using breath
     * first search,  the total  sum of the  neighbours stored for each
     * vertex is equal to the total number of edges, as oppose to  just
     * simply storing all the neighbouring vertexes for each vertex.
     * 
     * Runtime: Linear with respect to the number of triangles.
     */
    /**------------------------------------------------------------------**/
    public Hashtable<String, float[][]> getComposedFaces(
            Hashtable<String, float[][]> edges,
            Hashtable<String, float[][]> neighbouringVertexes,
            float [][]vertexes,
            Hashtable<String, float[]> triangleVertexes) {
        
        /**Table for storing the created faces by centroid**/
        Hashtable<String, float[][]> composedFaces  =
            new Hashtable<String, float[][]>();
        
        if((neighbouringVertexes.size() != vertexes.length) ||
                (neighbouringVertexes.size() == 0) ||
                (edges.size() == 0)) {
            System.out.println("Invalid number of vertexes");
            System.exit(1);
            return composedFaces;
        }
        
        /**A queue for traversing the edges in Bread-First-Search**/
        LinkedList <float[]>queue = new LinkedList<float[]>();

        /**Hashtable indicating which edge have been marked**/
        /**String: (a+d, b+e, c+f), where (a,b,c)-(d,e,f) is an edge**/
        Hashtable <String, Boolean> markedEdges =
            new Hashtable <String, Boolean>();
        
        /**Hashtable indicating the vertexes attached to a given vertex**/
        Hashtable <String, Vector<float[]>> neighbouringSearchedVertexes =
            new Hashtable <String, Vector<float[]>>();
        
        /**Push the first vertex onto the stack of visited vertexes**/
        queue.push(vertexes[0]);
        
        /**Search through the edges until there are no more new ones to be
         * found**/
        while(!queue.isEmpty()) {
            
            /**Pop the next visited vertex on the stack**/
            float [] nextVertex = queue.pop();
            
            /**Get the neighbours for that vertex**/
            float [][] nextVertexNeighbours =
                neighbouringVertexes.get(vForm(nextVertex));
            
            /**Checked if this vertex has any  neighbouring searched vertexes.
             * If not,  add them  manually as  edges will  come out  from this
             * vertex  and no  neighbouring  vertexes  for this  edge  will be
             * stored otherwise.**/	
            if(!neighbouringSearchedVertexes.containsKey(vForm(nextVertex))){
                
                /**
                Vector<float[]> temp = new Vector<float[]>();
                for(int i = 0; i < nextVertexNeighbours.length; i++) {
                    temp.add(nextVertexNeighbours[i].clone());
                }
                neighbouringSearchedVertexes.put(vForm(nextVertex), temp);
                **/
            }
            
            
            /**Iterate through the neighbouring vertexes**/
            for(int i = 0; i < nextVertexNeighbours.length; i++) {
                
                /**Check if a given neighbouring vertex is reached by an
                 * edge that is not yet marked.  (I.e., if breadth first
                 * search can expand on this vertex)**/
                
                if(!markedEdges.containsKey(edgeXYForm(
                        new float[][]{nextVertex, nextVertexNeighbours[i]}))
                        &&
                    !markedEdges.containsKey(edgeXYForm(
                        new float[][]{nextVertexNeighbours[i], nextVertex}))
                ){
                    
                    /**Mark and push the vertex onto the queue**/
                    markedEdges.put(edgeXYForm(
                            new float[][]{nextVertex, nextVertexNeighbours[i]}),
                            true);
                    queue.push(nextVertexNeighbours[i]);
                    
                    /**Store the edge newVertex-nextVertexNeighbour as:
                     * nextVertexNeighbour - {newVertex}, if
                     * nextVertexNeighbourdoesn't yet exist in the hashtable.
                     * Otherwise add newVertex to the rest of the stored vertex
                     * neighbours for nextVertexNeighbour.**/
                    if(neighbouringSearchedVertexes.containsKey(vForm(nextVertexNeighbours[i]))) {
                        
                        Vector <float[]> searchedVertexes =
                            neighbouringSearchedVertexes.get(vForm(nextVertexNeighbours[i]));
                        
                        searchedVertexes.add(nextVertex.clone());
                        neighbouringSearchedVertexes.put(vForm(nextVertexNeighbours[i]), searchedVertexes);
                    } else {
                        Vector<float[]> temp = new Vector<float[]>();
                        temp.add(nextVertex.clone());
                        neighbouringSearchedVertexes.put(vForm(nextVertexNeighbours[i]), temp);
                    }
                }
                /**End if**/
            }
            /**End for-loop**/
            
        }
        
        /**Iterate through the stored vertex neighbours for each vertex and
         * create the triangles**/
        for(int i = 0; i < vertexes.length; i++) {
            Vector<float[]> tempNeighbours = neighbouringSearchedVertexes.get(vForm(vertexes[i]));

            /**To see where the neighbours at a verex form a triangle, at least
             * three vertexes must exist (at least two neighbours)**/
            if(tempNeighbours != null && tempNeighbours.size() >= 2) {
                
                float[][] arrayStor = new float[tempNeighbours.size()][3];
                tempNeighbours.toArray(arrayStor);
                
                /**Iterate through all the possible combination of neighbours**/
                for(int j = 0; j < arrayStor.length; j++) {
                    for(int k = (j+1); k < arrayStor.length; k++) {
                        
                        /**Check if the  two  vertexes
                         * are connected by an edge**/
                        float [] v1 = arrayStor[j];
                        float [] v2 = arrayStor[k];
                        
                        if(edges.containsKey(edgeXYForm(new float[][]{v1, v2}))
                            ||
                            edges.containsKey(edgeXYForm(new float[][]{v2, v1})))
                        {
                            
                            composedFaces.put(this.getCentroid(vertexes[i], v1, v2),
                                    
                                    new float [][]{vertexes[i].clone(), v1, v2});
                            
                            /**Make sure  all possible  vertexes are  added as
                             * the  searched  neighbours  might  contain  less
                             * neighbours then in  the  actual  neighbourhood.
                             * Hence  not all  vertexes  will be  collected if
                             * they are just iterated through.**/
                            addVertexToTable(triangleVertexes, vertexes[i]);
                            addVertexToTable(triangleVertexes, v1);
                            addVertexToTable(triangleVertexes, v2);
                        }
                    }
                }
            }
        }
        
        return composedFaces;
    }

    /**------------------------------------------------------------------**/
    /**Given a set of vertexes, return a correspondance to their index
     * Param 1: the set of vertexes
    /**------------------------------------------------------------------**/
    /**Return value: Hashtable:
     * KEY: vertex (a b c). VALUE: vertex index (i) 
    **/
    /**------------------------------------------------------------------**/
    public Hashtable <String, Integer> createVertexToIndexCorrespondance(
            float [][] vertexes) {
        Hashtable <String, Integer> vertexToIndex = new Hashtable <String, Integer>();
        
        for(int i = 0; i < vertexes.length; i++) {
            vertexToIndex.put(vForm(vertexes[i]), i);
        }
        return vertexToIndex;
    }

    /**------------------------------------------------------------------**/	
    /**Given a  triangle in the  form of  vertexes, v1, v2 and v3,  return the
     * centroid in String form**/
    /**------------------------------------------------------------------**/	
    /** Note:A triangle will have multiple values for it's centroid due to how
     * the vertexes are added and of how the values  are rounded of.  Hence if
     * the triangles are accessed by just  the centroid itself, triangles will
     * fail to be found. **/
    /**------------------------------------------------------------------**/	
    public String getCentroid(float[]v1, float[]v2, float[]v3) {
        float[] centroid = new float[]{
            (  Math.round(v1[0]*10000) +  Math.round(v2[0]*10000) +  Math.round(v3[0]*10000))/3,
            (  Math.round(v1[1]*10000) +  Math.round(v2[1]*10000) +  Math.round(v3[1]*10000))/3,
            (  Math.round(v1[2]*10000) +  Math.round(v2[2]*10000) +  Math.round(v3[2]*10000))/3};
        
        return new String(centroid[0] +" "+ centroid[1] +" "+ centroid[2]);
    }
 
    public void addVertexToTable(Hashtable<String, float[]> table, float[]v) {
        table.put(vForm(v), 
                new float[]{v[0],v[1],v[2]});
    }
    
    /**Given a set of facess, return a vertex to index correspondance,
     * where the index is two dimensional for  searching with a set of
     * faces, as oppose to a one dimensional array of vertexes 
     */
    /** Param 1: the set of faces**/
    /**------------------------------------------------------------------**/
    /**Return value: Hashtable:
     * KEY: vertex (a b c). VALUE: face index (i)(j) 
    **/
    public Hashtable <String, int[]> createVertexToIndexCorrespondance(
            float [][][] faces) {
        Hashtable<String,int[]> vertexToIndex =
            new Hashtable <String,int[]>();
        
        for(int i = 0; i < faces.length; i++) {
            for(int j = 0; j < faces[i].length; j++) {
                vertexToIndex.put(vForm(faces[i][j]), new int[]{i, j});
            }
        }
        return vertexToIndex;
    }

    /**------------------------------------------------------------------**/	
    /**Given a set of faces, return a correspondance to their index
     * Param 1: the set of faces
    /**------------------------------------------------------------------**/
    /**Return value: Hashtable:
     * KEY: centroid of triangle (a b c). VALUE: Face index (i)
     * 
     * Note: A triangle will have multiple values for it's centroid due to how
     * the vertexes are added and of how the values  are rounded of.  Hence if
     * the triangles are accessed by just  the centroid itself, triangles will
     * fail to be found.
    **/

    /**------------------------------------------------------------------**/
    public Hashtable<String, Integer> createFacesToIndexCorrespondance(
            float [][] vertexes,
            int [][] faces) {
        Hashtable<String, Integer> faceToIndex = new Hashtable<String, Integer>();
        
        for(int i = 0; i < faces.length; i++) {
            int [] face = faces[i];

            float [] v1 = vertexes[face[0]-1];
            float [] v2 = vertexes[face[1]-1];
            float [] v3 = vertexes[face[2]-1];

            faceToIndex.put(getCentroid(v1, v2, v3), i);
        }		
        return faceToIndex;
    }

    /**Given a vertex, v = (a, b, c), return it in string form: a b c**/
    public String vForm(float []v) {
        return new String(
                v[0] +" "+ v[1] +" "+ v[2]);
    }

    /**Given  an  edge,  e =  (a, b, c)-(d, e, f),  return  it  in the  string
     * form: a|||b|||c|||d|||e|||f**/
    public String edgeXYForm(float [][]e) {
        return new String(
                (e[0][0])+"|||"+(e[0][1])+"|||"+(e[0][2])+"|||"+
                (e[1][0])+"|||"+(e[1][1])+"|||"+(e[1][2]));
    }
}