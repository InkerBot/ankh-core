dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.apache.httpcomponents:httpclient:4.5.14")
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