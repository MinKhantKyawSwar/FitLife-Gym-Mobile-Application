package com.example.fitlife.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class PermissionManager {
    
    // Permission request codes
    public static final int REQUEST_CAMERA_PERMISSIONS = 2003;
    public static final int REQUEST_STORAGE_PERMISSIONS = 2004;
    public static final int REQUEST_SMS_PERMISSIONS = 2005;
    public static final int REQUEST_CONTACTS_PERMISSIONS = 2006;
    public static final int REQUEST_LOCATION_PERMISSIONS = 2007;
    
    // Camera permissions
    private static final String[] CAMERA_PERMISSIONS = {
        Manifest.permission.CAMERA
    };
    
    // Storage permissions (different for different Android versions)
    private static final String[] STORAGE_PERMISSIONS_LEGACY = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    private static final String[] STORAGE_PERMISSIONS_MODERN = {
        Manifest.permission.READ_EXTERNAL_STORAGE
    };
    
    // SMS permissions
    private static final String[] SMS_PERMISSIONS = {
        Manifest.permission.SEND_SMS
    };
    
    // Contacts permissions
    private static final String[] CONTACTS_PERMISSIONS = {
        Manifest.permission.READ_CONTACTS
    };
    
    // Location permissions
    private static final String[] LOCATION_PERMISSIONS = {
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    };
    
    // Check camera permissions
    public static boolean hasCameraPermissions(Context context) {
        return hasPermissions(context, CAMERA_PERMISSIONS);
    }
    
    // Check storage permissions
    public static boolean hasStoragePermissions(Context context) {
        String[] permissions = getStoragePermissions();
        return hasPermissions(context, permissions);
    }
    
    // Check SMS permissions
    public static boolean hasSMSPermissions(Context context) {
        return hasPermissions(context, SMS_PERMISSIONS);
    }
    
    // Alternate method name for backward compatibility
    public static boolean hasSMSPermission(Context context) {
        return hasSMSPermissions(context);
    }
    
    // Check contacts permissions
    public static boolean hasContactsPermissions(Context context) {
        return hasPermissions(context, CONTACTS_PERMISSIONS);
    }
    
    // Check location permissions
    public static boolean hasLocationPermissions(Context context) {
        return hasPermissions(context, LOCATION_PERMISSIONS);
    }
    
    // Request camera permissions
    public static void requestCameraPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSIONS);
    }
    
    public static void requestCameraPermissions(Fragment fragment) {
        fragment.requestPermissions(CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSIONS);
    }
    
    // Request storage permissions
    public static void requestStoragePermissions(Activity activity) {
        String[] permissions = getStoragePermissions();
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_STORAGE_PERMISSIONS);
    }
    
    public static void requestStoragePermissions(Fragment fragment) {
        String[] permissions = getStoragePermissions();
        fragment.requestPermissions(permissions, REQUEST_STORAGE_PERMISSIONS);
    }
    
    // Request SMS permissions
    public static void requestSMSPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, SMS_PERMISSIONS, REQUEST_SMS_PERMISSIONS);
    }
    
    public static void requestSMSPermissions(Fragment fragment) {
        fragment.requestPermissions(SMS_PERMISSIONS, REQUEST_SMS_PERMISSIONS);
    }
    
    // Alternate method name for backward compatibility
    public static void requestSMSPermission(Fragment fragment) {
        requestSMSPermissions(fragment);
    }
    
    // Request contacts permissions
    public static void requestContactsPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, CONTACTS_PERMISSIONS, REQUEST_CONTACTS_PERMISSIONS);
    }
    
    public static void requestContactsPermissions(Fragment fragment) {
        fragment.requestPermissions(CONTACTS_PERMISSIONS, REQUEST_CONTACTS_PERMISSIONS);
    }
    
    // Request location permissions
    public static void requestLocationPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS);
    }
    
    public static void requestLocationPermissions(Fragment fragment) {
        fragment.requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS);
    }
    
    // Generic permission checker
    private static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    // Get appropriate storage permissions based on Android version
    private static String[] getStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return STORAGE_PERMISSIONS_MODERN;
        } else {
            return STORAGE_PERMISSIONS_LEGACY;
        }
    }
    
    // Check if permission request result was granted
    public static boolean isPermissionGranted(String[] permissions, int[] grantResults) {
        if (permissions == null || grantResults == null || permissions.length != grantResults.length) {
            return false;
        }
        
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    // Get permission rationale message
    public static String getPermissionRationaleMessage(int requestCode) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSIONS:
                return "FitLife needs camera permission to capture exercise photos and help you document your workouts.";
                
            case REQUEST_STORAGE_PERMISSIONS:
                return "FitLife needs storage permission to save exercise images and manage your workout data.";
                
            case REQUEST_SMS_PERMISSIONS:
                return "FitLife needs SMS permission to share workout routines and equipment lists with your friends.";
                
            case REQUEST_CONTACTS_PERMISSIONS:
                return "FitLife needs contacts permission to help you select friends to share workouts with.";
                
            case REQUEST_LOCATION_PERMISSIONS:
                return "FitLife needs location permission to tag workout locations and help you find nearby gyms.";
                
            default:
                return "FitLife needs this permission to provide the best workout experience.";
        }
    }
    
    // Check if user should be shown permission rationale
    public static boolean shouldShowRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
    
    public static boolean shouldShowRationale(Fragment fragment, String permission) {
        return fragment.shouldShowRequestPermissionRationale(permission);
    }
    
    // Handle permission denial
    public static void handlePermissionDenied(Context context, int requestCode) {
        String message = "Permission denied. " + getPermissionRationaleMessage(requestCode);
        
        if (context instanceof Activity) {
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show();
        }
    }
    
    // All required permissions array
    public static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.CAMERA,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.VIBRATE
    };
    
    // Check if all required permissions are granted
    public static boolean hasAllPermissions(Context context) {
        return hasPermissions(context, REQUIRED_PERMISSIONS);
    }
    
    // Request all required permissions
    public static void requestAllPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, REQUEST_CAMERA_PERMISSIONS);
    }
    
    // Permission callback interface
    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied(String[] deniedPermissions);
    }
    
    // Show permission rationale with callback
    public static void showPermissionRationale(Activity activity, String title, String message, 
                                              Runnable onAccept, Runnable onDeny) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        builder.setTitle(title)
               .setMessage(message)
               .setPositiveButton("Allow", (dialog, which) -> {
                   if (onAccept != null) onAccept.run();
               })
               .setNegativeButton("Deny", (dialog, which) -> {
                   if (onDeny != null) onDeny.run();
               })
               .setCancelable(false)
               .show();
    }
    
    // Handle permission results
    public static void handlePermissionResult(Context context, int requestCode, String[] permissions, 
                                            int[] grantResults, PermissionCallback callback) {
        if (isPermissionGranted(permissions, grantResults)) {
            if (callback != null) {
                callback.onPermissionGranted();
            }
        } else {
            // Find denied permissions
            java.util.List<String> deniedPermissions = new java.util.ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }
            
            if (callback != null) {
                callback.onPermissionDenied(deniedPermissions.toArray(new String[0]));
            }
            
            handlePermissionDenied(context, requestCode);
        }
    }
}