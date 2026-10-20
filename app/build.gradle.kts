plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.durpalla.merchant"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "DURPALLA_API_BASE_URL", "\"https://api.durpalla.com/v1/\"")
        // Set true to make repositories call the real Durpalla API instead of fake data.
        buildConfigField("boolean", "USE_REMOTE_API", "false")
    }

    flavorDimensions += "merchant"
    productFlavors {
        create("bus") {
            dimension = "merchant"
            applicationId = "com.durpalla.merchant.greenline"
            buildConfigField("String", "MERCHANT_ID", "\"mrc_greenline_paribahan\"")
        }
        create("hotel") {
            dimension = "merchant"
            applicationId = "com.durpalla.merchant.seapearl"
            buildConfigField("String", "MERCHANT_ID", "\"mrc_sea_pearl_resort\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
}
