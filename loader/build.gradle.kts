dependencies {
  api(project(":api"))
  compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT") {
    exclude("org.checkerframework", "checker-qual")
    exclude("org.jetbrains", "annotations")
  }
  api(project(":libs:shadow-spring-boot-loader", configuration = "shadow"))
}