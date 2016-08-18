package com.example.jasmitsx.cardboardtest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import android.Manifest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
//import java.util.jar.Manifest;

public class ResultActivity extends AppCompatActivity {
    private SimpleCursorAdapter dataAdapter;
    private final static Handler h1 = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        //scheduleRerun();
        displayDatabaseResultsListView();
        //v=this.findViewById(android.R.id.content);
        //stillRunning = true;
    }
    /*private void scheduleRerun(){
        h1.postDelayed(new Runnable(){
            public void run() {
                   // rerunWorkload(this.findViewById(android.R.id.content));
                if(stillRunning==true && (MainActivity.getCounter()<5)) {
                    rerunWorkload(v);
                    h1.postDelayed(this, 10000);
                }
            }
        }, 10000);
    }*/

    private void displayDatabaseResultsListView(){
        DatabaseHelper outputTable = new DatabaseHelper(getApplicationContext());
        //List<PerformanceRow> out = outputTable.getAllRows();
        SQLiteDatabase db = outputTable.getReadableDatabase();
        Cursor cursor = db.query("performance", new String[]{outputTable.getKeyId(),outputTable.getRowNumber(),
                outputTable.getRowFps(), outputTable.getRowCpu(),
                outputTable.getRowJanks(), outputTable.getRowAps()}, null, null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        String [] columns = new String[]{
                outputTable.getRowNumber(),
                outputTable.getRowFps(),
                outputTable.getRowCpu(),
                outputTable.getRowJanks(),
                outputTable.getRowAps()
        };

        //XML defined views the data will be bound
        int[] to = new int[] {
                R.id.num,
                R.id.fps,
                R.id.cpu,
                R.id.janks,
                R.id.aps, };

        //creates the adapter using the cursor pointing to the desired data
        dataAdapter = new SimpleCursorAdapter(
                this, R.layout.row_info,
                cursor,
                columns,
                to,
                0);

        ListView listView = (ListView) findViewById(R.id.databaseOutput);
        db.close();
        //assign the adapter to ListView
        listView.setAdapter(dataAdapter);

    }

    public void rerunWorkload(View view){
        MainActivity.addCounter();
        //stillRunning = false;
        DatabaseHelper perfTable = new DatabaseHelper(this);
        perfTable.onUpgrade(perfTable.getReadableDatabase(), perfTable.getDatabaseVersion(), perfTable.getDatabaseVersion()+1);
        Intent intent = new Intent(this, PerformanceWorkload.class);
        startActivity(intent);
    }

    public void exportCsvDB(View view){
       if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
               ==PackageManager.PERMISSION_GRANTED) {
           DatabaseHelper outputTable = new DatabaseHelper(this);
           File sdDir = Environment.getExternalStorageDirectory();
           String backupDBPath = "PERFORMANCE_WORKLOAD.csv";
           File backupDB = new File(sdDir, backupDBPath);
           SQLiteDatabase out = outputTable.getReadableDatabase();
           try {
               Cursor c = out.rawQuery("select * from performance", null);

               FileWriter fw = new FileWriter(backupDB);
               BufferedWriter bw = new BufferedWriter(fw);
               int rowCount = c.getCount();
               int colCount = c.getColumnCount();
               if (rowCount > 0) {
                   c.moveToFirst();
                   for (int i = 0; i < colCount; i++) {
                       if (i != colCount - 1) {
                           bw.write(c.getColumnName(i) + ",");
                       } else {
                           bw.write(c.getColumnName(i));
                       }
                   }
                   bw.newLine();
                   for (int i = 0; i < rowCount; i++) {
                       c.moveToPosition(i);
                       for (int j = 0; j < colCount; j++) {
                           if (j != colCount - 1)
                               bw.write(c.getString(j) + ",");
                           else
                               bw.write(c.getString(j));
                       }
                       bw.newLine();
                   }
                   bw.flush();
                   //bw.close();
                   //fw.flush();
                   Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
               }

           } catch (Exception ex) {
               Toast.makeText(this, "DB Export Failed", Toast.LENGTH_LONG).show();
               ex.printStackTrace();
           }
       }
        else{
           PermissionUtils.requestPermission(this, 1, Manifest.permission.WRITE_EXTERNAL_STORAGE, false);
       }

    }

    /**
     * Requests permission to store a file to local storage.
     * Displays a rationale to the user
     */
    public void requestLocationPermission(int requestCode){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            PermissionUtils.RationaleDialog.newInstance(requestCode, false).show(
                    getSupportFragmentManager(), "dialog");
        } else {
            //Storage permission has not been granted, request it
            PermissionUtils.requestPermission(this, requestCode,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, false);
        }
    }


}













