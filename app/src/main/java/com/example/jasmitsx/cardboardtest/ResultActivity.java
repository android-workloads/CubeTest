package com.example.jasmitsx.cardboardtest;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {
    private String[] resultArray;
    private SimpleCursorAdapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        //displayResults();
        //displayDatabaseResults();
        displayDatabaseResultsListView();
    }

    /*private void displayResults(){
        Intent intent = getIntent();
        float[] result = intent.getFloatArrayExtra(PerformanceWorkload.EXTRA_MESSAGE);
        int resultLen = result.length;
        EditText editText = (EditText) findViewById(R.id.myEditText);
        for(int i=0; i<resultLen; i++){
            String oString = Integer.toString(i+1);
            editText.append(oString);
            editText.append(" ");
            editText.append(Float.toString(result[i]));
            editText.append("\n");
        }

    }*/

    /*private void displayDatabaseResults(){
        Intent intent = getIntent();
        DatabaseHelper outputTable = new DatabaseHelper(getApplicationContext());
        EditText editText = (EditText) findViewById(R.id.myEditText);
       List<PerformanceRow> out = outputTable.getAllRows();
        for(PerformanceRow p: out) {
            editText.append(Integer.toString(p.getRowNumber()));
            editText.append(" ");
            editText.append(Double.toString(p.getRowFPS()));
            editText.append(" ");
            editText.append(Double.toString(p.getRowCPU()));
            editText.append(" ");
            editText.append(Integer.toString(p.getRowJanks()));
            editText.append(" ");
            editText.append(Double.toString(p.getRowAPS()));
            editText.append("\n");
        }
    }*/

    private void displayDatabaseResultsListView(){
        DatabaseHelper outputTable = new DatabaseHelper(getApplicationContext());
        List<PerformanceRow> out = outputTable.getAllRows();
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
        //assign the adapter to ListView
        listView.setAdapter(dataAdapter);

    }

    public void rerunWorkload(View view){
        DatabaseHelper perfTable = new DatabaseHelper(this);
        perfTable.onUpgrade(perfTable.getReadableDatabase(), perfTable.getDatabaseVersion(), perfTable.getDatabaseVersion()+1);
        Intent intent = new Intent(this, PerformanceWorkload.class);
        startActivity(intent);
    }

    public void exportDB(View view){
        DatabaseHelper outputTable = new DatabaseHelper(this);
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = "/data/"+"com.example.jasmitsx.cardboardtest"+"/databases/"+outputTable.getDatabaseName();
        String backupDBPath = "PERFORMANCE_WORKLOAD";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try{
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void exportCsvDB(View view){
        DatabaseHelper outputTable = new DatabaseHelper(this);
        File sdDir = Environment.getExternalStorageDirectory();
        String backupDBPath = "PERFORMANCE_WORKLOAD.csv";
        File backupDB = new File(sdDir, backupDBPath);
        SQLiteDatabase out = outputTable.getReadableDatabase();
        try{
            Cursor c = out.rawQuery("select * from performance", null);

            FileWriter fw = new FileWriter(backupDB);
            BufferedWriter bw = new BufferedWriter(fw);
            int rowCount = c.getCount();
            int colCount = c.getColumnCount();
            if(rowCount > 0){
                c.moveToFirst();
                for(int i=0; i<colCount; i++){
                    if(i != colCount-1){
                        bw.write(c.getColumnName(i)+",");
                    } else{
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
                Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }

    }


}













