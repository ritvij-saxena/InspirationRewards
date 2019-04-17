package com.rj.rewards;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RewardCommentsAdapter extends RecyclerView.Adapter<RewardCommentsAdapter.MyViewHolder>{
    private static final String TAG = "RewardCommentsAdapter";
    List<RewardsContent> rewardsContentList;
    public RewardCommentsAdapter(List<RewardsContent> rewardsContentList) {
        this.rewardsContentList = rewardsContentList;
    }

    @NonNull
    @Override
    public RewardCommentsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_recyclerview_reward_list_layout,viewGroup,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardCommentsAdapter.MyViewHolder myViewHolder, int i) {
        RewardsContent rewardsContent = rewardsContentList.get(i);
        setRewardContent(myViewHolder,rewardsContent);
    }

    private void setRewardContent(MyViewHolder holder, RewardsContent rewardsContent) {
        holder.first_last_names.setText(rewardsContent.getName());
        holder.date.setText(rewardsContent.getDate());
        holder.points.setText(Integer.toString(rewardsContent.getValue()));
        holder.comments.setText(rewardsContent.getNotes());
//        String reward_text = "Reward History: ("+Integer.toString(getItemCount())+")";
//        holder.rewardsNumber.setText(reward_text);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + rewardsContentList.size());
        return rewardsContentList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView date;
        public TextView first_last_names;
        public TextView points;
        public TextView comments;
        public TextView rewardsNumber;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.reward_date);
            first_last_names = itemView.findViewById(R.id.reward_name);
            points = itemView.findViewById(R.id.reward_points);
            comments = itemView.findViewById(R.id.reward_comment);
            rewardsNumber = itemView.findViewById(R.id.profile_rewardHistory);
        }
    }

}