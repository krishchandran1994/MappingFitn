package com.example.kchan.mappingfitness;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    TextView t2;
    TextView t4;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        t2= (TextView)findViewById(R.id.textView2);
        t4=(TextView)findViewById(R.id.textView4);

        Intent i= getIntent();
        String distance= i.getStringExtra("distance");
        t2.setText(distance);
        String calories= i.getStringExtra("calories");
        t4.setText(calories);
    }
}
