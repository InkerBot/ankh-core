plugins {
    id("io.izzel.taboolib") version "1.56"
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.hcloader)
}

group = "org.inksnow.ankh.kether"

taboolib {
    description {
        name = "ankh-kether"
        version = project.version.toString()
        dependencies {
            name("ankh-core")
        }
        contributors {
            name("inkerbot")
        }
    }
    install("common")
    install("common-5")
    install("platform-bukkit")
    install("module-configuration")
    install("module-kether")
    install("module-chat")
    install("module-nms")
    install("module-nms-util")
    install("module-lang")

    version = libs.versions.taboolib.get()
}

dependencies {
    compileOnly(project(":"))
    compileOnly(kotlin("stdlib"))
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

tasks.javadocJar {
    enabled = false
}

tasks.sourcesJar {
    enabled = false
}