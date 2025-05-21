package com.example.final_api;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.final_api.adapter.LiveStreamAdapter;
import com.example.final_api.api.ApiClient;
import com.example.final_api.api.HolodexApi;
import com.example.final_api.model.LiveStream;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnSearch;
    private BottomNavigationView bottomNavigationView;
    
    // Live streams UI elements
    private RecyclerView liveStreamsRecyclerView;
    private LiveStreamAdapter liveStreamAdapter;
    private ProgressBar progressBarLive;
    private TextView tvNoLiveStreams;
    private HolodexApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate started");
        try {
            setContentView(R.layout.activity_main);
            Log.d("MainActivity", "setContentView completed");
            
            // Initialize UI elements
            btnSearch = findViewById(R.id.btn_search);
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            liveStreamsRecyclerView = findViewById(R.id.recycler_live_streams);
            progressBarLive = findViewById(R.id.progress_live_streams);
            tvNoLiveStreams = findViewById(R.id.tv_no_live_streams);
            Log.d("MainActivity", "Views initialized");
            
            // Initialize API service
            apiService = ApiClient.getClient().create(HolodexApi.class);
            
            // Set up RecyclerView for live streams
            liveStreamsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            liveStreamAdapter = new LiveStreamAdapter(this);
            liveStreamsRecyclerView.setAdapter(liveStreamAdapter);
            
            // Load live streams
            fetchLiveStreams();
            
            // Set click listener for search button
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to Search Activity where users can search for Hololive members using Holodex API
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(intent);
                }
            });
            
            // Set up bottom navigation
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    
                    if (id == R.id.nav_home) {
                        // Already on home screen
                        return true;
                    } else if (id == R.id.nav_live) {
                        // Navigate to Live Streams
                        Intent liveIntent = new Intent(MainActivity.this, LiveStreamsActivity.class);
                        startActivity(liveIntent);
                        return true;
                    } else if (id == R.id.nav_favorites) {
                        // Navigate to Favorites
                        Intent favIntent = new Intent(MainActivity.this, FavoritesActivity.class);
                        startActivity(favIntent);
                        return true;
                    } else if (id == R.id.nav_profile) {
                        // Show logout confirmation
                        handleLogout();
                        return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void fetchLiveStreams() {
        // Show loading state
        progressBarLive.setVisibility(View.VISIBLE);
        liveStreamsRecyclerView.setVisibility(View.GONE);
        tvNoLiveStreams.setVisibility(View.GONE);
        
        // Call the API to get live streams
        apiService.getLiveStreamsByOrg()
                .enqueue(new Callback<List<LiveStream>>() {
                    @Override
                    public void onResponse(Call<List<LiveStream>> call, Response<List<LiveStream>> response) {
                        progressBarLive.setVisibility(View.GONE);
                        
                        // Log detailed response information
                        Log.d("MainActivity", "API Response Code: " + response.code());
                        Log.d("MainActivity", "API Response Message: " + response.message());
                        Log.d("MainActivity", "API Request URL: " + call.request().url());
                        
                        if (response.isSuccessful()) {
                            Log.d("MainActivity", "API Response is successful");
                            if (response.body() != null) {
                                List<LiveStream> liveStreams = response.body();
                                Log.d("MainActivity", "API Response Body size: " + liveStreams.size());
                                
                                if (!liveStreams.isEmpty()) {
                                    // Debug first stream to verify data
                                    LiveStream firstStream = liveStreams.get(0);
                                    Log.d("MainActivity", "First stream title: " + firstStream.getTitle());
                                    Log.d("MainActivity", "First stream ID: " + firstStream.getId());
                                    Log.d("MainActivity", "First stream thumbnail: " + firstStream.getThumbnail());
                                    
                                    if (firstStream.getChannel() != null) {
                                        Log.d("MainActivity", "First stream channel name: " + 
                                                firstStream.getChannel().getDisplayName());
                                    } else {
                                        Log.d("MainActivity", "First stream channel is null");
                                    }
                                    
                                    // Update UI with live streams
                                    liveStreamAdapter.updateData(liveStreams);
                                    liveStreamsRecyclerView.setVisibility(View.VISIBLE);
                                    Log.d("MainActivity", "Loaded " + liveStreams.size() + " live streams");
                                } else {
                                    Log.d("MainActivity", "API response body is empty list");
                                    tvNoLiveStreams.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Log.d("MainActivity", "API response body is null");
                                tvNoLiveStreams.setVisibility(View.VISIBLE);
                            }
                        } else {
                            // Show no streams message
                            tvNoLiveStreams.setVisibility(View.VISIBLE);
                            Log.d("MainActivity", "No live streams found - unsuccessful response");
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<LiveStream>> call, Throwable t) {
                        // Handle error
                        progressBarLive.setVisibility(View.GONE);
                        tvNoLiveStreams.setVisibility(View.VISIBLE);
                        
                        Log.e("MainActivity", "Error fetching live streams", t);
                        Log.e("MainActivity", "Request URL that failed: " + call.request().url());
                        Log.e("MainActivity", "Error message: " + t.getMessage());
                        
                        if (t.getCause() != null) {
                            Log.e("MainActivity", "Error cause: " + t.getCause().getMessage());
                        }
                        
                        Toast.makeText(MainActivity.this, 
                                "Error loading live streams: " + t.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    /**
     * Handles the logout process by showing a confirmation dialog
     */
    private void handleLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout")
               .setMessage("Are you sure you want to logout?")
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       // Handle logout logic - clear user session/tokens
                       // UserSession.getInstance().clearSession(); // You'd implement this class
                       
                       // Return to login screen
                       Intent intent = new Intent(MainActivity.this, Login.class);
                       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);
                       finish();
                       
                       Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                   }
               })
               .setNegativeButton("No", null)
               .show();
    }
}