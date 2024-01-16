package org.inksnow.ankh.kts.menu

import org.inksnow.ankh.kts.AnkhKotlinScriptModule
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm

object MenuKotlinConfiguration : ScriptCompilationConfiguration({
    defaultImports("org.bukkit.*")
    defaultImports("org.bukkit.inventory.*")

    defaultImports("org.inksnow.ankh.kts.dsl.*")
    defaultImports("org.inksnow.ankh.kts.menu.dsl.*")

    jvm {
        dependenciesFromClassloader(classLoader = AnkhKotlinScriptModule::class.java.classLoader, wholeClasspath = true)
    }

    refineConfiguration {
        //
    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere) // these scripts are recognized everywhere in the project structure
    }
})