plugins {
    application
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("org.jetbrains.dokka") version "2.2.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.sawears.algorithm_development.MainKt")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("be.tarsos.dsp:core:2.5")
    implementation("be.tarsos.dsp:jvm:2.5")
    implementation("com.github.wendykierp:JTransforms:3.1")

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

dokka {
    dokkaSourceSets.main {
        includes.from("docs/packages.md")
    }
}