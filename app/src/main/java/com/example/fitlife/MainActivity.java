package com.example.fitlife;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fitlife.activities.AuthActivity;
import com.example.fitlife.controllers.AuthController;
import com.example.fitlife.controllers.UserPreferencesController;
import com.example.fitlife.fragments.AddExerciseFragment;
import com.example.fitlife.fragments.CreateFragment;
import com.example.fitlife.fragments.HomeFragment;
import com.example.fitlife.fragments.ManageItemsFragment;
import com.example.fitlife.fragments.MyWorkoutsFragment;
import com.example.fitlife.fragments.ProfileFragment;
import com.example.fitlife.fragments.WorkoutsFragment;
import com.example.fitlife.fragments.WorkoutDetailFragment;
import com.example.fitlife.models.Routine;
import com.example.fitlife.models.WeeklyWorkout;
import com.example.fitlife.models.User;
import com.example.fitlife.utils.PermissionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    
    private AuthController authController;
    private UserPreferencesController preferencesController;
    private BottomNavigationView bottomNavigation;
    
    // Fragment instances
    private HomeFragment homeFragment;
    private MyWorkoutsFragment myWorkoutsFragment;
    private WorkoutsFragment workoutsFragment;
    private CreateFragment createFragment;
    private ManageItemsFragment manageItemsFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        authController = new AuthController(this);
        preferencesController = new UserPreferencesController(this);
        
        // Check if user is logged in
        if (!authController.isLoggedIn()) {
            navigateToAuth();
            return;
        }
        
        // Apply saved theme
        applySavedTheme();
        
        initializeViews();
        setupBottomNavigation();
        
        // Show home fragment by default
        if (savedInstanceState == null) {
            showHomeFragment();
        }
        
        // Request necessary permissions
        checkAndRequestPermissions();
    }
    
    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        setupNavigationItemBackgrounds();
    }
    
    private void setupNavigationItemBackgrounds() {
        // Set background colors for navigation items programmatically
        bottomNavigation.post(() -> {
            for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
                android.view.MenuItem item = bottomNavigation.getMenu().getItem(i);
                android.view.View view = bottomNavigation.findViewById(item.getItemId());
                if (view != null) {
                    view.setBackgroundResource(R.drawable.bottom_nav_item_background);
                }
            }
        });
    }
    
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int itemId = item.getItemId();
                
                if (itemId == R.id.nav_home) {
                    showHomeFragment();
                    updateNavigationBackgrounds(R.id.nav_home);
                    return true;
                } else if (itemId == R.id.nav_workouts) {
                    showMyWorkoutsFragment();
                    updateNavigationBackgrounds(R.id.nav_workouts);
                    return true;
                } else if (itemId == R.id.nav_create) {
                    showCreateFragment();
                    updateNavigationBackgrounds(R.id.nav_create);
                    return true;
                } else if (itemId == R.id.nav_workouts_star) {
                    showWorkoutsFragment();
                    updateNavigationBackgrounds(R.id.nav_workouts_star);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    showProfileFragment();
                    updateNavigationBackgrounds(R.id.nav_profile);
                    return true;
                }
                
                return false;
            }
        });
    }
    
    private void updateNavigationBackgrounds(int selectedItemId) {
        // Update all item backgrounds
        for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
            android.view.MenuItem item = bottomNavigation.getMenu().getItem(i);
            android.view.View view = bottomNavigation.findViewById(item.getItemId());
            if (view != null) {
                if (item.getItemId() == selectedItemId) {
                    view.setBackgroundColor(getResources().getColor(R.color.nav_background_active, null));
                } else {
                    view.setBackgroundColor(getResources().getColor(R.color.nav_background_inactive, null));
                }
            }
        }
    }
    
    private void showHomeFragment() {
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }
        replaceFragment(homeFragment);
    }
    
    private void showMyWorkoutsFragment() {
        if (myWorkoutsFragment == null) {
            myWorkoutsFragment = new MyWorkoutsFragment();
        }
        replaceFragment(myWorkoutsFragment);
    }
    
    private void showWorkoutsFragment() {
        if (workoutsFragment == null) {
            workoutsFragment = new WorkoutsFragment();
        }
        replaceFragment(workoutsFragment);
    }
    
    private void showCreateFragment() {
        if (createFragment == null) {
            createFragment = new CreateFragment();
        }
        replaceFragment(createFragment);
    }
    
    // Public method to show Add Exercise Fragment (called from CreateFragment)
    public void showAddExerciseFragment() {
        AddExerciseFragment addExerciseFragment = new AddExerciseFragment();
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, addExerciseFragment)
            .addToBackStack(null)
            .commit();
    }
    
    private void showManageItemsFragment() {
        if (manageItemsFragment == null) {
            manageItemsFragment = new ManageItemsFragment();
        }
        replaceFragment(manageItemsFragment);
    }
    
    private void showProfileFragment() {
        if (profileFragment == null) {
            profileFragment = new ProfileFragment();
        }
        replaceFragment(profileFragment);
    }
    
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }
    
    private void navigateToAuth() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Check if user is still logged in
        if (!authController.isLoggedIn()) {
            navigateToAuth();
        }
    }
    
    private void checkAndRequestPermissions() {
        if (!PermissionManager.hasAllPermissions(this)) {
            // Show rationale if needed
            boolean shouldShowRationale = false;
            for (String permission : PermissionManager.REQUIRED_PERMISSIONS) {
                if (PermissionManager.shouldShowRationale(this, permission)) {
                    shouldShowRationale = true;
                    break;
                }
            }
            
            if (shouldShowRationale) {
                PermissionManager.showPermissionRationale(this,
                    "Permissions Required",
                    "FitLife needs certain permissions to provide the best experience:\n\n" +
                    "• SMS: Share workouts with friends\n" +
                    "• Camera: Add exercise photos\n" +
                    "• Storage: Save exercise images\n" +
                    "• Contacts: Select friends to share with",
                    () -> PermissionManager.requestAllPermissions(this),
                    null
                );
            } else {
                PermissionManager.requestAllPermissions(this);
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        PermissionManager.handlePermissionResult(this, requestCode, permissions, grantResults, 
            new PermissionManager.PermissionCallback() {
                @Override
                public void onPermissionGranted() {
                    // Permissions granted - app can function fully
                }
                
                @Override
                public void onPermissionDenied(String[] deniedPermissions) {
                    // Some permissions denied - app will have limited functionality
                    String message = "Some permissions were denied: " + String.join(", ", deniedPermissions);
                    android.widget.Toast.makeText(MainActivity.this, message, android.widget.Toast.LENGTH_LONG).show();
                }
            }
        );
    }
    
    private void applySavedTheme() {
        User currentUser = AuthController.getCurrentUser();
        if (currentUser != null) {
            preferencesController.applySavedTheme(currentUser.getUserId());
        }
    }
    
    // Public method to switch to workouts tab (called from HomeFragment)
    public void switchToWorkoutsTab() {
        showWorkoutsFragment();
        // Update bottom navigation selection
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_workouts_star);
        }
    }
    
    // Public method to show workout detail (called from fragments)
    public void showWorkoutDetail(WeeklyWorkout workout) {
        if (workout != null && workout.getRoutine() != null) {
            WorkoutDetailFragment detailFragment = WorkoutDetailFragment.newInstance(workout.getRoutine());
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
        }
    }
    
    // Public method to show routine detail (called from WorkoutsFragment)
    public void showRoutineDetail(Routine routine) {
        if (routine != null) {
            WorkoutDetailFragment detailFragment = WorkoutDetailFragment.newInstance(routine);
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
        }
    }
}