package com.example.fitlife.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitlife.R;
import com.example.fitlife.controllers.AuthController;
import com.example.fitlife.utils.ValidationHelper;

public class SignupFragment extends Fragment {
    
    public interface OnSignupListener {
        void onSignupSuccess();
        void onSwitchToLogin();
    }
    
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonSignup;
    private TextView textViewLogin;
    
    private AuthController authController;
    private OnSignupListener listener;
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSignupListener) {
            listener = (OnSignupListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnSignupListener");
        }
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authController = new AuthController(requireContext());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        
        initializeViews(view);
        setupClickListeners();
        
        return view;
    }
    
    private void initializeViews(View view) {
        editTextUsername = view.findViewById(R.id.editTextUsername);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword);
        buttonSignup = view.findViewById(R.id.buttonSignup);
        textViewLogin = view.findViewById(R.id.textViewLogin);
    }
    
    private void setupClickListeners() {
        buttonSignup.setOnClickListener(v -> attemptSignup());
        
        textViewLogin.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSwitchToLogin();
            }
        });
    }
    
    private void attemptSignup() {
        // Get and sanitize input values
        String username = ValidationHelper.sanitizeInput(editTextUsername.getText().toString());
        String email = ValidationHelper.sanitizeInput(editTextEmail.getText().toString());
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
        
        // Validate all fields
        boolean isUsernameValid = ValidationHelper.validateAndSetError(editTextUsername, 
            ValidationHelper.validateUsername(username));
        
        boolean isEmailValid = ValidationHelper.validateAndSetError(editTextEmail, 
            ValidationHelper.validateEmail(email));
        
        boolean isPasswordValid = ValidationHelper.validateAndSetError(editTextPassword, 
            ValidationHelper.validatePassword(password));
        
        boolean isPasswordConfirmValid = ValidationHelper.validateAndSetError(editTextConfirmPassword, 
            ValidationHelper.validatePasswordConfirmation(password, confirmPassword));
        
        // Check for SQL injection attempts
        if (!ValidationHelper.isSqlSafe(username) || !ValidationHelper.isSqlSafe(email)) {
            Toast.makeText(getContext(), "Invalid characters detected in input", Toast.LENGTH_LONG).show();
            return;
        }
        
        boolean allValid = isUsernameValid && isEmailValid && isPasswordValid && isPasswordConfirmValid;
        
        if (!allValid) {
            // Focus on first error field
            ValidationHelper.validateForm(editTextUsername, editTextEmail, editTextPassword, editTextConfirmPassword);
        } else {
            // Show progress and attempt signup
            buttonSignup.setEnabled(false);
            buttonSignup.setText("Creating Account...");
            
            // Simulate network delay
            new Thread(() -> {
                boolean success = authController.register(username, email, password);
                
                requireActivity().runOnUiThread(() -> {
                    buttonSignup.setEnabled(true);
                    buttonSignup.setText("Sign Up");
                    
                    if (success) {
                        Toast.makeText(getContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onSignupSuccess();
                        }
                    } else {
                        Toast.makeText(getContext(), "Registration failed. Username or email may already exist.", Toast.LENGTH_LONG).show();
                    }
                });
            }).start();
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}