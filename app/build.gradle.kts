import java.security.MessageDigest
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.protobuf)
}

// Địa chỉ máy chủ: local.properties hoặc ~/.gradle/gradle.properties, không commit (DEC-023).
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun setting(name: String, default: String): String =
    localProperties.getProperty(name) ?: providers.gradleProperty(name).orNull ?: default

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
            buildConfigField("String", "GRPC_HOST", "\"${setting("learninge.grpcHost", "10.0.2.2")}\"")
            buildConfigField("int", "GRPC_PORT", setting("learninge.grpcPort", "50051"))
            buildConfigField("boolean", "GRPC_TLS", setting("learninge.grpcTls", "false"))
            buildConfigField("String", "GRPC_LAN_HOSTS", "\"${setting("learninge.grpcLanHosts", "")}\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "GRPC_HOST", "\"${setting("learninge.releaseGrpcHost", "")}\"")
            buildConfigField("int", "GRPC_PORT", setting("learninge.releaseGrpcPort", "443"))
            // NFR-S12: bản release luôn dùng TLS, không có cách tắt.
            buildConfigField("boolean", "GRPC_TLS", "true")
            buildConfigField("String", "GRPC_LAN_HOSTS", "\"\"")
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

    implementation(libs.grpc.okhttp)
    implementation(libs.grpc.protobuf.lite)
    implementation(libs.grpc.stub)
    implementation(libs.protobuf.javalite)
    // Mã sinh của grpc-java dùng @javax.annotation.Generated, chỉ cần lúc biên dịch.
    compileOnly(libs.tomcat.annotations.api)

    testImplementation(libs.junit)
    // org.json của Android chỉ là bản giả khi chạy unit test trên JVM.
    testImplementation(libs.org.json)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    plugins {
        create("grpc") {
            artifact = libs.grpc.protoc.gen.get().toString()
        }
    }
    generateProtoTasks {
        all().configureEach {
            builtins {
                create("java") {
                    option("lite")
                }
            }
            plugins {
                create("grpc") {
                    option("lite")
                }
            }
        }
    }
}

// proto là bản chép từ LearningE_Server: sửa tay thì build dừng (GRPC-API, DEC-022).
val verifyProtoLock by tasks.registering {
    val protoDir = layout.projectDirectory.dir("src/main/proto").asFile
    inputs.dir(protoDir)
    doLast {
        val locked = File(protoDir, "proto.lock").readLines()
            .filter { it.startsWith("sha256 ") }
            .associate { line -> line.split(" ").let { it[1] to it[2] } }
        val actual = protoDir.walkTopDown().filter { it.extension == "proto" }.associate { file ->
            val text = file.readText().replace("\r\n", "\n")
            val hash = MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
            file.relativeTo(protoDir).invariantSeparatorsPath to hash.joinToString("") { "%02x".format(it) }
        }
        if (locked != actual) {
            throw GradleException("proto khác proto.lock; chạy tools/sync-proto.ps1 thay vì sửa tay")
        }
    }
}

val checkReleaseServer by tasks.registering {
    val host = setting("learninge.releaseGrpcHost", "")
    doLast {
        if (host.isBlank()) {
            throw GradleException("Bản release cần learninge.releaseGrpcHost (TLS bắt buộc)")
        }
    }
}

tasks.configureEach {
    if (name.startsWith("generate") && name.endsWith("Proto")) dependsOn(verifyProtoLock)
    if (name == "preReleaseBuild") dependsOn(checkReleaseServer)
}
