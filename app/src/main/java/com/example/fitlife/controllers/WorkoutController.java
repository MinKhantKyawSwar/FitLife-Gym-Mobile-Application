package com.example.fitlife.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.fitlife.database.DatabaseHelper;
import com.example.fitlife.models.Routine;
import com.example.fitlife.models.User;
import com.example.fitlife.models.WeeklyWorkout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutController {
    private DatabaseHelper dbHelper;
    private Context context;
    private RoutineController routineController;
    
    public WorkoutController(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.routineController = new RoutineController(context);
    }
    
    // Add routine to weekly schedule
    public boolean addRoutineToWeek(Routine routine, Date targetDate) {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        Date weekStartDate = getWeekStartDate(targetDate);
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("user_id", currentUser.getUserId());
        values.put("routine_id", routine.getRoutineId());
        values.put("scheduled_date", dateToString(targetDate));
        values.put("is_completed", false);
        values.put("week_start_date", dateToString(weekStartDate));
        
        long result = db.insert("weekly_workouts", null, values);
        db.close();
        
        return result != -1;
    }
    
    // Get today's workout
    public WeeklyWorkout getTodayWorkout(int userId) {
        Date today = new Date();
        String todayString = dateToString(today);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ? AND scheduled_date = ?";
        String[] selectionArgs = {String.valueOf(userId), todayString};
        
        Cursor cursor = db.query("weekly_workouts", null, selection, selectionArgs, null, null, null);
        
        WeeklyWorkout todayWorkout = null;
        if (cursor != null && cursor.moveToFirst()) {
            todayWorkout = new WeeklyWorkout();
            todayWorkout.setWeeklyWorkoutId(cursor.getInt(cursor.getColumnIndexOrThrow("weekly_workout_id")));
            todayWorkout.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            todayWorkout.setRoutineId(cursor.getInt(cursor.getColumnIndexOrThrow("routine_id")));
            todayWorkout.setScheduledDate(stringToDate(cursor.getString(cursor.getColumnIndexOrThrow("scheduled_date"))));
            todayWorkout.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1);
            
            String completionTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("completion_time"));
            if (completionTimeStr != null) {
                todayWorkout.setCompletionTime(stringToDate(completionTimeStr));
            }
            
            todayWorkout.setWeekStartDate(stringToDate(cursor.getString(cursor.getColumnIndexOrThrow("week_start_date"))));
            
            // Load routine details
            Routine routine = routineController.getRoutineById(todayWorkout.getRoutineId());
            todayWorkout.setRoutine(routine);
            
            cursor.close();
        }
        
        db.close();
        return todayWorkout;
    }
    
    // Get current week's workouts
    public List<WeeklyWorkout> getCurrentWeekWorkouts(int userId) {
        List<WeeklyWorkout> workouts = new ArrayList<>();
        Date currentWeekStart = getWeekStartDate(new Date());
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ? AND week_start_date = ?";
        String[] selectionArgs = {String.valueOf(userId), dateToString(currentWeekStart)};
        
        Cursor cursor = db.query("weekly_workouts", null, selection, selectionArgs, 
                                null, null, "scheduled_date ASC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                WeeklyWorkout workout = new WeeklyWorkout();
                workout.setWeeklyWorkoutId(cursor.getInt(cursor.getColumnIndexOrThrow("weekly_workout_id")));
                workout.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                workout.setRoutineId(cursor.getInt(cursor.getColumnIndexOrThrow("routine_id")));
                workout.setScheduledDate(stringToDate(cursor.getString(cursor.getColumnIndexOrThrow("scheduled_date"))));
                workout.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1);
                
                String completionTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("completion_time"));
                if (completionTimeStr != null) {
                    workout.setCompletionTime(stringToDate(completionTimeStr));
                }
                
                workout.setWeekStartDate(stringToDate(cursor.getString(cursor.getColumnIndexOrThrow("week_start_date"))));
                
                // Load routine details
                Routine routine = routineController.getRoutineById(workout.getRoutineId());
                workout.setRoutine(routine);
                
                workouts.add(workout);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return workouts;
    }
    
    // Mark workout as completed
    public boolean markWorkoutCompleted(int weeklyWorkoutId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("is_completed", true);
        values.put("completion_time", dateToString(new Date()));
        
        int rowsAffected = db.update("weekly_workouts", values, "weekly_workout_id = ?", 
                                    new String[]{String.valueOf(weeklyWorkoutId)});
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Delete workout from weekly schedule
    public boolean deleteWeeklyWorkout(int weeklyWorkoutId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        int rowsAffected = db.delete("weekly_workouts", "weekly_workout_id = ?", 
                                    new String[]{String.valueOf(weeklyWorkoutId)});
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Clear current week's workouts (for shake reset)
    public boolean clearCurrentWeek() {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        Date currentWeekStart = getWeekStartDate(new Date());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        String whereClause = "user_id = ? AND week_start_date = ?";
        String[] whereArgs = {String.valueOf(currentUser.getUserId()), dateToString(currentWeekStart)};
        
        int rowsAffected = db.delete("weekly_workouts", whereClause, whereArgs);
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Get week start date (Monday)
    public Date getWeekStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        // Set to Monday (week start)
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        
        calendar.add(Calendar.DAY_OF_MONTH, -daysToSubtract);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        return calendar.getTime();
    }
    
    // Get workout completion statistics for current week
    public int[] getWeeklyStats(int userId) {
        List<WeeklyWorkout> weeklyWorkouts = getCurrentWeekWorkouts(userId);
        int totalWorkouts = weeklyWorkouts.size();
        int completedWorkouts = 0;
        
        for (WeeklyWorkout workout : weeklyWorkouts) {
            if (workout.isCompleted()) {
                completedWorkouts++;
            }
        }
        
        return new int[]{completedWorkouts, totalWorkouts};
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
}