package com.rj.rewards;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    LeaderboardActivity leaderboardActivity;
    List<AllProfilesData> profilesData;
    String currentUser;

    private static final String TAG = "MyAdapter";

    public MyAdapter(LeaderboardActivity leaderboardActivity, List<AllProfilesData> profilesContent, String currentUser) {
        this.leaderboardActivity = leaderboardActivity;
        this.profilesData = profilesContent;
        this.currentUser = currentUser;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_recyclerview_leaderboard_item_layout, viewGroup, false);
        v.setOnClickListener(leaderboardActivity);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int i) {
        //TODO get leaderboard data, put data
        AllProfilesData data = profilesData.get(i);
        setData(holder, data, currentUser);
    }

    private void setData(MyViewHolder holder, AllProfilesData data, String currentUser) {
        if (data.getUsername().equals(currentUser)) {
            holder.name.setTextColor(Color.MAGENTA);
            holder.rewardPoints.setTextColor(Color.MAGENTA);
            holder.position.setTextColor(Color.MAGENTA);
        }
        String s = data.getLastName() + ", " + data.getFirstName();
        holder.name.setText(s);
        holder.position.setText(data.getPosition());
        holder.profile_Image.setImageBitmap(getImageBitmap(data.getImageByteEncoded()));
        holder.rewardPoints.setText(Integer.toString(getRewardPoints(data.rewardsContents)));
    }

    private int getRewardPoints(List<RewardsContent> rewardsContents) {
        int sum = 0;
        for (RewardsContent content : rewardsContents) {
            sum += content.getValue();
        }
        return sum;
    }

    private Bitmap getImageBitmap(String imageByteEncoded) {
        byte[] decodedString = Base64.decode(imageByteEncoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + profilesData.size());
        return profilesData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView position;
        public TextView name;
        public TextView rewardPoints;
        public ImageView profile_Image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            position = itemView.findViewById(R.id.leaderboard_position);
            name = itemView.findViewById(R.id.leaderboard_names);
            rewardPoints = itemView.findViewById(R.id.leaderboard_rewards_point);
            profile_Image = itemView.findViewById(R.id.leaderboard_profile_image);


        }
    }
}
