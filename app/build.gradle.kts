import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

/**
 * Merges `local.properties` with `config/<flavor>.properties`.
 * Flavor entries override only when non-blank, so e.g. `MAPS_API_KEY=` in config does not wipe a key set in `local.properties`.
 */
fun loadMergedSecrets(flavorName: String): Properties {
    val merged = Properties()
    rootProject.file("local.properties").takeIf { it.exists() }?.reader()?.use { merged.load(it) }
    rootProject.file("config/$flavorName.properties").takeIf { it.exists() }?.reader()?.use { reader ->
        val flavor = Properties()
        flavor.load(reader)
        flavor.forEach { (k, v) ->
            val value = (v as String).trim()
            if (value.isNotEmpty()) merged[k as String] = value
        }
    }
    return merged
}

android {
    namespace = "com.tadamaps.mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tadamaps.mobile"
        minSdk = 26
        targetSdk = 35
        versionCode = 26050
        versionName = "26.05.00"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["MAPS_API_KEY"] = ""

        buildConfigField(
            "boolean",
            "LOG_TO_TIMBER",
            "false",
        )
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("boolean", "LOG_TO_TIMBER", "false")
            buildConfigField("boolean", "INTEGRITY_CHECKS_ENABLED", "true")
        }
        debug {
            isMinifyEnabled = false
            buildConfigField("boolean", "LOG_TO_TIMBER", "true")
            buildConfigField("boolean", "INTEGRITY_CHECKS_ENABLED", "false")
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            manifestPlaceholders["appLabel"] = "MVL dev"
        }
        create("qa") {
            dimension = "environment"
            applicationIdSuffix = ".qa"
            manifestPlaceholders["appLabel"] = "MVL qa"
        }
        create("prod") {
            dimension = "environment"
            manifestPlaceholders["appLabel"] = "MVLChain"
        }
    }

    productFlavors.configureEach {
        val secrets = loadMergedSecrets(name)
        manifestPlaceholders["MAPS_API_KEY"] = secrets.getProperty("MAPS_API_KEY", "")
        val releaseCertSha256 = secrets.getProperty("RELEASE_CERT_SHA256", "").trim()
        buildConfigField("String", "EXPECTED_CERT_SHA256", "\"$releaseCertSha256\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    debugImplementation(libs.androidx.ui.tooling)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.play.services.location)

    implementation(libs.timber)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.compose.bom))

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
