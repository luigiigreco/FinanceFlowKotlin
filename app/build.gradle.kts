plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id ("project-report")
}

android {
    namespace = "com.example.financeflow"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.financeflow"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        animationsDisabled = true
        unitTests.isIncludeAndroidResources = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:-options")
    }

    tasks.withType<Test> {
        systemProperty("robolectric.enabledSdks", "28")
    }


    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Firebase
    implementation("com.google.firebase:firebase-analytics")
    implementation(platform("com.google.firebase:firebase-bom:33.5.0"))
    implementation("com.google.firebase:firebase-firestore:24.0.0")
    implementation("com.google.firebase:firebase-auth:22.0.0")
    implementation("com.google.protobuf:protobuf-javalite:3.21.12")
    // AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.2") // Usa "-ktx" per la versione Kotlin
    implementation("androidx.navigation:navigation-ui-ktx:2.8.2")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    // Charting library
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")
    // Unit Testing
    testImplementation("junit:junit:4.13.2") // Per i test JUnit
    testImplementation("androidx.arch.core:core-testing:2.1.0") // Per test LiveData e ViewModel
    testImplementation("org.mockito:mockito-core:4.8.0") // Per creare mock
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.robolectric:robolectric:4.10.3") // Robolectric per emulare Android durante i test unitari
    testImplementation("androidx.test:core:1.5.0") // Per supportare i test di Android con Robolectric
    testImplementation("com.google.truth:truth:1.1.5") // Google Truth per assertions
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3") // Per testare coroutine
    testImplementation("io.mockk:mockk:1.13.8") // MockK, alternativa Kotlin-friendly a Mockito
    testImplementation("org.robolectric:shadows-framework:4.10.3")
    // Instrumented Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // Estensioni JUnit per Android
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // Per test UI con Espresso
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1") // Per interazioni con Intents
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1") // Per supporto avanzato (RecyclerView, Drawer, etc.)
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0") // Per test LiveData e ViewModel
    androidTestImplementation("androidx.test:core:1.5.0") // Per supportare il framework dei test Android
    androidTestImplementation("androidx.fragment:fragment-testing:1.5.7")
    androidTestImplementation("org.mockito:mockito-android:4.8.0")
    // Coroutines Testing (opzionale, se usi coroutines)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
}
