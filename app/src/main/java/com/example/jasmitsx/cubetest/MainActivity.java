package com.example.jasmitsx.cubetest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.jasmitsx.cardboardtest.MESSAGE";
    private DatabaseHelper perfTable;
    private static int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        counter = 0;
        perfTable = new DatabaseHelper(this);
        perfTable.onUpgrade(perfTable.getReadableDatabase(), perfTable.getDatabaseVersion(), perfTable.getDatabaseVersion()+1);
        setContentView(R.layout.activity_main);
    }

    public void startVR(View view) {
        EditText editText = (EditText) findViewById(R.id.number_of_cubes);
        int message = Integer.parseInt(editText.getText().toString());
        message = message*11;
        //add some code to handle the user putting in an unacceptable value
        int remainder = message%11;
        if(remainder!=0){
            //pop an error box saying that input must be a multiple of 11
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            String error = "Error, must enter a multiple of 11";
            intent.putExtra(EXTRA_MESSAGE, error);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(this, BatteryWorkload.class);
            intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
        }
    }

    public void startThermalWorkload(View view){
        Intent intent = new Intent(this, PerformanceWorkload.class);
        int runType = 2;
        intent.putExtra(EXTRA_MESSAGE, runType);
        startActivity(intent);
    }

    public void startPerformanceWorkload(View view){
        Intent intent = new Intent(this, PerformanceWorkload.class);
        int runType = 1;
        intent.putExtra(EXTRA_MESSAGE, runType);
        startActivity(intent);
    }

    public static int getCounter(){
        return counter;
    }

    public static void addCounter(){
        counter++;
    }
}
