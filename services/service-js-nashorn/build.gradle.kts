plugins {
  id("me.champeau.mrjar").version("0.1.1")
}

group = "org.inksnow.ankh.jsnashorn"

multiRelease {
  targetVersions(8, 11)
}

configurations {
  create("ankhShadow")
  create("ankhApi")
  create("ankhImpl")
}

dependencies {
  "java11Implementation"("org.openjdk.nashorn:nashorn-core:15.4")
  "java11Implementation"(project(":"))

  "ankhImpl"("org.openjdk.nashorn:nashorn-core:15.4") {
    exclude("org.ow2.asm")
  }
  compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
  compileOnly(project(":"))
}

tasks.jar {
  entryCompression = ZipEntryCompression.STORED
  from(configurations.getByName("ankhShadow").map {
    if (it.isFile) {
      zipTree(it)
    } else {
      it
    }
  })

  into("ankh-api") {
    from(configurations.getByName("ankhApi"))
  }

  into("ankh-impl") {
    from(configurations.getByName("ankhImpl").filter {
      !configurations.getByName("ankhShadow").contains(it) &&
          !configurations.getByName("ankhApi").contains(it)
    })
  }
}

tasks.publish {
  enabled = false
}

tasks.javadocJar {
  enabled = false
}

tasks.sourcesJar {
  enabled = false
}