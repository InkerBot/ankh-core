group = "org.inksnow.ankh.js-nashorn"

dependencies {
  implementation("org.openjdk.nashorn:nashorn-core:15.4") {
    exclude("org.ow2.asm:asm")
  }
  compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
  compileOnly(project(":"))
}

tasks.javadocJar {
  enabled = false
}

tasks.sourcesJar {
  enabled = false
}
