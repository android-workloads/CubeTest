package com.example.jasmitsx.cardboardtest;

/**
 * Created by jasmitsx on 6/29/2016.
 */
public class FloorObject extends WorldObject {

        //private int type;

        public FloorObject(float[] position) {
            super(position);
            //super.type = 1;
        }

        //get methods
        public float[] getFloorModel(){ return modelObject; }
        public float[] getCoords(){ return WorldLayoutData.FLOOR_COORDS; }
        public float[] getColors() { return WorldLayoutData.FLOOR_COLORS; }
        public float[] getNormals() { return WorldLayoutData.FLOOR_NORMALS; }
        public int getType(){ return 1; }
    }
