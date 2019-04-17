package com.rj.rewards;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.graphics.Bitmap.CompressFormat.JPEG;

public class EditProfileActivity extends AppCompatActivity {
    ImageView profilePicture;
    EditText editText_username, editText_password, editText_firstName, editText_lastName, editText_department, editText_position, editText_story;
    CheckBox admin_or_not;
    TextView char_num;
    ProgressBar progressBar;
    private LocationManager locationManager;
    private Criteria criteria;
    private static final int MAX_CHARS = 360;
    private int OPEN_CAMERA_GALLERY = 1;
    private int OPEN_CAMERA_CAPTURE = 2;
    private final static int ALL_PERMISSIONS = 100;
    private static final String TAG = "EditProfileActivity";
    Location currentLocation;

    String currentUser, password;
    Bitmap profileImageBitmap;
    String temp_user, temp_pass;
    String imageFileName;
    boolean admin;
    String first_name, last_name, username, department, position, story, imageBytes, location;
    int pointsToAward;
    Bitmap bitmap;
    private String api_response;
    List<RewardsContent> rewardsContentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        rewardsContentList = new ArrayList<>();


        if (check_Permissions()) {
            Log.i(TAG, "onCreate: REQUESTS_OK");

        }
        if (getIntent().hasExtra("username") && getIntent().hasExtra("password")) {
            currentUser = getIntent().getStringExtra("username");
            password = getIntent().getStringExtra("password");
        } else if (preferences != null) {
            temp_user = preferences.getString("username", "");
            temp_pass = preferences.getString("password", "");
            if (temp_user != null && temp_pass != null) {
                currentUser = temp_user;
                password = temp_pass;
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error")
                    .setIcon(R.drawable.icon)
                    .setMessage("Something Went Wrong")
                    .setCancelable(false)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        profilePicture = findViewById(R.id.profile_image_edit);     //Image view
        editText_username = findViewById(R.id.editUserName_edit_profile);
        editText_password = findViewById(R.id.editPassword_edit_profile);
        admin_or_not = findViewById(R.id.adminuser_edit_profile);
        editText_firstName = findViewById(R.id.editFirstName_edit_profile);
        editText_lastName = findViewById(R.id.editLastName_edit_profile);
        editText_department = findViewById(R.id.editDepartment_edit_profile);
        editText_position = findViewById(R.id.editPosition_edit_profile);
        editText_story = findViewById(R.id.editStory_edit_profile);
        char_num = findViewById(R.id.story_chars_edit_profile);
        progressBar = findViewById(R.id.progressBar_edit_profile);
        progressBar.setVisibility(View.INVISIBLE);

        editText_username.setClickable(false);
        editText_story.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_CHARS)});
        editText_story.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String char_text = "(" + s.toString().length() + " of " + MAX_CHARS + ")";
                char_num.setText(char_text);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);

        if (currentLocation == null) {
            setLocation();
        }

        new LoginAPIAsyncTask(EditProfileActivity.this, false).execute(currentUser, password);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog();
            }
        });

        final View arrowLogo = toolbar.getChildAt(0);
        arrowLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class)
                        .putExtra("username",currentUser)
                        .putExtra("password",password));
                Log.d(TAG, "logo onClick: " + currentUser + password);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_edit_profile:
//                NavUtils.navigateUpFromSameTask(this);
                saveData();
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean check_Permissions() {
        int Camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int WriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int ReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int GPS_FINE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int GPS_COARSE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> permissionRequired = new ArrayList<>();
        if (Camera != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.CAMERA);
        }
        if (WriteStorage != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ReadStorage != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == OPEN_CAMERA_CAPTURE) {
            if (resultCode == RESULT_OK) {
                cameraData();
            }
        }
        if (requestCode == OPEN_CAMERA_GALLERY) {
            if (resultCode == RESULT_OK) {
                galleryData(data);
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
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(bestProvider);
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

    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.icon);
        builder.setTitle("Choose Profile Picture");
        builder.setMessage("Take Picture From:");
//        builder.setItems(new String[]{"Gallery", "Camera"}, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                switch (which) {
//                    case 0:
//                        imageFromGallery();
//                        break;
//                    case 1:
//                        imageFromCamera();
//                }
//            }
//        });
        builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imageFromCamera();
            }
        }).setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imageFromGallery();
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void imageFromCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.rj.rewards.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, OPEN_CAMERA_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_temp";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        this.imageFileName = image.getAbsolutePath();
        return image;
    }

    private void imageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, OPEN_CAMERA_GALLERY);
    }

    private void saveData() {
        Log.d(TAG, "saveData: Start");
        if (checkFields()) {
            Log.d(TAG, "check field: Start");
            new UpdateProfileAsyncTask(this, rewardsContentList).execute(
                    editText_username.getText().toString(), //0
                    editText_password.getText().toString(), //1
                    admin_or_not.isChecked() ? "1" : "0", //2
                    editText_firstName.getText().toString(), //3
                    editText_lastName.getText().toString(), //4
                    editText_department.getText().toString(), //5
                    editText_position.getText().toString(), //6
                    editText_story.getText().toString(), //7
                    getPlace(currentLocation), //8
                    getEncodedImage(bitmap) //9
            );
            Log.d(TAG, "check field: END");
        } else {
            new CustomToast(EditProfileActivity.this).showCustomToast("Something Went Wrong", Color.RED);
        }
        Log.d(TAG, "saveData: END");
    }

    private String getEncodedImage(Bitmap bitmap) {
        if (bitmap == null) {
            bitmap = profileImageBitmap;
        }
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        bitmap.compress(JPEG, 70, byteArray);
        byte[] imageBytes = byteArray.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private boolean checkFields() {
        return isUserNameValid() && isPasswordValid()
                && isFirstNameValid() && isLastNameValid()
                && isDepartmentValid() && isPositionValid()
                && isStoryValid();
    }

    private boolean isStoryValid() {
        if (editText_story.getText().toString().isEmpty() || editText_story.getText().toString().length() > 360) {
            editText_story.setError("Please Enter Story");
            return false;
        } else {
            return true;
        }
    }

    private boolean isPositionValid() {
        if (editText_position.getText().toString().isEmpty()) {
            editText_position.setError("Please Enter Position");
            return false;
        } else {
            return true;
        }
    }

    private boolean isDepartmentValid() {
        if (editText_department.getText().toString().isEmpty()) {
            editText_department.setError("Please Enter Department Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean isLastNameValid() {
        if (editText_lastName.getText().toString().isEmpty()) {
            editText_lastName.setError("Please Enter Last Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean isFirstNameValid() {
        if (editText_firstName.getText().toString().isEmpty()) {
            editText_firstName.setError("Please Enter First Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean isPasswordValid() {
        if (editText_password.getText().toString().isEmpty()) {
            editText_password.setError("Please Enter Valid Password");
            return false;
        } else {
            return true;
        }
    }

    private boolean isUserNameValid() {
        if (editText_username.getText().toString().isEmpty()) {
            editText_username.setError("Please Enter Valid UserName");
            return false;
        } else {
            return true;
        }
    }

    private void galleryData(Intent data) {
        Uri imageUri = data.getData();
        if (imageUri == null) {
            return;
        }
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            profilePicture.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cameraData() {
        bitmap = BitmapFactory.decodeFile(imageFileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,70,byteArrayOutputStream);
        Bitmap final_image = BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        profilePicture.setImageBitmap(final_image);

    }

    public void sendResult(String result, String response) {
        CustomToast customToast = new CustomToast(EditProfileActivity.this);
        api_response = response;
        String status = "";
        String message = "";
        if (result.toLowerCase().contains("failed")) {
            try {
                JSONObject json = new JSONObject(response);
                String s = json.getString("errordetails");
                JSONObject jsonObject = new JSONObject(s);
                status = jsonObject.getString("status");
                message = jsonObject.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            customToast.showCustomToast(status + ": " + message, Color.GREEN);
        } else {
            customToast.showCustomToast("Process: " + result, Color.GREEN);
            logInSuccessful();
        }
    }

    private void logInSuccessful() {
        parseJSONData(api_response);
    }

    private void parseJSONData(String api_response) {
        try {
            JSONObject jsonObject = new JSONObject(api_response);
            first_name = jsonObject.getString("firstName");
            last_name = jsonObject.getString("lastName");
            username = jsonObject.getString("username");
            department = jsonObject.getString("department");
            story = jsonObject.getString("story");
            admin = jsonObject.getBoolean("admin");
            position = jsonObject.getString("position");
            pointsToAward = jsonObject.getInt("pointsToAward");
            imageBytes = jsonObject.getString("imageBytes");
            location = jsonObject.getString("position");
            password = jsonObject.getString("password");
            JSONArray jsonArray = jsonObject.getJSONArray("rewards");
            for (int i = 0; i < jsonArray.length(); i++) {
                RewardsContent rewardsContent = new RewardsContent();
                JSONObject object = jsonArray.getJSONObject(i);
                rewardsContent.setUsername(object.getString("username"));
                rewardsContent.setName(object.getString("name"));                   //full name
                rewardsContent.setDate(object.getString("date"));
                rewardsContent.setNotes(object.getString("notes"));                 // comments
                rewardsContent.setValue(object.getInt("value"));                    // reward value
                rewardsContentList.add(rewardsContent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setCurrentData();
    }

    private void setCurrentData() {
        editText_username.setText(username);
        editText_firstName.setText(first_name);
        editText_lastName.setText(last_name);
        editText_password.setText(password);
        editText_position.setText(position);
        admin_or_not.setChecked(admin);
        editText_department.setText(department);
        editText_story.setText(story);
        profilePicture.setImageBitmap(getDecodedProfileBitmap(imageBytes));
    }

    private Bitmap getDecodedProfileBitmap(String imageBytes) {
        byte[] decodedString = Base64.decode(imageBytes, Base64.DEFAULT);
        profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return profileImageBitmap;
    }

    public void sendResultPostUpdate(String result, String response) {
        Log.d(TAG, "sendResultPostUpdate: " + response);
        CustomToast customToast = new CustomToast(EditProfileActivity.this);
        String status = "";
        String message = "";
        if (result.toLowerCase().contains("failed")) {
            try {
                JSONObject json = new JSONObject(response);
                String s = json.getString("errordetails");
                JSONObject jsonObject = new JSONObject(s);
                status = jsonObject.getString("status");
                message = jsonObject.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            customToast.showCustomToast(status + " " + message, Color.RED);
        } else {
            customToast.showCustomToast("Process: " + result, Color.GREEN);
            new LoginAPIAsyncTask(EditProfileActivity.this, true).execute(currentUser, password);

        }
    }

    public void postUpdate() {
        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        intent.putExtra("response_data", api_response);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class)
                .putExtra("username",currentUser)
                .putExtra("password",password));
        Log.d(TAG, "onBackPressed: " + currentUser + password);
        finish();
    }
}
