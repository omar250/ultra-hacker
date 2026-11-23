plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ultrahacker.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ultrahacker.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 4
        versionName = "3.5"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
