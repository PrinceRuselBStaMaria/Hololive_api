package com.example.final_api;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FavoritesActivity extends AppCompatActivity {

    private EditText editText;
    private Button submitButton;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize views
        editText = findViewById(R.id.editTextFavoriteName);
        submitButton = findViewById(R.id.btnAddFavorite);
        Dataemp dao = new Dataemp();

        // Set click listener for the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = editText.getText().toString();
                Favorite emp = new Favorite((inputText));
                dao.add(emp).addOnSuccessListener(suc -> {
                    Toast.makeText(FavoritesActivity.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(err -> {
                    Toast.makeText(FavoritesActivity.this, "Error: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                });
                if (!inputText.isEmpty()) {
                    Toast.makeText(FavoritesActivity.this, "Submitted: " + inputText, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FavoritesActivity.this, "Please enter some text", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Minimal test model (like employee)
    public static class Favorite {
        private String name;
        public Favorite() {}
        public Favorite(String name) { this.name = name; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }


}