package com.example.final_api;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button btnSearch;
    private Button btnFavorites;
    private Button btnLiveStreams;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate started");
        try {
            setContentView(R.layout.activity_main);
            Log.d("MainActivity", "setContentView completed");
            
            // Initialize UI elements
            btnSearch = findViewById(R.id.btn_search);
            btnFavorites = findViewById(R.id.btn_favorites);
            btnLiveStreams = findViewById(R.id.btn_live_streams);
            btnLogout = findViewById(R.id.btn_logout);
            Log.d("MainActivity", "Views initialized");
            
            // Set click listeners
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to Search Activity where users can search for Hololive members using Holodex API
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(intent);
                }
            });

            btnFavorites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to Favorites Activity where users can view their saved favorites
                    Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                    startActivity(intent);
                }
            });
            
            btnLiveStreams.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to LiveStreamsActivity to show currently live Hololive streams from Holodex API
                    Intent intent = new Intent(MainActivity.this, LiveStreamsActivity.class);
                    startActivity(intent);
                }
            });

            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle logout logic - clear user session/tokens
                    // UserSession.getInstance().clearSession(); // You'd implement this class
                    
                    // Return to login screen
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    
                    Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}