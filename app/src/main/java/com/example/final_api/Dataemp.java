package com.example.final_api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Dataemp {

    private DatabaseReference reference;

    public Dataemp(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        reference = db.getReference(FavoritesActivity.Favorite.class.getSimpleName());

    }
    public Task<Void> add(FavoritesActivity.Favorite emp){
        return reference.push().setValue(emp);
    }
}
