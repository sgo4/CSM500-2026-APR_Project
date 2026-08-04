plugins {
    kotlin("jvm") version "2.2.10"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
    id("org.jetbrains.compose") version "1.10.3"
}

group = "org.sawears"
version = "unspecified"


dependencies {
    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib-jdk8"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation("com.github.wendykierp:JTransforms:3.1")
    implementation(project(":model"))
    implementation(project(":algorithm_development")) //TODO delete

}

kotlin {
    jvmToolchain(23)
}

tasks.test {
    useJUnitPlatform()
}