plugins {
  id("java-library")
  id("maven-publish")
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
    maven("file:///Users/inkerbot/IdeaProjects/ankhloader/build/publish")
    maven("https://r.irepo.space/maven/")
    maven("https://repo.inker.bot/repository/maven-snapshots/")
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
          if (project.version.toString().endsWith("-SNAPSHOT")) {
            maven("https://repo.inker.bot/repository/maven-snapshots/") {
              credentials {
                username = System.getenv("NEXUS_USERNAME")
                password = System.getenv("NEXUS_PASSWORD")
              }
            }
          } else {
            maven("https://repo.inker.bot/repository/maven-releases/") {
              credentials {
                username = System.getenv("NEXUS_USERNAME")
                password = System.getenv("NEXUS_PASSWORD")
              }
            }
            maven("https://s0.blobs.inksnow.org/maven/") {
              credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
              }
            }
          }
        } else {
          maven(rootProject.buildDir.resolve("publish"))
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

          if(project.ext.has("publishAction")){
            (project.ext["publishAction"] as Action<MavenPublication>)(this)
          }else{
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

  if(path.startsWith(":libs:")){
    arrayOf("sourceJar", "javadoc").forEach {
      tasks.findByName(it)?.enabled = false
    }
  }else{
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
  compileOnly(project(":libs:shadow-spring-boot-loader", configuration = "shadow"))

  // kotlin
  api("org.jetbrains.kotlin:kotlin-stdlib:1.8.20") {
    exclude("org.jetbrains", "annotations")
  }

  // minecraft
  compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT") {
    exclude("com.google.code.gson", "gson") // we use our version
    exclude("it.unimi.dsi", "fastutil") // we use our version
    exclude("org.checkerframework", "checker-qual")
    exclude("org.jetbrains", "annotations")
  }
  api("bot.inker.bukkit:callsite-nbt:1.0-27")

  // adventure
  compileOnly("net.kyori:adventure-api:4.13.1") {
    exclude("org.checkerframework", "checker-qual")
    exclude("org.jetbrains", "annotations")
  }
  compileOnly("net.kyori:adventure-text-minimessage:4.13.1") {
    exclude("org.checkerframework", "checker-qual")
    exclude("org.jetbrains", "annotations")
  }
  compileOnly("net.kyori:adventure-platform-bukkit:4.3.0") {
    exclude("org.checkerframework", "checker-qual")
    exclude("org.jetbrains", "annotations")
  }

  // base utils
  api("it.unimi.dsi:fastutil:8.5.12")
  api("com.google.inject:guice:5.1.0") {
    exclude("com.google.guava", "guava")  // we use our version
  }
  api("org.ow2.asm:asm:9.4")
  api("org.ow2.asm:asm-analysis:9.4")
  api("org.ow2.asm:asm-commons:9.4")
  api("org.ow2.asm:asm-tree:9.4")
  api("org.ow2.asm:asm-util:9.4")

  // script
  api(project(":libs:shadow-bsh"))

  // config
  api("com.typesafe:config:1.4.2")
  api("com.google.code.gson:gson:2.10.1")
  api("org.yaml:snakeyaml:2.0")
  api("org.hibernate.validator:hibernate-validator:7.0.5.Final") {
    exclude("jakarta.validation", "jakarta.validation-api") // use the version in api
    exclude("org.jboss.logging", "jboss-logging") // don't expose logger
  }
  runtimeOnly("org.jboss.logging:jboss-logging:3.4.1.Final")


  // shadow depends
  api(project(":libs:shadow-paper-lib", configuration = "shadow"))

  // other plugins (api usage)
  compileOnly("me.filoghost.holographicdisplays:holographicdisplays-api:3.0.0")
  compileOnly("org.inksnow.asteroid:core:1.0-SNAPSHOT"){
    exclude("*")
  }

  // logger binding
  implementation("org.apache.logging.log4j:log4j-to-slf4j:2.20.0")
  api("org.slf4j:slf4j-api:2.0.6")
}