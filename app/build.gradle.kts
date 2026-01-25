plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.fitlife"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.fitlife"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Navigation components
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    
    // ViewPager2 for intro
    implementation(libs.viewpager2)
    
    // Image handling
    implementation(libs.glide)
    
    // Date and time formatting
    implementation(libs.threetenbp)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}