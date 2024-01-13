plugins {
    alias(libs.plugins.hcloader)
}

group = "org.inksnow.ankh.js-nashorn"

configurations {
    delegateRuntime {
        extendsFrom(runtimeClasspath.get())
    }
}

dependencies {
    implementation(libs.nashorn.core) {
        exclude("org.ow2.asm:asm")
    }
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly(project(":"))
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