package com.rj.rewards;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    TextView textView_first_last_name, textView_username, textView_location, textView_pointsAwarded, textView_department, textView_position, textView_pointsToBeAwarded, textView_story, textView_reward_history;
    ImageView profile_picture;
    RecyclerView recyclerView;
    ProgressBar progressBar;

    RewardCommentsAdapter mAdapter;
    String login_api_response;
    String first_name, last_name, username, department, story, position, location, imageBytes, pass;
    int pointsToAward;
    List<RewardsContent> rewardsContentList;

    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rewardsContentList = new ArrayList<>();

        profile_picture = findViewById(R.id.profile_Image);
        textView_first_last_name = findViewById(R.id.profile_FirstLast_Name);
        textView_username = findViewById(R.id.profile_userName);
        textView_location = findViewById(R.id.profile_location);
        textView_pointsAwarded = findViewById(R.id.profile_PointsAwarded);
        textView_department = findViewById(R.id.profile_textViewDepartment);
        textView_position = findViewById(R.id.profile_textViewPosition);
        textView_pointsToBeAwarded = findViewById(R.id.profile_pointToAward);
        textView_story = findViewById(R.id.profile_your_story_text);
        textView_reward_history = findViewById(R.id.profile_rewardHistory);
        recyclerView = findViewById(R.id.recyclerView_reward_comments_list);
        progressBar = findViewById(R.id.progressBar_profile);
        progressBar.setVisibility(View.INVISIBLE);

        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);

        if (getIntent().hasExtra("response_data")) {
            Log.d(TAG, "onCreate: got response data");
            login_api_response = getIntent().getStringExtra("response_data");
            parseJSONData(login_api_response);
        } else if (getIntent().hasExtra("username") && getIntent().hasExtra("password")) {
            String username = getIntent().getStringExtra("username");
            String password = getIntent().getStringExtra("password");
            new LoginAPIAsyncTask(ProfileActivity.this).execute(username, password);
        } else if (preferences != null) {
            String temp_user = preferences.getString("username", "");
            String pass = preferences.getString("password", "");
            new LoginAPIAsyncTask(ProfileActivity.this).execute(temp_user, pass);
        } else {
            somethingWentWrong();
        }

        saveData(username, pass);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setHasFixedSize(true);
        mAdapter = new RewardCommentsAdapter(rewardsContentList);
        recyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void saveData(String username, String pass) {
        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().apply();
        editor.putString("username", username);
        editor.putString("password", pass);
        editor.apply();
    }

    private int getPoints() {
        int sum = 0;
        for (RewardsContent content : rewardsContentList) {
            if (content != null && rewardsContentList.size() != 0) {
                sum += content.getValue();
            }
        }
        return sum;
    }

    private Bitmap getDecodedProfileBitmap(String imageBytes) {
        byte[] decodedString = Base64.decode(imageBytes, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    private void parseJSONData(String login_api_response) {
        try {
            JSONObject jsonObject = new JSONObject(login_api_response);
            first_name = jsonObject.getString("firstName");
            last_name = jsonObject.getString("lastName");
            username = jsonObject.getString("username");
            department = jsonObject.getString("department");
            story = jsonObject.getString("story");
            position = jsonObject.getString("position");
            pointsToAward = jsonObject.getInt("pointsToAward");
            imageBytes = jsonObject.getString("imageBytes");
            location = jsonObject.getString("location");
            pass = jsonObject.getString("password");
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
        mAdapter = new RewardCommentsAdapter(rewardsContentList);
        recyclerView.setAdapter(mAdapter);
        setData();
    }

    private void setData() {
        String names = last_name + ", " + first_name;
        textView_first_last_name.setText(names);
        String temp_username = "(" + username + ")";
        textView_username.setText(temp_username);
        textView_location.setText(location);
        textView_pointsAwarded.setText(Integer.toString(getPoints()));
        textView_department.setText(department);
        textView_position.setText(position);
        textView_pointsToBeAwarded.setText(Integer.toString(pointsToAward));
        textView_story.setText(story);
        textView_reward_history.setText("Reward History(" + mAdapter.getItemCount() + ")");
        profile_picture.setImageBitmap(getDecodedProfileBitmap(imageBytes));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_your_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editYourProfileMenu: {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("password", pass);
                Log.d(TAG, "onOptionsItemSelected: " + username + pass);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            break;
            case R.id.leaderboardMenu: {
                Intent intent = new Intent(ProfileActivity.this, LeaderboardActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("password", pass);
                intent.putExtra("pointsToAward", pointsToAward);
                intent.putExtra("firstName",first_name);
                intent.putExtra("lastName",last_name);
                Log.d(TAG, "onOptionsItemSelected: " + username + pass);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void sendResult(String result, String response) {
        CustomToast customToast = new CustomToast(ProfileActivity.this);
        login_api_response = response;
        String status = "";
        String message = "";
        if (result.toLowerCase().contains("failed")) {
            try {
                JSONObject json = new JSONObject(login_api_response);
                String s = json.getString("errordetails");
                JSONObject jsonObject = new JSONObject(s);
                status = jsonObject.getString("status");
                message = jsonObject.getString("message");
                if (message.toLowerCase().contains("validation")) {
                    somethingWentWrong();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            customToast.showCustomToast(status + " " + message, Color.RED);
        } else {
//            customToast.showCustomToast("Process: " + result, Color.GREEN);
            Log.d(TAG, "sendResult: " + login_api_response);
            logInSuccessful();
        }
    }

    private void somethingWentWrong() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setIcon(R.drawable.icon)
                .setMessage("Something Went Wrong")
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void logInSuccessful() {
        parseJSONData(login_api_response);
    }

}
