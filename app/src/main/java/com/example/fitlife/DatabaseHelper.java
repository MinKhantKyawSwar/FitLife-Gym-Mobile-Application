package com.example.fitlife;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database helper for FitLife app
 * Manages all database operations for users, exercises, workouts, and related data
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "FitLife.db";
    private static final int DATABASE_VERSION = 4;

    // Table names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_USER_DETAILS = "user_details";
    private static final String TABLE_EXERCISES = "exercises";
    private static final String TABLE_EXERCISE_EQUIPMENT = "exercise_equipment";
    private static final String TABLE_EXERCISE_INSTRUCTIONS = "exercise_instructions";
    private static final String TABLE_WORKOUT_ROUTINES = "workout_routines";
    private static final String TABLE_WORKOUT_EXERCISES = "workout_exercises";
    private static final String TABLE_CURRENT_WORKOUTS = "current_workouts";
    private static final String TABLE_CURRENT_WORKOUT_EXERCISE_STATUS = "current_workout_exercise_status";
    private static final String TABLE_CURRENT_WORKOUT_REMOVED_EXERCISES = "current_workout_removed_exercises";
    private static final String TABLE_USER_STATS = "user_stats";

    // Users table columns
    private static final String COL_USER_ID = "user_id";
    private static final String COL_USERNAME_EMAIL = "username_email";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";

    // User details table columns
    private static final String COL_AGE = "age";
    private static final String COL_GENDER = "gender";
    private static final String COL_HEIGHT = "height";
    private static final String COL_WEIGHT = "weight";

    // Exercises table columns
    private static final String COL_EXERCISE_ID = "exercise_id";
    private static final String COL_EXERCISE_NAME = "name";
    private static final String COL_SETS = "sets";
    private static final String COL_REPS = "reps";
    private static final String COL_REST_TIME = "rest_time";
    private static final String COL_IMAGE_PATH = "image_path";

    // Exercise equipment table columns
    private static final String COL_EQUIPMENT_NAME = "equipment_name";

    // Exercise instructions table columns
    private static final String COL_INSTRUCTION_TEXT = "instruction_text";
    private static final String COL_INSTRUCTION_ORDER = "instruction_order";

    // Workout routines table columns
    private static final String COL_WORKOUT_ID = "workout_id";
    private static final String COL_WORKOUT_NAME = "name";
    private static final String COL_CREATED_DATE = "created_date";

    // Current workouts table columns
    private static final String COL_STATUS = "status";
    private static final String COL_STARTED_DATE = "started_date";

    // User stats table columns
    private static final String COL_TOTAL_SESSIONS = "total_sessions";
    private static final String COL_TOTAL_ROUTINES = "total_routines";
    private static final String COL_TOTAL_EXERCISES = "total_exercises";
    private static final String COL_ACTIVE_DAYS = "active_days";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COL_USERNAME + " TEXT, " +
                COL_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(createUsersTable);

        // Create user_details table
        String createUserDetailsTable = "CREATE TABLE " + TABLE_USER_DETAILS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY, " +
                COL_AGE + " INTEGER, " +
                COL_GENDER + " TEXT, " +
                COL_HEIGHT + " REAL, " +
                COL_WEIGHT + " REAL, " +
                "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createUserDetailsTable);

        // Create exercises table
        String createExercisesTable = "CREATE TABLE " + TABLE_EXERCISES + " (" +
                COL_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EXERCISE_NAME + " TEXT NOT NULL, " +
                COL_SETS + " INTEGER, " +
                COL_REPS + " TEXT, " +
                COL_REST_TIME + " TEXT, " +
                COL_IMAGE_PATH + " TEXT)";
        db.execSQL(createExercisesTable);

        // Create exercise_equipment table
        String createExerciseEquipmentTable = "CREATE TABLE " + TABLE_EXERCISE_EQUIPMENT + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EXERCISE_ID + " INTEGER NOT NULL, " +
                COL_EQUIPMENT_NAME + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + COL_EXERCISE_ID + "))";
        db.execSQL(createExerciseEquipmentTable);

        // Create exercise_instructions table
        String createExerciseInstructionsTable = "CREATE TABLE " + TABLE_EXERCISE_INSTRUCTIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EXERCISE_ID + " INTEGER NOT NULL, " +
                COL_INSTRUCTION_TEXT + " TEXT NOT NULL, " +
                COL_INSTRUCTION_ORDER + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + COL_EXERCISE_ID + "))";
        db.execSQL(createExerciseInstructionsTable);

        // Create workout_routines table
        String createWorkoutRoutinesTable = "CREATE TABLE " + TABLE_WORKOUT_ROUTINES + " (" +
                COL_WORKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_WORKOUT_NAME + " TEXT NOT NULL, " +
                COL_USER_ID + " INTEGER NOT NULL, " +
                COL_CREATED_DATE + " TEXT, " +
                "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createWorkoutRoutinesTable);

        // Create workout_exercises table (junction table)
        String createWorkoutExercisesTable = "CREATE TABLE " + TABLE_WORKOUT_EXERCISES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_WORKOUT_ID + " INTEGER NOT NULL, " +
                COL_EXERCISE_ID + " INTEGER NOT NULL, " +
                COL_SETS + " INTEGER, " +
                COL_REPS + " TEXT, " +
                "FOREIGN KEY(" + COL_WORKOUT_ID + ") REFERENCES " + TABLE_WORKOUT_ROUTINES + "(" + COL_WORKOUT_ID + "), " +
                "FOREIGN KEY(" + COL_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + COL_EXERCISE_ID + "))";
        db.execSQL(createWorkoutExercisesTable);

        // Create current_workouts table
        String createCurrentWorkoutsTable = "CREATE TABLE " + TABLE_CURRENT_WORKOUTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_WORKOUT_ID + " INTEGER NOT NULL, " +
                COL_USER_ID + " INTEGER NOT NULL, " +
                COL_STATUS + " TEXT DEFAULT 'pending', " +
                COL_STARTED_DATE + " TEXT, " +
                "FOREIGN KEY(" + COL_WORKOUT_ID + ") REFERENCES " + TABLE_WORKOUT_ROUTINES + "(" + COL_WORKOUT_ID + "), " +
                "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createCurrentWorkoutsTable);

        // Create current_workout_exercise_status table (per-exercise completion in current session)
        String createCurrentWorkoutExerciseStatusTable = "CREATE TABLE " + TABLE_CURRENT_WORKOUT_EXERCISE_STATUS + " (" +
                COL_WORKOUT_ID + " INTEGER NOT NULL, " +
                COL_USER_ID + " INTEGER NOT NULL, " +
                COL_EXERCISE_ID + " INTEGER NOT NULL, " +
                COL_STATUS + " TEXT DEFAULT 'Pending', " +
                "PRIMARY KEY (" + COL_WORKOUT_ID + ", " + COL_USER_ID + ", " + COL_EXERCISE_ID + "), " +
                "FOREIGN KEY(" + COL_WORKOUT_ID + ") REFERENCES " + TABLE_WORKOUT_ROUTINES + "(" + COL_WORKOUT_ID + "), " +
                "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "), " +
                "FOREIGN KEY(" + COL_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + COL_EXERCISE_ID + "))";
        db.execSQL(createCurrentWorkoutExerciseStatusTable);

        // Exercises "removed" from current session view only (not from actual workout routine)
        String createCurrentWorkoutRemovedExercisesTable = "CREATE TABLE " + TABLE_CURRENT_WORKOUT_REMOVED_EXERCISES + " (" +
                COL_WORKOUT_ID + " INTEGER NOT NULL, " +
                COL_USER_ID + " INTEGER NOT NULL, " +
                COL_EXERCISE_ID + " INTEGER NOT NULL, " +
                "PRIMARY KEY (" + COL_WORKOUT_ID + ", " + COL_USER_ID + ", " + COL_EXERCISE_ID + "))";
        db.execSQL(createCurrentWorkoutRemovedExercisesTable);

        // Create user_stats table
        String createUserStatsTable = "CREATE TABLE " + TABLE_USER_STATS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY, " +
                COL_TOTAL_SESSIONS + " INTEGER DEFAULT 0, " +
                COL_TOTAL_ROUTINES + " INTEGER DEFAULT 0, " +
                COL_TOTAL_EXERCISES + " INTEGER DEFAULT 0, " +
                COL_ACTIVE_DAYS + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createUserStatsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_USERNAME + " TEXT");
                db.execSQL("UPDATE " + TABLE_USERS + " SET " + COL_USERNAME + " = " + COL_USERNAME_EMAIL + " WHERE " + COL_USERNAME + " IS NULL");
            } catch (Exception e) {
                // Column may already exist
            }
        }
        if (oldVersion < 3) {
            String createCurrentWorkoutRemovedExercisesTable = "CREATE TABLE IF NOT EXISTS " + TABLE_CURRENT_WORKOUT_REMOVED_EXERCISES + " (" +
                    COL_WORKOUT_ID + " INTEGER NOT NULL, " +
                    COL_USER_ID + " INTEGER NOT NULL, " +
                    COL_EXERCISE_ID + " INTEGER NOT NULL, " +
                    "PRIMARY KEY (" + COL_WORKOUT_ID + ", " + COL_USER_ID + ", " + COL_EXERCISE_ID + "))";
            db.execSQL(createCurrentWorkoutRemovedExercisesTable);
        }
        if (oldVersion < 2) {
            String createCurrentWorkoutExerciseStatusTable = "CREATE TABLE " + TABLE_CURRENT_WORKOUT_EXERCISE_STATUS + " (" +
                    COL_WORKOUT_ID + " INTEGER NOT NULL, " +
                    COL_USER_ID + " INTEGER NOT NULL, " +
                    COL_EXERCISE_ID + " INTEGER NOT NULL, " +
                    COL_STATUS + " TEXT DEFAULT 'Pending', " +
                    "PRIMARY KEY (" + COL_WORKOUT_ID + ", " + COL_USER_ID + ", " + COL_EXERCISE_ID + "), " +
                    "FOREIGN KEY(" + COL_WORKOUT_ID + ") REFERENCES " + TABLE_WORKOUT_ROUTINES + "(" + COL_WORKOUT_ID + "), " +
                    "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "), " +
                    "FOREIGN KEY(" + COL_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + COL_EXERCISE_ID + "))";
            db.execSQL(createCurrentWorkoutExerciseStatusTable);
        }
        if (oldVersion < 1) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_STATS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENT_WORKOUTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT_EXERCISES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT_ROUTINES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISE_INSTRUCTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISE_EQUIPMENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DETAILS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        }
    }

    // User operations
    public long insertUser(String usernameEmail, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME_EMAIL, usernameEmail);
        values.put(COL_PASSWORD, password);
        long userId = db.insert(TABLE_USERS, null, values);
        db.close();
        return userId;
    }

    public boolean checkUser(String usernameEmail, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{usernameEmail, password},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public int getUserId(String usernameEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME_EMAIL + "=?",
                new String[]{usernameEmail},
                null, null, null);
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    public boolean userExists(String usernameEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME_EMAIL + "=?",
                new String[]{usernameEmail},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // User details operations
    public long insertUserDetails(int userId, int age, String gender, double height, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, userId);
        values.put(COL_AGE, age);
        values.put(COL_GENDER, gender);
        values.put(COL_HEIGHT, height);
        values.put(COL_WEIGHT, weight);
        long result = db.insert(TABLE_USER_DETAILS, null, values);
        db.close();
        return result;
    }

    public boolean updateUserDetails(int userId, int age, String gender, double height, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_AGE, age);
        values.put(COL_GENDER, gender);
        values.put(COL_HEIGHT, height);
        values.put(COL_WEIGHT, weight);
        int rowsAffected = db.update(TABLE_USER_DETAILS, values, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    public Cursor getUserDetails(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USER_DETAILS,
                null,
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
    }

    /**
     * Get account info (username, email) for profile edit.
     * Returns Cursor with columns: username_email (email), username
     */
    public Cursor getAccountInfo(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS,
                new String[]{COL_USERNAME_EMAIL, COL_USERNAME},
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
    }

    /**
     * Update user profile: username, email (username_email), and optionally password.
     * If hashedPassword is null, password is not changed.
     */
    public boolean updateUserProfile(int userId, String username, String email, String hashedPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_USERNAME_EMAIL, email);
        if (hashedPassword != null && !hashedPassword.isEmpty()) {
            values.put(COL_PASSWORD, hashedPassword);
        }
        int rowsAffected = db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean userDetailsExist(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER_DETAILS,
                new String[]{COL_USER_ID},
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Exercise operations
    public long insertExercise(String name, int sets, String reps, String restTime, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EXERCISE_NAME, name);
        values.put(COL_SETS, sets);
        values.put(COL_REPS, reps);
        values.put(COL_REST_TIME, restTime);
        values.put(COL_IMAGE_PATH, imagePath);
        long exerciseId = db.insert(TABLE_EXERCISES, null, values);
        db.close();
        return exerciseId;
    }

    public void insertExerciseEquipment(long exerciseId, String equipmentName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EXERCISE_ID, exerciseId);
        values.put(COL_EQUIPMENT_NAME, equipmentName);
        db.insert(TABLE_EXERCISE_EQUIPMENT, null, values);
        db.close();
    }

    public void insertExerciseInstruction(long exerciseId, String instructionText, int order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EXERCISE_ID, exerciseId);
        values.put(COL_INSTRUCTION_TEXT, instructionText);
        values.put(COL_INSTRUCTION_ORDER, order);
        db.insert(TABLE_EXERCISE_INSTRUCTIONS, null, values);
        db.close();
    }

    public Cursor getAllExercises() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EXERCISES,
                null,
                null, null, null, null,
                COL_EXERCISE_NAME + " ASC");
    }

    public Cursor getExercise(long exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EXERCISES,
                null,
                COL_EXERCISE_ID + "=?",
                new String[]{String.valueOf(exerciseId)},
                null, null, null);
    }

    public List<String> getExerciseEquipment(long exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> equipment = new ArrayList<>();
        Cursor cursor = db.query(TABLE_EXERCISE_EQUIPMENT,
                new String[]{COL_EQUIPMENT_NAME},
                COL_EXERCISE_ID + "=?",
                new String[]{String.valueOf(exerciseId)},
                null, null, null);
        if (cursor.moveToFirst()) {
            do {
                equipment.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return equipment;
    }

    public List<String> getExerciseInstructions(long exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> instructions = new ArrayList<>();
        Cursor cursor = db.query(TABLE_EXERCISE_INSTRUCTIONS,
                new String[]{COL_INSTRUCTION_TEXT},
                COL_EXERCISE_ID + "=?",
                new String[]{String.valueOf(exerciseId)},
                null, null,
                COL_INSTRUCTION_ORDER + " ASC");
        if (cursor.moveToFirst()) {
            do {
                instructions.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return instructions;
    }

    public boolean updateExercise(long exerciseId, String name, int sets, String reps, String restTime, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EXERCISE_NAME, name);
        values.put(COL_SETS, sets);
        values.put(COL_REPS, reps);
        values.put(COL_REST_TIME, restTime);
        if (imagePath != null) {
            values.put(COL_IMAGE_PATH, imagePath);
        }
        int rowsAffected = db.update(TABLE_EXERCISES, values, COL_EXERCISE_ID + "=?", new String[]{String.valueOf(exerciseId)});
        db.close();
        return rowsAffected > 0;
    }

    public void deleteExerciseEquipment(long exerciseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXERCISE_EQUIPMENT, COL_EXERCISE_ID + "=?", new String[]{String.valueOf(exerciseId)});
        db.close();
    }

    public void deleteExerciseInstructions(long exerciseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXERCISE_INSTRUCTIONS, COL_EXERCISE_ID + "=?", new String[]{String.valueOf(exerciseId)});
        db.close();
    }

    public boolean deleteExercise(long exerciseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_EXERCISE_EQUIPMENT, COL_EXERCISE_ID + "=?", new String[]{String.valueOf(exerciseId)});
            db.delete(TABLE_EXERCISE_INSTRUCTIONS, COL_EXERCISE_ID + "=?", new String[]{String.valueOf(exerciseId)});
            db.delete(TABLE_WORKOUT_EXERCISES, COL_EXERCISE_ID + "=?", new String[]{String.valueOf(exerciseId)});
            int rowsAffected = db.delete(TABLE_EXERCISES, COL_EXERCISE_ID + "=?", new String[]{String.valueOf(exerciseId)});
            return rowsAffected > 0;
        } finally {
            db.close();
        }
    }

    // Workout routine operations
    public long insertWorkoutRoutine(String name, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WORKOUT_NAME, name);
        values.put(COL_USER_ID, userId);
        values.put(COL_CREATED_DATE, String.valueOf(System.currentTimeMillis()));
        long workoutId = db.insert(TABLE_WORKOUT_ROUTINES, null, values);
        incrementUserStat(userId, COL_TOTAL_ROUTINES);
        db.close();
        return workoutId;
    }

    public Cursor getUserWorkouts(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_WORKOUT_ROUTINES,
                null,
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null,
                COL_CREATED_DATE + " DESC");
    }

    public Cursor getWorkout(long workoutId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_WORKOUT_ROUTINES,
                null,
                COL_WORKOUT_ID + "=?",
                new String[]{String.valueOf(workoutId)},
                null, null, null);
    }

    public boolean updateWorkoutRoutine(long workoutId, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WORKOUT_NAME, name);
        int rowsAffected = db.update(TABLE_WORKOUT_ROUTINES, values, COL_WORKOUT_ID + "=?", new String[]{String.valueOf(workoutId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteWorkoutRoutine(long workoutId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete related workout exercises
        db.delete(TABLE_WORKOUT_EXERCISES, COL_WORKOUT_ID + "=?", new String[]{String.valueOf(workoutId)});
        // Delete from current workouts
        db.delete(TABLE_CURRENT_WORKOUTS, COL_WORKOUT_ID + "=?", new String[]{String.valueOf(workoutId)});
        // Delete workout routine
        int rowsAffected = db.delete(TABLE_WORKOUT_ROUTINES, COL_WORKOUT_ID + "=?", new String[]{String.valueOf(workoutId)});
        db.close();
        return rowsAffected > 0;
    }

    // Workout exercises operations
    public long insertWorkoutExercise(long workoutId, long exerciseId, int sets, String reps) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WORKOUT_ID, workoutId);
        values.put(COL_EXERCISE_ID, exerciseId);
        values.put(COL_SETS, sets);
        values.put(COL_REPS, reps);
        long id = db.insert(TABLE_WORKOUT_EXERCISES, null, values);
        db.close();
        return id;
    }

    public Cursor getWorkoutExercises(long workoutId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT we." + COL_EXERCISE_ID + ", e." + COL_EXERCISE_NAME + ", we." + COL_SETS + ", we." + COL_REPS +
                ", e." + COL_REST_TIME +
                " FROM " + TABLE_WORKOUT_EXERCISES + " we " +
                "INNER JOIN " + TABLE_EXERCISES + " e ON we." + COL_EXERCISE_ID + " = e." + COL_EXERCISE_ID +
                " WHERE we." + COL_WORKOUT_ID + "=?";
        return db.rawQuery(query, new String[]{String.valueOf(workoutId)});
    }

    public boolean deleteWorkoutExercise(long workoutId, long exerciseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_WORKOUT_EXERCISES,
                COL_WORKOUT_ID + "=? AND " + COL_EXERCISE_ID + "=?",
                new String[]{String.valueOf(workoutId), String.valueOf(exerciseId)});
        db.close();
        return rowsAffected > 0;
    }

    // Current workouts operations
    public long insertCurrentWorkout(long workoutId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Check if already exists
        Cursor cursor = db.query(TABLE_CURRENT_WORKOUTS,
                new String[]{"id"},
                COL_WORKOUT_ID + "=? AND " + COL_USER_ID + "=?",
                new String[]{String.valueOf(workoutId), String.valueOf(userId)},
                null, null, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return -1; // Already exists
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_WORKOUT_ID, workoutId);
        values.put(COL_USER_ID, userId);
        values.put(COL_STATUS, "pending");
        values.put(COL_STARTED_DATE, String.valueOf(System.currentTimeMillis()));
        long id = db.insert(TABLE_CURRENT_WORKOUTS, null, values);
        incrementUserStat(userId, COL_TOTAL_SESSIONS);
        db.close();
        return id;
    }

    public Cursor getCurrentWorkouts(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT cw.id, cw." + COL_WORKOUT_ID + ", wr." + COL_WORKOUT_NAME + ", cw." + COL_STATUS +
                " FROM " + TABLE_CURRENT_WORKOUTS + " cw " +
                "INNER JOIN " + TABLE_WORKOUT_ROUTINES + " wr ON cw." + COL_WORKOUT_ID + " = wr." + COL_WORKOUT_ID +
                " WHERE cw." + COL_USER_ID + "=? " +
                "ORDER BY cw." + COL_STARTED_DATE + " DESC";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    public Cursor getCurrentWorkoutExercises(long workoutId, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT we." + COL_EXERCISE_ID + ", e." + COL_EXERCISE_NAME + ", we." + COL_SETS + ", we." + COL_REPS +
                ", COALESCE(ces." + COL_STATUS + ", 'Pending') AS exercise_status " +
                " FROM " + TABLE_WORKOUT_EXERCISES + " we " +
                "INNER JOIN " + TABLE_EXERCISES + " e ON we." + COL_EXERCISE_ID + " = e." + COL_EXERCISE_ID +
                " LEFT JOIN " + TABLE_CURRENT_WORKOUT_EXERCISE_STATUS + " ces ON we." + COL_WORKOUT_ID + "=ces." + COL_WORKOUT_ID +
                " AND we." + COL_EXERCISE_ID + "=ces." + COL_EXERCISE_ID + " AND ces." + COL_USER_ID + "=? " +
                " WHERE we." + COL_WORKOUT_ID + "=? AND we." + COL_EXERCISE_ID + " NOT IN (" +
                "SELECT " + COL_EXERCISE_ID + " FROM " + TABLE_CURRENT_WORKOUT_REMOVED_EXERCISES +
                " WHERE " + COL_WORKOUT_ID + "=? AND " + COL_USER_ID + "=?) " +
                "ORDER BY we.id";
        return db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(workoutId), String.valueOf(workoutId), String.valueOf(userId)});
    }

    public void addCurrentWorkoutRemovedExercise(long workoutId, int userId, long exerciseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WORKOUT_ID, workoutId);
        values.put(COL_USER_ID, userId);
        values.put(COL_EXERCISE_ID, exerciseId);
        db.insertWithOnConflict(TABLE_CURRENT_WORKOUT_REMOVED_EXERCISES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public void setCurrentWorkoutExerciseStatus(long workoutId, int userId, long exerciseId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WORKOUT_ID, workoutId);
        values.put(COL_USER_ID, userId);
        values.put(COL_EXERCISE_ID, exerciseId);
        values.put(COL_STATUS, status);
        db.insertWithOnConflict(TABLE_CURRENT_WORKOUT_EXERCISE_STATUS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void clearCurrentWorkoutExerciseStatus(long workoutId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CURRENT_WORKOUT_EXERCISE_STATUS,
                COL_WORKOUT_ID + "=? AND " + COL_USER_ID + "=?",
                new String[]{String.valueOf(workoutId), String.valueOf(userId)});
        db.close();
    }

    public boolean updateCurrentWorkoutStatus(long workoutId, int userId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        int rowsAffected = db.update(TABLE_CURRENT_WORKOUTS, values,
                COL_WORKOUT_ID + "=? AND " + COL_USER_ID + "=?",
                new String[]{String.valueOf(workoutId), String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteCurrentWorkout(long workoutId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CURRENT_WORKOUT_EXERCISE_STATUS,
                COL_WORKOUT_ID + "=? AND " + COL_USER_ID + "=?",
                new String[]{String.valueOf(workoutId), String.valueOf(userId)});
        db.delete(TABLE_CURRENT_WORKOUT_REMOVED_EXERCISES,
                COL_WORKOUT_ID + "=? AND " + COL_USER_ID + "=?",
                new String[]{String.valueOf(workoutId), String.valueOf(userId)});
        int rowsAffected = db.delete(TABLE_CURRENT_WORKOUTS,
                COL_WORKOUT_ID + "=? AND " + COL_USER_ID + "=?",
                new String[]{String.valueOf(workoutId), String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    public void deleteAllCurrentWorkouts(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CURRENT_WORKOUT_EXERCISE_STATUS, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.delete(TABLE_CURRENT_WORKOUT_REMOVED_EXERCISES, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.delete(TABLE_CURRENT_WORKOUTS, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
    }

    // User stats operations
    public void initializeUserStats(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, userId);
        values.put(COL_TOTAL_SESSIONS, 0);
        values.put(COL_TOTAL_ROUTINES, 0);
        values.put(COL_TOTAL_EXERCISES, 0);
        values.put(COL_ACTIVE_DAYS, 0);
        db.insert(TABLE_USER_STATS, null, values);
        db.close();
    }

    public void incrementUserStat(int userId, String statColumn) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Check if stats exist
        Cursor cursor = db.query(TABLE_USER_STATS,
                new String[]{COL_USER_ID},
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            initializeUserStats(userId);
        } else {
            cursor.close();
        }

        db.execSQL("UPDATE " + TABLE_USER_STATS + " SET " + statColumn + " = " + statColumn + " + 1 WHERE " + COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        db.close();
    }

    public Cursor getUserStats(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USER_STATS,
                null,
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
    }

    // Hybrid stats calculation - verify with actual database queries
    public int getActualTotalSessions(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CURRENT_WORKOUTS + " WHERE " + COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public int getActualTotalRoutines(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_WORKOUT_ROUTINES + " WHERE " + COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public int getActualTotalExercises(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(DISTINCT we." + COL_EXERCISE_ID + ") " +
                "FROM " + TABLE_WORKOUT_EXERCISES + " we " +
                "INNER JOIN " + TABLE_WORKOUT_ROUTINES + " wr ON we." + COL_WORKOUT_ID + " = wr." + COL_WORKOUT_ID +
                " WHERE wr." + COL_USER_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
}
