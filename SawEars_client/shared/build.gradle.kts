import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room3)
    id("org.jetbrains.dokka") version "2.2.0"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    jvm()
    
    android {
       namespace = "com.sawears.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {

        commonMain.dependencies {
            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            api(libs.androidx.room3.runtime)
			implementation(libs.androidx.sqlite.bundled)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.jetbrains.lifecycle.viewmodel.nav3)
            api(libs.androidx.datastore.preferences.core)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("be.tarsos.dsp:core:2.5")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
        }

		androidMain.dependencies{
            implementation("be.tarsos.dsp:core:2.5")
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }

        jvmMain.dependencies{
            implementation("be.tarsos.dsp:core:2.5")
        }
    }
}


// necessary to allow common string resources to be referenced in platform specific code
compose.resources {
    publicResClass = true
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspJvm", libs.androidx.room3.compiler)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dokka {
    dokkaPublications.html {
        includes.from(
            "docs/packages/com.sawears.data.md",
            "docs/packages/com.sawears.domain.md",
            "docs/packages/com.sawears.ui.md"
        )
    }
}