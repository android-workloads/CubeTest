package com.example.jasmitsx.cubetest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class BatteryResult extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_result);
        displayResults();
    }

    private void displayResults(){
        Intent intent = getIntent();
        String result = intent.getStringExtra(BatteryWorkload.EXTRA_MESSAGE);
        EditText editText = (EditText) findViewById(R.id.battEditText);
        editText.append(result);

    }
}
