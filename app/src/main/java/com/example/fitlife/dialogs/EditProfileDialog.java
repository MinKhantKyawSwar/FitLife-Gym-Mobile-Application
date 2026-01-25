package com.example.fitlife.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.fitlife.R;
import com.example.fitlife.controllers.AuthController;
import com.example.fitlife.models.User;
import com.example.fitlife.utils.ValidationHelper;

public class EditProfileDialog extends DialogFragment {
    
    public interface OnProfileUpdateListener {
        void onUsernameUpdated(String newUsername);
        void onPasswordUpdated();
    }
    
    private static final String ARG_EDIT_TYPE = "edit_type";
    public static final String EDIT_USERNAME = "username";
    public static final String EDIT_PASSWORD = "password";
    
    private EditText primaryEditText;
    private EditText secondaryEditText;
    private EditText tertiaryEditText;
    private Button saveButton;
    private Button cancelButton;
    
    private AuthController authController;
    private OnProfileUpdateListener listener;
    private String editType;
    
    public static EditProfileDialog newInstance(String editType) {
        EditProfileDialog dialog = new EditProfileDialog();
        Bundle args = new Bundle();
        args.putString(ARG_EDIT_TYPE, editType);
        dialog.setArguments(args);
        return dialog;
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnProfileUpdateListener) getParentFragment();
            if (listener == null) {
                listener = (OnProfileUpdateListener) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnProfileUpdateListener");
        }
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authController = new AuthController(requireContext());
        
        if (getArguments() != null) {
            editType = getArguments().getString(ARG_EDIT_TYPE, EDIT_USERNAME);
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_profile, null);
        
        initializeViews(view);
        setupDialog();
        setupClickListeners();
        
        builder.setView(view);
        return builder.create();
    }
    
    private void initializeViews(View view) {
        primaryEditText = view.findViewById(R.id.primaryEditText);
        secondaryEditText = view.findViewById(R.id.secondaryEditText);
        tertiaryEditText = view.findViewById(R.id.tertiaryEditText);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);
    }
    
    private void setupDialog() {
        User currentUser = AuthController.getCurrentUser();
        
        if (EDIT_USERNAME.equals(editType)) {
            // Username editing
            primaryEditText.setHint("New Username");
            if (currentUser != null) {
                primaryEditText.setText(currentUser.getUsername());
            }
            
            secondaryEditText.setVisibility(View.GONE);
            tertiaryEditText.setVisibility(View.GONE);
            
        } else if (EDIT_PASSWORD.equals(editType)) {
            // Password editing
            primaryEditText.setHint("Current Password");
            primaryEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            
            secondaryEditText.setHint("New Password");
            secondaryEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            secondaryEditText.setVisibility(View.VISIBLE);
            
            tertiaryEditText.setHint("Confirm New Password");
            tertiaryEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            tertiaryEditText.setVisibility(View.VISIBLE);
        }
    }
    
    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveChanges());
        cancelButton.setOnClickListener(v -> dismiss());
    }
    
    private void saveChanges() {
        if (EDIT_USERNAME.equals(editType)) {
            saveUsername();
        } else if (EDIT_PASSWORD.equals(editType)) {
            savePassword();
        }
    }
    
    private void saveUsername() {
        String newUsername = ValidationHelper.sanitizeInput(primaryEditText.getText().toString());
        
        // Validate username
        ValidationHelper.ValidationResult result = ValidationHelper.validateUsername(newUsername);
        if (!result.isValid()) {
            primaryEditText.setError(result.getErrorMessage());
            return;
        }
        
        // Check for SQL injection
        if (!ValidationHelper.isSqlSafe(newUsername)) {
            Toast.makeText(getContext(), "Invalid characters in username", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Update username
        boolean success = authController.updateUsername(newUsername);
        if (success) {
            Toast.makeText(getContext(), "Username updated successfully", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onUsernameUpdated(newUsername);
            }
            dismiss();
        } else {
            Toast.makeText(getContext(), "Failed to update username. Username may already exist.", Toast.LENGTH_LONG).show();
        }
    }
    
    private void savePassword() {
        String currentPassword = primaryEditText.getText().toString();
        String newPassword = secondaryEditText.getText().toString();
        String confirmPassword = tertiaryEditText.getText().toString();
        
        // Validate current password
        if (TextUtils.isEmpty(currentPassword)) {
            primaryEditText.setError("Current password is required");
            return;
        }
        
        // Validate new password
        ValidationHelper.ValidationResult passwordResult = ValidationHelper.validatePassword(newPassword);
        if (!passwordResult.isValid()) {
            secondaryEditText.setError(passwordResult.getErrorMessage());
            return;
        }
        
        // Validate password confirmation
        ValidationHelper.ValidationResult confirmResult = ValidationHelper.validatePasswordConfirmation(newPassword, confirmPassword);
        if (!confirmResult.isValid()) {
            tertiaryEditText.setError(confirmResult.getErrorMessage());
            return;
        }
        
        // Update password
        boolean success = authController.changePassword(currentPassword, newPassword);
        if (success) {
            Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onPasswordUpdated();
            }
            dismiss();
        } else {
            Toast.makeText(getContext(), "Failed to update password. Please check your current password.", Toast.LENGTH_LONG).show();
            primaryEditText.setError("Incorrect current password");
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}