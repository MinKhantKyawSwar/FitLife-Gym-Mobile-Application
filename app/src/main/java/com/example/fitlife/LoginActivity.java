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
 * Login activity - handles user authentication
 * Validates credentials and creates session
 */
public class LoginActivity extends AppCompatActivity {
    private TextInputLayout inputLayoutUsername;
    private TextInputLayout inputLayoutPassword;
    private TextInputEditText editTextUsername;
    private TextInputEditText editTextPassword;
    private MaterialButton buttonLogin;
    private TextView textSignUp;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // If already logged in, go to home
        if (sessionManager.isLoggedIn()) {
            navigateToHome();
            return;
        }

        inputLayoutUsername = findViewById(R.id.inputLayoutUsername);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textSignUp = findViewById(R.id.textSignUp);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        textSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                overridePendingTransition(0, 0);
            }
        });
    }

    private void handleLogin() {
        String usernameEmail = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (usernameEmail.isEmpty()) {
            inputLayoutUsername.setError("Please enter username or email");
            return;
        }
        inputLayoutUsername.setError(null);

        if (password.isEmpty()) {
            inputLayoutPassword.setError("Please enter password");
            return;
        }
        inputLayoutPassword.setError(null);

        // Hash password
        String hashedPassword = PasswordHasher.hashPassword(password);

        // Check credentials
        if (dbHelper.checkUser(usernameEmail, hashedPassword)) {
            // Get user ID
            int userId = dbHelper.getUserId(usernameEmail);
            // Create session
            sessionManager.createSession(userId, usernameEmail);
            // Navigate to home or add details
            navigateToHome();
        } else {
            Toast.makeText(this, R.string.invalid_credentials, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHome() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SessionManager sessionManager = new SessionManager(this);
        
        if (dbHelper.userDetailsExist(sessionManager.getUserId())) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        } else {
            startActivity(new Intent(LoginActivity.this, AddDetailsActivity.class));
        }
        overridePendingTransition(0, 0);
        finish();
    }
}
