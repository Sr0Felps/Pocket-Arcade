plugins {
    id("com.android.application")
}

android {
    namespace = "br.edu.ads.pocketarcade"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.edu.ads.pocketarcade"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // POR QUE ISSO FOI FEITO:
    // Ativa o View Binding usando a sintaxe Kotlin DSL (exigência dos Slides 2 e 13),
    // eliminando a necessidade de usar o proibido findViewById().
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}