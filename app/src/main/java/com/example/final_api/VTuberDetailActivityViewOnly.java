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
import com.example.final_api.model.VTuber;
import com.example.final_api.model.Video;

import java.util.ArrayList;
import java.util.List;

public class VTuberDetailActivityViewOnly extends AppCompatActivity {

    public static final String EXTRA_VTUBER = "vtuber_data";
    
    // UI Components
    private VideoAdapter recentVideosAdapter, upcomingStreamsAdapter, clipsAdapter, collabsAdapter;
    private RecyclerView recyclerVideos, recyclerUpcoming, recyclerClips, recyclerCollabs, recyclerLive;
    private ProgressBar progressVideos, progressUpcoming, progressClips, progressCollabs, progressLive;
    private TextView tvNoVideos, tvNoUpcoming, tvNoClips, tvNoCollabs, tvLiveStatus;
    private Button btnFavorite;

    // Data tracking variables (for demo purposes)
    private int recentVideosCount = 0;
    private int upcomingStreamsCount = 0;
    private int clipsCount = 0;
    private int collabsCount = 0;
    private boolean isLive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vtuber_detail);
        
        // Initialize all UI components
        initializeViews();
        
        // Get VTuber data from intent
        VTuber vtuber = (VTuber) getIntent().getSerializableExtra(EXTRA_VTUBER);
        
        if (vtuber == null) {
            finish();
            return;
        }
        
        // Populate UI with VTuber data
        populateVTuberInfo(vtuber);
        
        // Set up button listeners
        setupButtonListeners(vtuber);
        
        // Set up RecyclerViews
        setupRecyclerViews();
        
        // For demonstration - show loading first
        showLoadingStates();
        
        // For demonstration - you'll replace this with actual API calls
        simulateDataLoad(vtuber.getChannelId());
    }
    
    // Initialize all views from layout
    private void initializeViews() {
        // Header and basic info
        ImageButton btnBack = findViewById(R.id.btn_back);
        
        // RecyclerViews
        recyclerVideos = findViewById(R.id.recycler_videos);
        recyclerUpcoming = findViewById(R.id.recycler_upcoming);
        recyclerClips = findViewById(R.id.recycler_clips);
        recyclerCollabs = findViewById(R.id.recycler_collabs);
        recyclerLive = findViewById(R.id.recycler_live);

        // Progress indicators
        progressVideos = findViewById(R.id.progress_videos);
        progressUpcoming = findViewById(R.id.progress_upcoming);
        progressClips = findViewById(R.id.progress_clips);
        progressCollabs = findViewById(R.id.progress_collabs);
        progressLive = findViewById(R.id.progress_live);

        // Empty state views
        tvNoVideos = findViewById(R.id.tv_no_videos);
        tvNoUpcoming = findViewById(R.id.tv_no_upcoming);
        tvNoClips = findViewById(R.id.tv_no_clips);
        tvNoCollabs = findViewById(R.id.tv_no_collabs);
        tvLiveStatus = findViewById(R.id.tv_live_status);

        // Catalog button
        btnFavorite = findViewById(R.id.btn_favorite);
        
        // Back button
        btnBack.setOnClickListener(v -> finish());
    }
    
    // Populate UI with VTuber information
    private void populateVTuberInfo(VTuber vtuber) {
        // Find views
        ImageView imgBanner = findViewById(R.id.img_banner);
        TextView tvNameLarge = findViewById(R.id.tv_name_large);
        TextView tvChannelId = findViewById(R.id.tv_channel_id);
        TextView tvSubscriberCount = findViewById(R.id.tv_subscriber_count);
        TextView tvGeneration = findViewById(R.id.tv_generation);
        TextView tvDebutDate = findViewById(R.id.tv_debut_date);
        LinearLayout languagesContainer = findViewById(R.id.detail_languages_container);
        TextView tvDescription = findViewById(R.id.tv_description);
        
        // Set basic info
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
            // Fallback logic based on generation
            tvDebutDate.setText(guessDebutDate(generation));
        }
        
        // Set description
        if (vtuber.getDescription() != null && !vtuber.getDescription().isEmpty()) {
            tvDescription.setText(vtuber.getDescription());
        } else {
            tvDescription.setText("No description available.");
        }
        
        // Load banner image
        String imageUrl = vtuber.getBannerUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = vtuber.getThumbnailUrl();
        }
        
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_vtuber)
                .into(imgBanner);
                
        // Set up language tags
        languagesContainer.removeAllViews(); // Clear existing tags
        if (vtuber.getLanguages() != null && !vtuber.getLanguages().isEmpty()) {
            for (String language : vtuber.getLanguages()) {
                TextView languageTag = createLanguageTag(language);
                languagesContainer.addView(languageTag);
            }
        } else {
            // Add default languages based on name/generation
            addDefaultLanguageTags(languagesContainer, vtuber);
        }
    }
    
    // Set up button listeners
    private void setupButtonListeners(VTuber vtuber) {
        Button btnYouTube = findViewById(R.id.btn_youtube);
        Button btnTwitter = findViewById(R.id.btn_twitter);
        
        // YouTube button
        btnYouTube.setOnClickListener(v -> {
            String youtubeUrl = "https://www.youtube.com/channel/" + vtuber.getChannelId();
            openUrl(youtubeUrl);
        });
        
        // Twitter button
        btnTwitter.setOnClickListener(v -> {
            if (vtuber.getTwitterLink() != null && !vtuber.getTwitterLink().isEmpty()) {
                openUrl("https://twitter.com/" + vtuber.getTwitterLink());
            } else {
                // Try to search for them on Twitter
                openUrl("https://twitter.com/search?q=" + Uri.encode(vtuber.getName()));
            }
        });        // Favorite button
        btnFavorite.setOnClickListener(v -> {
            // Toggle the selected state to make the button stay red after clicking
            boolean isFavorite = !v.isSelected();
            v.setSelected(isFavorite);
            
            // Show a message indicating the VTuber has been added to favorites
            if (isFavorite) {
                android.widget.Toast.makeText(this, vtuber.getName() + " added to favorites!", 
                    android.widget.Toast.LENGTH_SHORT).show();
            } else {
                android.widget.Toast.makeText(this, vtuber.getName() + " removed from favorites!", 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Set up RecyclerViews
    private void setupRecyclerViews() {
        // Recent Videos
        recyclerVideos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recentVideosAdapter = new VideoAdapter(this);
        recyclerVideos.setAdapter(recentVideosAdapter);

        // Upcoming Streams
        recyclerUpcoming.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        upcomingStreamsAdapter = new VideoAdapter(this);
        recyclerUpcoming.setAdapter(upcomingStreamsAdapter);

        // Clips
        recyclerClips.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        clipsAdapter = new VideoAdapter(this);
        recyclerClips.setAdapter(clipsAdapter);

        // Collaborations
        recyclerCollabs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        collabsAdapter = new VideoAdapter(this);
        recyclerCollabs.setAdapter(collabsAdapter);

        // Live status
        recyclerLive.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        VideoAdapter liveAdapter = new VideoAdapter(this);
        recyclerLive.setAdapter(liveAdapter);
    }
    
    // Show loading indicators, hide recyclers and empty states
    private void showLoadingStates() {
        progressVideos.setVisibility(View.VISIBLE);
        progressUpcoming.setVisibility(View.VISIBLE);
        progressClips.setVisibility(View.VISIBLE);
        progressCollabs.setVisibility(View.VISIBLE);
        progressLive.setVisibility(View.VISIBLE);
        
        recyclerVideos.setVisibility(View.GONE);
        recyclerUpcoming.setVisibility(View.GONE);
        recyclerClips.setVisibility(View.GONE);
        recyclerCollabs.setVisibility(View.GONE);
        recyclerLive.setVisibility(View.GONE);
        
        tvNoVideos.setVisibility(View.GONE);
        tvNoUpcoming.setVisibility(View.GONE);
        tvNoClips.setVisibility(View.GONE);
        tvNoCollabs.setVisibility(View.GONE);
        tvLiveStatus.setVisibility(View.GONE);
    }
    
    // METHODS THAT WILL NEED TO BE REPLACED WITH YOUR ACTUAL API CALLS
    
    // Simulate data loading
    private void simulateDataLoad(String channelId) {
        // This method simulates what would happen with API calls
        // Replace these with your actual API calls
        
        // For the catalog dialog to have some data
        recentVideosCount = 5;
        upcomingStreamsCount = 2;
        clipsCount = 10;
        collabsCount = 3;
        isLive = false;
        
        // Example: Loading recent videos - replace with your API implementation
        new android.os.Handler().postDelayed(() -> {
            progressVideos.setVisibility(View.GONE);
            
            // Simulate some data
            List<Video> videos = createSampleVideos(channelId, 5);
            if (!videos.isEmpty()) {
                recentVideosAdapter.updateData(videos);
                recyclerVideos.setVisibility(View.VISIBLE);
            } else {
                tvNoVideos.setVisibility(View.VISIBLE);
            }
        }, 1500);
        
        // Example: Loading upcoming streams - replace with your API implementation
        new android.os.Handler().postDelayed(() -> {
            progressUpcoming.setVisibility(View.GONE);
            
            List<Video> upcoming = createSampleVideos(channelId, 2);
            if (!upcoming.isEmpty()) {
                upcomingStreamsAdapter.updateData(upcoming);
                recyclerUpcoming.setVisibility(View.VISIBLE);
            } else {
                tvNoUpcoming.setVisibility(View.VISIBLE);
            }
        }, 2000);
        
        // Example: Loading clips - replace with your API implementation
        new android.os.Handler().postDelayed(() -> {
            progressClips.setVisibility(View.GONE);
            
            List<Video> clips = createSampleVideos(channelId, 10);
            if (!clips.isEmpty()) {
                clipsAdapter.updateData(clips);
                recyclerClips.setVisibility(View.VISIBLE);
            } else {
                tvNoClips.setVisibility(View.VISIBLE);
            }
        }, 1800);
        
        // Example: Loading collaborations - replace with your API implementation
        new android.os.Handler().postDelayed(() -> {
            progressCollabs.setVisibility(View.GONE);
            
            List<Video> collabs = createSampleVideos(channelId, 3);
            if (!collabs.isEmpty()) {
                collabsAdapter.updateData(collabs);
                recyclerCollabs.setVisibility(View.VISIBLE);
            } else {
                tvNoCollabs.setVisibility(View.VISIBLE);
            }
        }, 2200);
        
        // Example: Checking live status - replace with your API implementation
        new android.os.Handler().postDelayed(() -> {
            progressLive.setVisibility(View.GONE);
            tvLiveStatus.setText("Currently offline");
            tvLiveStatus.setVisibility(View.VISIBLE);
        }, 1000);
    }
    
    // Create sample videos for demo purposes
    private List<Video> createSampleVideos(String channelId, int count) {
        List<Video> videos = new ArrayList<>();
        // NOTE: You'll replace this with actual API data
        // This is just a placeholder for the view structure
        
        // In your actual implementation, you'll get real videos from your API
        return videos;
    }
    
    // UTILITY METHODS (NO API CALLS)
    
    // Create a language tag view
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
    
    // Add default language tags based on VTuber info
    private void addDefaultLanguageTags(LinearLayout container, VTuber vtuber) {
        String subOrg = vtuber.getSuborg() != null ? vtuber.getSuborg().toLowerCase() : "";
        String name = vtuber.getName().toLowerCase();
        
        if (subOrg.contains("en") || name.contains("gura") || name.contains("fauna") || 
            name.contains("kronii") || name.contains("mumei") || name.contains("baelz")) {
            container.addView(createLanguageTag("English"));
        } else if (subOrg.contains("id") || name.contains("risu") || 
                  name.contains("moona") || name.contains("ollie")) {
            container.addView(createLanguageTag("English"));
            container.addView(createLanguageTag("Indonesian"));
        } else {
            container.addView(createLanguageTag("Japanese"));
        }
    }
    
    // Open a URL in the browser
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    
    // Format subscriber count to human-readable format
    private String formatSubscriberCount(long count) {
        if (count >= 1000000) {
            return String.format("%.1fM", count / 1000000.0);
        } else if (count >= 1000) {
            return String.format("%.1fK", count / 1000.0);
        } else {
            return String.valueOf(count);
        }
    }
    
    // Format generation name
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
    
    // Format debut date to readable format
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
    
    // Guess debut date based on generation
    private String guessDebutDate(String generation) {
        if (generation.contains("Myth")) {
            return "September 2020";
        } else if (generation.contains("Council")) {
            return "August 2021";
        } else {
            return "Unknown";
        }
    }
    
    // Show data catalog dialog
    private void showDataCatalog(String channelId) {
        StringBuilder catalogData = new StringBuilder();
        catalogData.append("DATA CATALOG FOR: ").append(channelId).append("\n\n");
        catalogData.append("✅ Recent Videos: ").append(recentVideosCount).append("\n");
        catalogData.append("📅 Upcoming Streams: ").append(upcomingStreamsCount).append("\n");
        catalogData.append("✂️ Clips: ").append(clipsCount).append("\n");
        catalogData.append("👥 Collaborations: ").append(collabsCount).append("\n");
        catalogData.append("🔴 Currently Live: ").append(isLive ? "Yes" : "No").append("\n\n");
        catalogData.append("API Key: [YOUR_API_KEY_HERE]");

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
