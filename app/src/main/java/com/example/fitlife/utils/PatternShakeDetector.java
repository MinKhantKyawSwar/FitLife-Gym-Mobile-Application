package com.example.fitlife.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PatternShakeDetector implements SensorEventListener {
    
    public interface OnShakeListener {
        void onShakeDetected();
    }
    
    public interface OnPatternShakeListener {
        void onPatternShakeDetected();
    }
    
    private static final float SHAKE_THRESHOLD = 15.0f;
    private static final int PATTERN_WINDOW = 3000; // 3 seconds
    private static final int REQUIRED_SHAKES = 3; // 3 distinct shakes
    private static final int COOLDOWN_PERIOD = 2000; // 2 seconds cooldown
    
    private Context context;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private OnShakeListener listener;
    private OnPatternShakeListener patternShakeListener;
    
    private List<Long> shakeTimestamps;
    private long lastShakeTime = 0;
    private boolean isListening = false;
    
    private Handler handler;
    
    public PatternShakeDetector(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.shakeTimestamps = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    public void setOnShakeListener(OnShakeListener listener) {
        this.listener = listener;
    }
    
    public void setOnPatternShakeListener(OnPatternShakeListener patternShakeListener) {
        this.patternShakeListener = patternShakeListener;
    }
    
    public void startListening() {
        if (accelerometer != null && !isListening) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            isListening = true;
        }
    }
    
    public void stopListening() {
        if (isListening) {
            sensorManager.unregisterListener(this);
            isListening = false;
            shakeTimestamps.clear();
        }
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }
        
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        
        // Calculate acceleration magnitude (excluding gravity)
        float acceleration = (float) Math.sqrt(x*x + y*y + z*z) - SensorManager.GRAVITY_EARTH;
        
        long currentTime = System.currentTimeMillis();
        
        // Check if acceleration exceeds threshold and cooldown has passed
        if (acceleration > SHAKE_THRESHOLD && (currentTime - lastShakeTime) > 300) { // 300ms between individual shakes
            lastShakeTime = currentTime;
            shakeTimestamps.add(currentTime);
            
            // Clean old timestamps outside pattern window
            cleanOldShakes(currentTime);
            
            // Check if pattern is complete
            if (shakeTimestamps.size() >= REQUIRED_SHAKES) {
                triggerPatternShake();
                shakeTimestamps.clear();
            }
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
    
    private void cleanOldShakes(long currentTime) {
        Iterator<Long> iterator = shakeTimestamps.iterator();
        while (iterator.hasNext()) {
            Long timestamp = iterator.next();
            if (currentTime - timestamp > PATTERN_WINDOW) {
                iterator.remove();
            }
        }
    }
    
    private void triggerPatternShake() {
        // Check cooldown to prevent multiple dialogs
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShakeTime < COOLDOWN_PERIOD) {
            return;
        }
        
        // Trigger shake event on main thread
        handler.post(() -> {
            if (listener != null) {
                listener.onShakeDetected();
            }
            if (patternShakeListener != null) {
                patternShakeListener.onPatternShakeDetected();
            }
        });
    }
    
    public void showShakeResetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Reset Weekly Workouts")
               .setMessage("Are you sure you want to reset all workouts for this week? This action cannot be undone.")
               .setIcon(android.R.drawable.ic_dialog_alert)
               .setPositiveButton("Reset", (dialog, id) -> {
                   if (listener != null) {
                       listener.onShakeDetected();
                   }
                   if (patternShakeListener != null) {
                       patternShakeListener.onPatternShakeDetected();
                   }
               })
               .setNegativeButton("Cancel", (dialog, id) -> {
                   dialog.dismiss();
               })
               .setCancelable(true)
               .show();
    }
    
    // Get current shake count for UI feedback
    public int getCurrentShakeCount() {
        long currentTime = System.currentTimeMillis();
        cleanOldShakes(currentTime);
        return shakeTimestamps.size();
    }
    
    // Check if shake detection is active
    public boolean isListening() {
        return isListening;
    }
}