import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

// ---- Release signing: read from keystore/keystore.properties (never committed with real secrets) ----
val keystorePropsFile = rootProject.file("keystore/keystore.properties")
val ksProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        FileInputStream(keystorePropsFile).use { load(it) }
    }
}
// Distinct names so they never shadow the SigningConfig properties inside the
// signingConfigs.create { } lambda (self-assignment would silently leave them null).
val keystoreFile: File? = ksProps.getProperty("storeFile")?.let { project.file(it) }
    ?: project.file("../keystore/masked-release.jks")
val keystoreStorePassword: String = ksProps.getProperty("storePassword", "")
val keystoreAlias: String = ksProps.getProperty("keyAlias", "masked")
val keystoreKeyPassword: String = ksProps.getProperty("keyPassword", "")

android {
    namespace = "com.maskedparty.game"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.maskedparty.game"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    // Allow Robolectric unit tests to load Android resources (needed for Compose UI tests).
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystoreFile != null && keystoreFile.exists() && keystoreStorePassword.isNotEmpty()) {
                signingConfig = signingConfigs.create("release") {
                    storeFile = keystoreFile
                    // AGP 8.7 leaves storeType null by default, which NPEs the AAB
                    // signing path (FinalizeBundleTask). Derive it from the file name.
                    storeType = keystoreFile.name.substringAfterLast('.', "JKS").uppercase()
                    storePassword = keystoreStorePassword
                    keyAlias = keystoreAlias
                    keyPassword = keystoreKeyPassword
                }
            } else {
                // No usable keystore -> build unsigned (CI can sign separately).
                println("[Masked] Release keystore not available at $keystoreFile; building unsigned.")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources.excludes += "META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // Persistence
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Unit tests (pure JVM) + Robolectric Compose UI tests (run on JVM, no emulator needed)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.21")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")

    // Instrumented tests (require a device/emulator at runtime).
    // The Compose BOM is only applied to `implementation`, so pin ui-test explicitly
    // (1.7.8 is the version bundled by compose-bom 2025.01.00).
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.6")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.6")
}
