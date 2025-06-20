plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.droiddevs"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.droiddevs"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true // Recommended, but sticking to findViewById as per original code
    }
}

dependencies {
    // Core Android libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation(libs.androidx.room.common.jvm)

    // Room Database for local caching (replaces file storage)
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version") // For Coroutines support
    ksp("androidx.room:room-compiler:$room_version")

    // Material Design
    implementation("com.google.android.material:material:1.11.0")

    // ViewModel and LiveData for MVVM
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2") // For by viewModels()

    // Coroutines for asynchronous programming
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Gson for JSON serialization/deserialization (for local storage)
    implementation("com.google.code.gson:gson:2.10.1")

    // TODO: Add Google Sign-In and Firebase dependencies here later
    // implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    // implementation("com.google.firebase:firebase-auth-ktx")
    // implementation("com.google.firebase:firebase-firestore-ktx")
    // implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}