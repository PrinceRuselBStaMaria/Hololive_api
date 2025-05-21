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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.example.final_api.R;
import com.example.final_api.model.LiveStream;
import android.util.Log;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class LiveStreamAdapter extends RecyclerView.Adapter<LiveStreamAdapter.ViewHolder> {
    
    private List<LiveStream> liveStreams;
    private Context context;
    
    public LiveStreamAdapter(Context context) {
        this.context = context;
        this.liveStreams = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_live_stream, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            LiveStream stream = liveStreams.get(position);
            Log.d("LiveStreamAdapter", "Binding stream at position " + position + ": " + stream.getId());
            
            // Set stream title
            if (stream.getTitle() != null) {
                holder.tvTitle.setText(stream.getTitle());
            } else {
                Log.w("LiveStreamAdapter", "Stream title is null for position: " + position);
                holder.tvTitle.setText("No title available");
            }
            
            // Set channel name
            if (stream.getChannel() != null) {
                Log.d("LiveStreamAdapter", "Channel info: " + stream.getChannel().getId() + " - " + stream.getChannel().getName());
                holder.tvChannelName.setText(stream.getChannel().getDisplayName());
                
                // Load channel thumbnail with Glide
                if (stream.getChannel().getPhoto() != null && !stream.getChannel().getPhoto().isEmpty()) {
                    Log.d("LiveStreamAdapter", "Loading channel photo: " + stream.getChannel().getPhoto());
                    Glide.with(context)
                            .load(stream.getChannel().getPhoto())
                            .circleCrop()
                            .placeholder(R.drawable.placeholder_vtuber)
                            .into(holder.imgChannel);
                } else {
                    Log.w("LiveStreamAdapter", "Channel photo is null or empty");
                }
            } else {
                Log.w("LiveStreamAdapter", "Channel is null for stream: " + stream.getId());
            }
            
            // Set viewer count
            if (stream.getLiveViewers() > 0) {
                String viewerCount = NumberFormat.getNumberInstance().format(stream.getLiveViewers());
                holder.tvViewerCount.setText(viewerCount + " watching");
                holder.tvViewerCount.setVisibility(View.VISIBLE);
            } else {
                holder.tvViewerCount.setVisibility(View.GONE);
            }
            
            // Load stream thumbnail with Glide
            if (stream.getThumbnail() != null && !stream.getThumbnail().isEmpty()) {
                Log.d("LiveStreamAdapter", "Loading stream thumbnail: " + stream.getThumbnail());
                Glide.with(context)
                        .load(stream.getThumbnail())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_thumbnail)
                        .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                Log.e("LiveStreamAdapter", "Failed to load thumbnail: " + model, e);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                Log.d("LiveStreamAdapter", "Thumbnail loaded successfully: " + model);
                                return false;
                            }
                        })
                        .into(holder.imgThumbnail);
            } else {
                Log.w("LiveStreamAdapter", "Stream thumbnail is null or empty for stream: " + stream.getId());
            }
            
            // Set click listener to open YouTube
            holder.cardView.setOnClickListener(v -> {
                String videoId = stream.getId();
                if (videoId != null && !videoId.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://www.youtube.com/watch?v=" + videoId));
                    context.startActivity(intent);
                }
            });
        } catch (Exception e) {
            Log.e("LiveStreamAdapter", "Error binding view at position " + position, e);
            // Set fallback UI
            holder.tvTitle.setText("Error displaying stream");
            holder.tvChannelName.setText("Data error");
            holder.tvViewerCount.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return liveStreams.size();
    }
    
    public void updateData(List<LiveStream> newData) {
        this.liveStreams.clear();
        if (newData != null) {
            this.liveStreams.addAll(newData);
        }
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imgThumbnail;
        ImageView imgChannel;
        TextView tvTitle;
        TextView tvChannelName;
        TextView tvViewerCount;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
            imgChannel = itemView.findViewById(R.id.img_channel);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvChannelName = itemView.findViewById(R.id.tv_channel_name);
            tvViewerCount = itemView.findViewById(R.id.tv_viewer_count);
        }
    }
}
