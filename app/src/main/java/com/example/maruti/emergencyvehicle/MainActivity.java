package com.example.maruti.emergencyvehicle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    Button initiateButton;
    EditText destinationEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initiateButton=(Button) findViewById(R.id.initiateButton);
        destinationEditText=(EditText) findViewById(R.id.destinationEditText);

        initiateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(getApplicationContext(), MapActivity.class);
                i.putExtra("destination", destinationEditText.getText().toString());
                startActivity(i);
            }
        });

    }
}
