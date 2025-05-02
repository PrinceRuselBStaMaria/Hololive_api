package com.example.final_api;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView forgotPassword, registerLink;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupClickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        try {
            emailInput = findViewById(R.id.emailInput);
            passwordInput = findViewById(R.id.passwordInput);
            loginButton = findViewById(R.id.loginButton);
            forgotPassword = findViewById(R.id.forgotPassword);
            registerLink = findViewById(R.id.registerLink);
            auth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> handleLogin());
        }
        if (forgotPassword != null) {
            forgotPassword.setOnClickListener(v -> handleForgotPassword());
        }
        if (registerLink != null) {
            registerLink.setOnClickListener(v -> handleRegister());
        }
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }else{
            auth_na_authentication(email,password);
        }
    }
    private void auth_na_authentication(String email, String password){

        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(Login.this, "Successfully login", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login.this, welcome_screen.class));
                } else {
                    Toast.makeText(Login.this, "login Failed"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void handleRegister() {
        startActivity(new Intent(Login.this, register_screen.class));
    }

    private void handleForgotPassword() {
        Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void handleSocialLogin(String provider) {
        Toast.makeText(this, provider + " login coming soon", Toast.LENGTH_SHORT).show();
    }

}

/* old login
*
*
*
*
*     private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserCredentials", Context.MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("email", null);
        String savedPassword = sharedPreferences.getString("password", null);
        String firstName = sharedPreferences.getString("firstName", "");

        if (email.equals(savedUsername) && password.equals(savedPassword)) {
            Intent intent = new Intent(Login.this, welcome_screen.class);
            intent.putExtra("fullName", firstName);
            startActivity(intent);
            finish();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Invalid credentials")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

*
*
*
* */