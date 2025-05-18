package com.example.final_api.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.final_api.R;
import com.example.final_api.model.Video;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    
    private List<Video> videos;
    private Context context;
    
    public VideoAdapter(Context context) {
        this.context = context;
        this.videos = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Video video = videos.get(position);
        
        // Set title
        holder.tvTitle.setText(video.getTitle());
        
        // Set thumbnail
        Glide.with(context)
                .load(video.getThumbnail())
                .placeholder(R.drawable.placeholder_vtuber)
                .into(holder.imgThumbnail);
        
        // Set publish date
        String formattedDate = formatDate(video.getAvailableAt());
        holder.tvDate.setText(formattedDate);
        
        // Set duration
        holder.tvDuration.setText(video.getFormattedDuration());
        
        // Set status indicator
        if ("live".equals(video.getStatus())) {
            holder.tvLiveIndicator.setVisibility(View.VISIBLE);
            holder.tvLiveIndicator.setText("LIVE");
            holder.tvLiveIndicator.setBackgroundResource(R.drawable.bg_live_indicator);
        } else if ("upcoming".equals(video.getStatus())) {
            holder.tvLiveIndicator.setVisibility(View.VISIBLE);
            holder.tvLiveIndicator.setText("UPCOMING");
            holder.tvLiveIndicator.setBackgroundResource(R.drawable.bg_upcoming_indicator);
        } else {
            holder.tvLiveIndicator.setVisibility(View.GONE);
        }
        
        // Set click listener to open YouTube video
        holder.itemView.setOnClickListener(v -> {
            String videoUrl = "https://www.youtube.com/watch?v=" + video.getId();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            context.startActivity(intent);
        });
    }
    
    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Unknown date";
        }
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }
    
    @Override
    public int getItemCount() {
        return videos.size();
    }
    
    public void updateData(List<Video> newData) {
        this.videos.clear();
        if (newData != null) {
            this.videos.addAll(newData);
        }
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView tvTitle, tvDate, tvDuration, tvLiveIndicator;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvLiveIndicator = itemView.findViewById(R.id.tv_live_indicator);
        }
    }
}