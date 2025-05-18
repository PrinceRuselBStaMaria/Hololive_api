package com.example.final_api;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_api.adapter.VTuberAdapter;
import com.example.final_api.api.ApiClient;
import com.example.final_api.api.HolodexApi;
import com.example.final_api.model.VTuber;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText editSearch;
    private ImageButton btnSearch;
    private RecyclerView recyclerResults;
    private ProgressBar progressBar;
    private VTuberAdapter adapter;
    private HolodexApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize views
        editSearch = findViewById(R.id.edit_search);
        btnSearch = findViewById(R.id.btn_search);
        recyclerResults = findViewById(R.id.recycler_results);
        progressBar = findViewById(R.id.progress_bar); // Make sure this exists in your layout

        // Set up RecyclerView
        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VTuberAdapter(this, vtuber -> {
            // Launch detail activity when a VTuber is clicked
            Intent intent = new Intent(SearchActivity.this, VTuberDetailActivity.class);
            intent.putExtra(VTuberDetailActivity.EXTRA_VTUBER, vtuber);
            startActivity(intent);
        });
        recyclerResults.setAdapter(adapter);

        // Initialize API service
        apiService = ApiClient.getClient().create(HolodexApi.class);

        // Set click listener for search button
        btnSearch.setOnClickListener(v -> {
            String query = editSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                searchVtubers(query);
            } else {
                Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchVtubers(String query) {
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        recyclerResults.setVisibility(View.GONE);

        // First try to get all Hololive members, then filter client-side
        Call<List<VTuber>> call = apiService.getAllHololiveMembers(
            "Hololive",   // org - specific organization filter
            "vtuber",     // type - ensures we get VTuber channels only
            100           // limit - get up to 100 results to ensure we get everyone
        );

        call.enqueue(new Callback<List<VTuber>>() {
            @Override
            public void onResponse(Call<List<VTuber>> call, Response<List<VTuber>> response) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                recyclerResults.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    List<VTuber> allMembers = response.body();
                    
                    // Debug: Log what we got back
                    for (VTuber vtuber : allMembers) {
                        Log.d("HolodexAPI", "Member: " + vtuber.getName() + 
                              ", Suborg: " + vtuber.getSuborg() + 
                              ", Debut: " + vtuber.getDebutDate() + 
                              ", Languages: " + (vtuber.getLanguages() != null ? vtuber.getLanguages().toString() : "null"));
                    }
                    
                    // Client-side filtering with fuzzy matching
                    List<VTuber> filteredResults = new ArrayList<>();
                    String lowercaseQuery = query.toLowerCase();
                    
                    for (VTuber vtuber : allMembers) {
                        // Check if the query matches any part of name or English name
                        boolean nameMatch = vtuber.getName() != null && 
                            vtuber.getName().toLowerCase().contains(lowercaseQuery);
                        boolean englishNameMatch = vtuber.getEnglishName() != null && 
                            vtuber.getEnglishName().toLowerCase().contains(lowercaseQuery);
                        
                        // Special case for some common names that might be searched differently
                        boolean specialCaseMatch = false;
                        
                        // Example: Handle Fauna case
                        if (lowercaseQuery.equals("fauna") && 
                            (vtuber.getName() != null && vtuber.getName().toLowerCase().contains("ceres fauna"))) {
                            specialCaseMatch = true;
                        }
                        
                        // Add more cases as needed for other members
                        
                        if (nameMatch || englishNameMatch || specialCaseMatch) {
                            filteredResults.add(vtuber);
                        }
                    }

                    adapter.updateData(filteredResults);
                    
                    if (filteredResults.isEmpty()) {
                        Toast.makeText(SearchActivity.this, 
                            "No Hololive members found matching '" + query + "'", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SearchActivity.this,
                            "Error: " + response.code() + " " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<VTuber>> call, Throwable t) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                recyclerResults.setVisibility(View.VISIBLE);
                
                Toast.makeText(SearchActivity.this, 
                        "Failed to fetch data: " + t.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}