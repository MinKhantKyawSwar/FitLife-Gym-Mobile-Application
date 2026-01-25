package com.example.fitlife.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.fitlife.R;
import com.example.fitlife.controllers.AuthController;
import com.example.fitlife.fragments.LoginFragment;
import com.example.fitlife.fragments.SignupFragment;

public class AuthActivity extends AppCompatActivity implements 
    LoginFragment.OnLoginListener, SignupFragment.OnSignupListener {
    
    private AuthController authController;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        
        authController = new AuthController(this);
        
        // Check if user is already logged in
        if (authController.isLoggedIn()) {
            navigateToMainActivity();
            return;
        }
        
        // Show login fragment by default
        if (savedInstanceState == null) {
            showLoginFragment();
        }
    }
    
    @Override
    public void onLoginSuccess() {
        navigateToMainActivity();
    }
    
    @Override
    public void onSwitchToSignup() {
        showSignupFragment();
    }
    
    @Override
    public void onSignupSuccess() {
        navigateToMainActivity();
    }
    
    @Override
    public void onSwitchToLogin() {
        showLoginFragment();
    }
    
    private void showLoginFragment() {
        LoginFragment fragment = new LoginFragment();
        replaceFragment(fragment);
    }
    
    private void showSignupFragment() {
        SignupFragment fragment = new SignupFragment();
        replaceFragment(fragment);
    }
    
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.auth_fragment_container, fragment);
        transaction.commit();
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, com.example.fitlife.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}