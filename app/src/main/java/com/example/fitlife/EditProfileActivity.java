package com.example.fitlife;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.fitlife.utils.PasswordHasher;
import com.example.fitlife.utils.SessionManager;

/**
 * Edit profile activity - allows user to update profile information.
 * Layout matches design: toolbar, Username, Email, Password, Age, Gender, Height, Weight, Edit button, bottom nav.
 */
public class EditProfileActivity extends AppCompatActivity {
    private TextInputLayout inputLayoutUsername;
    private TextInputLayout inputLayoutEmail;
    private TextInputLayout inputLayoutPassword;
    private TextInputEditText editTextUsername;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextAge;
    private TextInputEditText editTextGender;
    private TextInputEditText editTextHeight;
    private TextInputEditText editTextWeight;
    private MaterialButton buttonSave;
    private BottomNavigationView bottomNavigation;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        setupBottomNavigation();
        loadUserData();
    }

    private void initializeViews() {
        inputLayoutUsername = findViewById(R.id.inputLayoutUsername);
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextAge = findViewById(R.id.editTextAge);
        editTextGender = findViewById(R.id.editTextGender);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextWeight = findViewById(R.id.editTextWeight);
        buttonSave = findViewById(R.id.buttonSignUp);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        buttonSave.setOnClickListener(v -> handleSave());
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_profile);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_my_workouts) {
                startActivity(new Intent(this, MyWorkoutsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_create) {
                startActivity(new Intent(this, CreateActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_workouts) {
                startActivity(new Intent(this, WorkoutsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        int userId = sessionManager.getUserId();
        Cursor cursor = dbHelper.getAccountInfo(userId);
        if (cursor != null && cursor.moveToFirst()) {
            String email = cursor.getString(cursor.getColumnIndexOrThrow("username_email"));
            int usernameIdx = cursor.getColumnIndex("username");
            String username = (usernameIdx >= 0) ? cursor.getString(usernameIdx) : null;
            editTextEmail.setText(email != null ? email : "");
            editTextUsername.setText((username != null && !username.isEmpty()) ? username : email);
        } else {
            String usernameEmail = sessionManager.getUsernameEmail();
            editTextUsername.setText(usernameEmail);
            editTextEmail.setText(usernameEmail);
        }
        if (cursor != null) {
            cursor.close();
        }

        Cursor detailsCursor = dbHelper.getUserDetails(userId);
        if (detailsCursor != null && detailsCursor.moveToFirst()) {
            int age = detailsCursor.getInt(detailsCursor.getColumnIndexOrThrow("age"));
            String gender = detailsCursor.getString(detailsCursor.getColumnIndexOrThrow("gender"));
            double height = detailsCursor.getDouble(detailsCursor.getColumnIndexOrThrow("height"));
            double weight = detailsCursor.getDouble(detailsCursor.getColumnIndexOrThrow("weight"));
            editTextAge.setText(String.valueOf(age));
            editTextGender.setText(gender != null ? gender : "");
            editTextHeight.setText(String.valueOf((int) height));
            editTextWeight.setText(String.valueOf((int) weight));
        }
        if (detailsCursor != null) {
            detailsCursor.close();
        }
    }

    private void handleSave() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        String hashedPasswordOrNull = (password.isEmpty()) ? null : PasswordHasher.hashPassword(password);

        if (!dbHelper.updateUserProfile(userId, username, email, hashedPasswordOrNull)) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            return;
        }
        sessionManager.createSession(userId, email);

        // Parse and save user details (age, gender, height, weight)
        int age = 0;
        double height = 0, weight = 0;
        try {
            String ageStr = editTextAge.getText().toString().trim();
            if (!ageStr.isEmpty()) age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ignored) { }
        try {
            String heightStr = editTextHeight.getText().toString().trim();
            if (!heightStr.isEmpty()) height = Double.parseDouble(heightStr);
        } catch (NumberFormatException ignored) { }
        try {
            String weightStr = editTextWeight.getText().toString().trim();
            if (!weightStr.isEmpty()) weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException ignored) { }
        String gender = editTextGender.getText().toString().trim();

        if (dbHelper.userDetailsExist(userId)) {
            dbHelper.updateUserDetails(userId, age, gender, height, weight);
        } else {
            dbHelper.insertUserDetails(userId, age, gender, height, weight);
        }

        Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
        finish();
    }
}
