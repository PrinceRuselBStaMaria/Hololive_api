package com.example.final_api.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;
import java.util.Arrays;

public class VTuber implements Serializable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("english_name")
    private String englishName;
    
    // We're using the id field for channel_id because that's what the API returns
    private String channelId;
    
    @SerializedName("org")
    private String org;
    
    @SerializedName("suborg")
    private String suborg;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("photo")
    private String thumbnailUrl;
    
    @SerializedName("banner")
    private String bannerUrl;
    
    @SerializedName("twitter")
    private String twitterLink;
    
    @SerializedName("subscriber_count")
    private long subscriberCount;
    
    @SerializedName("video_count")
    private long videoCount;
    
    // The API uses published_at as the debut date
    @SerializedName("published_at")
    private String debutDate;
    
    // The API uses lang (single language) but we convert it to a list in getter
    @SerializedName("lang")
    private String language;
    
    private List<String> languages;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { 
        this.id = id;
        // Also set channelId to match id since that's what we expect in the UI
        this.channelId = id;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEnglishName() { return englishName; }
    public void setEnglishName(String englishName) { this.englishName = englishName; }
    
    public String getChannelId() { 
        // If channelId is null but id exists, use id
        if (channelId == null && id != null) {
            return id;
        }
        return channelId; 
    }
    public void setChannelId(String channelId) { this.channelId = channelId; }
    
    public String getOrg() { return org; }
    public void setOrg(String org) { this.org = org; }
    
    public String getSuborg() { return suborg; }
    public void setSuborg(String suborg) { this.suborg = suborg; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    
    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
    
    public String getTwitterLink() { return twitterLink; }
    public void setTwitterLink(String twitterLink) { this.twitterLink = twitterLink; }
    
    public long getSubscriberCount() { 
        // Handle string subscriber count from API
        if (subscriberCount == 0 && language != null) {
            try {
                return Long.parseLong(language);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return subscriberCount; 
    }
    public void setSubscriberCount(long subscriberCount) { this.subscriberCount = subscriberCount; }
    
    public long getVideoCount() { return videoCount; }
    public void setVideoCount(long videoCount) { this.videoCount = videoCount; }
    
    public String getDebutDate() { return debutDate; }
    public void setDebutDate(String debutDate) { this.debutDate = debutDate; }
    
    public List<String> getLanguages() { 
        // If languages list is null but we have a language string, convert it to a list
        if (languages == null && language != null && !language.isEmpty()) {
            languages = Arrays.asList(language);
        }
        return languages; 
    }
    public void setLanguages(List<String> languages) { this.languages = languages; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}