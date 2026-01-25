package com.example.fitlife.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.fitlife.database.DatabaseHelper;
import com.example.fitlife.models.WeeklyWorkout;
import com.example.fitlife.models.Routine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeeklyWorkoutController {
    private DatabaseHelper dbHelper;
    private Context context;
    private RoutineController routineController;
    
    public WeeklyWorkoutController(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.routineController = new RoutineController(context);
    }
    
    // Get current week's workouts for user
    public List<WeeklyWorkout> getCurrentWeekWorkouts(int userId) {
        List<WeeklyWorkout> weeklyWorkouts = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Calculate current week's start date
        Date weekStartDate = getCurrentWeekStartDate();
        
        String selection = "user_id = ? AND week_start_date = ?";
        String[] selectionArgs = {String.valueOf(userId), dateToString(weekStartDate)};
        String orderBy = "scheduled_date ASC";
        
        Cursor cursor = db.query("weekly_workouts", null, selection, selectionArgs, 
                                null, null, orderBy);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                WeeklyWorkout workout = createWeeklyWorkoutFromCursor(cursor);
                weeklyWorkouts.add(workout);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return weeklyWorkouts;
    }
    
    // Mark workout as completed
    public boolean markWorkoutCompleted(int weeklyWorkoutId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("is_completed", 1);
        values.put("completion_time", timestampToString(new Date()));
        
        String whereClause = "weekly_workout_id = ?";
        String[] whereArgs = {String.valueOf(weeklyWorkoutId)};
        
        int rowsAffected = db.update("weekly_workouts", values, whereClause, whereArgs);
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Delete weekly workout
    public boolean deleteWeeklyWorkout(int weeklyWorkoutId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        String whereClause = "weekly_workout_id = ?";
        String[] whereArgs = {String.valueOf(weeklyWorkoutId)};
        
        int rowsAffected = db.delete("weekly_workouts", whereClause, whereArgs);
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Clear all workouts for current week
    public boolean clearCurrentWeekWorkouts(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        Date weekStartDate = getCurrentWeekStartDate();
        
        String whereClause = "user_id = ? AND week_start_date = ?";
        String[] whereArgs = {String.valueOf(userId), dateToString(weekStartDate)};
        
        int rowsAffected = db.delete("weekly_workouts", whereClause, whereArgs);
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Add routine to weekly schedule
    public boolean addRoutineToWeek(int userId, int routineId, Date scheduledDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        Date weekStartDate = getWeekStartDate(scheduledDate);
        
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("routine_id", routineId);
        values.put("scheduled_date", dateToString(scheduledDate));
        values.put("is_completed", 0);
        values.put("week_start_date", dateToString(weekStartDate));
        
        long result = db.insert("weekly_workouts", null, values);
        db.close();
        
        return result > 0;
    }
    
    // Get workout by ID
    public WeeklyWorkout getWeeklyWorkoutById(int weeklyWorkoutId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        WeeklyWorkout workout = null;
        
        String selection = "weekly_workout_id = ?";
        String[] selectionArgs = {String.valueOf(weeklyWorkoutId)};
        
        Cursor cursor = db.query("weekly_workouts", null, selection, selectionArgs, 
                                null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            workout = createWeeklyWorkoutFromCursor(cursor);
            cursor.close();
        }
        
        db.close();
        return workout;
    }
    
    // Get workouts for specific date range
    public List<WeeklyWorkout> getWorkoutsInDateRange(int userId, Date startDate, Date endDate) {
        List<WeeklyWorkout> weeklyWorkouts = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ? AND scheduled_date >= ? AND scheduled_date <= ?";
        String[] selectionArgs = {
            String.valueOf(userId),
            dateToString(startDate),
            dateToString(endDate)
        };
        String orderBy = "scheduled_date ASC";
        
        Cursor cursor = db.query("weekly_workouts", null, selection, selectionArgs, 
                                null, null, orderBy);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                WeeklyWorkout workout = createWeeklyWorkoutFromCursor(cursor);
                weeklyWorkouts.add(workout);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return weeklyWorkouts;
    }
    
    // Get completed workouts count for current week
    public int getCompletedWorkoutsCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Date weekStartDate = getCurrentWeekStartDate();
        
        String selection = "user_id = ? AND week_start_date = ? AND is_completed = 1";
        String[] selectionArgs = {String.valueOf(userId), dateToString(weekStartDate)};
        
        Cursor cursor = db.query("weekly_workouts", new String[]{"COUNT(*)"}, 
                                selection, selectionArgs, null, null, null);
        
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        
        db.close();
        return count;
    }
    
    // Get total workouts count for current week
    public int getTotalWorkoutsCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Date weekStartDate = getCurrentWeekStartDate();
        
        String selection = "user_id = ? AND week_start_date = ?";
        String[] selectionArgs = {String.valueOf(userId), dateToString(weekStartDate)};
        
        Cursor cursor = db.query("weekly_workouts", new String[]{"COUNT(*)"}, 
                                selection, selectionArgs, null, null, null);
        
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        
        db.close();
        return count;
    }
    
    // Helper method to create WeeklyWorkout from cursor
    private WeeklyWorkout createWeeklyWorkoutFromCursor(Cursor cursor) {
        WeeklyWorkout workout = new WeeklyWorkout();
        workout.setWeeklyWorkoutId(cursor.getInt(cursor.getColumnIndexOrThrow("weekly_workout_id")));
        workout.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        workout.setRoutineId(cursor.getInt(cursor.getColumnIndexOrThrow("routine_id")));
        workout.setScheduledDate(stringToDate(cursor.getString(cursor.getColumnIndexOrThrow("scheduled_date"))));
        workout.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1);
        workout.setWeekStartDate(stringToDate(cursor.getString(cursor.getColumnIndexOrThrow("week_start_date"))));
        
        String completionTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("completion_time"));
        if (completionTimeStr != null) {
            workout.setCompletionTime(stringToTimestamp(completionTimeStr));
        }
        
        // Load routine details
        Routine routine = routineController.getRoutineById(workout.getRoutineId());
        workout.setRoutine(routine);
        
        return workout;
    }
    
    // Helper method to get current week's start date (Monday)
    private Date getCurrentWeekStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
    // Helper method to get week start date for any given date
    private Date getWeekStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
    // Helper methods for date conversion
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