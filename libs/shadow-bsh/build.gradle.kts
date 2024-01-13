dependencies {
    api("org.beanshell:bsh:3.0.0-SNAPSHOT")
}

ext["publishAction"] = Action<MavenPublication> {
    val singleFile = configurations.runtimeClasspath
        .get()
        .resolve()
        .single()
    artifact(singleFile) {
        extension = "jar"
        classifier = ""
    }
}