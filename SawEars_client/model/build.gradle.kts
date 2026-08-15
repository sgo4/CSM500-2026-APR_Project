import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    kotlin("jvm")

    kotlin("plugin.serialization") version "2.2.0"
    id("org.jetbrains.dokka") version "2.2.0"
    alias(libs.plugins.ksp)
    alias(libs.plugins.room) version "3.0.1"

}

dokka {
    dokkaPublications.html {
        moduleName.set("sawears.model")
        moduleVersion.set(project.version.toString())

        outputDirectory.set(
            layout.buildDirectory.dir("documentation/html")
        )

        failOnWarning.set(true)
    }

    dokkaSourceSets.main {
        documentedVisibilities.set(
            setOf(VisibilityModifier.Public)
        )

        skipDeprecated.set(false)
        suppressGeneratedFiles.set(true)
    }
}


dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.13.4")
    implementation("be.tarsos.dsp:core:2.5")
    implementation("be.tarsos.dsp:jvm:2.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation(libs.androidx.room3.compiler)
}

tasks.test {
    useJUnitPlatform()
}