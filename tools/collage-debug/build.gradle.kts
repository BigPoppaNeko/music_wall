plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

application {
    mainClass.set("CollageDebugKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

// Output lands in the project root (same place as local.properties)
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}
