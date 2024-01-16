import ink.bgp.hcloader.gradle.HcLoaderConfigEntry

plugins {
    alias(libs.plugins.hcloader)
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    delegateRuntime(project(":"))

    implementation(project(":api"))
    implementation(project(":libs:shadow-callsite-nbt", configuration = "shadow"))

    implementation(libs.bundles.adventure)

    // minecraft
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT") {
        exclude("com.google.code.gson", "gson") // we use our version
        exclude("it.unimi.dsi", "fastutil") // we use our version
        exclude("org.checkerframework", "checker-qual")
        exclude("org.jetbrains", "annotations")
    }

    implementation(libs.hcloader)
}

tasks.shadowJar {
    exclude("classpath.index")
    exclude("module-info.class", "META-INF/versions/*/module-info.class")

    relocate("ink.bgp.hcloader", "org.inksnow.ankh.loader")
}

tasks.hcLoaderJar {
    dependsOn(tasks.shadowJar)

    enableCopyJar.set(false)
    enableStaticInject.set(false)
    enableCopyInjector.set(false)


    from(tasks.shadowJar.map { shadowJar ->
        shadowJar.outputs
            .files
            .map {
                if (it.isFile) {
                    zipTree(it)
                } else {
                    it
                }
            }
    })

    loadConfig.add(
        HcLoaderConfigEntry.of(
            10,
            "org/inksnow/ankh/core/api/**",
            HcLoaderConfigEntry.HcLoaderLoadPolicy.PARENT_ONLY
        )
    )

    loadConfig.add(
        HcLoaderConfigEntry.of(
            10,
            "kotlin/**",
            HcLoaderConfigEntry.HcLoaderLoadPolicy.SELF_ONLY
        )
    )
}

tasks.assemble {
    dependsOn(tasks.hcLoaderJar)
}