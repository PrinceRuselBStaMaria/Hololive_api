package com.example.final_api;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView favoritesRecyclerView;
    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize views
        favoritesRecyclerView = findViewById(R.id.recycler_favorites);
        emptyStateTextView = findViewById(R.id.text_empty_state);

        // Set up RecyclerView
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // TODO: Load favorites from local storage or database
        // For now, just show the empty state
        showEmptyState();
    }

    private void showEmptyState() {
        favoritesRecyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
    }
}