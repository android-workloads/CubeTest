package com.example.jasmitsx.cardboardtest;

/**
 * Created by jasmitsx on 6/29/2016.
 */

public class CubeObject extends WorldObject{
    //protected float[] modelPosition;
    //rotected float[] cubeModel;

    private static final String TAG = "CubeObject";
    private final int cubeID;

    public CubeObject(float[] position, int cubeID){
        super(position);
        this.cubeID=cubeID;
    }

    /**
     * Updates the cube model position.
     */
   /* protected void updateModelPosition() {
        Matrix.setIdentityM(modelCube, 0);
        Matrix.translateM(modelCube, 0, modelPosition[0], modelPosition[1], modelPosition[2]);

        TreasureHuntActivity.checkGLError("updateCubePosition");
    }*/

    //get methods
    public int getCubeId(){
        return cubeID;
    }

    public float[] getCubePosition(){
        return modelPosition;
    }

    public float[] getCubeModel(){
        return modelObject;
    }
    //get methods
    public float[] getCoords(){ return WorldLayoutData.CUBE_COORDS; }
    public float[] getColors() { return WorldLayoutData.CUBE_COLORS; }
    public float[] getNormals() { return WorldLayoutData.CUBE_NORMALS; }
    public int getType() {return 2;}


    //set methods
    public void setModelPosition(float x, float y, float z){
        super.position = new float[] {x, y, z};
    }

    public void setCubeModel(float modelCube){

    }


}