import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
}

/**
 * Same merge as `:app` — flavor overrides `local.properties` only for non-blank values.
 */
fun loadMergedSecrets(flavorName: String): Properties {
    val merged = Properties()
    rootProject.file("local.properties").takeIf { it.exists() }?.reader()?.use { merged.load(it) }
    rootProject.file("config/$flavorName.properties").takeIf { it.exists() }?.reader()?.use { reader ->
        val flavor = Properties()
        flavor.load(reader)
        flavor.forEach { k, v ->
            val value = (v as String).trim()
            if (value.isNotEmpty()) merged[k as String] = value
        }
    }
    return merged
}

android {
    namespace = "com.mvlchain.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "AQI_TOKEN", "\"\"")
        buildConfigField("String", "GEO_API_KEY", "\"\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "BOOKS_BASE_URL", "\"https://dev.mock.mvlchain/\"")
            buildConfigField("boolean", "ENABLE_HTTP_LOGGING", "true")
            buildConfigField("boolean", "MOCK_BOOKS_NETWORK", "true")
            buildConfigField("long", "MOCK_DELAY_MS", "600L")
        }
        create("qa") {
            dimension = "environment"
            buildConfigField("String", "BOOKS_BASE_URL", "\"https://qa.mock.mvlchain/\"")
            buildConfigField("boolean", "ENABLE_HTTP_LOGGING", "true")
            buildConfigField("boolean", "MOCK_BOOKS_NETWORK", "true")
            buildConfigField("long", "MOCK_DELAY_MS", "400L")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BOOKS_BASE_URL", "\"https://api.mvlchain/\"")
            buildConfigField("boolean", "ENABLE_HTTP_LOGGING", "false")
            buildConfigField("boolean", "MOCK_BOOKS_NETWORK", "true")
            buildConfigField("long", "MOCK_DELAY_MS", "250L")
        }
    }

    productFlavors.configureEach {
        val secrets = loadMergedSecrets(name)
        buildConfigField(
            "String",
            "AQI_TOKEN",
            "\"${secrets.getProperty("AQI_API_TOKEN", "")}\"",
        )
        buildConfigField(
            "String",
            "GEO_API_KEY",
            "\"${secrets.getProperty("GEO_API_KEY", "")}\"",
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        buildConfig = true
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.javax.inject)
    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
}
