package com.example.fitlife.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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

public class LoginFragment extends Fragment {
    
    public interface OnLoginListener {
        void onLoginSuccess();
        void onSwitchToSignup();
    }
    
    private EditText editTextUsernameEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewSignup;
    
    private AuthController authController;
    private OnLoginListener listener;
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginListener) {
            listener = (OnLoginListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnLoginListener");
        }
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authController = new AuthController(requireContext());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        
        initializeViews(view);
        setupClickListeners();
        
        return view;
    }
    
    private void initializeViews(View view) {
        editTextUsernameEmail = view.findViewById(R.id.editTextUsernameEmail);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        buttonLogin = view.findViewById(R.id.buttonLogin);
        textViewSignup = view.findViewById(R.id.textViewSignup);
    }
    
    private void setupClickListeners() {
        buttonLogin.setOnClickListener(v -> attemptLogin());
        
        textViewSignup.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSwitchToSignup();
            }
        });
    }
    
    private void attemptLogin() {
        // Get and sanitize input values
        String usernameOrEmail = ValidationHelper.sanitizeInput(editTextUsernameEmail.getText().toString());
        String password = editTextPassword.getText().toString();
        
        // Validate username/email field
        boolean isUsernameEmailValid = true;
        if (TextUtils.isEmpty(usernameOrEmail)) {
            editTextUsernameEmail.setError("Username or email is required");
            isUsernameEmailValid = false;
        } else if (!ValidationHelper.isSqlSafe(usernameOrEmail)) {
            editTextUsernameEmail.setError("Invalid characters detected");
            isUsernameEmailValid = false;
        } else {
            editTextUsernameEmail.setError(null);
        }
        
        // Validate password
        boolean isPasswordValid = true;
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            isPasswordValid = false;
        } else if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            isPasswordValid = false;
        } else {
            editTextPassword.setError(null);
        }
        
        boolean allValid = isUsernameEmailValid && isPasswordValid;
        
        if (!allValid) {
            // Focus on first error field
            ValidationHelper.validateForm(editTextUsernameEmail, editTextPassword);
        } else {
            // Show progress and attempt login
            buttonLogin.setEnabled(false);
            buttonLogin.setText("Logging in...");
            
            // Simulate network delay
            new Thread(() -> {
                boolean success = authController.login(usernameOrEmail, password);
                
                requireActivity().runOnUiThread(() -> {
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText("Login");
                    
                    if (success) {
                        Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onLoginSuccess();
                        }
                    } else {
                        Toast.makeText(getContext(), "Invalid username/email or password", Toast.LENGTH_LONG).show();
                        editTextPassword.setText("");
                        editTextUsernameEmail.requestFocus();
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