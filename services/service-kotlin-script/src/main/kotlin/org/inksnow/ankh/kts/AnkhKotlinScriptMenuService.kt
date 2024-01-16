package org.inksnow.ankh.kts

import kotlinx.coroutines.runBlocking
import org.bukkit.entity.Player
import org.inksnow.ankh.kts.menu.MenuKotlinInstance
import org.inksnow.ankh.kts.menu.MenuKotlinScript
import org.inksnow.ankh.kts.menu.dsl.MenuDsl
import org.inksnow.ankh.kts.util.DigestUtils
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.compilationCache
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.CompiledScriptJarsCache
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

@Singleton
class AnkhKotlinScriptMenuService @Inject private constructor() {
    val scriptHost by lazy {
        val hostConfig = ScriptingHostConfiguration {
            jvm {
                baseClassLoader(AnkhKotlinScriptModule::class.java.classLoader)
                compilationCache(CompiledScriptJarsCache { source, _ ->
                    val hash = DigestUtils.sha256(source.text)
                    val safeName = source.name?.replace("[:\\\\/*\"?|<>']", "_") ?: hash
                    File("cache/ankh-core/script/${safeName}/$hash.jar")
                        .apply { parentFile.mkdirs() }
                })
            }
        }
        BasicJvmScriptingHost(hostConfig)
    }

    private val menuCompilationConfiguration by lazy {
        createJvmCompilationConfigurationFromTemplate<MenuKotlinScript>()
    }

    private val menuEvaluationConfiguration by lazy {
        ScriptEvaluationConfiguration {
            jvm {
                baseClassLoader(AnkhKotlinScriptModule::class.java.classLoader)
            }
            constructorArgs(null)
        }
    }

    fun showMenu(player: Player, scriptFile: File) {
        val compileResult = runBlocking {
            scriptHost.compiler(scriptFile.toScriptSource(), menuCompilationConfiguration)
        }.collect()
        val invokeResult = runBlocking {
            compileResult.getClass(menuEvaluationConfiguration)
        }.collect()
        val menuKotlinScript = invokeResult.java
            .getConstructor()
            .newInstance() as MenuKotlinScript

        val menuInstance = MenuKotlinInstance(player)
        val menuDsl = MenuDsl(menuInstance)
        menuKotlinScript.menuActions.forEach(menuDsl::apply)
        player.openInventory(menuInstance.inventory)
    }

    private fun <T> ResultWithDiagnostics<T>.collect(): T {
        reports.forEach {
            println(it.message)
            it.exception?.printStackTrace()
        }
        return valueOrThrow()
    }

    private fun onLoad() {
        scriptHost.compiler
    }
}