package com.example.final_api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

/**
 * Configuration class for Firebase to enable persistence and other optimizations
 */
public class FirebaseConfig {
    private static final String TAG = "FirebaseConfig";
    private static boolean initialized = false;
    
    // Use the correct regional URL for your Firebase project (from logs)
    private static final String FIREBASE_URL = "https://final-project-api-3d05d-default-rtdb.asia-southeast1.firebasedatabase.app/";

    /**
     * Initialize Firebase with optimal settings for data persistence
     */
    public static void initializeFirebase() {
        if (initialized) {
            return;
        }

        try {
            // Get database instance with specific URL
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_URL);
            
            // Enable disk persistence (must be called before any other Firebase reference is created)
            firebaseDatabase.setPersistenceEnabled(true);
            
            // Set cache size to 100MB (default is 10MB)
            firebaseDatabase.setPersistenceCacheSizeBytes(100 * 1024 * 1024);
            
            // Set keep synced for favorites data
            firebaseDatabase.getReference("favorites").keepSynced(true);
            
            // Configure database logging
            firebaseDatabase.setLogLevel(com.google.firebase.database.Logger.Level.DEBUG);
            
            // Set the connection state to log connectivity issues
            DatabaseReference connectedRef = firebaseDatabase.getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    if (connected) {
                        Log.d(TAG, "Firebase connected");
                    } else {
                        Log.w(TAG, "Firebase disconnected");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Firebase connection listener was cancelled", error.toException());
                }
            });
                
            Log.d(TAG, "Firebase persistence enabled successfully");
            initialized = true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to enable Firebase persistence: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if the device has internet connectivity
     * @param context Application context
     * @return true if connected, false otherwise
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    /**
     * Gets a configured instance of FirebaseDatabase
     * @return A configured FirebaseDatabase instance
     */
    public static FirebaseDatabase getDatabase() {
        if (!initialized) {
            initializeFirebase();
        }
        return FirebaseDatabase.getInstance(FIREBASE_URL);
    }
}