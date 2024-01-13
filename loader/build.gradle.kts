import ink.bgp.hcloader.gradle.HcLoaderConfigEntry

plugins {
    id("io.github.hongyuncloud.hcloader.gradle") version "1.0-7"
}

configurations {
    shadowRuntime {
        extendsFrom(runtimeClasspath.get())
    }
}

dependencies {
    delegateRuntime(project(":"))

    implementation(project(":api"))
    implementation(project(":libs:shadow-callsite-nbt", configuration = "shadow"))

    implementation("net.kyori:adventure-api:4.13.1")
    implementation("net.kyori:adventure-text-minimessage:4.13.1")
    implementation("net.kyori:adventure-platform-bukkit:4.3.0")

    // minecraft
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT") {
        exclude("com.google.code.gson", "gson") // we use our version
        exclude("it.unimi.dsi", "fastutil") // we use our version
        exclude("org.checkerframework", "checker-qual")
        exclude("org.jetbrains", "annotations")
    }
    compileOnly("io.github.hongyuncloud.hcloader:hcloader:1.0-5")
}

tasks.hcLoaderJar {
    dependsOn(tasks.jar)

    loaderPackage.set("ink.bgp.hcloader")
    enableStaticInject.set(true)
    staticInjectClass.set("org.inksnow.ankh.loader.AnkhCoreLoaderPlugin")

    exclude("classpath.index")
    exclude("module-info.class", "META-INF/versions/*/module-info.class")

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