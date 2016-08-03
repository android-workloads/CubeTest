package com.example.jasmitsx.cardboardtest;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by jasmitsx on 6/29/2016.
 */
public class TreasureHuntActivity extends GvrActivity implements GvrView.StereoRenderer {

    private float[] tempModelPosition;

    private CubeObject[] cubeArray;
    private ArrayList<CubeObject> cubeArrayList;

    private FloorObject floor;
    private FloorObject ceiling;

    private static final String TAG = "TreasureHuntActivity";

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final float CAMERA_Z = 0.01f;
    private static final float TIME_DELTA = 0.3f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.0f;

    private static final Handler handler = new Handler();

    private static final int COORDS_PER_VERTEX = 3;

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[]{0.0f, 2.0f, 0.0f, 1.0f};

    // Convenience vector for extracting the position from a matrix via multiplication.
    private static final float[] POS_MATRIX_MULTIPLY_VEC = {0, 0, 0, 1.0f};

    private static final float MIN_MODEL_DISTANCE = 3.0f;
    private static final float MAX_MODEL_DISTANCE = 7.0f;

    private static final String SOUND_FILE = "cube_sound.wav";

    private static final float[] lightPosInEyeSpace = new float[4];

    //time counter for fps
    private long frameTime;
    private long previousFrameTime = SystemClock.elapsedRealtime();
    private long totalFrameTime=0;
    private int frameCounter=0;


    private float[] camera;
    private float[] view;
    private float[] view2;
    private float[] headView;
    private float[] modelViewProjection;
    private float[] getModelViewProjection2;
    private float[] modelView;
    private float[] modelView2;
    private float[] modelFloor;

    private float[] tempPosition;
    private float[] headRotation;

    //example coordinate array
    private float[][] floatArray;
    private ArrayList<float[]> floatArrayList;

    private float objectDistance = MAX_MODEL_DISTANCE / 2.0f;
    private static final float floorDepth = 20f;
    private float ceilingHeight = 40f;

    //shaders
    private int vertexShader;
    private int gridShader;
    private int passthroughShader;

    private FloatBuffer cubeVertices;
    private FloatBuffer cubeColors;
    private FloatBuffer cubeNormals;
    private int cubeProgram;

    private FloatBuffer floorVertices;
    private FloatBuffer floorColors;
    private FloatBuffer floorNormals;
    private int floorProgram;

    private IntentFilter ifilter;
    private Intent batteryStatus;
    private float startPct;
    private float endPct;


    private Vibrator vibrator;

    public final static String EXTRA_MESSAGE = "com.example.jasmitsx.cardboardtest.MESSAGE";

    //private static GvrAudioEngine gvrAudioEngine;
    //private volatile int soundId = GvrAudioEngine.INVALID_ID;

    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
     *
     * @param type  The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The shader object handler.
     */
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    /**
     * Sets the view to our GvrView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stillRunning = true;
        scheduleAddCubes();

        initializeGvrView();

        Intent intent = getIntent();
        int numberOfCubes= intent.getIntExtra(MainActivity.EXTRA_MESSAGE, 1); //gets the number of cubes wanted by the user

        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED); //define function
        batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        startPct = level / (float)scale;
        Log.i(TAG, "Starting battery Percentage: "+Float.toString(startPct));

        floatArrayList = new ArrayList<>();
        floatArrayListBuilder(numberOfCubes);
        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        modelFloor = new float[16];
        tempPosition = new float[4];
        cubeArrayList = new ArrayList<>();
        for (int n = 0; n < floatArrayList.size(); n++) {
            CubeObject c1 = new CubeObject(floatArrayList.get(n), n);
            cubeArrayList.add(c1);
        }
        tempModelPosition = new float[]{0.0f, -floorDepth, 0.0f}; //sets the position of the floor
        floor = new FloorObject(tempModelPosition);
        headRotation = new float[4];
        headView = new float[16];
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Initialize 3D audio engine.
        //gvrAudioEngine = new GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
    }

    /**
     * Creates an ArrayList of float objects for object position
     */
    private void floatArrayListBuilder(int n){
        float start = -20f;
        float elevation = 0.0f;
        for(int p=0; p<(n/11); p++){
            for(int i=0; i<11; i++){
                float[] f1 = new float[]{start, elevation, -20.0f};
                floatArrayList.add(f1);
                start=start+(float)40/11;
            }
            start = -20.0f;
            elevation += 5.0f;
        }
    }


    private void initializeGvrView() {
        setContentView(R.layout.common_ui);

        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setRenderer(this);
        gvrView.setTransitionViewEnabled(true);
        gvrView.setOnCardboardBackButtonListener(
                        new Runnable() {
                    @Override
                    public void run() {
                        onBackPressed();
                    }
                });
        setGvrView(gvrView);
    }

    //resets the gvrView but does not show the transition screen
    private void updateGvrView(){
        setContentView(R.layout.common_ui);

        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setRenderer(this);
        gvrView.setOnCardboardBackButtonListener(
                new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        });
        setGvrView(gvrView);
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     * <p/>
     * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

        vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
        passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);

        makeCubeObject();
        makeFloorObject();

        for(int n=0; n<cubeArrayList.size(); n++){
            setCubeParams(cubeArrayList.get(n));
            updateModelPosition(cubeArrayList.get(n));
            drawObject(cubeArrayList.get(n));
        }

        //create and draw floor
        setFloorParams(floor);
        Matrix.setIdentityM(floor.modelObject, 0);
        Matrix.translateM(floor.modelObject, 0, 0, -floorDepth, 0); // Floor appears below user.
        drawObject(floor);

        checkGLError("onSurfaceCreated");
    }

    /**
     * Creates an general object
     */
    private void makeCubeObject() {
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        cubeVertices = bbVertices.asFloatBuffer();
        cubeVertices.put(WorldLayoutData.CUBE_COORDS);
        cubeVertices.position(0);

        ByteBuffer bbColors = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COLORS.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        cubeColors = bbColors.asFloatBuffer();
        cubeColors.put(WorldLayoutData.CUBE_COLORS);
        cubeColors.position(0);

        ByteBuffer bbNormals = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_NORMALS.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        cubeNormals = bbNormals.asFloatBuffer();
        cubeNormals.put(WorldLayoutData.CUBE_NORMALS);
        cubeNormals.position(0);


        cubeProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(cubeProgram, vertexShader);
        GLES20.glAttachShader(cubeProgram, passthroughShader);
        GLES20.glLinkProgram(cubeProgram);
        GLES20.glUseProgram(cubeProgram);

        checkGLError("cube program");

    }

    private void makeFloorObject(){
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        floorVertices = bbVertices.asFloatBuffer();
        floorVertices.put(WorldLayoutData.FLOOR_COORDS);
        floorVertices.position(0);

        ByteBuffer bbColors = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COLORS.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        floorColors = bbColors.asFloatBuffer();
        floorColors.put(WorldLayoutData.FLOOR_COLORS);
        floorColors.position(0);

        ByteBuffer bbNormals = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_NORMALS.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        floorNormals = bbNormals.asFloatBuffer();
        floorNormals.put(WorldLayoutData.FLOOR_NORMALS);
        floorNormals.position(0);


        floorProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(floorProgram, vertexShader);
        GLES20.glAttachShader(floorProgram, gridShader);
        GLES20.glLinkProgram(floorProgram);
        GLES20.glUseProgram(floorProgram);

        checkGLError("Floor program");
    }

    private void setCubeParams(WorldObject object){
        object.objectModelParam = GLES20.glGetUniformLocation(cubeProgram, "u_Model");
        object.objectModelViewParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVMatrix");
        object.objectModelViewProjectionParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVP");

        object.objectPositionParam = GLES20.glGetAttribLocation(cubeProgram, "a_Position");
        object.objectNormalParam = GLES20.glGetAttribLocation(cubeProgram, "a_Normal");
        object.objectColorParam = GLES20.glGetAttribLocation(cubeProgram, "a_Color");

        checkGLError("Object program params");
    }


    private void setFloorParams(WorldObject object){
        object.objectModelParam = GLES20.glGetUniformLocation(floorProgram, "u_Model");
        object.objectModelViewParam = GLES20.glGetUniformLocation(floorProgram, "u_MVMatrix");
        object.objectModelViewProjectionParam = GLES20.glGetUniformLocation(floorProgram, "u_MVP");

        object.objectPositionParam = GLES20.glGetAttribLocation(floorProgram, "a_Position");
        object.objectNormalParam = GLES20.glGetAttribLocation(floorProgram, "a_Normal");
        object.objectColorParam = GLES20.glGetAttribLocation(floorProgram, "a_Color");

        checkGLError("Object program params");
    }


    /**
     * Updates the cube model position.
     */
    private void updateModelPosition(WorldObject object) {
        Matrix.setIdentityM(object.modelObject, 0);
        Matrix.translateM(object.modelObject, 0, object.getModelPosition(0),
                object.getModelPosition(1), object.getModelPosition(2));
        checkGLError("updateCubePosition");
    }

    /**
     * Converts a raw text file into a string.
     *
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The context of the text file, or null in case of error.
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        frameTime = SystemClock.elapsedRealtime() - previousFrameTime;
        previousFrameTime = SystemClock.elapsedRealtime();
        if(frameTime<100) {
            totalFrameTime = totalFrameTime + frameTime;
        }
        frameCounter++;
        double averageFrameTime = (double)totalFrameTime/frameCounter;
        double aFps = 0;
        double fps = 0;
        if(frameTime != 0) {
            fps = 1/((double)frameTime/1000);
        }
        if(averageFrameTime != 0) {
            aFps = 1 / (averageFrameTime / 1000);
        }
        setCubeRotation();

        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        headTransform.getHeadView(headView, 0);
        checkGLError("onReadyToDraw");

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        checkGLError("colorParam");
    }

    private void setCubeRotation() {
        //set the rotation for each cube in the ArrayList
        for(int n=0; n<cubeArrayList.size(); n++){
            Matrix.rotateM(cubeArrayList.get(n).getCubeModel(), 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);
        }


    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        // Set the position of the light
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);

        /**
         * Iterates through the array of cube objects and draws each of them
         */

        for(int n=0; n<cubeArrayList.size(); n++){
            CubeObject c1 = cubeArrayList.get(n);
            Matrix.multiplyMM(modelView, 0, view, 0, c1.modelObject, 0);
            Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
            drawObject(c1);

        }


        /**
         * Draws the floor object
         */
        //Set modelView for the floor, so we draw floor in the correct location
        Matrix.multiplyMM(modelView, 0, view, 0, floor.modelObject, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        drawObject(floor);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    private static boolean stillRunning;

    //Runs the workload for n milliseconds
    private void scheduleAddCubes(){
        final int n=300000;
        handler.postDelayed(new Runnable(){
            public void run() {
                if(stillRunning) {
                    endActivity();
                    handler.postDelayed(this, n);
                }
            }
        }, n);
    }

    private void endActivity(){

        batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        endPct = level / (float)scale;

        //Log.i(TAG, "Ending battery Percentage: "+Float.toString(endPct));
        Intent intent = new Intent(this, BatteryResult.class);
        float percentDrop = (startPct - endPct)*100;
        String dropString = Float.toString(percentDrop);
        stillRunning = false;
        intent.putExtra(EXTRA_MESSAGE, dropString);
        startActivity(intent);
    }
    /**
     * Draw the cube.
     * <p/>
     * <p>We've set all of our transformation matrices. Now we simply pass them into the shader.
     */
    private void drawObject(WorldObject object) {
        int type = object.getType();
        if(type==1){
            GLES20.glUseProgram(floorProgram);
        }
        else if(type==2){
            GLES20.glUseProgram(cubeProgram);
        }

        // Set the Model in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(object.objectModelParam, 1, false, object.modelObject, 0);

        // Set the ModelView in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(object.objectModelViewParam, 1, false, modelView, 0);

        // Set the position of the cube
        if(type==1){
            GLES20.glVertexAttribPointer(
                    object.objectPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, floorVertices);
            GLES20.glVertexAttribPointer(object.objectNormalParam, 3, GLES20.GL_FLOAT, false, 0, floorNormals);
            GLES20.glVertexAttribPointer(object.objectColorParam, 4, GLES20.GL_FLOAT, false, 0, floorColors);

        }

        if(type==2) {
            GLES20.glVertexAttribPointer(
                    object.objectPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, cubeVertices);
            GLES20.glVertexAttribPointer(object.objectNormalParam, 3, GLES20.GL_FLOAT, false, 0, cubeNormals);
            GLES20.glVertexAttribPointer(object.objectColorParam, 4, GLES20.GL_FLOAT, false, 0, cubeColors);
        }

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(object.objectModelViewProjectionParam, 1, false, modelViewProjection, 0);

        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(object.objectPositionParam);
        GLES20.glEnableVertexAttribArray(object.objectNormalParam);
        GLES20.glEnableVertexAttribArray(object.objectColorParam);
        if(object.getType()==1){
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 24);
            checkGLError("Drawing floor");
        }
        else if(object.getType()==2) {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
            checkGLError("Drawing cube");
        }
    }

    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
        // Always give user feedback.
        vibrator.vibrate(50);

        updateGvrView();
        //record the size of the old cubeArrayList and add 11
        int oldSize = cubeArrayList.size();
        int newSize = oldSize+11;
        //reset the frameCounter and totalFrameTime to keep FPS count accurate
        frameCounter=0;
        totalFrameTime=0;
        //empty the floatArrayList and cubeArrayList in preperation to create new cubeArrayList
        floatArrayList.clear();
        cubeArrayList.clear();

        //reset the floor
        floor = null;

        //refill the floatArrayList, adding a new row of cubes
        floatArrayListBuilder(newSize);

        //rebuild the cubeArrayList with the new floatArrayList
        for(int i=0; i<floatArrayList.size(); i++){
            CubeObject c1 = new CubeObject(floatArrayList.get(i), i);
            cubeArrayList.add(i, c1);
        }

        tempModelPosition = new float[]{0.0f, -floorDepth, 0.0f}; //sets the position of the floor
        floor = new FloorObject(tempModelPosition);

        headRotation = new float[4];
        headView = new float[16];
    }

    public static float[] getLightPosInEyeSpace() {
        return lightPosInEyeSpace;
    }
}









































