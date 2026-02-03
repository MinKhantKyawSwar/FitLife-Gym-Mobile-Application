package com.example.fitlife.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import java.util.List;

/**
 * Helper class for SMS sharing functionality
 * Formats workout data and opens SMS intent
 */
public class SMSHelper {
    /**
     * Formats workout information for SMS sharing
     * Includes workout name, exercises with sets/reps, equipment, and instructions
     */
    public static String formatWorkoutForSMS(String workoutName, List<String> exercises, 
                                             List<String> equipment, List<String> instructions) {
        StringBuilder message = new StringBuilder();
        message.append("FitLife Workout: ").append(workoutName).append("\n\n");
        
        message.append("Exercises:\n");
        for (String exercise : exercises) {
            message.append("• ").append(exercise).append("\n");
        }
        
        if (equipment != null && !equipment.isEmpty()) {
            message.append("\nEquipment Needed:\n");
            for (String eq : equipment) {
                message.append("• ").append(eq).append("\n");
            }
        }
        
        if (instructions != null && !instructions.isEmpty()) {
            message.append("\nInstructions:\n");
            for (int i = 0; i < instructions.size(); i++) {
                message.append((i + 1)).append(". ").append(instructions.get(i)).append("\n");
            }
        }
        
        message.append("\nShared from FitLife App");
        return message.toString();
    }

    /**
     * Opens SMS intent with formatted workout information
     */
    public static void shareWorkoutViaSMS(Context context, String message) {
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.putExtra("sms_body", message);
        smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startSmsIntent(context, smsIntent);
    }

    /**
     * Opens the default SMS app with the recipient number and message pre-filled.
     * Sends directly to the given number (no chooser, no recipient selection).
     *
     * @param context     Context
     * @param phoneNumber Recipient phone number (digits only or with +)
     * @param message     The workout message to send
     */
    public static void shareWorkoutToPhone(Context context, String phoneNumber, String message) {
        String normalized = phoneNumber.replaceAll("[^0-9+]", "");
        if (normalized.isEmpty()) {
            return;
        }
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + normalized));
        smsIntent.putExtra("sms_body", message);
        smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startSmsIntent(context, smsIntent);
    }

    private static void startSmsIntent(Context context, Intent smsIntent) {
        try {
            context.startActivity(smsIntent);
        } catch (Exception e) {
            Toast.makeText(context, "No app can send SMS", Toast.LENGTH_SHORT).show();
        }
    }
}
