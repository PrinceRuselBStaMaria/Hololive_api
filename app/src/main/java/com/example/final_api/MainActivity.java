package com.example.final_api;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Button btnSearch;
    private BottomNavigationView bottomNavigationView;

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
            Log.d("MainActivity", "Views initialized");
            
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