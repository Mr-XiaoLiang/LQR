import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.lollipop.qr"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lollipop.qr"
        minSdk = 27
        targetSdk = 36
        versionCode = 2_09_00
        versionName = "2.9.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
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
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.glide)
    implementation(project(path = ":clip"))
    implementation(project(path = ":base"))
    implementation(project(path = ":fileChooser"))
    implementation(project(path = ":core"))
    implementation(project(path = ":colorRes"))
    implementation(project(path = ":widget"))
    implementation(project(path = ":pigment"))
    implementation(project(path = ":privacy"))
    implementation(project(path = ":faceIcon"))
    implementation(project(path = ":palette"))
    implementation(project(path = ":fragmentHelper"))
    implementation(project(path = ":insets"))
}