plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Compose compiler plugin, required from Kotlin 2.0 onward.
    id("org.jetbrains.kotlin.plugin.compose")
    // Lets us mark data classes @Serializable for JSON export and import.
    id("org.jetbrains.kotlin.plugin.serialization")
    // Annotation processor for Room, faster than kapt.
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.elendheim.codex"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.elendheim.codex"
        minSdk = 26
        targetSdk = 34
        // versionCode goes up by one every release, versionName is the friendly label.
        versionCode = 6
        versionName = "1.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            // Shrink and obfuscate the release build to keep the download small.
            isMinifyEnabled = true
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
    kotlinOptions { jvmTarget = "17" }

    // compose for the UI, buildConfig so the About screen reads the version name.
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    // Compose is version aligned through the Bill of Materials.
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")

    // Compose UI and Material 3 for the dark first design.
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.1")

    // Room is the on device cache for fast search and sort.
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // kotlinx.serialization is the real long term home of the data, JSON on disk.
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Local unit tests, including the export round trip test.
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
