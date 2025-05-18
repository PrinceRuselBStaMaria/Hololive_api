package com.example.final_api;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LiveStreamsActivity extends AppCompatActivity {

    private RecyclerView liveStreamsRecyclerView;
    private ProgressBar loadingProgressBar;
    private TextView emptyStateTextView;

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
        
        // Load live streams
        fetchLiveStreams();
    }

    private void fetchLiveStreams() {
        // Show loading indicator
        loadingProgressBar.setVisibility(View.VISIBLE);
        liveStreamsRecyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.GONE);
        
        // TODO: Implement Holodex API call to fetch live streams
        // This would involve:
        // 1. Making a network request to the Holodex API endpoint for live streams
        // 2. Parsing the response into LiveStream objects
        // 3. Setting up a RecyclerView adapter to display the streams
        
        // For now, just show a message
        Toast.makeText(this, "Fetching live streams (API integration pending)", Toast.LENGTH_SHORT).show();
        loadingProgressBar.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
    }
}