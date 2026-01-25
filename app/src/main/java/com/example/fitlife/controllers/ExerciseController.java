package com.example.fitlife.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.fitlife.database.DatabaseHelper;
import com.example.fitlife.models.Exercise;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExerciseController {
    private DatabaseHelper dbHelper;
    private Context context;
    
    public ExerciseController(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }
    
    // Create new exercise
    public long createExercise(Exercise exercise) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("user_id", exercise.getUserId());
        values.put("exercise_name", exercise.getExerciseName());
        values.put("equipment_needed", exercise.getEquipmentNeeded());
        values.put("instructions", exercise.getInstructions());
        values.put("image_path", exercise.getImagePath());
        values.put("created_at", getCurrentTimestamp());
        
        long exerciseId = db.insert("exercises", null, values);
        db.close();
        
        return exerciseId;
    }
    
    // Get exercise by ID
    public Exercise getExerciseById(int exerciseId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Exercise exercise = null;
        
        String selection = "exercise_id = ?";
        String[] selectionArgs = {String.valueOf(exerciseId)};
        
        Cursor cursor = db.query("exercises", null, selection, selectionArgs, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            exercise = createExerciseFromCursor(cursor);
            cursor.close();
        }
        
        db.close();
        return exercise;
    }
    
    // Get all exercises for a user
    public List<Exercise> getUserExercises(int userId) {
        List<Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = "exercise_name ASC";
        
        Cursor cursor = db.query("exercises", null, selection, selectionArgs, 
                                null, null, orderBy);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Exercise exercise = createExerciseFromCursor(cursor);
                exercises.add(exercise);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return exercises;
    }
    
    // Search exercises by name
    public List<Exercise> searchExercises(int userId, String searchQuery) {
        List<Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ? AND exercise_name LIKE ?";
        String[] selectionArgs = {String.valueOf(userId), "%" + searchQuery + "%"};
        String orderBy = "exercise_name ASC";
        
        Cursor cursor = db.query("exercises", null, selection, selectionArgs, 
                                null, null, orderBy);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Exercise exercise = createExerciseFromCursor(cursor);
                exercises.add(exercise);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return exercises;
    }
    
    // Update exercise
    public boolean updateExercise(Exercise exercise) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("exercise_name", exercise.getExerciseName());
        values.put("equipment_needed", exercise.getEquipmentNeeded());
        values.put("instructions", exercise.getInstructions());
        values.put("image_path", exercise.getImagePath());
        
        String whereClause = "exercise_id = ?";
        String[] whereArgs = {String.valueOf(exercise.getExerciseId())};
        
        int rowsAffected = db.update("exercises", values, whereClause, whereArgs);
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Delete exercise
    public boolean deleteExercise(int exerciseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // First, get the exercise to potentially clean up image file
        Exercise exercise = getExerciseById(exerciseId);
        
        String whereClause = "exercise_id = ?";
        String[] whereArgs = {String.valueOf(exerciseId)};
        
        int rowsAffected = db.delete("exercises", whereClause, whereArgs);
        db.close();
        
        // Clean up image file if exercise was deleted
        if (rowsAffected > 0 && exercise != null && exercise.getImagePath() != null) {
            // Delete image file
            try {
                java.io.File imageFile = new java.io.File(exercise.getImagePath());
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return rowsAffected > 0;
    }
    
    // Get exercises by equipment type
    public List<Exercise> getExercisesByEquipment(int userId, String equipment) {
        List<Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ? AND equipment_needed LIKE ?";
        String[] selectionArgs = {String.valueOf(userId), "%" + equipment + "%"};
        String orderBy = "exercise_name ASC";
        
        Cursor cursor = db.query("exercises", null, selection, selectionArgs, 
                                null, null, orderBy);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Exercise exercise = createExerciseFromCursor(cursor);
                exercises.add(exercise);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return exercises;
    }
    
    // Get recent exercises (for suggestions)
    public List<Exercise> getRecentExercises(int userId, int limit) {
        List<Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = "created_at DESC";
        String limitClause = String.valueOf(limit);
        
        Cursor cursor = db.query("exercises", null, selection, selectionArgs, 
                                null, null, orderBy, limitClause);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Exercise exercise = createExerciseFromCursor(cursor);
                exercises.add(exercise);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return exercises;
    }
    
    // Check if exercise name exists for user
    public boolean exerciseNameExists(int userId, String exerciseName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ? AND exercise_name = ?";
        String[] selectionArgs = {String.valueOf(userId), exerciseName};
        
        Cursor cursor = db.query("exercises", new String[]{"exercise_id"}, 
                                selection, selectionArgs, null, null, null);
        
        boolean exists = cursor != null && cursor.getCount() > 0;
        
        if (cursor != null) {
            cursor.close();
        }
        
        db.close();
        return exists;
    }
    
    // Get exercise count for user
    public int getUserExerciseCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        Cursor cursor = db.query("exercises", new String[]{"COUNT(*)"}, 
                                selection, selectionArgs, null, null, null);
        
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        
        db.close();
        return count;
    }
    
    // Helper method to create Exercise object from cursor
    private Exercise createExerciseFromCursor(Cursor cursor) {
        Exercise exercise = new Exercise();
        exercise.setExerciseId(cursor.getInt(cursor.getColumnIndexOrThrow("exercise_id")));
        exercise.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        exercise.setExerciseName(cursor.getString(cursor.getColumnIndexOrThrow("exercise_name")));
        exercise.setEquipmentNeeded(cursor.getString(cursor.getColumnIndexOrThrow("equipment_needed")));
        exercise.setInstructions(cursor.getString(cursor.getColumnIndexOrThrow("instructions")));
        exercise.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("image_path")));
        
        // Handle created_at timestamp
        String createdAtStr = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
        if (createdAtStr != null) {
            try {
                exercise.setCreatedAt(java.text.DateFormat.getDateTimeInstance().parse(createdAtStr));
            } catch (Exception e) {
                exercise.setCreatedAt(new Date());
            }
        }
        
        return exercise;
    }
    
    // Helper method to get current timestamp string
    private String getCurrentTimestamp() {
        return java.text.DateFormat.getDateTimeInstance().format(new Date());
    }
    
    // Bulk operations
    public boolean deleteMultipleExercises(List<Integer> exerciseIds) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            db.beginTransaction();
            
            for (int exerciseId : exerciseIds) {
                String whereClause = "exercise_id = ?";
                String[] whereArgs = {String.valueOf(exerciseId)};
                db.delete("exercises", whereClause, whereArgs);
            }
            
            db.setTransactionSuccessful();
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    
    // Get popular exercise names (for auto-complete)
    public List<String> getPopularExerciseNames(int userId, int limit) {
        List<String> exerciseNames = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = "created_at DESC";
        String limitClause = String.valueOf(limit);
        
        Cursor cursor = db.query("exercises", new String[]{"exercise_name"}, 
                                selection, selectionArgs, null, null, orderBy, limitClause);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String exerciseName = cursor.getString(cursor.getColumnIndexOrThrow("exercise_name"));
                if (!exerciseNames.contains(exerciseName)) {
                    exerciseNames.add(exerciseName);
                }
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return exerciseNames;
    }
}