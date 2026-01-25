package com.example.fitlife.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.fitlife.database.DatabaseHelper;
import com.example.fitlife.models.Routine;
import com.example.fitlife.models.User;
import com.example.fitlife.models.WorkoutHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutHistoryController {
    private DatabaseHelper dbHelper;
    private Context context;
    private RoutineController routineController;
    
    public WorkoutHistoryController(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.routineController = new RoutineController(context);
    }
    
    // Create workout history record
    public long createWorkoutHistory(int userId, int routineId, Date workoutDate, Date startTime, Date completionTime, int exercisesCompleted, String notes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // Calculate duration in minutes
        int durationMinutes = 0;
        if (startTime != null && completionTime != null) {
            long durationMs = completionTime.getTime() - startTime.getTime();
            durationMinutes = (int) (durationMs / (1000 * 60));
        }
        
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("routine_id", routineId);
        values.put("workout_date", dateToString(workoutDate));
        
        if (startTime != null) {
            values.put("start_time", timestampToString(startTime));
        }
        if (completionTime != null) {
            values.put("completion_time", timestampToString(completionTime));
        }
        
        values.put("total_duration_minutes", durationMinutes);
        values.put("exercises_completed", exercisesCompleted);
        
        if (notes != null && !notes.trim().isEmpty()) {
            values.put("notes", notes);
        }
        
        long historyId = db.insert("workout_history", null, values);
        db.close();
        
        return historyId;
    }
    
    // Get workout history for user
    public List<WorkoutHistory> getUserWorkoutHistory(int userId) {
        return getUserWorkoutHistory(userId, 50); // Default to last 50 workouts
    }
    
    // Get workout history for user with limit
    public List<WorkoutHistory> getUserWorkoutHistory(int userId, int limit) {
        List<WorkoutHistory> historyList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = "workout_date DESC, completion_time DESC";
        String limitClause = String.valueOf(limit);
        
        Cursor cursor = db.query("workout_history", null, selection, selectionArgs, 
                                null, null, orderBy, limitClause);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                WorkoutHistory history = new WorkoutHistory();
                history.setHistoryId(cursor.getInt(cursor.getColumnIndexOrThrow("history_id")));
                history.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                history.setRoutineId(cursor.getInt(cursor.getColumnIndexOrThrow("routine_id")));
                history.setWorkoutDate(stringToDate(cursor.getString(cursor.getColumnIndexOrThrow("workout_date"))));
                
                String startTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
                if (startTimeStr != null) {
                    history.setStartTime(stringToTimestamp(startTimeStr));
                }
                
                String completionTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("completion_time"));
                if (completionTimeStr != null) {
                    history.setCompletionTime(stringToTimestamp(completionTimeStr));
                }
                
                history.setTotalDurationMinutes(cursor.getInt(cursor.getColumnIndexOrThrow("total_duration_minutes")));
                history.setExercisesCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("exercises_completed")));
                history.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
                
                // Load routine details
                Routine routine = routineController.getRoutineById(history.getRoutineId());
                history.setRoutine(routine);
                
                historyList.add(history);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return historyList;
    }
    
    // Get workout history by date range
    public List<WorkoutHistory> getWorkoutHistoryByDateRange(int userId, Date startDate, Date endDate) {
        List<WorkoutHistory> historyList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ? AND workout_date >= ? AND workout_date <= ?";
        String[] selectionArgs = {
            String.valueOf(userId),
            dateToString(startDate),
            dateToString(endDate)
        };
        String orderBy = "workout_date DESC, completion_time DESC";
        
        Cursor cursor = db.query("workout_history", null, selection, selectionArgs, 
                                null, null, orderBy);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                WorkoutHistory history = createWorkoutHistoryFromCursor(cursor);
                historyList.add(history);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return historyList;
    }
    
    // Get workout statistics
    public int[] getWorkoutStats(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Total workouts
        String countQuery = "SELECT COUNT(*) FROM workout_history WHERE user_id = ?";
        Cursor countCursor = db.rawQuery(countQuery, new String[]{String.valueOf(userId)});
        int totalWorkouts = 0;
        if (countCursor.moveToFirst()) {
            totalWorkouts = countCursor.getInt(0);
        }
        countCursor.close();
        
        // Total exercise count
        String exerciseQuery = "SELECT SUM(exercises_completed) FROM workout_history WHERE user_id = ?";
        Cursor exerciseCursor = db.rawQuery(exerciseQuery, new String[]{String.valueOf(userId)});
        int totalExercises = 0;
        if (exerciseCursor.moveToFirst()) {
            totalExercises = exerciseCursor.getInt(0);
        }
        exerciseCursor.close();
        
        // Total duration
        String durationQuery = "SELECT SUM(total_duration_minutes) FROM workout_history WHERE user_id = ?";
        Cursor durationCursor = db.rawQuery(durationQuery, new String[]{String.valueOf(userId)});
        int totalDuration = 0;
        if (durationCursor.moveToFirst()) {
            totalDuration = durationCursor.getInt(0);
        }
        durationCursor.close();
        
        db.close();
        
        return new int[]{totalWorkouts, totalExercises, totalDuration};
    }
    
    // Create workout history from completed weekly workout
    public long createHistoryFromWeeklyWorkout(int weeklyWorkoutId, Date startTime, Date completionTime, String notes) {
        // This would typically be called when a user completes a workout
        // For now, we'll create a simple implementation
        User currentUser = AuthController.getCurrentUser();
        if (currentUser == null) {
            return -1;
        }
        
        // Get routine exercises count (simplified)
        // In a full implementation, you'd track individual exercise completions
        int exercisesCompleted = 5; // Default value
        
        return createWorkoutHistory(currentUser.getUserId(), 1, new Date(), startTime, completionTime, exercisesCompleted, notes);
    }
    
    // Delete workout history
    public boolean deleteWorkoutHistory(int historyId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        int rowsAffected = db.delete("workout_history", "history_id = ?", 
                                    new String[]{String.valueOf(historyId)});
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Helper method to create WorkoutHistory from cursor
    private WorkoutHistory createWorkoutHistoryFromCursor(Cursor cursor) {
        WorkoutHistory history = new WorkoutHistory();
        history.setHistoryId(cursor.getInt(cursor.getColumnIndexOrThrow("history_id")));
        history.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        history.setRoutineId(cursor.getInt(cursor.getColumnIndexOrThrow("routine_id")));
        history.setWorkoutDate(stringToDate(cursor.getString(cursor.getColumnIndexOrThrow("workout_date"))));
        
        String startTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
        if (startTimeStr != null) {
            history.setStartTime(stringToTimestamp(startTimeStr));
        }
        
        String completionTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("completion_time"));
        if (completionTimeStr != null) {
            history.setCompletionTime(stringToTimestamp(completionTimeStr));
        }
        
        history.setTotalDurationMinutes(cursor.getInt(cursor.getColumnIndexOrThrow("total_duration_minutes")));
        history.setExercisesCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("exercises_completed")));
        history.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
        
        // Load routine details
        Routine routine = routineController.getRoutineById(history.getRoutineId());
        history.setRoutine(routine);
        
        return history;
    }
    
    // Helper methods for date/time conversion
    private String dateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }
    
    private Date stringToDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return new Date();
        }
    }
    
    private String timestampToString(Date timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(timestamp);
    }
    
    private Date stringToTimestamp(String timestampString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.parse(timestampString);
        } catch (Exception e) {
            e.printStackTrace();
            return new Date();
        }
    }
}