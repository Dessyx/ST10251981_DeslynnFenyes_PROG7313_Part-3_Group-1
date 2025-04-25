import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.prog7313_groupwork"
    compileSdk = 35
    viewBinding {
        enable = true
    }

    defaultConfig {
        applicationId = "com.example.prog7313_groupwork"
        minSdk = 24
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

val camerax_version = "1.4.2"

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.camera.camera2)
    // Simplifies the management of the camera's lifecycle (properly opened,
    // closed, and released based on the lifecycle of the associated LifecycleOwner (such as an Activity)
    implementation(libs.androidx.camera.lifecycle)
    // Provides a set of UI components that can be used to create camera-related UI's to the
    // app. Such as PreviewView
    implementation(libs.androidx.camera.view)

    implementation(libs.androidx.room.runtime.v261)
    annotationProcessor(libs.androidx.room.compiler.v261)
    implementation(libs.androidx.room.ktx.v261) // for kotlin support

    implementation(libs.guava)
    implementation(libs.gson)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
}