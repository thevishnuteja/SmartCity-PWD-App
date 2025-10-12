import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val properties = Properties()
if (rootProject.file("local.properties").exists()) {
    properties.load(rootProject.file("local.properties").inputStream())
}

android {
    namespace = "com.SIMATS.smartcity"
    compileSdk = 35

    // THIS BLOCK IS REQUIRED TO FIX THE ERROR
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.SIMATS.smartcity"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // This line reads your NEW, secure key from local.properties
        buildConfigField("String", "GEMINI_API_KEY", "\"${properties.getProperty("GEMINI_API_KEY")}\"")
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

// THIS SECTION HAS BEEN CLEANED UP TO REMOVE ALL DUPLICATES
dependencies {
    // AndroidX & Material Design
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.coordinatorlayout)
    implementation(libs.cardview)
    implementation(libs.core.ktx)

    // Google Services (Auth, Maps, Places)
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.libraries.places:places:3.5.0")

    // Gemini AI & Required Guava Library
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
    implementation("com.google.guava:guava:33.2.1-android")

    // Networking
    implementation("com.android.volley:volley:1.2.1")

    // UI & Image Libraries
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.13.3")
    implementation("com.airbnb.android:lottie:6.4.1")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}