group = "org.inksnow.ankh.groovy"

dependencies {
  implementation("org.apache.groovy:groovy:4.0.10")
  compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
  compileOnly(project(":"))
}

tasks.javadocJar {
  enabled = false
}

tasks.sourcesJar {
  enabled = false
}