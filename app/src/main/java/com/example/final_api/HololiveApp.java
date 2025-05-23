package com.example.final_api;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class HololiveApp extends Application {
    private static final String TAG = "HololiveApp";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application onCreate - initializing Firebase");
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            
            // Initialize Firebase with correct region URL before any database references are created
            // This must be done here, in the Application class, before any other Firebase operations
            FirebaseDatabase.getInstance("https://final-project-api-3d05d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .setPersistenceEnabled(true);
            
            Log.d(TAG, "Firebase initialization completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
        }
    }
}