package com.example.final_api.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class LiveStream implements Serializable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("topic_id")
    private String topicId;
    
    @SerializedName("published_at")
    private String publishedAt;
    
    @SerializedName("available_at")
    private String availableAt;
    
    @SerializedName("duration")
    private int duration;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("start_scheduled")
    private String startScheduled;
    
    @SerializedName("start_actual")
    private String startActual;
    
    @SerializedName("end_actual")
    private String endActual;
    
    @SerializedName("live_viewers")
    private int liveViewers;
    
    @SerializedName("channel")
    private Channel channel;
    
    @SerializedName("thumbnail")
    private String thumbnail;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getTopicId() {
        return topicId;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getAvailableAt() {
        return availableAt;
    }

    public int getDuration() {
        return duration;
    }

    public String getStatus() {
        return status;
    }

    public String getStartScheduled() {
        return startScheduled;
    }

    public String getStartActual() {
        return startActual;
    }

    public String getEndActual() {
        return endActual;
    }

    public int getLiveViewers() {
        return liveViewers;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
