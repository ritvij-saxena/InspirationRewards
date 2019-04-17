package com.rj.rewards;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
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

public class CreateProfileActivity extends AppCompatActivity {
    ImageView profilePicture;
    EditText username, pass, first_name, last_name, department, position, story;
    CheckBox admin_or_not;
    TextView char_num;
    ProgressBar progressBar;

    private LocationManager locationManager;
    Location currentLocation;
    private Criteria criteria;
    public static int MAX_CHARS = 360;
    private static final String TAG = "CreateProfileActivity";
    private int OPEN_CAMERA_GALLERY = 1;
    private int OPEN_CAMERA_CAPTURE = 2;
    private final static int ALL_PERMISSIONS = 100;
    String imageFileName;
    Bitmap bitmap;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (check_Permissions()) {
            Log.i(TAG, "onCreate: REQUESTS_OK");

        }

        profilePicture = findViewById(R.id.profile_image_edit);
        username = findViewById(R.id.editUserName_create_profile);
        pass = findViewById(R.id.editPassword_create_profile);
        admin_or_not = findViewById(R.id.adminuser_create_profile);
        first_name = findViewById(R.id.editFirstName_create_profile);
        last_name = findViewById(R.id.editLastName_create_profile);
        department = findViewById(R.id.editDepartment_create_profile);
        position = findViewById(R.id.editPosition_create_profile);
        story = findViewById(R.id.editStory_create_profile);
        char_num = findViewById(R.id.story_chars_edit_profile);
        progressBar = findViewById(R.id.progressBar_create_profile);


        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog();
            }
        });

        story.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_CHARS)});
        story.addTextChangedListener(new TextWatcher() {
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
        setLocation();
        Log.e(TAG, "onCreate: " + getPlace(currentLocation));

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
        builder.setTitle("Profile Picture");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveProfile:
                saveData();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        Log.d(TAG, "saveData:saved data ");
        if (checkFields()) {
            new CreateProfileAsyncTask(this).execute(
                    username.getText().toString(), //0
                    pass.getText().toString(), //1
                    admin_or_not.isChecked() ? "1" : "0", //2
                    first_name.getText().toString(), //3
                    last_name.getText().toString(), //4
                    department.getText().toString(), //5
                    position.getText().toString(), //6
                    story.getText().toString(), //7
                    getPlace(currentLocation), //8
                    getEncodedImage(bitmap) //9
            );
        } else {
            new CustomToast(CreateProfileActivity.this).showCustomToast("Something Went Wrong", Color.RED);
        }
    }

    private String getEncodedImage(Bitmap bitmap) {
        if (bitmap == null) {
            return "";
        }
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArray);
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
        if (story.getText().toString().isEmpty() || story.getText().toString().length() > 360) {
            story.setError("Please Enter Story");
            return false;
        } else {
            return true;
        }
    }

    private boolean isPositionValid() {
        if (position.getText().toString().isEmpty()) {
            position.setError("Please Enter Position");
            return false;
        } else {
            return true;
        }
    }

    private boolean isDepartmentValid() {
        if (department.getText().toString().isEmpty()) {
            department.setError("Please Enter Department Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean isLastNameValid() {
        if (last_name.getText().toString().isEmpty()) {
            last_name.setError("Please Enter Last Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean isFirstNameValid() {
        if (first_name.getText().toString().isEmpty()) {
            first_name.setError("Please Enter First Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean isPasswordValid() {
        if (pass.getText().toString().isEmpty()) {
            pass.setError("Please Enter Valid Password");
            return false;
        } else {
            return true;
        }
    }

    private boolean isUserNameValid() {
        if (username.getText().toString().isEmpty()) {
            username.setError("Please Enter Valid UserName");
            return false;
        } else {
            return true;
        }
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean check_Permissions() {
        int Camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int WriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int ReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int GPS_FINE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int GPS_COARSE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int INTERNET = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
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

        if (INTERNET != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.INTERNET);
        }

        if (!permissionRequired.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionRequired.toArray(new String[permissionRequired.size()]), ALL_PERMISSIONS);
            return false;
        }

        return true;
    }

    public void sendResult(String result, String response) {
        Log.d(TAG, "sendResult: " + response);
        String status = "";
        String message = "";
        CustomToast customToast = new CustomToast(CreateProfileActivity.this);
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
            finish();
        }
    }
}

