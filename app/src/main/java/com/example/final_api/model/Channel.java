package com.example.final_api.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Channel implements Serializable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("english_name")
    private String englishName;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("photo")
    private String photo;
    
    @SerializedName("org")
    private String org;
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    public String getType() {
        return type;
    }
    
    public String getPhoto() {
        return photo;
    }
    
    public String getOrg() {
        return org;
    }
    
    // Display name - returns english_name if available, otherwise name
    public String getDisplayName() {
        return englishName != null && !englishName.isEmpty() ? englishName : name;
    }
}
