plugins {
    alias(libs.plugins.android.application)
    //id("com.android.application")
}

android {
    namespace = "com.example.meteoweatherchannel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.meteoweatherchannel"
        minSdk = 28
        targetSdk = 34
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
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // custom added libraries
    implementation(libs.picasso)
    implementation(libs.volley)
    implementation(libs.play.services.location)
    implementation(libs.viewpager2)
    implementation(libs.gson)
    implementation(libs.fragment)
}