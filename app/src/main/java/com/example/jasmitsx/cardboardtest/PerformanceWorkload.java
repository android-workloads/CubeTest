package com.example.jasmitsx.cardboardtest;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLES20;
import android.opengl.Matrix;
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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by jasmitsx on 7/8/2016.
 */
public class PerformanceWorkload extends GvrActivity implements GvrView.StereoRenderer {
    private float[] tempModelPosition;

    private ArrayList<CubeObject> cubeArrayList;

    private FloorObject floor;

    private static final String TAG = "TreasureHuntActivity";

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final float CAMERA_Z = 0.01f;
    private static final float TIME_DELTA = 0.6f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.0f;

    private final static Handler h1 = new Handler();

    private final static Handler h2 = new Handler();


    private static final int COORDS_PER_VERTEX = 3;

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[]{0.0f, 2.0f, 0.0f, 1.0f};

    // Convenience vector for extracting the position from a matrix via multiplication.
    private static final float[] POS_MATRIX_MULTIPLY_VEC = {0, 0, 0, 1.0f};

    private static final float MIN_MODEL_DISTANCE = 3.0f;
    private static final float MAX_MODEL_DISTANCE = 7.0f;

    private static final String SOUND_FILE = "cube_sound.wav";

    private static final float[] lightPosInEyeSpace = new float[4];

    //time variables for fps calculation
    private long frameTime;
    private long previousFrameTime = SystemClock.elapsedRealtime();
    private int frameCounter=0;
    private double aFps;
    private ArrayList<Double> aFpsArray;
    private double totalFps=0;

    private long totalTime = previousFrameTime;

    //other performance metrics
    private long lastDT=0;
    private int janks=0;
    private double aps = 0;


    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] modelViewProjection;
    private float[] modelView;
    private float[] modelView2;
    private float[] modelFloor;

    private float[] tempPosition;
    private float[] headRotation;

    //example coordinate array
    private ArrayList<float[]> positionArrayList;


    private float objectDistance = MAX_MODEL_DISTANCE / 2.0f;
    private final static float floorDepth = 20f;

    private int vertexShader;
    private int gridShader;
    private int passthroughShader;

    private int totalFrameCounter;

    private FloatBuffer cubeVertices;
    private FloatBuffer cubeColors;
    private FloatBuffer cubeNormals;
    private int cubeProgram;

    private FloatBuffer floorVertices;
    private FloatBuffer floorColors;
    private FloatBuffer floorNormals;
    private int floorProgram;

    private DatabaseHelper perfTable;

    //CPU use
    private float cpuUse;
    private int cpuCount;

    private int runType;

    private Vibrator vibrator;

    private final static String EXTRA_MESSAGE = "com.example.jasmitsx.cardboardtest.MESSAGE";

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

        Intent intent = getIntent();
        runType= intent.getIntExtra(MainActivity.EXTRA_MESSAGE, 1); //gets the number of cubes wanted by the

        stillRunning = true;
        cpuUse=0;
        cpuCount=0;
        scheduleCPURead();  //reads the cpu usage every second
        scheduleAddCubes(); //adds a new row of cubes every 5 seconds
        perfTable = new DatabaseHelper(this);  //database to hold workload output information

        initializeGvrView();
        int numberOfCubes = 11; //11 cubes per row because 11 fit in the view at once
        positionArrayList = new ArrayList<>();
        aFpsArray = new ArrayList<>();
        floatArrayListBuilder(numberOfCubes); //populates the positionArrayList with numberOfCubes positions
        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        modelFloor = new float[16];
        tempPosition = new float[4];
        cubeArrayList = new ArrayList<>();
        totalFrameCounter = 0;
        for (int n = 0; n < positionArrayList.size(); n++) {
            CubeObject c1 = new CubeObject(positionArrayList.get(n), n);
            cubeArrayList.add(c1);
        }
        tempModelPosition = new float[]{0.0f, -floorDepth, 0.0f}; //sets the position of the floor
        floor = new FloorObject(tempModelPosition);
        headRotation = new float[4];
        headView = new float[16];

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

   /**
    * Creates an array list of float objects for object position
    */
    private void floatArrayListBuilder(int n){
        float start = -20f;
        float elevation = 0.0f;
        for(int p=0; p<(n/11); p++){
            for(int i=0; i<11; i++){
                float[] f1 = new float[]{start, elevation, -20.0f};
                positionArrayList.add(f1);
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

    //Same as initilizeGvrView but does not display the transition view
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
        /*Log.i(TAG, "onRendererShutdown");*/
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        /*Log.i(TAG, "onSurfaceChanged");*/
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
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

        vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
        passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);

        makeCubeObject();
        makeFloorObject();

        //create and draw the cubes
        int size = cubeArrayList.size();
        for (int n = 0; n < size; n++) {
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
        totalFrameCounter++;
        //calculations for rudimentary FPS counter
        frameTime = SystemClock.elapsedRealtime() - previousFrameTime;
        previousFrameTime = SystemClock.elapsedRealtime();
        double fps = 0;
        if(frameTime != 0) {
            frameCounter++; //counts the total number of frames
            fps = 1 / ((double) frameTime / 1000);
            totalFps=totalFps+fps;
            aFps=totalFps/frameCounter;
            //Log.i(TAG, Double.toString(frameTime-lastDT)+"   =?   "+Float.toString((1.0f/120)*1000));
            //A jank is if dt-lastDT > VSYNC_TIME/2
            if(lastDT!=0 && (((frameTime-lastDT)*.001f)>(1.0f/120)||((frameTime-lastDT)*.001f)<-(1.0f/120))){
                janks++;
            }
        }
        lastDT = frameTime;
        setCubeRotation();

        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        headTransform.getHeadView(headView, 0);
        /*Log.i(TAG, "break: ");
        for(int i=0; i<headView.length; i++) {
            Log.i(TAG, "View: " + Float.toString(headView[i]));
        }*/
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
            drawCubeObject(c1);
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

    //adds a new row of cubes every 5 seconds
   private void scheduleAddCubes(){
        h1.postDelayed(new Runnable(){
        public void run() {
            if(stillRunning) {
                addNewCubeRow();
                h1.postDelayed(this, 5000);
            }
        }
    }, 5000);
    }

    //takes a read of the CPUsage every second
    private void scheduleCPURead(){
        h2.postDelayed(new Runnable() {
            public void run() {
                if(stillRunning){
                    cpuUse+=readUsage();
                    cpuCount++;
                    h2.postDelayed(this, 500);
                }
            }
        }, 500);
    }

    /**
     * Draw the cube.
     * <p/>
     * <p>We've set all of our transformation matrices. Now we simply pass them into the shader.
     */
    private void drawObject(WorldObject object) {
        GLES20.glUseProgram(floorProgram);

        // Set the Model in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(object.objectModelParam, 1, false, object.modelObject, 0);

        // Set the ModelView in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(object.objectModelViewParam, 1, false, modelView, 0);

        GLES20.glVertexAttribPointer(
                object.objectPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, floorVertices);
        GLES20.glVertexAttribPointer(object.objectNormalParam, 3, GLES20.GL_FLOAT, false, 0, floorNormals);
        GLES20.glVertexAttribPointer(object.objectColorParam, 4, GLES20.GL_FLOAT, false, 0, floorColors);

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(object.objectModelViewProjectionParam, 1, false, modelViewProjection, 0);

        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(object.objectPositionParam);
        GLES20.glEnableVertexAttribArray(object.objectNormalParam);
        GLES20.glEnableVertexAttribArray(object.objectColorParam);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 24);
        checkGLError("Drawing floor");
    }

    private void drawCubeObject(WorldObject object) {
        GLES20.glUseProgram(cubeProgram);


        // Set the Model in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(object.objectModelParam, 1, false, object.modelObject, 0);

        // Set the ModelView in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(object.objectModelViewParam, 1, false, modelView, 0);

        GLES20.glVertexAttribPointer(
                object.objectPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, cubeVertices);

        // Set the normal positions of the cube, again for shading
        GLES20.glVertexAttribPointer(object.objectNormalParam, 3, GLES20.GL_FLOAT, false, 0, cubeNormals);
        GLES20.glVertexAttribPointer(object.objectColorParam, 4, GLES20.GL_FLOAT, false, 0, cubeColors);

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(object.objectModelViewProjectionParam, 1, false, modelViewProjection, 0);
        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(object.objectPositionParam);
        GLES20.glEnableVertexAttribArray(object.objectNormalParam);
        GLES20.glEnableVertexAttribArray(object.objectColorParam);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        checkGLError("Drawing cube");
    }

    /**
     * Draw the floor.
     *
     * <p>This feeds in data for the floor into the shader. Note that this doesn't feed in data about
     * position of the light, so if we rewrite our code to draw the floor first, the lighting might
     * look strange.
     */

    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
        // Always give user feedback.
        vibrator.vibrate(50);
    }

    private void addNewCubeRow(){
        aps = cubeArrayList.size()*aFps;
        GvrView view = (GvrView)this.findViewById(R.id.gvr_view);
        onRendererShutdown();
        view.shutdown();
        for(int n=0; n<cubeArrayList.size(); n++){
            Matrix.rotateM(cubeArrayList.get(n).getCubeModel(), 0, 0, 0.5f, 0.5f, 1.0f);
        }
        //Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        long jps = janks/((SystemClock.elapsedRealtime()-totalTime)/1000);
        Log.i(TAG, "Average FPS: "+ Double.toString(aFps));
        Log.i(TAG,"Janks per second: "+Long.toString(jps));
        Log.i(TAG, "Animations per second:"+Double.toString(aps));
        Log.i(TAG, "Total number of frames: "+Integer.toString(totalFrameCounter));
        aFpsArray.add(aFps);
        perfTable.addRow(new PerformanceRow(cubeArrayList.size()/11, aFps, (cpuUse/cpuCount), (int)jps, aps));
        //if(cubeArrayList.size()/11>5){
        if(aFps<55){
            showResults();
        }
        updateGvrView();

        //adds a new row of cubes
        int oldSize = cubeArrayList.size();
        int newSize = oldSize+11;
        aFps = 0;
        totalTime = SystemClock.elapsedRealtime();
        frameCounter = 0;
        totalFps = 0;
        cpuUse = 0;
        cpuCount = 0;
        janks=0;
        lastDT=0;
        totalFrameCounter = 0;
        //GLES20.glDeleteShader(vertexShader);
        //GLES20.glDeleteShader(gridShader);
        //GLES20.glDeleteShader(passthroughShader);
        positionArrayList.clear();
        cubeArrayList.clear();
        floatArrayListBuilder(newSize);
        for(int i=0; i<positionArrayList.size(); i++){
            CubeObject c1 = new CubeObject(positionArrayList.get(i), i);
            cubeArrayList.add(i, c1);
        }
        tempModelPosition = new float[]{0.0f, -floorDepth, 0.0f}; //sets the position of the floor
        floor = new FloorObject(tempModelPosition);

    }

    private void showResults(){
        Intent intent = new Intent(this, ResultActivity.class);
        stillRunning = false;
        startActivity(intent);
    }

    protected static float[] getLightPosInEyeSpace() {
        return lightPosInEyeSpace;
    }

    private float readUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" +");  // Split on one or more spaces

            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {}

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

}
