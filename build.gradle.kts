plugins {
    `java-library`
    `maven-publish`
}

allprojects {
    if (project.buildscript.sourceFile?.exists() != true) {
        project.tasks.forEach { it.enabled = false }
        return@allprojects
    }

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = if (rootProject == project) {
        "org.inksnow.ankh"
    } else {
        "org.inksnow.ankh.core"
    }


    val buildNumber = System.getenv("BUILD_NUMBER")
    version = if (buildNumber == null) {
        "1.1-dev-SNAPSHOT"
    } else {
        "1.1-$buildNumber-SNAPSHOT"
    }

    repositories {
        mavenCentral()
        maven("https://r.bgp.ink/maven/")
        maven("https://r.irepo.space/maven/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }

    dependencies {
        // lombok
        compileOnly("org.projectlombok:lombok:1.18.26")
        annotationProcessor("org.projectlombok:lombok:1.18.26")
    }

    java {
        if (path != ":services:service-js-nashorn") {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        withSourcesJar()
        withJavadocJar()
    }

    afterEvaluate {
        publishing {
            repositories {
                if (System.getenv("BUILD_NUMBER")?.isNotEmpty() == true) {
                    maven("https://s0.blobs.inksnow.org/maven/") {
                        credentials {
                            username = System.getenv("REPO_USERNAME")
                            password = System.getenv("REPO_PASSWORD")
                        }
                    }
                } else {
                    maven(rootProject.layout.buildDirectory.dir("publish"))
                }
            }

            publications {
                create<MavenPublication>("mavenJar") {
                    artifactId = project.path
                        .removePrefix(":")
                        .replace(':', '-')
                        .ifEmpty { "core" }

                    pom {
                        name.set("AnkhCore ${project.name}")
                        description.set("A bukkit plugin loader named AnkhCore")
                        url.set("https://github.com/ankhorg/ankhcore")
                        properties.set(mapOf())
                        licenses {
                            license {
                                name.set("MIT")
                                url.set("https://opensource.org/licenses/MIT")
                                distribution.set("repo")
                            }
                        }
                        developers {
                            developer {
                                id.set("inkerbot")
                                name.set("InkerBot")
                                email.set("im@inker.bot")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/ankhorg/ankhcore.git")
                            developerConnection.set("scm:git:ssh://github.com/ankhorg/ankhcore.git")
                            url.set("https://github.com/ankhorg/ankhcore")
                        }
                    }

                    if (project.ext.has("publishAction")) {
                        (project.ext["publishAction"] as Action<MavenPublication>)(this)
                    } else {
                        from(components["java"])
                    }
                }
            }
        }
    }

    tasks.javadoc {
        options.encoding = "UTF-8"
        (options as CoreJavadocOptions).addStringOption("Xdoclint:none")
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
    }

    if (path.startsWith(":libs:")) {
        arrayOf("sourceJar", "javadoc").forEach {
            tasks.findByName(it)?.enabled = false
        }
    } else {
        tasks.findByName("javadoc")?.run {
            this as Javadoc
            options.encoding = "UTF-8"
            (options as CoreJavadocOptions).addStringOption("Xdoclint:none")
        }
    }
}

dependencies {
    // project base
    api(project(":api"))

    // kotlin
    api(libs.kotlin.stdlib) {
        exclude("org.jetbrains", "annotations")
    }

    // minecraft
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT") {
        exclude("com.google.code.gson", "gson") // we use our version
        exclude("it.unimi.dsi", "fastutil") // we use our version
        exclude("org.checkerframework", "checker-qual")
        exclude("org.jetbrains", "annotations")
    }

    // adventure
    compileOnly(libs.bundles.adventure) {
        exclude("org.checkerframework", "checker-qual")
        exclude("org.jetbrains", "annotations")
    }

    // base utils
    api(libs.fastutil)
    api(libs.guice) {
        exclude("com.google.guava", "guava")  // we use our version
    }
    api(libs.glob)

    api(libs.bundles.asm)

    // script
    api(project(":libs:shadow-bsh"))

    // config
    api(libs.typesafe.config)
    api(libs.gson)
    api(libs.snakeyaml)
    api(libs.hibernate.validator) {
        exclude("jakarta.validation", "jakarta.validation-api") // use the version in api
        exclude("org.jboss.logging", "jboss-logging") // don't expose logger
    }
    runtimeOnly(libs.jobss.logging)

    // shadow depends
    api(project(":libs:shadow-paper-lib", configuration = "shadow"))
    api(project(":libs:shadow-callsite-nbt", configuration = "shadow"))

    // other plugins (api usage)
    compileOnly("me.filoghost.holographicdisplays:holographicdisplays-api:3.0.0")

    // logger binding
    api(libs.slf4j.api)
    runtimeOnly(libs.slf4j.jdk14)
}