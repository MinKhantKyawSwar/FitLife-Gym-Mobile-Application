package com.example.fitlife.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "fitlife.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table creation SQL statements
    
    private static final String CREATE_USERS_TABLE = 
        "CREATE TABLE users (" +
        "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "username TEXT UNIQUE NOT NULL, " +
        "email TEXT UNIQUE NOT NULL, " +
        "password_hash TEXT NOT NULL, " +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
        ");";
    
    private static final String CREATE_EXERCISES_TABLE = 
        "CREATE TABLE exercises (" +
        "exercise_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "user_id INTEGER, " +
        "exercise_name TEXT NOT NULL, " +
        "equipment_needed TEXT, " +
        "instructions TEXT, " +
        "image_path TEXT, " +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
        ");";
    
    private static final String CREATE_ROUTINES_TABLE = 
        "CREATE TABLE routines (" +
        "routine_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "user_id INTEGER, " +
        "routine_name TEXT NOT NULL, " +
        "description TEXT, " +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
        ");";
    
    private static final String CREATE_ROUTINE_EXERCISES_TABLE = 
        "CREATE TABLE routine_exercises (" +
        "routine_exercise_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "routine_id INTEGER, " +
        "exercise_id INTEGER, " +
        "sets INTEGER DEFAULT 1, " +
        "reps INTEGER DEFAULT 1, " +
        "rest_seconds INTEGER DEFAULT 60, " +
        "FOREIGN KEY (routine_id) REFERENCES routines(routine_id), " +
        "FOREIGN KEY (exercise_id) REFERENCES exercises(exercise_id)" +
        ");";
    
    private static final String CREATE_WEEKLY_WORKOUTS_TABLE = 
        "CREATE TABLE weekly_workouts (" +
        "weekly_workout_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "user_id INTEGER, " +
        "routine_id INTEGER, " +
        "scheduled_date DATE, " +
        "is_completed BOOLEAN DEFAULT 0, " +
        "completion_time TIMESTAMP, " +
        "week_start_date DATE, " +
        "FOREIGN KEY (user_id) REFERENCES users(user_id), " +
        "FOREIGN KEY (routine_id) REFERENCES routines(routine_id)" +
        ");";
    
    private static final String CREATE_EQUIPMENT_TABLE = 
        "CREATE TABLE equipment (" +
        "equipment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "equipment_name TEXT UNIQUE NOT NULL, " +
        "category TEXT NOT NULL, " +
        "description TEXT, " +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
        ");";
    
    private static final String CREATE_WEEKLY_EQUIPMENT_CHECKLIST_TABLE = 
        "CREATE TABLE weekly_equipment_checklist (" +
        "checklist_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "user_id INTEGER, " +
        "equipment_id INTEGER, " +
        "week_start_date DATE, " +
        "is_obtained BOOLEAN DEFAULT 0, " +
        "notes TEXT, " +
        "FOREIGN KEY (user_id) REFERENCES users(user_id), " +
        "FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id)" +
        ");";
    
    private static final String CREATE_WORKOUT_HISTORY_TABLE = 
        "CREATE TABLE workout_history (" +
        "history_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "user_id INTEGER, " +
        "routine_id INTEGER, " +
        "workout_date DATE NOT NULL, " +
        "start_time TIMESTAMP, " +
        "completion_time TIMESTAMP, " +
        "total_duration_minutes INTEGER, " +
        "exercises_completed INTEGER, " +
        "notes TEXT, " +
        "FOREIGN KEY (user_id) REFERENCES users(user_id), " +
        "FOREIGN KEY (routine_id) REFERENCES routines(routine_id)" +
        ");";
    
    private static final String CREATE_EXERCISE_COMPLETIONS_TABLE = 
        "CREATE TABLE exercise_completions (" +
        "completion_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "history_id INTEGER, " +
        "exercise_id INTEGER, " +
        "sets_completed INTEGER, " +
        "reps_completed INTEGER, " +
        "weight_used REAL, " +
        "completion_time TIMESTAMP, " +
        "FOREIGN KEY (history_id) REFERENCES workout_history(history_id), " +
        "FOREIGN KEY (exercise_id) REFERENCES exercises(exercise_id)" +
        ");";
    
    private static final String CREATE_USER_PREFERENCES_TABLE = 
        "CREATE TABLE user_preferences (" +
        "preference_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "user_id INTEGER UNIQUE, " +
        "theme_preference TEXT DEFAULT 'light', " +
        "notification_enabled BOOLEAN DEFAULT 1, " +
        "default_rest_time INTEGER DEFAULT 60, " +
        "preferred_units TEXT DEFAULT 'metric', " +
        "shake_sensitivity INTEGER DEFAULT 2, " +
        "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
        ");";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all tables
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_EXERCISES_TABLE);
        db.execSQL(CREATE_ROUTINES_TABLE);
        db.execSQL(CREATE_ROUTINE_EXERCISES_TABLE);
        db.execSQL(CREATE_WEEKLY_WORKOUTS_TABLE);
        db.execSQL(CREATE_EQUIPMENT_TABLE);
        db.execSQL(CREATE_WEEKLY_EQUIPMENT_CHECKLIST_TABLE);
        db.execSQL(CREATE_WORKOUT_HISTORY_TABLE);
        db.execSQL(CREATE_EXERCISE_COMPLETIONS_TABLE);
        db.execSQL(CREATE_USER_PREFERENCES_TABLE);
        
        // Insert predefined equipment data
        insertPredefinedEquipment(db);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop all tables and recreate
        db.execSQL("DROP TABLE IF EXISTS exercise_completions");
        db.execSQL("DROP TABLE IF EXISTS workout_history");
        db.execSQL("DROP TABLE IF EXISTS weekly_equipment_checklist");
        db.execSQL("DROP TABLE IF EXISTS equipment");
        db.execSQL("DROP TABLE IF EXISTS weekly_workouts");
        db.execSQL("DROP TABLE IF EXISTS routine_exercises");
        db.execSQL("DROP TABLE IF EXISTS routines");
        db.execSQL("DROP TABLE IF EXISTS exercises");
        db.execSQL("DROP TABLE IF EXISTS user_preferences");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
    
    private void insertPredefinedEquipment(SQLiteDatabase db) {
        // Strength Equipment
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Dumbbells', 'strength', 'Various weight dumbbells')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Barbell', 'strength', 'Olympic barbell with plates')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Kettlebells', 'strength', 'Various weight kettlebells')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Resistance Bands', 'strength', 'Elastic resistance bands')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Pull-up Bar', 'strength', 'Doorway or wall-mounted pull-up bar')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Weight Bench', 'strength', 'Adjustable weight bench')");
        
        // Cardio Equipment
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Jump Rope', 'cardio', 'Speed jump rope')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Treadmill', 'cardio', 'Electric treadmill')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Stationary Bike', 'cardio', 'Indoor cycling bike')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Rowing Machine', 'cardio', 'Indoor rowing machine')");
        
        // Flexibility Equipment
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Yoga Mat', 'flexibility', 'Non-slip yoga mat')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Yoga Blocks', 'flexibility', 'Support blocks for yoga')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Yoga Straps', 'flexibility', 'Stretching assistance straps')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Foam Roller', 'flexibility', 'Muscle recovery foam roller')");
        
        // Accessories
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Water Bottle', 'accessories', 'Hydration water bottle')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Workout Towel', 'accessories', 'Sweat towel')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Gym Gloves', 'accessories', 'Grip and protection gloves')");
        db.execSQL("INSERT INTO equipment (equipment_name, category, description) VALUES ('Heart Rate Monitor', 'accessories', 'Wearable heart rate tracker')");
    }
}