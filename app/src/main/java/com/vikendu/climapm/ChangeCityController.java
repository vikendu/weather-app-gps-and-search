package com.vikendu.climapm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChangeCityController extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_city_layout);

        final EditText mEditText = (EditText)findViewById(R.id.queryET);
        ImageButton mButton = (ImageButton)findViewById(R.id.backButton);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String newCity = mEditText.getText().toString();
                Intent newCityIntent = new Intent(ChangeCityController.this, WeatherController.class);

                newCityIntent.putExtra("city", newCity);

                //The following 2 flags clear the stack and prevent multiple instances being launched
                newCityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                newCityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(newCityIntent);

                finish(); //testing
                return false;
            }
        });

    }
}
