import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Secrets never live in source control. Each key is read from an environment variable (CI) or from
// local.properties (git-ignored). A missing key becomes "" so a fresh clone still builds; the app then
// shows a clear message instead of crashing.
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}

fun secret(name: String): String =
    System.getenv(name)?.takeIf { it.isNotBlank() } ?: localProperties.getProperty(name, "").trim()

// Keys the app needs at runtime; warn at build time (without printing values) if one is missing.
listOf("MAPS_API_KEY", "WAQI_TOKEN")
    .filter { secret(it).isBlank() }
    .forEach { logger.warn("w: $it is not set. Add it to local.properties (see README > Setup); the app builds but that feature will show an error.") }

fun String.asBuildConfigString(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.mvl.assignment"
    // AGP 8.13 supports up to API 36.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mvl.assignment"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["MAPS_API_KEY"] = secret("MAPS_API_KEY")
        // Only whether the key exists (never its value) so the map screen can explain a blank map.
        buildConfigField("boolean", "HAS_MAPS_API_KEY", secret("MAPS_API_KEY").isNotBlank().toString())
        buildConfigField("String", "WAQI_TOKEN", secret("WAQI_TOKEN").asBuildConfigString())
        buildConfigField("String", "WAQI_BASE_URL", "\"https://api.waqi.info/\"")
        buildConfigField("String", "GEOCODING_BASE_URL", "\"https://api.bigdatacloud.net/\"")
    }

    flavorDimensions += "backend"
    productFlavors {
        // Default for reviewers: POST/GET /books are served by an in-app OkHttp interceptor.
        create("mock") {
            dimension = "backend"
            isDefault = true
            applicationIdSuffix = ".mock"
            buildConfigField("String", "BOOKS_BASE_URL", "\"https://mock.books.local/\"")
        }
        // Real backend: base URL supplied via local.properties / environment.
        create("prod") {
            dimension = "backend"
            buildConfigField("String", "BOOKS_BASE_URL", secret("BOOKS_BASE_URL").asBuildConfigString())
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging) // only installed when BuildConfig.DEBUG

    implementation(libs.maps.compose)
    implementation(libs.play.services.location)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)

    debugImplementation(libs.androidx.compose.ui.tooling) // Compose previews
}
