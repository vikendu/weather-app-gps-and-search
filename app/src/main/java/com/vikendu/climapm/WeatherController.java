package com.vikendu.climapm;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final int REQUEST_CODE = 123; //Can be anything just for tracking our request
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = ""; //ENTER API KEY HERE
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates in metres
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
    TextView mCredits;
    TextView mLocationAt;

    LocationManager mLocationManager;
    LocationListener mLocationListner;

    DatabaseReference rootRef, didiRef, viduRef, mammaRef, papaRef;

    Boolean flag;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);
        //setContentView(R.layout.credits_layout);

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
        mLocationAt = findViewById(R.id.locationof);

        View header = mNavigationView.getHeaderView(0);
        mCredits = header.findViewById(R.id.credits);

        flag = false;

        Date currentTime = Calendar.getInstance().getTime();
        mCredits.setText("Last Updated: "+currentTime);

        //database reference pointing to root of database
        rootRef = FirebaseDatabase.getInstance().getReference();
        //database reference pointing to user specific node
        didiRef = rootRef.child("didi");
        viduRef = rootRef.child("vidu");
        mammaRef = rootRef.child("mamma");
        papaRef = rootRef.child("papa");

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //menuItem.setChecked(true);
                if(menuItem.getTitle().equals("Report an issue")) {
                    Log.d("drawer", "touch detected");
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/vikendu/weather-app-gps-and-search"));
                    startActivity(i);
                    //Toast.makeText(WeatherController.this, "Not for you!", Toast.LENGTH_SHORT).show();

                }
                else if(menuItem.getTitle().equals("Share on WhatsApp"))
                {
                    mDrawer.closeDrawer(Gravity.START);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            // do something
                            takeScreenshot();
                        }
                    }, 1000);
                    //takeScreenshot();

                }
                else if(menuItem.getTitle().equals("User Info"))
                {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(WeatherController.this);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    alert.setTitle("User Info:");
                    alert.setCancelable(true);
                    Boolean access = false;
                    String email = user.getEmail();
                    if(email.equals("vikendu@gmail.com"))
                    {
                        access = true;
                    }
                    alert.setMessage("User: "+email+"\nAdministrator Access: "+access);
                    alert.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
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
                    alert.setMessage("Version: v2.0-Stable-rc-5\nRelease Date: 19th October, 2018");
                    alert.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                }
                else if(menuItem.getTitle().equals("Refresh Current Location"))
                {
                    getWeatherForCurrentLocation();
                    Toast.makeText(WeatherController.this, "Refreshing Location...", Toast.LENGTH_SHORT).show();
                }
                else if(menuItem.getTitle().equals("Vidu"))
                {
                    viduRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String value = dataSnapshot.getValue(String.class);
                            Log.d("TAG", "Value is: " + value);
                            getWeatherForNewCity(value);
                            mLocationAt.setText("Vidu is at:");
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w("TAG", "Failed to read value.", error.toException());
                        }
                    });

                    Toast.makeText(WeatherController.this, "Vidu's Location!", Toast.LENGTH_SHORT).show();

                }
                else if(menuItem.getTitle().equals("Didi"))
                {
                    didiRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String value = dataSnapshot.getValue(String.class);
                            Log.d("TAG", "Value is: " + value);
                            getWeatherForNewCity(value);
                            mLocationAt.setText("Didi is at:");
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w("TAG", "Failed to read value.", error.toException());
                        }
                    });
                    Toast.makeText(WeatherController.this, "Didi's Location!", Toast.LENGTH_SHORT).show();

                }
                else if(menuItem.getTitle().equals("Mamma"))
                {
                    mammaRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String value = dataSnapshot.getValue(String.class);
                            Log.d("TAG", "Value is: " + value);
                            getWeatherForNewCity(value);
                            mLocationAt.setText("Mamma is at:");
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w("TAG", "Failed to read value.", error.toException());
                        }
                    });
                    Toast.makeText(WeatherController.this, "Mamma's Location...", Toast.LENGTH_SHORT).show();

                }
                else if(menuItem.getTitle().equals("Papa"))
                {
                    papaRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String value = dataSnapshot.getValue(String.class);
                            Log.d("TAG", "Value is: " + value);
                            getWeatherForNewCity(value);
                            mLocationAt.setText("Papa is at:");
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w("TAG", "Failed to read value.", error.toException());
                        }
                    });
                    Toast.makeText(WeatherController.this, "Papa's Location...", Toast.LENGTH_SHORT).show();

                }
                else {}

                mDrawer.closeDrawer(Gravity.START);
                return false;
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
            flag = false;
            getWeatherForNewCity(city);
        }
        else
            {
                flag = true;
                getWeatherForCurrentLocation();
            }
    }

    private void getWeatherForNewCity(String city)
    {
        flag = false;
        mLocationAt.setText("");
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

                mCityLabel.setText("GPS Connecting...");

                Log.d("weatherapp", "Location Changed Callback");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                Log.d("weatherapp", longitude);
                Log.d("weatherapp", latitude);

                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                flag = true;
                letsDoSomeNetworking(params);
                mLocationAt.setText("");


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
                    //mCityLabel.setText("GPS Disabled...");
                    Toast.makeText(WeatherController.this, "Turn on Location!", Toast.LENGTH_SHORT).show();
                }
                Log.d("weatherapp", "Provider disabled Callback");

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
                if(flag == true){
                updateUser(weatherData);}

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

    private void updateUser(WeatherDataModel weather)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Name, email address, and profile photo Url
            String email = user.getEmail();
            Log.d("name", "name is" + email);
            if (email.equals("vikendu@gmail.com")) {
                viduRef.setValue(""+weather.getmCity());
                Log.d("TAG", "Value is: " + weather.getmCity());
            }
            else if(email.equals("aditi.singh11192@gmail.com"))
            {
                didiRef.setValue(""+weather.getmCity());
                Log.d("TAG", "Value is: " + weather.getmCity());
            }
            else if(email.equals("vks0578@gmail.com"))
            {
                papaRef.setValue(""+weather.getmCity());
                Log.d("TAG", "Value is: " + weather.getmCity());
            }
            else if(email.equals("itsmeindu73@gmail.com"))
            {
                mammaRef.setValue(""+weather.getmCity());
                Log.d("TAG", "Value is: " + weather.getmCity());
            }

        }
    }
    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
            Log.d("share",mPath);
            Log.d("share","problem1");
            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            //View v1 = getActivity().getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);
            Log.d("share","problem2");
            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            Log.d("share","problem3");
            openScreenshot(imageFile);
            Log.d("share","problem5");
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
            Log.d("share","problem4");
        }
    }

    private void openScreenshot(File imageFile) {

        Intent intent = new Intent();
//        intent.setAction(android.content.Intent.ACTION_VIEW);
//        //Uri uri = Uri.parse("file://" + path);
          Uri uri = FileProvider.getUriForFile(WeatherController.this,
               BuildConfig.APPLICATION_ID+ ".provider",imageFile);
//          intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.setDataAndType(uri,"image/*");

        intent.setAction(Intent.ACTION_SEND);
        //Target whatsapp:
        intent.setPackage("com.whatsapp");
        //Add text and then Image URI
        //intent.putExtra(Intent.EXTRA_TEXT, picture_text);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("image/jpeg");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);
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
            //Too abrupt of an end to the app, animations are skipped
            //System.exit(1);
            WeatherController.this.finish(); //testing
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
