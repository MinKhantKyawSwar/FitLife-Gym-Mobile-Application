package com.example.fitlife;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.fitlife.utils.SessionManager;

/**
 * Add details activity - collects user information on first login
 * Age, gender, height, and weight
 */
public class AddDetailsActivity extends AppCompatActivity {
    private TextInputLayout inputLayoutAge;
    private TextInputLayout inputLayoutGender;
    private TextInputLayout inputLayoutHeight;
    private TextInputLayout inputLayoutWeight;
    private TextInputEditText editTextAge;
    private TextInputEditText editTextGender;
    private TextInputEditText editTextHeight;
    private TextInputEditText editTextWeight;
    private MaterialButton buttonSave;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_details);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        // Check if details already exist
        int userId = sessionManager.getUserId();
        if (dbHelper.userDetailsExist(userId)) {
            // Details already exist, go to home
            startActivity(new Intent(this, HomeActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        inputLayoutAge = findViewById(R.id.inputLayoutAge);
        inputLayoutGender = findViewById(R.id.inputLayoutGender);
        inputLayoutHeight = findViewById(R.id.inputLayoutHeight);
        inputLayoutWeight = findViewById(R.id.inputLayoutWeight);
        editTextAge = findViewById(R.id.editTextAge);
        editTextGender = findViewById(R.id.editTextGender);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextWeight = findViewById(R.id.editTextWeight);
        buttonSave = findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSave();
            }
        });
    }

    private void handleSave() {
        String ageStr = editTextAge.getText().toString().trim();
        String gender = editTextGender.getText().toString().trim();
        String heightStr = editTextHeight.getText().toString().trim();
        String weightStr = editTextWeight.getText().toString().trim();

        boolean isValid = true;

        if (ageStr.isEmpty()) {
            inputLayoutAge.setError("Please enter age");
            isValid = false;
        } else {
            inputLayoutAge.setError(null);
        }

        if (gender.isEmpty()) {
            inputLayoutGender.setError("Please enter gender");
            isValid = false;
        } else {
            inputLayoutGender.setError(null);
        }

        if (heightStr.isEmpty()) {
            inputLayoutHeight.setError("Please enter height");
            isValid = false;
        } else {
            inputLayoutHeight.setError(null);
        }

        if (weightStr.isEmpty()) {
            inputLayoutWeight.setError("Please enter weight");
            isValid = false;
        } else {
            inputLayoutWeight.setError(null);
        }

        if (!isValid) {
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            double height = Double.parseDouble(heightStr);
            double weight = Double.parseDouble(weightStr);

            int userId = sessionManager.getUserId();
            long result = dbHelper.insertUserDetails(userId, age, gender, height, weight);

            if (result > 0) {
                Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
}
