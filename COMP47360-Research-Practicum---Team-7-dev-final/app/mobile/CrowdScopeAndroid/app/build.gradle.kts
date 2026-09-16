plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.example.crowdscopeandroid"

    defaultConfig {
        applicationId = "com.example.crowdscopeandroid"
        minSdk = 26
        versionCode = 1
        versionName = "1.0"
        compileSdk = 35
        targetSdk = 35

        val mapboxToken: String = project.findProperty("MAPBOX_DOWNLOADS_TOKEN") as String?
            ?: error("MAPBOX_DOWNLOADS_TOKEN property not found in gradle.properties")

        resValue("string", "mapbox_access_token", mapboxToken)
        buildConfigField("String", "MAPBOX_TOKEN", "\"$mapboxToken\"")

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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }

    // Exclude the old Gestures lib to avoid conflicts
    configurations {
        all {
            exclude(group = "com.mapbox.mapboxsdk", module = "mapbox-android-gestures")
        }
    }
}

// at the top of your dependencies block
val mapboxVersion = "11.13.4"
val accompanistVersion = "0.34.0"

dependencies {
    // Core SDK
    implementation("com.mapbox.maps:android:$mapboxVersion")
    implementation("com.mapbox.extension:maps-compose:$mapboxVersion")

    // ADDED: Explicit Mapbox Plugin Dependencies for clarity and to resolve potential issues
    implementation("com.mapbox.plugin:maps-gestures:${mapboxVersion}")
    implementation("com.mapbox.plugin:maps-scalebar:${mapboxVersion}")
    implementation("com.mapbox.plugin:maps-compass:${mapboxVersion}")
    implementation("com.mapbox.plugin:maps-annotation:${mapboxVersion}")
// Often needed for map data
    implementation("com.mapbox.plugin:maps-locationcomponent:${mapboxVersion}")

    implementation("com.google.accompanist:accompanist-systemuicontroller:${accompanistVersion}")
// If you use location
    //      implementation("com.mapbox.plugin:maps-style:$mapboxVersion")
// For GeoJsonSource, updateGeoJsonSource, etc.

    // ── OTHER DEPENDENCIES (keep as-is) ──
    implementation(libs.androidxCoreKtx)
    implementation(libs.androidxLifecycleRuntimeKtx)
    implementation(libs.androidxActivityCompose)
    implementation(platform(libs.androidxComposeBom))
    implementation(libs.androidxUi)
    implementation(libs.androidxUiGraphics)
    implementation(libs.androidxUiToolingPreview)
    implementation(libs.androidxMaterial3)
    implementation("androidx.navigation:navigation-compose:2.7.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation(libs.coilCompose)
    implementation(libs.coilGif)
    implementation(libs.coilBase)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidxJunit)
    androidTestImplementation(libs.androidxEspressoCore)
    androidTestImplementation(platform(libs.androidxComposeBom))
    androidTestImplementation(libs.androidxUiTestJunit4)
    debugImplementation(libs.androidxUiTooling)
    debugImplementation(libs.androidxUiTestManifest)
}
