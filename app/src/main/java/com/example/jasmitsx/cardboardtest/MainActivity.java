package com.example.jasmitsx.cardboardtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;

import java.lang.Object;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.jasmitsx.cardboardtest.MESSAGE";
    public final static String EXTRA_MESSAGE1="com.example.jasmitsx.cardboardtest.MESSAGE1"; //redundant, remove before sending anywhere


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startVR(View view) {
        Intent intent = new Intent(this, TreasureHuntActivity.class);
        EditText editText = (EditText) findViewById(R.id.number_of_cubes);
        int message = Integer.parseInt(editText.getText().toString());
        //add some code to handle the user putting in an unacceptable value
        /*if(message%11!=0){
            //pop an error box saying that input must be a multiple of 11
            Intent intent1 = new Intent(this, DisplayMessageActivity.class);
            intent1.putExtra(EXTRA_MESSAGE1, message);
            startActivity(intent1);
        }*/
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
