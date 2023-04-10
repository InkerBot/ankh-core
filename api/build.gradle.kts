dependencies {
  compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT") {
    exclude("org.checkerframework", "checker-qual")
    exclude("org.jetbrains", "annotations")
  }
  api("javax.inject:javax.inject:1")
}