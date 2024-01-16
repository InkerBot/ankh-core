plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.hcloader)
}

group = "org.inksnow.ankh.kts"

dependencies {
    api(project(":"))
    api("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    api(libs.bundles.kotlin.scripting)

    "delegateRuntime"(libs.bundles.kotlin.scripting)
}

tasks.create<Copy>("copyLibs") {
    from(configurations.runtimeClasspath)
    into("/Users/inkerbot/IdeaProjects/AnkhCore/run/paper-1-20-4/cache/ankh-core/context")
}

tasks.hcLoaderJar {
    dependsOn(tasks.jar)

    enableStaticInject.set(false)
    enableCopyJar.set(false)
    enableCopyInjector.set(false)

    archiveExtension.set("ankhplugin")

    with(copySpec {
        from(tasks.jar)
        into("META-INF/hcloader/delegate")
    })
}

tasks.javadocJar {
    enabled = false
}

tasks.sourcesJar {
    enabled = false
}

tasks.assemble {
    dependsOn(tasks.hcLoaderJar)
}