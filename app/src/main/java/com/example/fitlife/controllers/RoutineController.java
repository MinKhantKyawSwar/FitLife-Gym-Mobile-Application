package com.example.fitlife.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.fitlife.database.DatabaseHelper;
import com.example.fitlife.models.Exercise;
import com.example.fitlife.models.Routine;
import com.example.fitlife.models.RoutineExercise;
import com.example.fitlife.models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RoutineController {
    private DatabaseHelper dbHelper;
    private Context context;
    
    public RoutineController(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }
    
    // Create a new routine
    public long createRoutine(String routineName, String description) {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser == null) {
            return -1;
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("user_id", currentUser.getUserId());
        values.put("routine_name", routineName);
        values.put("description", description);
        
        long routineId = db.insert("routines", null, values);
        db.close();
        
        return routineId;
    }
    
    // Get all routines for current user
    public List<Routine> getUserRoutines() {
        List<Routine> routines = new ArrayList<>();
        User currentUser = AuthController.getCurrentUser();
        if (currentUser == null) {
            return routines;
        }
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ?";
        String[] selectionArgs = {String.valueOf(currentUser.getUserId())};
        
        Cursor cursor = db.query("routines", null, selection, selectionArgs, 
                                null, null, "created_at DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Routine routine = new Routine();
                routine.setRoutineId(cursor.getInt(cursor.getColumnIndexOrThrow("routine_id")));
                routine.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                routine.setRoutineName(cursor.getString(cursor.getColumnIndexOrThrow("routine_name")));
                routine.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                
                // Load exercises for this routine
                routine.setExercises(getRoutineExercises(routine.getRoutineId()));
                
                routines.add(routine);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return routines;
    }
    
    // Get routine by ID
    public Routine getRoutineById(int routineId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "routine_id = ?";
        String[] selectionArgs = {String.valueOf(routineId)};
        
        Cursor cursor = db.query("routines", null, selection, selectionArgs, null, null, null);
        
        Routine routine = null;
        if (cursor != null && cursor.moveToFirst()) {
            routine = new Routine();
            routine.setRoutineId(cursor.getInt(cursor.getColumnIndexOrThrow("routine_id")));
            routine.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            routine.setRoutineName(cursor.getString(cursor.getColumnIndexOrThrow("routine_name")));
            routine.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            
            // Load exercises for this routine
            routine.setExercises(getRoutineExercises(routine.getRoutineId()));
            
            cursor.close();
        }
        
        db.close();
        return routine;
    }
    
    // Get exercises for a specific routine
    public List<RoutineExercise> getRoutineExercises(int routineId) {
        List<RoutineExercise> routineExercises = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT re.*, e.exercise_name, e.equipment_needed, e.instructions, e.image_path " +
                       "FROM routine_exercises re " +
                       "JOIN exercises e ON re.exercise_id = e.exercise_id " +
                       "WHERE re.routine_id = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(routineId)});
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                RoutineExercise routineExercise = new RoutineExercise();
                routineExercise.setRoutineExerciseId(cursor.getInt(cursor.getColumnIndexOrThrow("routine_exercise_id")));
                routineExercise.setRoutineId(cursor.getInt(cursor.getColumnIndexOrThrow("routine_id")));
                routineExercise.setExerciseId(cursor.getInt(cursor.getColumnIndexOrThrow("exercise_id")));
                routineExercise.setSets(cursor.getInt(cursor.getColumnIndexOrThrow("sets")));
                routineExercise.setReps(cursor.getInt(cursor.getColumnIndexOrThrow("reps")));
                routineExercise.setRestSeconds(cursor.getInt(cursor.getColumnIndexOrThrow("rest_seconds")));
                
                // Create exercise object
                Exercise exercise = new Exercise();
                exercise.setExerciseId(cursor.getInt(cursor.getColumnIndexOrThrow("exercise_id")));
                exercise.setExerciseName(cursor.getString(cursor.getColumnIndexOrThrow("exercise_name")));
                exercise.setEquipmentNeeded(cursor.getString(cursor.getColumnIndexOrThrow("equipment_needed")));
                exercise.setInstructions(cursor.getString(cursor.getColumnIndexOrThrow("instructions")));
                exercise.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("image_path")));
                
                routineExercise.setExercise(exercise);
                routineExercises.add(routineExercise);
                
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return routineExercises;
    }
    
    // Create a new exercise
    public long createExercise(String exerciseName, String equipmentNeeded, String instructions, String imagePath) {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser == null) {
            return -1;
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("user_id", currentUser.getUserId());
        values.put("exercise_name", exerciseName);
        values.put("equipment_needed", equipmentNeeded);
        values.put("instructions", instructions);
        values.put("image_path", imagePath);
        
        long exerciseId = db.insert("exercises", null, values);
        db.close();
        
        return exerciseId;
    }
    
    // Add exercise to routine
    public boolean addExerciseToRoutine(int routineId, int exerciseId, int sets, int reps, int restSeconds) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("routine_id", routineId);
        values.put("exercise_id", exerciseId);
        values.put("sets", sets);
        values.put("reps", reps);
        values.put("rest_seconds", restSeconds);
        
        long result = db.insert("routine_exercises", null, values);
        db.close();
        
        return result != -1;
    }
    
    // Delete routine
    public boolean deleteRoutine(int routineId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            db.beginTransaction();
            
            // Delete routine exercises first
            db.delete("routine_exercises", "routine_id = ?", new String[]{String.valueOf(routineId)});
            
            // Delete routine
            int rowsAffected = db.delete("routines", "routine_id = ?", new String[]{String.valueOf(routineId)});
            
            db.setTransactionSuccessful();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    
    // Update routine
    public boolean updateRoutine(int routineId, String routineName, String description) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("routine_name", routineName);
        values.put("description", description);
        
        int rowsAffected = db.update("routines", values, "routine_id = ?", 
                                    new String[]{String.valueOf(routineId)});
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Get all exercises for current user
    public List<Exercise> getUserExercises() {
        List<Exercise> exercises = new ArrayList<>();
        User currentUser = AuthController.getCurrentUser();
        if (currentUser == null) {
            return exercises;
        }
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ?";
        String[] selectionArgs = {String.valueOf(currentUser.getUserId())};
        
        Cursor cursor = db.query("exercises", null, selection, selectionArgs, 
                                null, null, "created_at DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Exercise exercise = new Exercise();
                exercise.setExerciseId(cursor.getInt(cursor.getColumnIndexOrThrow("exercise_id")));
                exercise.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                exercise.setExerciseName(cursor.getString(cursor.getColumnIndexOrThrow("exercise_name")));
                exercise.setEquipmentNeeded(cursor.getString(cursor.getColumnIndexOrThrow("equipment_needed")));
                exercise.setInstructions(cursor.getString(cursor.getColumnIndexOrThrow("instructions")));
                exercise.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("image_path")));
                
                exercises.add(exercise);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return exercises;
    }
}