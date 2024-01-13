pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://r.bgp.ink/maven")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "ankh-core"

include("api")

include("loader:logger")
include("loader:cloud")

// include("test-plugin")
include("gradle-plugin")

include("libs:shadow-bsh")
include("libs:shadow-paper-lib")
include("libs:shadow-callsite-nbt")

include("services:service-groovy")
include("services:service-js-nashorn")
include("services:service-kether")
include("services:service-neigeitems")
