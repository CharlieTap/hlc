@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinter)
    id("maven-publish")
}

group = "com.tap.hlc"
version = "1.1.0"

kotlin {
    targets {
        jvm()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.okio.core)
                api(libs.kotlinx.datetime)
                api(libs.result)
                api(libs.uuid)
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.okio.fakefilesystem)
            }
        }
    }
}