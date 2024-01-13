plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    api("bot.inker.bukkit:callsite-nbt-fat:1.1-3")
}

tasks.shadowJar {
    relocate("bot.inker.bukkit.nbt", "org.inksnow.ankh.core.nbt")
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

ext["publishAction"] = Action<MavenPublication> {
    artifact(tasks.shadowJar) {
        classifier = ""
    }
}