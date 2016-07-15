package com.example.jasmitsx.cardboardtest;

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
        String result = intent.getStringExtra(TreasureHuntActivity.EXTRA_MESSAGE);
        EditText editText = (EditText) findViewById(R.id.battEditText);
        //String resString = Float.toString(result);
        editText.append(result);

    }
}
