package com.example.final_api;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.final_api.adapter.LiveStreamAdapter;
import com.example.final_api.api.HolodexApi;

public class LiveStreamsActivity extends AppCompatActivity {

    private RecyclerView liveStreamsRecyclerView;
    private ProgressBar loadingProgressBar;
    private TextView emptyStateTextView;
    private LiveStreamAdapter liveStreamAdapter;
    private HolodexApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_streams);

        // Initialize views
        liveStreamsRecyclerView = findViewById(R.id.recycler_live_streams);
        loadingProgressBar = findViewById(R.id.progress_loading);
        emptyStateTextView = findViewById(R.id.text_empty_state);

        // Set up RecyclerView
        liveStreamsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        liveStreamAdapter = new LiveStreamAdapter(this);
        liveStreamsRecyclerView.setAdapter(liveStreamAdapter);

        // Initialize API service
        apiService = com.example.final_api.api.ApiClient.getClient().create(com.example.final_api.api.HolodexApi.class);

        // Load live streams
        fetchLiveStreams();
    }

    private void fetchLiveStreams() {
        // Show loading indicator
        loadingProgressBar.setVisibility(View.VISIBLE);
        liveStreamsRecyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.GONE);

        // Fetch all currently live streams and filter for Hololive
        apiService.getAllLiveStreams().enqueue(new retrofit2.Callback<java.util.List<com.example.final_api.model.LiveStream>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.example.final_api.model.LiveStream>> call, retrofit2.Response<java.util.List<com.example.final_api.model.LiveStream>> response) {
                loadingProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    java.util.List<com.example.final_api.model.LiveStream> hololiveStreams = new java.util.ArrayList<>();
                    for (com.example.final_api.model.LiveStream stream : response.body()) {
                        if (stream.getChannel() != null &&
                            ("Hololive".equalsIgnoreCase(stream.getChannel().getOrg()) ||
                             "hololive".equalsIgnoreCase(stream.getChannel().getOrg()))) {
                            hololiveStreams.add(stream);
                        }
                    }
                    if (!hololiveStreams.isEmpty()) {
                        liveStreamAdapter.updateData(hololiveStreams);
                        liveStreamsRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        emptyStateTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    emptyStateTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.example.final_api.model.LiveStream>> call, Throwable t) {
                loadingProgressBar.setVisibility(View.GONE);
                emptyStateTextView.setVisibility(View.VISIBLE);
                android.widget.Toast.makeText(LiveStreamsActivity.this, "Error loading live streams: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}