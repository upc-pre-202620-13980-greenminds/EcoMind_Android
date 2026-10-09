plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val remoteBackend = providers.gradleProperty("ecomind.remote").orElse("false").map(String::toBoolean).get()
val configuredBackendUrl = providers.gradleProperty("ecomind.apiBaseUrl")
val backendUrl = configuredBackendUrl.orElse("http://10.0.2.2:8092/api/v1/").get()
val releaseBackendUrl = configuredBackendUrl.orElse("https://ecomind-backend.example.com/api/v1/").get()
require(backendUrl.endsWith("/")) { "ecomind.apiBaseUrl must end with /" }

android {
    namespace = "pe.greenminds.ecomind"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "pe.greenminds.ecomind"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        buildConfigField("boolean", "REMOTE_BACKEND", remoteBackend.toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // 10.0.2.2 is the host machine seen from the Android emulator
            buildConfigField("String", "API_BASE_URL", "\"${backendUrl}\"")
        }
        release {
            optimization {
                enable = false
            }
            // Placeholder: replace with the real host once the backend is deployed
            buildConfigField("String", "API_BASE_URL", "\"${releaseBackendUrl}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    testImplementation(libs.mockwebserver)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}