package com.example.fitlife.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.fitlife.database.DatabaseHelper;
import com.example.fitlife.models.Equipment;
import com.example.fitlife.models.Routine;
import com.example.fitlife.models.RoutineExercise;
import com.example.fitlife.models.User;
import com.example.fitlife.models.WeeklyEquipmentChecklist;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EquipmentChecklistController {
    private DatabaseHelper dbHelper;
    private Context context;
    private WorkoutController workoutController;
    
    public EquipmentChecklistController(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.workoutController = new WorkoutController(context);
    }
    
    // Generate weekly equipment checklist from selected routines
    public void generateWeeklyChecklist(int userId, List<Routine> selectedRoutines) {
        Date weekStartDate = workoutController.getWeekStartDate(new Date());
        
        // Extract unique equipment from all exercises in selected routines
        Set<String> allEquipmentNames = extractEquipmentFromRoutines(selectedRoutines);
        
        // Get equipment objects by names
        List<Equipment> allEquipment = getEquipmentByNames(allEquipmentNames);
        
        // Clear existing checklist for current week
        clearWeeklyChecklist(userId, weekStartDate);
        
        // Create new checklist entries
        saveWeeklyChecklist(userId, allEquipment, weekStartDate);
    }
    
    // Extract unique equipment from routines
    private Set<String> extractEquipmentFromRoutines(List<Routine> routines) {
        Set<String> equipmentNames = new HashSet<>();
        
        for (Routine routine : routines) {
            for (RoutineExercise routineExercise : routine.getExercises()) {
                String equipmentNeeded = routineExercise.getExercise().getEquipmentNeeded();
                if (equipmentNeeded != null && !equipmentNeeded.trim().isEmpty()) {
                    // Split by comma and add each piece of equipment
                    String[] equipmentArray = equipmentNeeded.split(",");
                    for (String equipment : equipmentArray) {
                        equipmentNames.add(equipment.trim());
                    }
                }
            }
        }
        
        return equipmentNames;
    }
    
    // Get equipment objects by names
    private List<Equipment> getEquipmentByNames(Set<String> equipmentNames) {
        List<Equipment> equipmentList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        for (String equipmentName : equipmentNames) {
            String selection = "equipment_name = ?";
            String[] selectionArgs = {equipmentName};
            
            Cursor cursor = db.query("equipment", null, selection, selectionArgs, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                Equipment equipment = new Equipment();
                equipment.setEquipmentId(cursor.getInt(cursor.getColumnIndexOrThrow("equipment_id")));
                equipment.setEquipmentName(cursor.getString(cursor.getColumnIndexOrThrow("equipment_name")));
                equipment.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                equipment.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                
                equipmentList.add(equipment);
                cursor.close();
            } else {
                // Create new equipment entry if not found
                Equipment newEquipment = createEquipment(equipmentName, "accessories", "User-added equipment");
                if (newEquipment != null) {
                    equipmentList.add(newEquipment);
                }
            }
        }
        
        db.close();
        return equipmentList;
    }
    
    // Create new equipment
    private Equipment createEquipment(String equipmentName, String category, String description) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("equipment_name", equipmentName);
        values.put("category", category);
        values.put("description", description);
        
        long equipmentId = db.insert("equipment", null, values);
        db.close();
        
        if (equipmentId != -1) {
            Equipment equipment = new Equipment();
            equipment.setEquipmentId((int) equipmentId);
            equipment.setEquipmentName(equipmentName);
            equipment.setCategory(category);
            equipment.setDescription(description);
            return equipment;
        }
        
        return null;
    }
    
    // Clear existing weekly checklist
    private void clearWeeklyChecklist(int userId, Date weekStartDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        String whereClause = "user_id = ? AND week_start_date = ?";
        String[] whereArgs = {String.valueOf(userId), dateToString(weekStartDate)};
        
        db.delete("weekly_equipment_checklist", whereClause, whereArgs);
        db.close();
    }
    
    // Save weekly checklist
    private void saveWeeklyChecklist(int userId, List<Equipment> equipment, Date weekStartDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            db.beginTransaction();
            
            for (Equipment eq : equipment) {
                ContentValues values = new ContentValues();
                values.put("user_id", userId);
                values.put("equipment_id", eq.getEquipmentId());
                values.put("week_start_date", dateToString(weekStartDate));
                values.put("is_obtained", false);
                
                db.insert("weekly_equipment_checklist", null, values);
            }
            
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    
    // Get current week's equipment checklist
    public Map<String, List<WeeklyEquipmentChecklist>> getCurrentWeekChecklist(int userId) {
        Map<String, List<WeeklyEquipmentChecklist>> categorizedEquipment = new HashMap<>();
        Date weekStartDate = workoutController.getWeekStartDate(new Date());
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT wec.*, e.equipment_name, e.category, e.description " +
                       "FROM weekly_equipment_checklist wec " +
                       "JOIN equipment e ON wec.equipment_id = e.equipment_id " +
                       "WHERE wec.user_id = ? AND wec.week_start_date = ? " +
                       "ORDER BY e.category, e.equipment_name";
        
        Cursor cursor = db.rawQuery(query, new String[]{
            String.valueOf(userId), 
            dateToString(weekStartDate)
        });
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                WeeklyEquipmentChecklist checklistItem = new WeeklyEquipmentChecklist();
                checklistItem.setChecklistId(cursor.getInt(cursor.getColumnIndexOrThrow("checklist_id")));
                checklistItem.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                checklistItem.setEquipmentId(cursor.getInt(cursor.getColumnIndexOrThrow("equipment_id")));
                checklistItem.setWeekStartDate(stringToDate(cursor.getString(cursor.getColumnIndexOrThrow("week_start_date"))));
                checklistItem.setObtained(cursor.getInt(cursor.getColumnIndexOrThrow("is_obtained")) == 1);
                checklistItem.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
                
                // Create equipment object
                Equipment equipment = new Equipment();
                equipment.setEquipmentId(cursor.getInt(cursor.getColumnIndexOrThrow("equipment_id")));
                equipment.setEquipmentName(cursor.getString(cursor.getColumnIndexOrThrow("equipment_name")));
                equipment.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                equipment.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                
                checklistItem.setEquipment(equipment);
                
                // Group by category
                String category = equipment.getCategory();
                if (!categorizedEquipment.containsKey(category)) {
                    categorizedEquipment.put(category, new ArrayList<>());
                }
                categorizedEquipment.get(category).add(checklistItem);
                
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return categorizedEquipment;
    }
    
    // Mark equipment as obtained/not obtained
    public boolean updateEquipmentStatus(int checklistId, boolean isObtained) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("is_obtained", isObtained);
        
        int rowsAffected = db.update("weekly_equipment_checklist", values, "checklist_id = ?", 
                                    new String[]{String.valueOf(checklistId)});
        db.close();
        
        return rowsAffected > 0;
    }
    
    // Get equipment checklist statistics
    public int[] getChecklistStats(int userId) {
        Date weekStartDate = workoutController.getWeekStartDate(new Date());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = "user_id = ? AND week_start_date = ?";
        String[] selectionArgs = {String.valueOf(userId), dateToString(weekStartDate)};
        
        Cursor cursor = db.query("weekly_equipment_checklist", null, selection, selectionArgs, null, null, null);
        
        int totalItems = 0;
        int obtainedItems = 0;
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                totalItems++;
                if (cursor.getInt(cursor.getColumnIndexOrThrow("is_obtained")) == 1) {
                    obtainedItems++;
                }
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return new int[]{obtainedItems, totalItems};
    }
    
    // Get all equipment categories
    public List<String> getEquipmentCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT DISTINCT category FROM equipment ORDER BY category";
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return categories;
    }
    
    // Format equipment list for SMS sharing
    public String formatEquipmentForSMS(Map<String, List<WeeklyEquipmentChecklist>> categorizedEquipment) {
        StringBuilder smsContent = new StringBuilder();
        smsContent.append("FitLife Weekly Equipment:\n\n");
        
        for (Map.Entry<String, List<WeeklyEquipmentChecklist>> entry : categorizedEquipment.entrySet()) {
            String categoryIcon = getCategoryIcon(entry.getKey());
            smsContent.append(categoryIcon).append(" ").append(entry.getKey().toUpperCase()).append(":\n");
            
            for (WeeklyEquipmentChecklist item : entry.getValue()) {
                String checkMark = item.isObtained() ? "✅" : "☐";
                smsContent.append(checkMark).append(" ").append(item.getEquipment().getEquipmentName()).append("\n");
            }
            smsContent.append("\n");
        }
        
        smsContent.append("Check off items as you gather them!");
        return smsContent.toString();
    }
    
    // Get category icon for SMS
    private String getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "strength":
                return "🏋️";
            case "cardio":
                return "🏃";
            case "flexibility":
                return "🧘";
            case "accessories":
            default:
                return "🎯";
        }
    }
    
    // Helper methods
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