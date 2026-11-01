import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.artifact.SingleArtifact
import java.util.Properties
import java.util.zip.ZipFile

plugins {
    alias(libs.plugins.android.application)
    // Kotlin compilation comes from AGP 9 built-in Kotlin
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    alias(libs.plugins.google.services)
    id("com.google.protobuf") version "0.10.0"
    id("org.jetbrains.kotlin.plugin.serialization")
}

secrets {
    defaultPropertiesFileName = "secrets.properties"
}

android {
    namespace = "com.singularis.eateria"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.singularis.eateria"
        minSdk = 26
        targetSdk = 36
        versionCode = 30
        versionName = "5.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // GOOGLE_CLIENT_ID will be automatically added by secrets-gradle-plugin
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        // Optional Play upload key: create keystore.properties at project root
        // (storeFile, storePassword, keyAlias, keyPassword). Ignored if missing.
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        if (keystorePropertiesFile.exists()) {
            val keystoreProperties = Properties().apply {
                keystorePropertiesFile.inputStream().use { load(it) }
            }
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
            // Studio / local only — may use https://chater.singularis.work/dev
            buildConfigField("boolean", "USE_DEV_API", "true")
        }
        release {
            // Legacy R8 DSL (AGP < 9.3): code + resource optimization for Play release.
            // Keep rules: app/proguard-rules.pro + proguard-android-optimize.txt
            isMinifyEnabled = true
            isShrinkResources = true
            // Play Store MUST hit prod (no /dev). Never override via prefs.
            buildConfigField("boolean", "USE_DEV_API", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Ask AGP to extract symbols from libraries that still have a symbol table.
            // Pre-stripped AndroidX .so files are handled by packageReleasePlayNativeSymbolTables.
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            signingConfigs.findByName("release")?.let { signingConfig = it }
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
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// CameraX, DataStore, and Compose ship stripped .so files (dynamic symbols only).
// AGP skips those because llvm-strip does not change the file, so the App Bundle
// has native code and no debug-symbols metadata. Play then warns that symbols
// were not uploaded. Copy each stripped library to <abi>/<name>.so.sym, which is
// the file AGP would have produced with objcopy --strip-debug.
androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val cap = variant.name.replaceFirstChar { it.uppercase() }
        val packageSymbols =
            tasks.register<PackagePlayNativeSymbolTablesTask>("package${cap}PlayNativeSymbolTables") {
                mergedNativeLibs.set(variant.artifacts.get(SingleArtifact.MERGED_NATIVE_LIBS))
            }
        variant.artifacts
            .use(packageSymbols)
            .wiredWith(PackagePlayNativeSymbolTablesTask::getSymbolTables)
            .toAppendTo(MultipleArtifact.NATIVE_SYMBOL_TABLES)
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.36.2"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    
    // Jetpack Compose BOM - manages all Compose library versions
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Activity Compose
    implementation(libs.androidx.activity.compose)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // Serialization
    implementation(libs.gson)
    
    // Protobuf
    implementation("com.google.protobuf:protobuf-kotlin-lite:4.36.2")
    
    // Google Services
    implementation(libs.play.services.maps)
    
    // Credential Manager (replaces deprecated Google Sign-In)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    
    // Camera
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // Security Crypto for Token Storage
    implementation("androidx.security:security-crypto:1.1.0")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.12.0")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    
    // Debug tools
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Material Compose for pull-to-refresh and swipe gestures
    implementation("androidx.compose.material:material:1.12.1")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
    
    // Coil for image loading
    implementation("io.coil-kt.coil3:coil-compose:3.6.3")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.6.3")

    // Accompanist - Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
}

afterEvaluate {
    tasks.named("bundleRelease").configure {
        doLast {
            val bundle = layout.buildDirectory.file("outputs/bundle/release/app-release.aab").get().asFile
            val symbols =
                ZipFile(bundle).use { zip ->
                    zip.entries().asSequence().map { it.name }.filter { entry ->
                        entry.contains("com.android.tools.build.debugsymbols/") &&
                            (entry.endsWith(".sym") || entry.endsWith(".dbg"))
                    }.toList()
                }
            check(symbols.isNotEmpty()) {
                "Release App Bundle has native code but no debug symbols. Play will keep the missing-symbols warning."
            }
            logger.lifecycle("Play native debug symbols in ${bundle.name}: ${symbols.size} files")
        }
    }
}