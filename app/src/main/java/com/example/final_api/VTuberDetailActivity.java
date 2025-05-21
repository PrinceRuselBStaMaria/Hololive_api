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
import com.example.final_api.adapter.VTuberAdapter;
import com.example.final_api.api.ApiClient;
import com.example.final_api.api.HolodexApi;
import com.example.final_api.model.Video;
import com.example.final_api.model.VTuber;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VTuberDetailActivity extends AppCompatActivity {

    public static final String EXTRA_VTUBER = "vtuber_data";
    
    private HolodexApi apiService;
    private VideoAdapter recentVideosAdapter, upcomingStreamsAdapter, clipsAdapter, collabsAdapter;
    private RecyclerView recyclerVideos, recyclerUpcoming, recyclerClips, recyclerCollabs, recyclerLive;
    private ProgressBar progressVideos, progressUpcoming, progressClips, progressCollabs, progressLive;
    private TextView tvNoVideos, tvNoUpcoming, tvNoClips, tvNoCollabs, tvLiveStatus;

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
        
        // Set data to views
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
            tvDescription.setText("No description available.");
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
        
        // Initialize API service
        apiService = ApiClient.getClient().create(HolodexApi.class);
        
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
        tvNoUpcoming = findViewById(R.id.tv_no_upcoming);
        tvNoClips = findViewById(R.id.tv_no_clips);
        tvNoCollabs = findViewById(R.id.tv_no_collabs);
        tvLiveStatus = findViewById(R.id.tv_live_status);

        // Load additional data
        loadRecentVideos(vtuber.getChannelId());
        loadUpcomingStreams(vtuber.getChannelId());
        loadClips(vtuber.getChannelId());
        loadCollaborations(vtuber.getChannelId());
        checkLiveStatus(vtuber.getChannelId());
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
        progressVideos.setVisibility(View.VISIBLE);
        recyclerVideos.setVisibility(View.GONE);
        tvNoVideos.setVisibility(View.GONE);
        
        apiService.getRecentVideos(channelId, "stream", 10)
                .enqueue(new Callback<List<Video>>() {
                    @Override
                    public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                        progressVideos.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Video> videos = response.body();
                            recentVideosAdapter.updateData(videos);
                            recyclerVideos.setVisibility(View.VISIBLE);
                        } else {
                            tvNoVideos.setVisibility(View.VISIBLE);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Video>> call, Throwable t) {
                        progressVideos.setVisibility(View.GONE);
                        tvNoVideos.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void loadUpcomingStreams(String channelId) {
        progressUpcoming.setVisibility(View.VISIBLE);
        recyclerUpcoming.setVisibility(View.GONE);
        tvNoUpcoming.setVisibility(View.GONE);
        
        apiService.getUpcomingStreams(channelId, "upcoming", 5)
                .enqueue(new Callback<List<Video>>() {
                    @Override
                    public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                        progressUpcoming.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Video> videos = response.body();
                            upcomingStreamsAdapter.updateData(videos);
                            recyclerUpcoming.setVisibility(View.VISIBLE);
                        } else {
                            tvNoUpcoming.setVisibility(View.VISIBLE);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Video>> call, Throwable t) {
                        progressUpcoming.setVisibility(View.GONE);
                        tvNoUpcoming.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void loadClips(String channelId) {
        progressClips.setVisibility(View.VISIBLE);
        recyclerClips.setVisibility(View.GONE);
        tvNoClips.setVisibility(View.GONE);
        
        apiService.getClips(channelId, 10)
                .enqueue(new Callback<List<Video>>() {
                    @Override
                    public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                        progressClips.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Video> videos = response.body();
                            clipsAdapter.updateData(videos);
                            recyclerClips.setVisibility(View.VISIBLE);
                        } else {
                            tvNoClips.setVisibility(View.VISIBLE);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Video>> call, Throwable t) {
                        progressClips.setVisibility(View.GONE);
                        tvNoClips.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void loadCollaborations(String channelId) {
        progressCollabs.setVisibility(View.VISIBLE);
        recyclerCollabs.setVisibility(View.GONE);
        tvNoCollabs.setVisibility(View.GONE);
        
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
                            
                            if (!collabs.isEmpty()) {
                                collabsAdapter.updateData(collabs);
                                recyclerCollabs.setVisibility(View.VISIBLE);
                            } else {
                                tvNoCollabs.setVisibility(View.VISIBLE);
                            }
                        } else {
                            tvNoCollabs.setVisibility(View.VISIBLE);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Video>> call, Throwable t) {
                        progressCollabs.setVisibility(View.GONE);
                        tvNoCollabs.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void checkLiveStatus(String channelId) {
        progressLive.setVisibility(View.VISIBLE);
        recyclerLive.setVisibility(View.GONE);
        tvLiveStatus.setVisibility(View.GONE);
        
        apiService.getLiveStreamsByChannel(channelId)
                .enqueue(new Callback<List<Video>>() {
                    @Override
                    public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                        progressLive.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Video> liveStreams = response.body();
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
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Video>> call, Throwable t) {
                        progressLive.setVisibility(View.GONE);
                        tvLiveStatus.setText("Unable to check live status");
                        tvLiveStatus.setVisibility(View.VISIBLE);
                    }
                });
    }
}