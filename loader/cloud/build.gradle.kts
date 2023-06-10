dependencies {
  compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
  compileOnly("org.apache.httpcomponents:httpclient:4.5.14")

  // lombok
  compileOnly("org.projectlombok:lombok:1.18.26")
  annotationProcessor("org.projectlombok:lombok:1.18.26")
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