configurations {
  create("ankhShadow")
}

dependencies {
  compileOnly(project(":api"))
  compileOnly(project(":loader:cloud"))
  compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
  compileOnly("org.inksnow.asteroid:core:1.0-SNAPSHOT"){
    exclude("*")
  }

  "ankhShadow"(project(":api"))
  "ankhShadow"(project(":loader:cloud"))

  implementation("net.kyori:adventure-api:4.13.1")
  implementation("net.kyori:adventure-text-minimessage:4.13.1")
  implementation("net.kyori:adventure-platform-bukkit:4.3.0")
}

tasks.processResources {
  dependsOn(project(":api").tasks["jar"])
  dependsOn(project(":loader:cloud").tasks["jar"])

  from(configurations.getByName("ankhShadow").map {
    if (it.isFile) {
      zipTree(it)
    }else{
      it
    }
  })
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}