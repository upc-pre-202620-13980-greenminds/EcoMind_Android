import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) propertiesFile.inputStream().use { load(it) }
}
val debugApiBaseUrl = localProperties.getProperty(
    "api.base.url",
    "https://ca-ecomind-api.icybush-bd135296.westus2.azurecontainerapps.io/api/v1/"
)

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

        // Sandbox public credentials from the previous EcoMind web checkout.
        // Provider secret keys remain exclusively in the backend.
        buildConfigField("String", "CULQI_PUBLIC_KEY", "\"pk_test_Z2WRdxu0EQOGU2Ge\"")
        buildConfigField(
            "String",
            "PAYPAL_CLIENT_ID",
            "\"AUmHZeDzviWVJj_rnXU_c5wEhRKANH7yy_HFdJVDWxgsc1lX_8xYrfG0NJqLJHynBKAsXy4YsaIzZeh2\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Use Azure by default; local development can override api.base.url.
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
        }
        release {
            optimization {
                enable = false
            }
            // Public Azure API used by the release build.
            buildConfigField("String", "API_BASE_URL", "\"https://ca-ecomind-api.icybush-bd135296.westus2.azurecontainerapps.io/api/v1/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true

    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("io.coil-kt.coil3:coil-compose:3.6.3")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.6.3")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    coreLibraryDesugaring(libs.desugar.jdk)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.compose.material.icons.extended)

    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
