group = "org.inksnow.ankh.neigeitems"

repositories {
  maven("https://r.irepo.space/maven/")
}

dependencies {
  compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
  compileOnly(project(":"))
  compileOnly("pers.neige.neigeitems:NeigeItems:1.12.9")
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