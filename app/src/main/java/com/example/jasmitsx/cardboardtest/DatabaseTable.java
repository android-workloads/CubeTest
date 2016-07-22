package com.example.jasmitsx.cardboardtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasmitsx on 7/18/2016.
 */
public class DatabaseTable extends SQLiteOpenHelper{
    //database tag
    private static final String TAG = "WorkloadDatabase";
    //datatbase version
    private static int DATABASE_VERSION  = 1;
    //database name
    private static final String DATABASE_NAME = "workloadInfo";
    //Performance workload table name
    private static final String PERFORMANCE_WORKLOAD = "performance";
    //Workload Table Columns names
    private static final String ROW_NUM = "num";
    private static final String ROW_FPS = "fps";
    private static final String ROW_CPU = "cpu";
    private static final String ROW_JANKS = "janks";
    private static final String ROW_APS = "aps";

    public DatabaseTable(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //onUpgrade(this.getWritableDatabase(), DATABASE_VERSION, DATABASE_VERSION+1);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_WORKLOAD_TABLE = "CREATE TABLE "+ PERFORMANCE_WORKLOAD +"("+
                ROW_NUM+" INTEGER PRIMARY KEY,"+ROW_FPS +" DOUBLE,"+ROW_CPU+" DOUBLE,"+ROW_JANKS+
                " INT,"+ROW_APS+" DOUBLE"+")";
        db.execSQL(CREATE_WORKLOAD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop older table if existant
        //db.execSQL("ALTER TABLE performance ADD "+ROW_APS+" DOUBLE");
        db.execSQL("DROP TABLE IF EXISTS "+PERFORMANCE_WORKLOAD);
        //recreate table
        onCreate(db);
    }

    //add a new row
    public void addRow(PerformanceRow row){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ROW_NUM, row.getRowNumber()); //row number
        values.put(ROW_FPS, row.getRowFPS());    //row fps
        values.put(ROW_CPU, row.getRowCPU());
        values.put(ROW_JANKS, row.getRowJanks());
        values.put(ROW_APS, row.getRowAPS());

        //insert the row into the databse
        db.insert(PERFORMANCE_WORKLOAD, null, values);
        //close the database connection
        db.close();
    }

    //get one row
    public PerformanceRow getRow(int num){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(PERFORMANCE_WORKLOAD, new String[]{ROW_NUM, ROW_FPS, ROW_CPU}, ROW_NUM + "=?",
                new String[]{String.valueOf(num) }, null, null, null, null);
        if(cursor!=null)
            cursor.moveToFirst();
        PerformanceRow row = new PerformanceRow(Integer.parseInt(cursor.getString(0)),
                Double.parseDouble(cursor.getString(1)), Double.parseDouble(cursor.getString(2)),
                Integer.parseInt(cursor.getString(3)), Double.parseDouble(cursor.getString(4)));

        return row;
    }

    //return all the rows
   public List<PerformanceRow> getAllRows(){
        List<PerformanceRow> rowList = new ArrayList<PerformanceRow>();
        //select all query
        String selectQuery = "SELECT * FROM " + PERFORMANCE_WORKLOAD;

       SQLiteDatabase db = this.getReadableDatabase();
       Cursor cursor = db.rawQuery(selectQuery, null);

       if(cursor.moveToFirst()){
           do{
               PerformanceRow p = new PerformanceRow();
               p.setRowNumber(Integer.parseInt(cursor.getString(0)));
               p.setRowFPS(Double.parseDouble(cursor.getString(1)));
               p.setRowCPU(Double.parseDouble(cursor.getString(2)));
               p.setRowJanks(Integer.parseInt(cursor.getString(3)));
               p.setRowAPS(Double.parseDouble(cursor.getString(4)));
               rowList.add(p);
           }while(cursor.moveToNext());
       }
        return rowList;
    }
    //return the database name
    public String getDatabaseName(){
        return DATABASE_NAME;
    }
    public int getDatabaseVersion(){return DATABASE_VERSION; }



}













