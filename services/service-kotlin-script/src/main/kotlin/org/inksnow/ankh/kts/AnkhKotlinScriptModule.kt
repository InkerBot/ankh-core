package org.inksnow.ankh.kts

import com.google.inject.AbstractModule
import org.inksnow.ankh.core.api.plugin.annotations.PluginModule
import org.inksnow.ankh.kts.command.AnkhKtsDebugCommand

@PluginModule
class AnkhKotlinScriptModule : AbstractModule() {
    override fun configure() {
        bind(AnkhKtsDebugCommand::class.java)
        bind(AnkhKotlinScriptMenuService::class.java)
    }
}
