import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech.publish)
    signing
}

version = "1.0.0"

android {
    namespace = "com.appliedrec.facerecognition.r300.cloud"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
}

dependencies {
    api(project(":core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.verid.serialization)
    androidTestImplementation(libs.face.detection.retinaface)
}

mavenPublishing {
    coordinates("com.appliedrec", "face-recognition-r300-cloud")
    pom {
        name.set("Face recognition R300")
        description.set("Android face recognition Ver-ID SDK")
        url.set("https://github.com/AppliedRecognition/Face-Recognition-R300-Android")
        licenses {
            license {
                name.set("Commercial")
                url.set("https://raw.githubusercontent.com/AppliedRecognition/Face-Recognition-R300-Android/main/LICENCE.txt")
            }
        }
        developers {
            developer {
                id.set("appliedrec")
                name.set("Applied Recognition")
                email.set("support@appliedrecognition.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/AppliedRecognition/Face-Recognition-R300-Android.git")
            developerConnection.set("scm:git:ssh://github.com/AppliedRecognition/Face-Recognition-R300-Android.git")
            url.set("https://github.com/AppliedRecognition/Face-Recognition-R300-Android")
        }
    }
    publishToMavenCentral(true)
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}