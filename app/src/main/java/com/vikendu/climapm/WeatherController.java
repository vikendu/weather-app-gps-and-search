package com.vikendu.climapm;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final int REQUEST_CODE = 123; //Can be anything just for tracking our request
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = ""; //ENTER API KEY HERE
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;

    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;
    TextView mSpeed;
    TextView mHumidity;
    TextView mCondition;
    NavigationView mNavigationView;
    DrawerLayout mDrawer;

    LocationManager mLocationManager;
    LocationListener mLocationListner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);
        ImageButton mNavDrawer = (ImageButton) findViewById(R.id.nav_button);
        mSpeed = (TextView)findViewById(R.id.speed);
        mHumidity = (TextView)findViewById(R.id.humidity);
        mCondition = (TextView)findViewById(R.id.condition);
        mNavigationView = findViewById(R.id.nav_view);
        mDrawer = findViewById(R.id.my_drawer);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //menuItem.setChecked(true);
                if(menuItem.getTitle().equals("Report an issue")) {
                    Log.d("drawer", "touch detected");
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/vikendu/weather-app-gps-and-search"));
                    startActivity(i);
                }
                else if(menuItem.getTitle().equals("Change Location"))
                {

                    Intent myIntent = new Intent(WeatherController.this, ChangeCityController.class);
                    startActivity(myIntent);
                }
                else if(menuItem.getTitle().equals("About"))
                {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(WeatherController.this);
                    alert.setTitle("About");
                    alert.setCancelable(true);
                    alert.setMessage("Version: v1.0-Stable-rc-5\nRelease Date: 6th October, 2018");
                    alert.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                }
                else {}

                mDrawer.closeDrawer(Gravity.START);
                return false;
            }
        });


        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(WeatherController.this, ChangeCityController.class);
                startActivity(myIntent);
                //Ambiguity in using the following finish()
                //It does stop the activity but using the back button while navigating resumes it(onResume())
                //Use the flags from intents package
                //finish();
            }
        });

        mNavDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(Gravity.START);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("city");
        Log.d("weatherapp", "onResume Called");
        if(city != null)
        {
            getWeatherForNewCity(city);
        }
        else
            {
        getWeatherForCurrentLocation();
    }}


    private void getWeatherForNewCity(String city)
    {
        RequestParams params = new RequestParams();
        params.put("q",city);
        params.put("appid", APP_ID);
        letsDoSomeNetworking(params);
    }


    private void getWeatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d("weatherapp", "Location Changed Callback");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                Log.d("weatherapp", longitude);
                Log.d("weatherapp", latitude);

                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                letsDoSomeNetworking(params);



            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                for(int i = 0; i < 2; i++) {
                    Toast.makeText(WeatherController.this, "Turn GPS on!", Toast.LENGTH_SHORT).show();
                }
                Log.d("weatherapp", "Provider disabled Callback");

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consi        android:noHistory="true"der calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListner);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Log.d("weatherapp", "onRequestPermission permission granted");
                getWeatherForCurrentLocation();
            }
            else
            {
                Log.d("weatherapp", "onRequestPermission permission DENIED");
            }
        }
    }
    private void letsDoSomeNetworking(RequestParams params)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                Log.d("weatherapp", "Success JSON Content"+response.toString());

                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                updateUI(weatherData);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response)
            {
                Log.d("weatherapp", "failed"+e.toString());
                Log.d("weatherapp", "StatusCode"+statusCode);

                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void updateUI(WeatherDataModel weather)
    {
        mTemperatureLabel.setText(weather.getmTemperature());
        mCityLabel.setText(weather.getmCity()+", "+weather.getCountry());
        mHumidity.setText("Humidity: "+weather.getHumidity()+"%");
        mSpeed.setText("Wind Speed: "+weather.getSpeed()+"Km/Hr");
        mCondition.setText("\""+weather.getCondition()+"\"");

        int resourceID = getResources().getIdentifier(weather.getmIconName(), "drawable", getPackageName());

        mWeatherImage.setImageResource(resourceID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager != null) mLocationManager.removeUpdates(mLocationListner);
    }
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            //System.exit(1);
            finish(); //testing
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
