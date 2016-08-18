package com.example.jasmitsx.cardboardtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasmitsx on 7/26/2016.
 */
class DatabaseHelper extends SQLiteOpenHelper{

    //Logcat tag
    private static final String LOG = "DatabaseHelper";

    //Database Version
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "performanceManager";

    //Table names
    private static final String PERFORMANCE_TABLE = "performance";



    //Column Names
    private static final String KEY_ID = "_id"; //integer ID
    private static final String ROW_NUMBER = "number"; //integer row number
    private static final String ROW_FPS = "fps"; //double row average FPS
    private static final String ROW_CPU = "cpu"; //double row average CPU load
    private static final String ROW_JANKS = "janks"; //integer number of janks
    private static final String ROW_APS = "aps"; //double number of animations per second

    //Performance table create statement
    private static final String CREATE_TABLE_PERFORMANCE = "CREATE TABLE "
            +PERFORMANCE_TABLE+"("+KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+ROW_NUMBER
            +" INTEGER,"+ROW_FPS+" DOUBLE,"+ROW_CPU+" DOUBLE,"+ROW_JANKS
            +" INTEGER,"+ROW_APS+" DOUBLE"+")";

    //Performance table create statement
    private static final String CREATE_TABLE_PERFORMANCE2 = "CREATE TABLE "
            +PERFORMANCE_TABLE+"("+KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+ROW_NUMBER
            +" INTEGER,"+ROW_FPS+" DOUBLE,"+ROW_CPU+" DOUBLE,"+ROW_JANKS
            +" DOUBLE,"+ROW_APS+" DOUBLE"+")";


    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PERFORMANCE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //drop old tables
        db.execSQL("DROP TABLE IF EXISTS "+PERFORMANCE_TABLE);

        //alter the TABLE

        //db.execSQL("ALTER TABLE "+PERFORMANCE_TABLE+" RENAME TO tmp");

        //db.execSQL(CREATE_TABLE_PERFORMANCE2);

        /*db.execSQL("ALTER TABLE "+PERFORMANCE_TABLE+" RENAME TO tmp;"+
                CREATE_TABLE_PERFORMANCE2+";"+
                "INSERT INTO "+PERFORMANCE_TABLE+"("+KEY_ID+", "+ROW_NUMBER+", "+ROW_FPS+", "
                +ROW_CPU+", "+ROW_JANKS+", "+ROW_APS+") " +
                "SELECT "+KEY_ID+", "+ROW_NUMBER+", "+ROW_FPS+", "
                +ROW_CPU+", "+ROW_JANKS+", "+ROW_APS+" " +
                "FROM tmp;" +
                "DROP TABLE tmp;");*/

        //create new tables
        onCreate(db);
    }

    /**
     * Add a new row to performance table
     */
    public void addRow(PerformanceRow row){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ROW_NUMBER, row.getRowNumber());
        values.put(ROW_FPS, row.getRowFPS());
        values.put(ROW_CPU, row.getRowCPU());
        values.put(ROW_JANKS, row.getRowJanks());
        values.put(ROW_APS, row.getRowAPS());

        db.insert(PERFORMANCE_TABLE, null, values);
    }

    /**
     * Fetch all rows
     */
    public List<PerformanceRow> getAllRows(){
        List<PerformanceRow> rows = new ArrayList<PerformanceRow>();
        String selectQuery = "SELECT * FROM "+PERFORMANCE_TABLE;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);


        //loop through all rows and add to list
        if(c.moveToFirst()){
            do{
                PerformanceRow r = new PerformanceRow();
                r.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                r.setRowNumber(c.getInt(c.getColumnIndex(ROW_NUMBER)));
                r.setRowFPS(c.getDouble(c.getColumnIndex(ROW_FPS)));
                r.setRowCPU(c.getDouble(c.getColumnIndex(ROW_CPU)));
                r.setRowJanks(c.getInt(c.getColumnIndex(ROW_JANKS)));
                r.setRowAPS(c.getDouble(c.getColumnIndex(ROW_APS)));

                rows.add(r);
            }while(c.moveToNext());
        }
        return rows;
    }

    //close the database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if(db != null && db.isOpen()){
            db.close();
        }
    }

    //get methods
    public String getKeyId(){
        return KEY_ID;
    }

    public String getRowNumber(){
        return ROW_NUMBER;
    }

    public String getRowFps(){
        return ROW_FPS;
    }

    public String getRowCpu(){
        return ROW_CPU;
    }

    public String getRowJanks(){
        return ROW_JANKS;
    }

    public String getRowAps(){ return ROW_APS; }

    public String getDatabaseName() { return DATABASE_NAME; }

    public int getDatabaseVersion(){ return DATABASE_VERSION; }


}

















