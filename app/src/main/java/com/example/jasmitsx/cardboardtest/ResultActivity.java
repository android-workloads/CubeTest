package com.example.jasmitsx.cardboardtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {
    private String[] resultArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        displayResults();
    }

    private void displayResults(){
        Intent intent = getIntent();
        float[] result = intent.getFloatArrayExtra(PerformanceWorkload.EXTRA_MESSAGE);
        int resultLen = result.length;
        EditText editText = (EditText) findViewById(R.id.myEditText);
        for(int i=0; i<resultLen; i++){
            editText.append(Integer.toString(i+1)+" ");
            editText.append(Float.toString(result[i]));
            editText.append("\n");
        }

    }
}
