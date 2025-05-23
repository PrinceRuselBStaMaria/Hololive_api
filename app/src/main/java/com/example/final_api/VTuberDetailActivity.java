package com.example.final_api;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.final_api.adapter.VideoAdapter;
import com.example.final_api.api.ApiClient;
import com.example.final_api.api.HolodexApi;
import com.example.final_api.model.Video;
import com.example.final_api.model.VTuber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VTuberDetailActivity extends AppCompatActivity {    public static final String EXTRA_VTUBER = "vtuber_data";
    
    private HolodexApi apiService;
    private VideoAdapter recentVideosAdapter, upcomingStreamsAdapter, clipsAdapter, collabsAdapter;
    private RecyclerView recyclerVideos, recyclerUpcoming, recyclerClips, recyclerCollabs, recyclerLive;
    private ProgressBar progressVideos, progressUpcoming, progressClips, progressCollabs, progressLive;
    private TextView tvNoVideos, tvNoUpcoming, tvNoClips, tvNoCollabs, tvLiveStatus;
    private ImageButton btnFavorite;
    private int recentVideosCount = 0;
    private int upcomingStreamsCount = 0;
    private int clipsCount = 0;
    private int collabsCount = 0;
    private boolean isLive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vtuber_detail);
        
        // Get views
        ImageView imgBanner = findViewById(R.id.img_banner);
        TextView tvNameLarge = findViewById(R.id.tv_name_large);
        TextView tvChannelId = findViewById(R.id.tv_channel_id);
        TextView tvSubscriberCount = findViewById(R.id.tv_subscriber_count);
        TextView tvGeneration = findViewById(R.id.tv_generation);
        TextView tvDebutDate = findViewById(R.id.tv_debut_date);
        LinearLayout languagesContainer = findViewById(R.id.detail_languages_container);
        TextView tvDescription = findViewById(R.id.tv_description);
        Button btnYouTube = findViewById(R.id.btn_youtube);
        Button btnTwitter = findViewById(R.id.btn_twitter);
        ImageButton btnBack = findViewById(R.id.btn_back);
        
        // Get VTuber data from intent
        VTuber vtuber = (VTuber) getIntent().getSerializableExtra(EXTRA_VTUBER);
        
        if (vtuber == null) {
            finish();
            return;
        }
        
        // Initialize API service
        apiService = ApiClient.getClient().create(HolodexApi.class);
        
        // Set initial data to views with the basic information we have
        displayBasicVTuberInfo(vtuber);
        
        // Fetch detailed VTuber information from the specific channel endpoint
        fetchDetailedVTuberInfo(vtuber);
        
        // Set up RecyclerViews
        // Recent Videos
        recyclerVideos = findViewById(R.id.recycler_videos);
        recyclerVideos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recentVideosAdapter = new VideoAdapter(this);
        recyclerVideos.setAdapter(recentVideosAdapter);

        // Upcoming Streams
        recyclerUpcoming = findViewById(R.id.recycler_upcoming);
        recyclerUpcoming.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        upcomingStreamsAdapter = new VideoAdapter(this);
        recyclerUpcoming.setAdapter(upcomingStreamsAdapter);

        // Clips
        recyclerClips = findViewById(R.id.recycler_clips);
        recyclerClips.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        clipsAdapter = new VideoAdapter(this);
        recyclerClips.setAdapter(clipsAdapter);

        // Collaborations
        recyclerCollabs = findViewById(R.id.recycler_collabs);
        recyclerCollabs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        collabsAdapter = new VideoAdapter(this);
        recyclerCollabs.setAdapter(collabsAdapter);

        // Live status
        recyclerLive = findViewById(R.id.recycler_live);
        recyclerLive.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        VideoAdapter liveAdapter = new VideoAdapter(this);
        recyclerLive.setAdapter(liveAdapter);

        // Progress and empty views
        progressVideos = findViewById(R.id.progress_videos);
        progressUpcoming = findViewById(R.id.progress_upcoming);
        progressClips = findViewById(R.id.progress_clips);
        progressCollabs = findViewById(R.id.progress_collabs);
        progressLive = findViewById(R.id.progress_live);

        tvNoVideos = findViewById(R.id.tv_no_videos);
        tvNoUpcoming = findViewById(R.id.tv_no_upcoming);        tvNoClips = findViewById(R.id.tv_no_clips);
        tvNoCollabs = findViewById(R.id.tv_no_collabs);
        tvLiveStatus = findViewById(R.id.tv_live_status);        // Get the favorite button from the banner area
        btnFavorite = findViewById(R.id.btn_favorite);
        // Set the click listener for the favorite button
        btnFavorite.setOnClickListener(v -> {
            // Toggle the selected state to make the button stay red after clicking
            boolean isFavorite = !v.isSelected();
            v.setSelected(isFavorite);
            
            // If adding to favorites, save to Firebase
            if (isFavorite) {
                // Show a message indicating the VTuber has been added to favorites
                FavoritesActivity.addVTuberToFavorites(this, vtuber);
            } else {
                // Show a message indicating the VTuber has been removed from favorites
                android.widget.Toast.makeText(this, vtuber.getName() + " removed from favorites!", 
                    android.widget.Toast.LENGTH_SHORT).show();
                // Note: for a complete implementation, we would need to remove from Firebase here
            }
        });
        
        // Load additional data
        loadRecentVideos(vtuber.getChannelId());
        loadUpcomingStreams(vtuber.getChannelId());
        loadClips(vtuber.getChannelId());
        loadCollaborations(vtuber.getChannelId());
        checkLiveStatus(vtuber.getChannelId());
    }
    
    /**
     * Displays basic VTuber information with the data we already have from the intent
     */
    private void displayBasicVTuberInfo(VTuber vtuber) {
        ImageView imgBanner = findViewById(R.id.img_banner);
        TextView tvNameLarge = findViewById(R.id.tv_name_large);
        TextView tvChannelId = findViewById(R.id.tv_channel_id);
        TextView tvSubscriberCount = findViewById(R.id.tv_subscriber_count);
        TextView tvGeneration = findViewById(R.id.tv_generation);
        TextView tvDebutDate = findViewById(R.id.tv_debut_date);
        LinearLayout languagesContainer = findViewById(R.id.detail_languages_container);
        TextView tvDescription = findViewById(R.id.tv_description);
        Button btnYouTube = findViewById(R.id.btn_youtube);
        Button btnTwitter = findViewById(R.id.btn_twitter);
        ImageButton btnBack = findViewById(R.id.btn_back);
        
        // Set basic data
        tvNameLarge.setText(vtuber.getName());
        tvChannelId.setText(vtuber.getChannelId());
        
        // Format and set subscriber count
        String subscriberCount = formatSubscriberCount(vtuber.getSubscriberCount());
        tvSubscriberCount.setText(subscriberCount);
        
        // Set generation
        String generation = formatGeneration(vtuber.getSuborg(), vtuber);
        tvGeneration.setText(generation);
        
        // Set debut date
        if (vtuber.getDebutDate() != null && !vtuber.getDebutDate().isEmpty()) {
            tvDebutDate.setText(formatDebutDate(vtuber.getDebutDate()));
        } else {
            if (generation.contains("Myth")) {
                tvDebutDate.setText("September 2020");
            } else if (generation.contains("Council")) {
                tvDebutDate.setText("August 2021");
            } else {
                tvDebutDate.setText("Unknown");
            }
        }
        
        // Set description
        if (vtuber.getDescription() != null && !vtuber.getDescription().isEmpty()) {
            tvDescription.setText(vtuber.getDescription());
        } else {
            tvDescription.setText("Loading description...");
        }
        
        // Load thumbnail or banner
        String imageUrl = vtuber.getBannerUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = vtuber.getThumbnailUrl();
        }
        
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_vtuber)
                .into(imgBanner);
                
        // Set up language tags
        languagesContainer.removeAllViews(); // Clear existing language tags
        if (vtuber.getLanguages() != null && !vtuber.getLanguages().isEmpty()) {
            for (String language : vtuber.getLanguages()) {
                TextView languageTag = createLanguageTag(language);
                languagesContainer.addView(languageTag);
            }
        } else {
            // Guess languages based on name/generation
            String subOrg = vtuber.getSuborg() != null ? vtuber.getSuborg().toLowerCase() : "";
            String name = vtuber.getName().toLowerCase();
            
            if (subOrg.contains("en") || name.contains("gura") || name.contains("fauna") || 
                name.contains("kronii") || name.contains("mumei") || name.contains("baelz")) {
                languagesContainer.addView(createLanguageTag("English"));
            } else if (subOrg.contains("id") || name.contains("risu") || 
                      name.contains("moona") || name.contains("ollie")) {
                languagesContainer.addView(createLanguageTag("English"));
                languagesContainer.addView(createLanguageTag("Indonesian"));
            } else {
                languagesContainer.addView(createLanguageTag("Japanese"));
            }
        }
        
        // Set up YouTube button
        btnYouTube.setOnClickListener(v -> {
            String youtubeUrl = "https://www.youtube.com/channel/" + vtuber.getChannelId();
            openUrl(youtubeUrl);
        });
        
        // Set up Twitter button
        btnTwitter.setOnClickListener(v -> {
            if (vtuber.getTwitterLink() != null && !vtuber.getTwitterLink().isEmpty()) {
                openUrl("https://twitter.com/" + vtuber.getTwitterLink());
            } else {
                // Try to search for them on Twitter
                openUrl("https://twitter.com/search?q=" + Uri.encode(vtuber.getName()));
            }
        });
        
        // Set up back button
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Fetches detailed VTuber information from the specific channel endpoint
     */    private void fetchDetailedVTuberInfo(VTuber basicVTuber) {
        // Null check to ensure we have a valid VTuber object
        if (basicVTuber == null || basicVTuber.getChannelId() == null || basicVTuber.getChannelId().isEmpty()) {
            android.util.Log.e("HolodexAPI", "Cannot fetch details: VTuber or channelId is null");
            return;
        }
        
        // Show loading for description
        TextView tvDescription = findViewById(R.id.tv_description);
        if (basicVTuber.getDescription() == null || basicVTuber.getDescription().isEmpty()) {
            tvDescription.setText("Loading description...");
        }
        
        // Check if apiService is initialized
        if (apiService == null) {
            apiService = ApiClient.getClient().create(HolodexApi.class);
        }
        
        // Log the request being made
        android.util.Log.d("HolodexAPI", "Fetching details for channel: " + basicVTuber.getChannelId());
        
        // Make API call to get detailed VTuber info
        Call<VTuber> call = apiService.getChannelDetails(basicVTuber.getChannelId());
        call.enqueue(new Callback<VTuber>() {
            @Override
            public void onResponse(Call<VTuber> call, Response<VTuber> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VTuber detailedVTuber = response.body();
                    
                    // Log the response we got
                    android.util.Log.d("HolodexAPI", "Got channel details: " + detailedVTuber.getName() +
                                      ", Description: " + (detailedVTuber.getDescription() != null ? 
                                      detailedVTuber.getDescription().substring(0, Math.min(50, detailedVTuber.getDescription().length())) + "..." : "null") +
                                      ", Banner: " + detailedVTuber.getBannerUrl() +
                                      ", Twitter: " + detailedVTuber.getTwitterLink() +
                                      ", Subscribers: " + detailedVTuber.getSubscriberCount() +
                                      ", Language: " + detailedVTuber.getLanguage());
                    
                    updateVTuberDetails(basicVTuber, detailedVTuber);
                } else {
                    // If we failed to get detailed info, just continue using the basic info
                    // Error message would be visible where we show "Loading description..." earlier
                    if (tvDescription.getText().toString().equals("Loading description...")) {
                        tvDescription.setText("Description not available.");
                    }
                    
                    // Log the failure
                    android.util.Log.e("HolodexAPI", "Failed to get channel details. Code: " + response.code() + ", Message: " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            android.util.Log.e("HolodexAPI", "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            android.util.Log.e("HolodexAPI", "Error reading error body: " + e.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<VTuber> call, Throwable t) {
                // Handle failure
                if (tvDescription.getText().toString().equals("Loading description...")) {
                    tvDescription.setText("Description not available.");
                }
                
                // Log the failure
                android.util.Log.e("HolodexAPI", "Failed to fetch channel details: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    /**
     * Updates the UI with more detailed VTuber information
     */    private void updateVTuberDetails(VTuber basicVTuber, VTuber detailedVTuber) {
        // Update our VTuber object with the new data
        if (detailedVTuber.getDescription() != null && !detailedVTuber.getDescription().isEmpty()) {
            basicVTuber.setDescription(detailedVTuber.getDescription());
            android.util.Log.d("HolodexAPI", "Updated description: " + detailedVTuber.getDescription());
        }
        
        if (detailedVTuber.getThumbnailUrl() != null && !detailedVTuber.getThumbnailUrl().isEmpty()) {
            basicVTuber.setThumbnailUrl(detailedVTuber.getThumbnailUrl());
            android.util.Log.d("HolodexAPI", "Updated thumbnail: " + detailedVTuber.getThumbnailUrl());
        }
        
        if (detailedVTuber.getBannerUrl() != null && !detailedVTuber.getBannerUrl().isEmpty()) {
            basicVTuber.setBannerUrl(detailedVTuber.getBannerUrl());
            android.util.Log.d("HolodexAPI", "Updated banner: " + detailedVTuber.getBannerUrl());
        }
        
        if (detailedVTuber.getTwitterLink() != null && !detailedVTuber.getTwitterLink().isEmpty()) {
            basicVTuber.setTwitterLink(detailedVTuber.getTwitterLink());
            android.util.Log.d("HolodexAPI", "Updated Twitter: " + detailedVTuber.getTwitterLink());
        }
          // Handle subscriber count safely
        try {
            long subscriberCount = detailedVTuber.getSubscriberCount();
            if (subscriberCount > 0) {
                basicVTuber.setSubscriberCount(subscriberCount);
                android.util.Log.d("HolodexAPI", "Updated subscriber count: " + subscriberCount);
            } else {
                String subCount = String.valueOf(detailedVTuber.getSubscriberCount());
                if (subCount != null && !subCount.isEmpty() && !subCount.equals("0")) {
                    subscriberCount = Long.parseLong(subCount);
                    basicVTuber.setSubscriberCount(subscriberCount);
                    android.util.Log.d("HolodexAPI", "Updated subscriber count from string: " + subscriberCount);
                }
            }
        } catch (NumberFormatException e) {
            android.util.Log.e("HolodexAPI", "Failed to parse subscriber count: " + e.getMessage());
        } catch (Exception e) {
            android.util.Log.e("HolodexAPI", "Error handling subscriber count: " + e.getMessage());
        }
        
        // If video_count is a string, parse it
        if (detailedVTuber.getVideoCount() > 0) {
            basicVTuber.setVideoCount(detailedVTuber.getVideoCount());
            android.util.Log.d("HolodexAPI", "Updated video count: " + detailedVTuber.getVideoCount());
        }
        
        if (detailedVTuber.getDebutDate() != null && !detailedVTuber.getDebutDate().isEmpty()) {
            basicVTuber.setDebutDate(detailedVTuber.getDebutDate());
            android.util.Log.d("HolodexAPI", "Updated debut date: " + detailedVTuber.getDebutDate());
        }
        
        // Handle the language field mapping to languages list
        if (detailedVTuber.getLanguage() != null && !detailedVTuber.getLanguage().isEmpty()) {
            // Set the language for this VTuber
            basicVTuber.setLanguage(detailedVTuber.getLanguage());
            
            // Convert single language to a list with one item
            List<String> languages = Arrays.asList(detailedVTuber.getLanguage());
            basicVTuber.setLanguages(languages);
            android.util.Log.d("HolodexAPI", "Updated language: " + detailedVTuber.getLanguage());
        }
        
        // Traditional list of languages if available
        if (detailedVTuber.getLanguages() != null && !detailedVTuber.getLanguages().isEmpty()) {
            basicVTuber.setLanguages(detailedVTuber.getLanguages());
            android.util.Log.d("HolodexAPI", "Updated languages: " + detailedVTuber.getLanguages());
        }
        
        // Update UI with the more detailed information
        updateUI(basicVTuber);
    }

    /**
     * Updates UI elements with detailed VTuber information
     */
    private void updateUI(VTuber vtuber) {
        ImageView imgBanner = findViewById(R.id.img_banner);
        TextView tvSubscriberCount = findViewById(R.id.tv_subscriber_count);
        TextView tvDebutDate = findViewById(R.id.tv_debut_date);
        LinearLayout languagesContainer = findViewById(R.id.detail_languages_container);
        TextView tvDescription = findViewById(R.id.tv_description);
        Button btnTwitter = findViewById(R.id.btn_twitter);
        
        // Update description
        if (vtuber.getDescription() != null && !vtuber.getDescription().isEmpty()) {
            tvDescription.setText(vtuber.getDescription());
        }
        
        // Update subscriber count
        String subscriberCount = formatSubscriberCount(vtuber.getSubscriberCount());
        tvSubscriberCount.setText(subscriberCount);
        
        // Update debut date if available
        if (vtuber.getDebutDate() != null && !vtuber.getDebutDate().isEmpty()) {
            tvDebutDate.setText(formatDebutDate(vtuber.getDebutDate()));
        }
        
        // Update banner/thumbnail if a better one is available
        String imageUrl = vtuber.getBannerUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_vtuber)
                    .into(imgBanner);
        }
          // Update language tags if needed
        if (vtuber.getLanguages() != null && !vtuber.getLanguages().isEmpty()) {
            languagesContainer.removeAllViews();
            for (String language : vtuber.getLanguages()) {
                if (language != null && !language.isEmpty()) {
                    TextView languageTag = createLanguageTag(language);
                    languagesContainer.addView(languageTag);
                }
            }
        }
        
        // Update Twitter button to use the correct link
        if (vtuber.getTwitterLink() != null && !vtuber.getTwitterLink().isEmpty()) {
            btnTwitter.setOnClickListener(v -> openUrl("https://twitter.com/" + vtuber.getTwitterLink()));
        }
    }
    
    private TextView createLanguageTag(String language) {
        TextView tag = new TextView(this);
        tag.setText(language);
        tag.setBackgroundResource(R.drawable.bg_language_tag);
        
        // Set different colors based on language
        int colorResId;
        switch (language.toLowerCase()) {
            case "english":
                colorResId = android.R.color.holo_blue_light;
                break;
            case "japanese":
                colorResId = android.R.color.holo_red_light;
                break;
            case "indonesian":
                colorResId = android.R.color.holo_orange_light;
                break;
            default:
                colorResId = android.R.color.holo_purple;
                break;
        }
        
        tag.getBackground().setTint(getResources().getColor(colorResId));
        tag.setTextColor(getResources().getColor(android.R.color.white));
        tag.setPadding(24, 12, 24, 12);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 16, 0);
        tag.setLayoutParams(params);
        
        return tag;
    }
    
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    
    // These helper methods are duplicated from VTuberAdapter - you might want to move them to a utility class
    private String formatSubscriberCount(long count) {
        if (count >= 1000000) {
            return String.format("%.1fM", count / 1000000.0);
        } else if (count >= 1000) {
            return String.format("%.1fK", count / 1000.0);
        } else {
            return String.valueOf(count);
        }
    }
    
    private String formatGeneration(String suborg, VTuber vtuber) {
        if (suborg == null || suborg.isEmpty()) {
            String name = vtuber.getName().toLowerCase();
            if (name.contains("council") || name.contains("irys") || 
                name.contains("fauna") || name.contains("kronii") || 
                name.contains("mumei") || name.contains("baelz") || 
                name.contains("sana")) {
                return "Hololive EN - Council";
            } else if (name.contains("myth") || name.contains("gura") || 
                      name.contains("amelia") || name.contains("kiara") || 
                      name.contains("ina") || name.contains("calliope")) {
                return "Hololive EN - Myth";
            } else if (name.contains("indonesia") || name.contains("risu") || 
                      name.contains("moona") || name.contains("iofi") || 
                      name.contains("ollie") || name.contains("anya")) {
                return "Hololive Indonesia";
            }
            return "Hololive";
        }
        
        String suborgLower = suborg.toLowerCase();
        if (suborgLower.contains("council") || suborgLower.contains("en-council")) {
            return "Hololive EN - Council";
        } else if (suborgLower.contains("myth") || suborgLower.contains("en-myth")) {
            return "Hololive EN - Myth";  
        } else if (suborgLower.contains("en")) {
            return "Hololive English";
        } else if (suborgLower.contains("jp")) {
            return "Hololive Japan";
        } else if (suborgLower.contains("id")) {
            return "Hololive Indonesia";
        } else if (suborgLower.contains("hololive-")) {
            return "Hololive " + suborg.replace("hololive-", "");
        }
        return suborg;
    }
    
    private String formatDebutDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return "Unknown";
        }
        
        try {
            if (rawDate.contains(" ")) {
                return rawDate;
            }
            
            if (rawDate.contains("-")) {
                String[] parts = rawDate.split("-");
                if (parts.length >= 3) {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    
                    String monthName = "";
                    switch (month) {
                        case 1: monthName = "January"; break;
                        case 2: monthName = "February"; break;
                        case 3: monthName = "March"; break;
                        case 4: monthName = "April"; break;
                        case 5: monthName = "May"; break;
                        case 6: monthName = "June"; break;
                        case 7: monthName = "July"; break;
                        case 8: monthName = "August"; break;
                        case 9: monthName = "September"; break;
                        case 10: monthName = "October"; break;
                        case 11: monthName = "November"; break;
                        case 12: monthName = "December"; break;
                    }
                    
                    return monthName + " " + day + ", " + year;
                }
            }
            
            return rawDate;
        } catch (Exception e) {
            return rawDate;
        }
    }
    
    private void loadRecentVideos(String channelId) {
        // Add null check for channelId
        if (channelId == null || channelId.isEmpty()) {
            if (tvNoVideos != null) tvNoVideos.setVisibility(View.VISIBLE);
            if (progressVideos != null) progressVideos.setVisibility(View.GONE);
            return;
        }
        
        progressVideos.setVisibility(View.VISIBLE);
        recyclerVideos.setVisibility(View.GONE);
        tvNoVideos.setVisibility(View.GONE);
        
        // Check if apiService is initialized
        if (apiService == null) {
            apiService = ApiClient.getClient().create(HolodexApi.class);
        }
        
        apiService.getRecentVideos(channelId, "stream", 10)
                .enqueue(new Callback<List<Video>>() {
                    @Override
                    public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                        progressVideos.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Video> videos = response.body();
                            recentVideosCount = videos.size(); // Track count
                            recentVideosAdapter.updateData(videos);
                            recyclerVideos.setVisibility(View.VISIBLE);
                        } else {
                            tvNoVideos.setVisibility(View.VISIBLE);
                            recentVideosCount = 0; // Reset count on failure
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Video>> call, Throwable t) {
                        progressVideos.setVisibility(View.GONE);
                        tvNoVideos.setVisibility(View.VISIBLE);
                        recentVideosCount = 0; // Reset count on failure
                    }
                });
    }

    private void loadUpcomingStreams(String channelId) {
        // Add null check for channelId
        if (channelId == null || channelId.isEmpty()) {
            if (tvNoUpcoming != null) tvNoUpcoming.setVisibility(View.VISIBLE);
            if (progressUpcoming != null) progressUpcoming.setVisibility(View.GONE);
            return;
        }
        
        progressUpcoming.setVisibility(View.VISIBLE);
        recyclerUpcoming.setVisibility(View.GONE);
        tvNoUpcoming.setVisibility(View.GONE);
        
        // Check if apiService is initialized
        if (apiService == null) {
            apiService = ApiClient.getClient().create(HolodexApi.class);
        }
        
        apiService.getUpcomingStreams(channelId, "upcoming", 5)
                .enqueue(new Callback<List<Video>>() {
                    @Override
                    public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                        progressUpcoming.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Video> videos = response.body();
                            upcomingStreamsCount = videos.size(); // Track count
                            upcomingStreamsAdapter.updateData(videos);
                            recyclerUpcoming.setVisibility(View.VISIBLE);
                        } else {
                            tvNoUpcoming.setVisibility(View.VISIBLE);
                            upcomingStreamsCount = 0;
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Video>> call, Throwable t) {
                        progressUpcoming.setVisibility(View.GONE);
                        tvNoUpcoming.setVisibility(View.VISIBLE);
                        upcomingStreamsCount = 0;
                    }
                });
    }

    private void loadClips(String channelId) {
        // Add null check for channelId
        if (channelId == null || channelId.isEmpty()) {
            if (tvNoClips != null) tvNoClips.setVisibility(View.VISIBLE);
            if (progressClips != null) progressClips.setVisibility(View.GONE);
            return;
        }
        
        progressClips.setVisibility(View.VISIBLE);
        recyclerClips.setVisibility(View.GONE);
        tvNoClips.setVisibility(View.GONE);
        
        // Check if apiService is initialized
        if (apiService == null) {
            apiService = ApiClient.getClient().create(HolodexApi.class);
        }
        
        apiService.getClips(channelId, 10)
                .enqueue(new Callback<List<Video>>() {
                    @Override
                    public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                        progressClips.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Video> videos = response.body();
                            clipsCount = videos.size(); // Track count
                            clipsAdapter.updateData(videos);
                            recyclerClips.setVisibility(View.VISIBLE);
                        } else {
                            tvNoClips.setVisibility(View.VISIBLE);
                            clipsCount = 0;
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Video>> call, Throwable t) {
                        progressClips.setVisibility(View.GONE);
                        tvNoClips.setVisibility(View.VISIBLE);
                        clipsCount = 0;
                    }
                });
    }

    private void loadCollaborations(String channelId) {
        // Add null check for channelId
        if (channelId == null || channelId.isEmpty()) {
            if (tvNoCollabs != null) tvNoCollabs.setVisibility(View.VISIBLE);
            if (progressCollabs != null) progressCollabs.setVisibility(View.GONE);
            return;
        }
        
        progressCollabs.setVisibility(View.VISIBLE);
        recyclerCollabs.setVisibility(View.GONE);
        tvNoCollabs.setVisibility(View.GONE);
        
        // Check if apiService is initialized
        if (apiService == null) {
            apiService = ApiClient.getClient().create(HolodexApi.class);
        }
        
        apiService.getCollaborations(channelId, "mentions", 10)
                .enqueue(new Callback<List<Video>>() {
                    @Override
                    public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                        progressCollabs.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Video> videos = response.body();
                            // Filter to only show videos with at least one mention
                            List<Video> collabs = new ArrayList<>();
                            for (Video video : videos) {
                                if (video.getMentions() != null && !video.getMentions().isEmpty()) {
                                    collabs.add(video);
                                }
                            }
                            
                            collabsCount = collabs.size(); // Track count
                            if (!collabs.isEmpty()) {
                                collabsAdapter.updateData(collabs);
                                recyclerCollabs.setVisibility(View.VISIBLE);
                            } else {
                                tvNoCollabs.setVisibility(View.VISIBLE);
                            }
                        } else {
                            tvNoCollabs.setVisibility(View.VISIBLE);
                            collabsCount = 0;
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Video>> call, Throwable t) {
                        progressCollabs.setVisibility(View.GONE);
                        tvNoCollabs.setVisibility(View.VISIBLE);
                        collabsCount = 0;
                    }
                });
    }

    private void checkLiveStatus(String channelId) {
        // Add null check for channelId
        if (channelId == null || channelId.isEmpty()) {
            if (tvLiveStatus != null) {
                tvLiveStatus.setText("Status unavailable");
                tvLiveStatus.setVisibility(View.VISIBLE);
            }
            if (progressLive != null) progressLive.setVisibility(View.GONE);
            isLive = false;
            return;
        }
        
        progressLive.setVisibility(View.VISIBLE);
        recyclerLive.setVisibility(View.GONE);
        tvLiveStatus.setVisibility(View.GONE);
        
        // Check if apiService is initialized
        if (apiService == null) {
            apiService = ApiClient.getClient().create(HolodexApi.class);
        }
        
        apiService.getLiveStreamsByChannel(channelId)
                .enqueue(new Callback<List<Video>>() {
                    @Override
                    public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                        progressLive.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Video> liveStreams = response.body();
                            isLive = !liveStreams.isEmpty(); // Track live status
                            if (!liveStreams.isEmpty()) {
                                VideoAdapter liveAdapter = new VideoAdapter(VTuberDetailActivity.this);
                                liveAdapter.updateData(liveStreams);
                                recyclerLive.setAdapter(liveAdapter);
                                recyclerLive.setVisibility(View.VISIBLE);
                            } else {
                                tvLiveStatus.setText("Currently offline");
                                tvLiveStatus.setVisibility(View.VISIBLE);
                            }
                        } else {
                            tvLiveStatus.setText("Currently offline");
                            tvLiveStatus.setVisibility(View.VISIBLE);
                            isLive = false;
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Video>> call, Throwable t) {
                        progressLive.setVisibility(View.GONE);
                        tvLiveStatus.setText("Unable to check live status");
                        tvLiveStatus.setVisibility(View.VISIBLE);
                        isLive = false;
                    }
                });
    }
    
    private void showDataCatalog(String channelId) {
        // Add null check for channelId
        if (channelId == null || channelId.isEmpty()) {
            channelId = "Unknown";
        }
        
        StringBuilder catalogData = new StringBuilder();
        catalogData.append("DATA CATALOG FOR: ").append(channelId).append("\n\n");
        catalogData.append("✅ Recent Videos: ").append(recentVideosCount).append("\n");
        catalogData.append("📅 Upcoming Streams: ").append(upcomingStreamsCount).append("\n");
        catalogData.append("✂️ Clips: ").append(clipsCount).append("\n");
        catalogData.append("👥 Collaborations: ").append(collabsCount).append("\n");
        catalogData.append("🔴 Currently Live: ").append(isLive ? "Yes" : "No").append("\n\n");
        catalogData.append("API Key Used: ").append(ApiClient.getApiKey());

        // Show dialog with catalog data
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Data Retrieval Catalog")
            .setMessage(catalogData.toString())
            .setPositiveButton("OK", null)
            .setNeutralButton("Log to Console", (dialog, which) -> {
                android.util.Log.d("DataCatalog", catalogData.toString());
            })
            .show();
    }
}