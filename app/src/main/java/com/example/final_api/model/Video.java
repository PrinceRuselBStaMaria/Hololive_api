package com.example.final_api.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Video implements Serializable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("type")
    private String type; // stream, clip
    
    @SerializedName("published_at")
    private String publishedAt;
    
    @SerializedName("available_at")
    private String availableAt;
    
    @SerializedName("duration")
    private int duration;
    
    @SerializedName("status")
    private String status; // live, upcoming, past
    
    @SerializedName("start_scheduled")
    private String startScheduled;
    
    @SerializedName("start_actual")
    private String startActual;
    
    @SerializedName("end_actual")
    private String endActual;
    
    @SerializedName("live_viewers")
    private int liveViewers;
    
    @SerializedName("thumbnail")
    private String thumbnail;
    
    @SerializedName("channel")
    private VTuber channel;
    
    @SerializedName("mentions")
    private List<VTuber> mentions;
    
    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getPublishedAt() { return publishedAt; }
    public String getAvailableAt() { return availableAt; }
    public int getDuration() { return duration; }
    public String getStatus() { return status; }
    public String getStartScheduled() { return startScheduled; }
    public String getStartActual() { return startActual; }
    public String getEndActual() { return endActual; }
    public int getLiveViewers() { return liveViewers; }
    public String getThumbnail() { return thumbnail; }
    public VTuber getChannel() { return channel; }
    public List<VTuber> getMentions() { return mentions; }
    
    // Format duration to readable format (HH:MM:SS)
    public String getFormattedDuration() {
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}