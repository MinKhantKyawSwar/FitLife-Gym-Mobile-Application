package com.example.fitlife.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.fitlife.controllers.EquipmentChecklistController;
import com.example.fitlife.models.Equipment;
import com.example.fitlife.models.Routine;
import com.example.fitlife.models.RoutineExercise;
import com.example.fitlife.models.WeeklyEquipmentChecklist;

import java.util.List;
import java.util.Map;

public class SMSHelper {
    
    public interface OnSMSResultListener {
        void onSMSSuccess();
        void onSMSFailure(String error);
    }
    
    private Context context;
    
    public SMSHelper(Context context) {
        this.context = context;
    }
    
    public void shareRoutine(Routine routine, OnSMSResultListener listener) {
        if (!hasPermissions()) {
            listener.onSMSFailure("SMS permission not granted");
            return;
        }
        
        String message = formatRoutineForSMS(routine);
        showContactPicker(message, listener);
    }
    
    public void shareEquipmentList(Map<String, List<WeeklyEquipmentChecklist>> categorizedEquipment, OnSMSResultListener listener) {
        if (!hasPermissions()) {
            listener.onSMSFailure("SMS permission not granted");
            return;
        }
        
        EquipmentChecklistController equipmentController = new EquipmentChecklistController(context);
        String message = equipmentController.formatEquipmentForSMS(categorizedEquipment);
        showContactPicker(message, listener);
    }
    
    public void shareWorkoutReminder(String routineName, String scheduledDate, OnSMSResultListener listener) {
        if (!hasPermissions()) {
            listener.onSMSFailure("SMS permission not granted");
            return;
        }
        
        String message = "FitLife Reminder: \"" + routineName + "\" is scheduled for " + scheduledDate + ". Ready to crush this workout? 💪";
        showContactPicker(message, listener);
    }
    
    private String formatRoutineForSMS(Routine routine) {
        StringBuilder smsContent = new StringBuilder();
        smsContent.append("🏋️ FitLife Routine: ").append(routine.getRoutineName()).append("\n\n");
        
        if (routine.getDescription() != null && !routine.getDescription().trim().isEmpty()) {
            smsContent.append("📝 ").append(routine.getDescription()).append("\n\n");
        }
        
        smsContent.append("💪 Exercises:\n");
        for (int i = 0; i < routine.getExercises().size(); i++) {
            RoutineExercise routineExercise = routine.getExercises().get(i);
            smsContent.append(String.format("%d. %s (%dx%d)\n", 
                i + 1, 
                routineExercise.getExercise().getExerciseName(),
                routineExercise.getSets(),
                routineExercise.getReps()
            ));
        }
        
        smsContent.append("\n🎯 Equipment Needed:\n");
        for (RoutineExercise routineExercise : routine.getExercises()) {
            String equipment = routineExercise.getExercise().getEquipmentNeeded();
            if (equipment != null && !equipment.trim().isEmpty()) {
                smsContent.append("• ").append(equipment).append("\n");
            }
        }
        
        smsContent.append("\nShared from FitLife App 📱");
        
        return smsContent.toString();
    }
    
    private void showContactPicker(String message, OnSMSResultListener listener) {
        // For simplicity, we'll show a dialog to enter phone number
        // In a full implementation, you'd integrate with the contacts picker
        showPhoneNumberDialog(message, listener);
    }
    
    private void showPhoneNumberDialog(String message, OnSMSResultListener listener) {
        android.widget.EditText phoneInput = new android.widget.EditText(context);
        phoneInput.setHint("Enter phone number");
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Send SMS")
               .setMessage("Enter the phone number to send the workout:")
               .setView(phoneInput)
               .setPositiveButton("Send", (dialog, which) -> {
                   String phoneNumber = ValidationHelper.sanitizeInput(phoneInput.getText().toString());
                   ValidationHelper.ValidationResult phoneValidation = ValidationHelper.validatePhoneNumber(phoneNumber);
                   
                   if (phoneValidation.isValid()) {
                       sendSMS(phoneNumber, message, listener);
                   } else {
                       listener.onSMSFailure(phoneValidation.getErrorMessage());
                   }
               })
               .setNegativeButton("Cancel", (dialog, which) -> {
                   listener.onSMSFailure("SMS cancelled by user");
               })
               .show();
    }
    
    private void sendSMS(String phoneNumber, String message, OnSMSResultListener listener) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            
            // If message is too long, split it
            if (message.length() > 160) {
                java.util.ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            }
            
            listener.onSMSSuccess();
            
        } catch (Exception e) {
            listener.onSMSFailure("Failed to send SMS: " + e.getMessage());
        }
    }
    
    private boolean hasPermissions() {
        return PermissionManager.hasSMSPermission(context);
    }
    
    public static boolean hasSMSPermission(Context context) {
        return PermissionManager.hasSMSPermission(context);
    }
    
    public static void requestSMSPermission(Fragment fragment) {
        PermissionManager.requestSMSPermission(fragment);
    }
}