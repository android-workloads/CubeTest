package com.example.jasmitsx.cardboardtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;

import java.lang.Object;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.jasmitsx.cardboardtest.MESSAGE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startVR(View view) {
        Intent intent = new Intent(this, TreasureHuntActivity.class);
        //EditText editText = (EditText) findViewById(R.id.number_of_cubes);
        //int message = Integer.parseInt(editText.getText().toString());
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
