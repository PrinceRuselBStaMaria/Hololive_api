package com.example.final_api;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class FavoritesActivity extends AppCompatActivity {

    private static final String TAG = "FavoritesActivity";
    private EditText editText;
    private Button submitButton;
    private Button debugButton;
    private Button retryConnectButton;
    private Button forceSaveButton;
    private TextView connectionStatus;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference favoritesRef;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        
        Log.d(TAG, "FavoritesActivity starting up");
        
        try {
            // Initialize Firebase with our configuration
            FirebaseConfig.initializeFirebase();
            
            // Initialize views
            editText = findViewById(R.id.editTextFavoriteName);
            submitButton = findViewById(R.id.btnAddFavorite);
            debugButton = findViewById(R.id.btnDebugFirebase);
            retryConnectButton = findViewById(R.id.btnRetryConnection);
            forceSaveButton = findViewById(R.id.btnForceWrite);
            connectionStatus = findViewById(R.id.tvConnectionStatus);
            
            // Initialize Firebase
            firebaseDatabase = FirebaseConfig.getDatabase();
            favoritesRef = firebaseDatabase.getReference("favorites");
            
            Log.d(TAG, "Firebase Database URL: " + firebaseDatabase.getReference().toString());
            
            // Setup connection monitoring
            checkFirebaseConnection();
            
            if (connectionStatus != null) {
                connectionStatus.setText("Checking connection...");
            }
            
            // Setup retry button
            if (retryConnectButton != null) {
                retryConnectButton.setOnClickListener(v -> handleRetryConnection());
            }
            
            // Setup debug button
            debugButton.setOnClickListener(v -> debugFirebase());
            
            // Setup force save button
            if (forceSaveButton != null) {
                forceSaveButton.setOnClickListener(v -> forceWriteToFirebase());
            }

            // Setup DAO
            Dataemp dao = new Dataemp();

            // Set click listener for the submit button
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String inputText = editText.getText().toString();
                    
                    Log.d(TAG, "Submit button clicked with text: " + inputText);
                    
                    if (!inputText.isEmpty()) {
                        // Check network connectivity first
                        if (!isNetworkAvailable()) {
                            showNetworkErrorDialog();
                            return;
                        }
                        
                        Log.d(TAG, "Creating Favorite object");
                        Favorite favorite = new Favorite(inputText);
                        
                        // Show a progress toast
                        Toast.makeText(FavoritesActivity.this, "Adding favorite...", Toast.LENGTH_SHORT).show();
                        
                        // Try a direct simple write first
                        Map<String, Object> testValues = new HashMap<>();
                        testValues.put("test_value", "Test at " + System.currentTimeMillis());
                        testValues.put("device", android.os.Build.MODEL);
                        
                        firebaseDatabase.getReference("test").setValue(testValues)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Test write successful");
                                
                                // Now try the actual write using our DAO
                                dao.add(favorite)
                                    .addOnSuccessListener(suc -> {
                                        Log.d(TAG, "DAO Add successful");
                                        Toast.makeText(FavoritesActivity.this, 
                                            "Favorite added successfully: " + inputText, Toast.LENGTH_SHORT).show();
                                        editText.setText(""); // Clear input field after successful addition
                                        
                                        // Update connection status
                                        if (connectionStatus != null) {
                                            connectionStatus.setText("Connected to Firebase");
                                            connectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                        }
                                    })
                                    .addOnFailureListener(err -> {
                                        Log.e(TAG, "DAO Add failed: " + err.getMessage(), err);
                                        Toast.makeText(FavoritesActivity.this, 
                                            "Failed to add: " + err.getMessage(), Toast.LENGTH_LONG).show();
                                        
                                        // Try direct write as fallback
                                        directWriteToFirebase(favorite);
                                    });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Test write failed: " + e.getMessage());
                                // Try direct write
                                directWriteToFirebase(favorite);
                            });
                    } else {
                        Toast.makeText(FavoritesActivity.this, "Please enter some text", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Direct write to Firebase, bypassing the DAO
     */
    private void directWriteToFirebase(Favorite favorite) {
        try {
            Log.d(TAG, "Attempting direct write to Firebase");
            Toast.makeText(this, "Trying direct write...", Toast.LENGTH_SHORT).show();
            
            // Write directly to "favorites" node
            String key = firebaseDatabase.getReference().child("direct_favorites").push().getKey();
            Map<String, Object> values = new HashMap<>();
            values.put("name", favorite.getName());
            values.put("timestamp", System.currentTimeMillis());
            
            firebaseDatabase.getReference().child("direct_favorites").child(key).setValue(values)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Direct write successful with key: " + key);
                    Toast.makeText(FavoritesActivity.this, 
                        "Direct write successful", Toast.LENGTH_SHORT).show();
                    editText.setText("");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Direct write failed: " + e.getMessage(), e);
                    Toast.makeText(FavoritesActivity.this, 
                        "Direct write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        } catch (Exception e) {
            Log.e(TAG, "Exception during direct write: " + e.getMessage(), e);
        }
    }
    
    /**
     * Emergency write to Firebase with minimal data
     */
    private void forceWriteToFirebase() {
        try {
            Log.d(TAG, "Forcing write to Firebase");
            Toast.makeText(this, "Attempting forced write...", Toast.LENGTH_SHORT).show();
            
            // Write directly to root with minimal data
            Map<String, Object> testData = new HashMap<>();
            testData.put("test_timestamp", System.currentTimeMillis());
            testData.put("device_model", android.os.Build.MODEL);
            
            firebaseDatabase.getReference().child("test_data").setValue(testData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Forced write successful");
                    Toast.makeText(FavoritesActivity.this, 
                        "Forced write successful", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Forced write failed: " + e.getMessage(), e);
                    Toast.makeText(FavoritesActivity.this, 
                        "Forced write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        } catch (Exception e) {
            Log.e(TAG, "Exception during forced write: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check connection whenever activity comes to foreground
        checkFirebaseConnection();
    }
    
    /**
     * Check if the device has internet connectivity
     * @return true if connected, false otherwise
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean connected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        
        Log.d(TAG, "Network connectivity: " + (connected ? "CONNECTED" : "DISCONNECTED"));
        return connected;
    }
    
    /**
     * Shows a dialog when there is no internet connection
     */
    private void showNetworkErrorDialog() {
        new AlertDialog.Builder(this)
            .setTitle("No Internet Connection")
            .setMessage("Please check your internet connection and try again.")
            .setPositiveButton("Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Shows a dialog when Firebase connection fails
     */
    private void showFirebaseConnectionErrorDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Firebase Connection Error")
            .setMessage("Cannot connect to Firebase. This could be due to internet connectivity issues or Firebase service problems. Would you like to retry?")
            .setPositiveButton("Retry", (dialog, which) -> handleRetryConnection())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Handles retry connection button click
     */
    private void handleRetryConnection() {
        if (!isNetworkAvailable()) {
            showNetworkErrorDialog();
            return;
        }
        
        Toast.makeText(this, "Attempting to reconnect to Firebase...", Toast.LENGTH_SHORT).show();
        
        // Test Firebase connection
        favoritesRef.child("_connection_test").setValue(System.currentTimeMillis())
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(FavoritesActivity.this, 
                    "Successfully reconnected to Firebase", Toast.LENGTH_SHORT).show();
                if (connectionStatus != null) {
                    connectionStatus.setText("Connected to Firebase");
                    connectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(FavoritesActivity.this, 
                    "Failed to reconnect: " + e.getMessage(), Toast.LENGTH_LONG).show();
                if (connectionStatus != null) {
                    connectionStatus.setText("Disconnected from Firebase");
                    connectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            });
    }
    
    /**
     * Checks Firebase connection status
     */
    private void checkFirebaseConnection() {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                Log.d(TAG, "Firebase connection state: " + (connected ? "CONNECTED" : "DISCONNECTED"));
                
                if (connectionStatus != null) {
                    connectionStatus.setText(connected ? "Connected to Firebase" : "Disconnected from Firebase");
                    connectionStatus.setTextColor(getResources().getColor(
                        connected ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
                }
                
                // If disconnected, show retry button
                if (retryConnectButton != null) {
                    retryConnectButton.setVisibility(connected ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase connection check failed: " + error.getMessage());
                if (connectionStatus != null) {
                    connectionStatus.setText("Connection check failed");
                    connectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
                
                // Show retry button if checking connection failed
                if (retryConnectButton != null) {
                    retryConnectButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    
    /**
     * Debug helper method to check Firebase connectivity and data
     */
    private void debugFirebase() {
        Log.d(TAG, "Running Firebase debug checks");
        Toast.makeText(this, "Checking Firebase connection... See logs", Toast.LENGTH_SHORT).show();
        
        // First check internet connectivity
        boolean networkAvailable = isNetworkAvailable();
        Toast.makeText(this, 
            "Internet connection: " + (networkAvailable ? "Available" : "Not available"), 
            Toast.LENGTH_SHORT).show();
        
        if (!networkAvailable) {
            showNetworkErrorDialog();
            return;
        }
        
        // Check if we can read from Firebase
        FirebaseDatabase.getInstance().getReference(".info/connected")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean connected = Boolean.TRUE.equals(dataSnapshot.getValue(Boolean.class));
                    Log.d(TAG, "Firebase connection state: " + (connected ? "CONNECTED" : "DISCONNECTED"));
                    Toast.makeText(FavoritesActivity.this, 
                        "Firebase is " + (connected ? "connected" : "disconnected"), 
                        Toast.LENGTH_SHORT).show();
                    
                    if (!connected) {
                        showFirebaseConnectionErrorDialog();
                        return;
                    }
                    
                    // Try to read the Favorites node
                    readFavoritesData();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Firebase connection check failed: " + databaseError.getMessage());
                    Toast.makeText(FavoritesActivity.this, 
                        "Failed to check connection: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    /**
     * Read data from Favorites node in Firebase
     */
    private void readFavoritesData() {
        FirebaseDatabase.getInstance().getReference("Favorites")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Favorites node exists: " + dataSnapshot.exists());
                    Log.d(TAG, "Favorites child count: " + dataSnapshot.getChildrenCount());
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("Found ").append(dataSnapshot.getChildrenCount()).append(" entries:\n");
                    
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        sb.append(" - Key: ").append(child.getKey());
                        Object value = child.getValue();
                        if (value != null) {
                            sb.append(", Value type: ").append(value.getClass().getSimpleName());
                        }
                        sb.append("\n");
                    }
                    
                    String message = sb.toString();
                    Log.d(TAG, message);
                    Toast.makeText(FavoritesActivity.this, message, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to read Favorites: " + databaseError.getMessage());
                    Toast.makeText(FavoritesActivity.this, 
                        "Failed to read data: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    // Model class
    public static class Favorite {
        private String name;
        
        public Favorite() {} // Required for Firebase
        
        public Favorite(String name) { 
            this.name = name; 
        }
        
        public String getName() { return name; }
        
        public void setName(String name) { this.name = name; }
        
        @Override
        public String toString() {
            return "Favorite{name='" + name + "'}";
        }
    }
}