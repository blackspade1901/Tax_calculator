plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.taxcalculator"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.taxcalculator"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.filament.android)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.mlkit.barcode)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.guava)

    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.firestore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

// --- FIX: Custom Javadoc Task to handle encoding and Android dependencies ---
tasks.register<Javadoc>("generateJavadoc") {
    group = "Reporting"
    description = "Generates Javadoc for the application."

    // Access AppExtension to get source sets and variants
    val androidExtension = project.extensions.getByType(com.android.build.gradle.AppExtension::class.java)
    val mainSourceSet = androidExtension.sourceSets.getByName("main")

    // Source files (Java)
    source(mainSourceSet.java.srcDirs)

    // Exclude generated files
    exclude("**/R.java", "**/BuildConfig.java")

    // Set classpath including Android boot classpath and compile dependencies
    doFirst {
        val debugVariant = androidExtension.applicationVariants.find { it.name == "debug" }
        if (debugVariant != null) {
            classpath = files(
                androidExtension.bootClasspath,
                debugVariant.javaCompileProvider.get().classpath
            )
        }
    }

    options {
        this as StandardJavadocDocletOptions
        // Ensure UTF-8 encoding to prevent errors with special characters
        encoding = "UTF-8"
        charSet = "UTF-8"
        docEncoding = "UTF-8"
        
        // Disable strict doclint to prevent failure on missing @param tags or HTML errors
        addStringOption("Xdoclint:none", "-quiet")
        
        windowTitle = "TrueRate Tax Calculator API"
        docTitle = "TrueRate Tax Calculator API"
        memberLevel = org.gradle.external.javadoc.JavadocMemberLevel.PROTECTED
        links("https://developer.android.com/reference/")
        links("https://docs.oracle.com/javase/8/docs/api/")
    }
    
    // Prevent task failure on minor Javadoc errors
    isFailOnError = false
}