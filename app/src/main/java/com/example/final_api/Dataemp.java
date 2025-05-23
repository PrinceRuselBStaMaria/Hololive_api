package com.example.final_api;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Dataemp {

    private static final String TAG = "Dataemp";
    private DatabaseReference reference;
    private FirebaseDatabase db;

    public Dataemp(){
        try {
            Log.d(TAG, "Initializing Firebase Database");
            // Use the configured database instance
            db = FirebaseConfig.getDatabase();
            Log.d(TAG, "Firebase Database instance obtained: " + (db != null ? "success" : "failure"));
            
            // Use a simpler path for the database reference
            reference = db.getReference("favorites");
            Log.d(TAG, "Database reference path: " + reference.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
        }
    }
    
    public Task<Void> add(FavoritesActivity.Favorite emp){
        try {
            Log.d(TAG, "Adding data to Firebase: " + emp.getName());
            if (reference == null) {
                Log.e(TAG, "Database reference is null, reinitializing");
                db = FirebaseConfig.getDatabase();
                reference = db.getReference("favorites");
            }
            
            DatabaseReference pushRef = reference.push();
            String key = pushRef.getKey();
            Log.d(TAG, "Generated key for new entry: " + key);
            
            // Log the full path for debugging
            Log.d(TAG, "Full database path for write: " + pushRef.toString());
            
            return pushRef.setValue(emp)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Data successfully added to Firebase with key: " + key))
                .addOnFailureListener(e -> Log.e(TAG, "Error adding data to Firebase: " + e.getMessage(), e));
        } catch (Exception e) {
            Log.e(TAG, "Exception during add operation: " + e.getMessage(), e);
            throw e;
        }
    }
}
