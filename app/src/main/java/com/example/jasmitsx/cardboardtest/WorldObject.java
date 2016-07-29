package com.example.jasmitsx.cardboardtest;

import java.nio.FloatBuffer;


/**
 * Created by jasmitsx on 6/29/2016.
 */
public abstract class WorldObject {

    protected final float[] modelObject;
    protected float[] modelPosition;

    protected float[] position;

    private static final String TAG = "WorldObject";

    protected FloatBuffer vertices;
    protected FloatBuffer colors;
    protected FloatBuffer normals;

    protected int objectProgram;

    protected int objectPositionParam;
    protected int objectNormalParam;
    protected int objectColorParam;
    protected int objectModelParam;
    protected int objectModelViewParam;
    protected int objectModelViewProjectionParam;
    protected int objectLightPosParam;

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final float CAMERA_Z = 0.01f;
    private static final float TIME_DELTA = 0.3f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    private static final int COORDS_PER_VERTEX = 3;

    private int type;


    public WorldObject(float[] position){
        this.position=new float[]{position[0], position[1], position[2]};
        modelObject = new float[16];
    }

    public static int getCoordsPerVertex(){
        return COORDS_PER_VERTEX;
    }


    //get methods
    /*public static float[] getCoords(){ return WorldLayoutData.CUBE_COORDS; }
    public static float[] getColors() { return WorldLayoutData.CUBE_COLORS; }
    public static float[] getNormals() { return WorldLayoutData.CUBE_NORMALS; }*/
    public float[] getCoords(){ return null; }
    public float[] getColors(){ return null; }
    public float[] getNormals(){ return null; }
    public int getType(){ return 0; }
    public float getModelPosition(int n){
        return position[n];
    }




}
