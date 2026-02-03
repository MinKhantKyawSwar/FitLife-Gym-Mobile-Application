package com.example.fitlife;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.fitlife.utils.PasswordHasher;
import com.example.fitlife.utils.SessionManager;

/**
 * Registration activity - handles new user registration
 * Validates input and creates user account
 */
public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout inputLayoutUsername;
    private TextInputLayout inputLayoutEmail;
    private TextInputLayout inputLayoutPassword;
    private TextInputLayout inputLayoutConfirmPassword;
    private TextInputEditText editTextUsername;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextConfirmPassword;
    private MaterialButton buttonSignUp;
    private TextView textLogIn;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        inputLayoutUsername = findViewById(R.id.inputLayoutUsername);
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
        inputLayoutConfirmPassword = findViewById(R.id.inputLayoutConfirmPassword);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textLogIn = findViewById(R.id.textLogIn);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });

        textLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    private void handleRegistration() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validate inputs
        boolean isValid = true;

        if (username.isEmpty()) {
            inputLayoutUsername.setError("Please enter username");
            isValid = false;
        } else {
            inputLayoutUsername.setError(null);
        }

        if (email.isEmpty()) {
            inputLayoutEmail.setError("Please enter email");
            isValid = false;
        } else {
            inputLayoutEmail.setError(null);
        }

        if (password.isEmpty()) {
            inputLayoutPassword.setError("Please enter password");
            isValid = false;
        } else {
            inputLayoutPassword.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            inputLayoutConfirmPassword.setError("Please confirm password");
            isValid = false;
        } else {
            inputLayoutConfirmPassword.setError(null);
        }

        if (!isValid) {
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            inputLayoutConfirmPassword.setError(getString(R.string.password_mismatch));
            return;
        }

        // Check if user already exists
        if (dbHelper.userExists(username) || dbHelper.userExists(email)) {
            Toast.makeText(this, R.string.user_exists, Toast.LENGTH_SHORT).show();
            return;
        }

        // Hash password
        String hashedPassword = PasswordHasher.hashPassword(password);

        // Register user (use email as username_email in database, but store both username and email)
        // For simplicity, we'll use email as the login identifier
        long userId = dbHelper.insertUser(email, hashedPassword);
        
        if (userId > 0) {
            // Initialize user stats
            dbHelper.initializeUserStats((int) userId);
            
            // Create session
            SessionManager sessionManager = new SessionManager(this);
            sessionManager.createSession((int) userId, email);
            
            Toast.makeText(this, R.string.registration_success, Toast.LENGTH_SHORT).show();
            
            // Navigate to add details
            startActivity(new Intent(RegisterActivity.this, AddDetailsActivity.class));
            overridePendingTransition(0, 0);
            finish();
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }
}
