plugins {
    alias(libs.plugins.android.application)
}

android {
    // The module is written entirely in Java, so the Kotlin compile task and the
    // implicit Kotlin standard library dependency added by AGP 9 are not needed.
    enableKotlin = false

    namespace = "com.comp1786.logbook.bmi"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.comp1786.logbook.bmi"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
    testOptions {
        // Espresso cannot wait for animations, so instrumented tests run with them off.
        animationsDisabled = true
    }
}

dependencies {
    implementation(libs.core)
    implementation(libs.activity)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    testImplementation(libs.junit)
    testImplementation(libs.arch.core.testing)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}