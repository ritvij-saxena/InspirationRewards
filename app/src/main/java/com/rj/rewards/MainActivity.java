package com.rj.rewards;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final int ALL_PERMISSIONS = 100;
    private static final int LOCATIONS = 101;
    LocationManager locationManager;
    Location currentLocation;
    Criteria criteria;
    Button createProfile;
    Button login;
    Button deleteAllUsers;
    EditText username;
    EditText password;
    CheckBox checkBox_remember_credentials;
    public ProgressBar progressBar;
    String api_response = "";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        createProfile = findViewById(R.id.buttonCreateAccount_main);
        login = findViewById(R.id.buttonLogin_main);
//        deleteAllUsers = findViewById(R.id.delete_all_users);
        username = findViewById(R.id.editusername_main);
        password = findViewById(R.id.editPassword_main);
        checkBox_remember_credentials = findViewById(R.id.checkBoxRemember_main);
        checkBox_remember_credentials.setChecked(false);
        progressBar = findViewById(R.id.progressBar_edit_profile);
        progressBar.setVisibility(View.INVISIBLE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //background authentication to be done.
                if (checkFields()) {
                    new LoginAPIAsyncTask(MainActivity.this).execute(username.getText().toString(), password.getText().toString());
                }
            }
        });

        createProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateProfileActivity.class));
            }
        });

//        deleteAllUsers.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                builder.setTitle("Delete All Users");
//                final EditText user = new EditText(MainActivity.this);
//                final EditText pass = new EditText(MainActivity.this);
//                user.setHint("username");
//                pass.setHint("password");
//                LinearLayout linearLayout = new LinearLayout(MainActivity.this);
//                linearLayout.setOrientation(LinearLayout.VERTICAL);
//                linearLayout.addView(user);
//                linearLayout.addView(pass);
//                builder.setPositiveButton("okay", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (!user.getText().toString().isEmpty() && !pass.getText().toString().isEmpty()) {
//                            new DeleteAllUsersAsyncTask(MainActivity.this).execute(user.getText().toString(), pass.getText().toString());
//                        }
//                    }
//                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        return;
//                    }
//                });
//                builder.setView(linearLayout);
//                AlertDialog alertDialog = builder.create();
//                alertDialog.show();
//            }
//        });

        if (check_Permissions()) {
            Log.i(TAG, "onCreate: REQUESTS_OK");
        }

//        if (currentLocation == null) {
//            setLocation();
//        }
//

        String temp_username;
        String temp_password;

        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        if (preferences != null) {
            temp_username = preferences.getString("username", "");
            temp_password = preferences.getString("password", "");
            if (temp_username != null && temp_password != null) {
                username.setText(temp_username);
                password.setText(temp_password);
                checkBox_remember_credentials.setChecked(preferences.getBoolean("remember", false));
            }
        }

    }

    private boolean checkFields() {
        if (isUserNameEntered() && isPassword()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPassword() {
        if (password.getText().toString().isEmpty()) {
            password.setError("Enter your password");
            return false;
        }
        return true;
    }

    private boolean isUserNameEntered() {
        if (username.getText().toString().isEmpty()) {
            username.setError("Enter your Username");
            return false;
        }
        return true;
    }

    public void sendResult(String result, String response) {
        CustomToast customToast = new CustomToast(MainActivity.this);
        api_response = response;
        String status="";String message="";
        Log.d(TAG, "sendResult: " + response);
        if (result.toLowerCase().contains("failed")) {
            try {
                JSONObject json = new JSONObject(api_response);
                String s = json.getString("errordetails");
                JSONObject jsonObject = new JSONObject(s);
                status = jsonObject.getString("status");
                message = jsonObject.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            customToast.showCustomToast(status + " " + message, Color.RED);
        } else {
            customToast.showCustomToast("Login:" + result, Color.GREEN);
            logInSuccessful();
        }

    }

    private void logInSuccessful() {
        Log.d(TAG, "logInSuccessful: Start");
        getSharedPreferences("credentials",MODE_PRIVATE).edit().clear().apply();
        if (checkBox_remember_credentials.isChecked()) {
            SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("username", username.getText().toString());
            editor.putString("password", password.getText().toString());
            editor.putBoolean("remember", true);
            editor.apply();
        }
        Log.d(TAG, "logInSuccessful: before intent");
//        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
//        intent.putExtra("response_data", api_response);
        try {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class).putExtra("response_data", api_response));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "logInSuccessful: intent dne");
        Log.d(TAG, "logInSuccessful: End");
    }

    private boolean check_Permissions() {
        int GPS_FINE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int GPS_COARSE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> permissionRequired = new ArrayList<>();
        if (GPS_FINE != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (GPS_COARSE != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!permissionRequired.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionRequired.toArray(new String[permissionRequired.size()]), ALL_PERMISSIONS);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Permissions Granted");
                    setLocation();
                }
            }
            case LOCATIONS : {
                if (grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    setLocation();
                }
            }
        }
    }

    private void setLocation() {
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATIONS);
        }
        currentLocation = locationManager.getLastKnownLocation(bestProvider);
        Log.d(TAG, "setLocation: " + String.valueOf(currentLocation));
        Log.d(TAG, "setLocation: " + getPlace(currentLocation));
    }

    private String getPlace(Location loc) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            return city + ", " + state;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void sendAllProfileDeleteResponse(String result, String response) {
        Log.d(TAG, "sendAllProfileDeleteResponse: " + response);
        CustomToast customToast = new CustomToast(MainActivity.this);
        if (result.toLowerCase().contains("failed")) {
            customToast.showCustomToast("Process: " + result, Color.RED);
        } else {
            customToast.showCustomToast("Process: " + result, Color.GREEN);

        }
    }

}
