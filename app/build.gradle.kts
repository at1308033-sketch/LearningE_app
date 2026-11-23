plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.learninge.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.learninge.app"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.recyclerview)

    testImplementation(libs.junit)
    // org.json của Android chỉ là bản giả khi chạy unit test trên JVM.
    testImplementation(libs.org.json)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
