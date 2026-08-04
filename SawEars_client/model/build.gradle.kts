plugins {
    kotlin("jvm")

    kotlin("plugin.serialization") version "2.2.0"

}


dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.13.4")
    implementation("be.tarsos.dsp:core:2.5")
    implementation("be.tarsos.dsp:jvm:2.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation(project(":data"))
}

tasks.test {
    useJUnitPlatform()
}