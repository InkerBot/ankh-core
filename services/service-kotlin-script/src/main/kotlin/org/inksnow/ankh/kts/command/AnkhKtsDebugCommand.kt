package org.inksnow.ankh.kts.command

import bot.inker.acj.JvmHacker
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.executors.ConsoleCommandExecutor
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.inksnow.ankh.core.api.plugin.PluginLifeCycle
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptLifecycle
import org.inksnow.ankh.kts.AnkhKotlinScriptMenuService
import java.io.File
import java.lang.invoke.MethodType
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteRecursively

@Singleton
@OptIn(ExperimentalPathApi::class)
class AnkhKtsDebugCommand @Inject private constructor(
    private val menuService: AnkhKotlinScriptMenuService
) {
    private val findResources: (ClassLoader.(String) -> Enumeration<URL>) by lazy {
        val methodHandle = JvmHacker.lookup()
            .findVirtual(
                ClassLoader::class.java,
                "findResources",
                MethodType.methodType(Enumeration::class.java, String::class.java)
            );
        { name ->
            methodHandle.invoke(this, name) as Enumeration<URL>
        }
    }

    @SubscriptLifecycle(PluginLifeCycle.LOAD)
    fun registerCommand() {
        CommandAPICommand("ankhkts")
            .withSubcommand(
                CommandAPICommand("execute")
                    .withArguments(GreedyStringArgument("file"))
                    .executesPlayer(PlayerCommandExecutor { player, args ->
                        val startTime = System.nanoTime()
                        menuService.showMenu(player, File(args.getUnchecked<String>("file")!!))
                        val costTime = System.nanoTime() - startTime
                        player.sendMessage("execute in $costTime ns")
                    })
            )
            .withSubcommand(
                CommandAPICommand("context")
                    .executesConsole(ConsoleCommandExecutor { sender, args ->
                        val savePath = Paths.get("cache", "ankh-core", "context")
                        sender.sendMessage("Collecting context...")
                        savePath.deleteRecursively()

                        val scarched = IdentityHashMap<ClassLoader, Boolean>()
                        scan(scarched, savePath, AnkhKtsDebugCommand::class.java.classLoader)
                        for (plugin in Bukkit.getPluginManager().plugins) {
                            if (plugin is JavaPlugin) {
                                scan(scarched, savePath, plugin::class.java.classLoader)
                            }
                        }

                        sender.sendMessage("runtime context collected, saved to ${savePath.absolutePathString()}")
                    })
            )
            .register()
    }

    private fun scan(scarched: IdentityHashMap<ClassLoader, Boolean>, savePath: Path, classLoader: ClassLoader?) {
        if (scarched[classLoader] == true) {
            return
        }
        scarched[classLoader] = true
        val urls: Array<URL>
        if (classLoader is URLClassLoader) {
            urls = classLoader.urLs
        } else if (classLoader != null) {
            urls = classLoader.findResources("")
                .toList()
                .toTypedArray()
        } else {
            urls = object : ClassLoader(null) {}
                .findResources("")
                .toList()
                .toTypedArray()
        }
        for (url in urls) {
            if ("jar" == url.protocol) {
                val urlFilePart = url.file
                if (urlFilePart.endsWith("!/")) {
                    val urlFile = urlFilePart.dropLast(2)
                    var fileName = urlFile.substringAfterLast('/')
                    if (!fileName.endsWith(".jar")) {
                        fileName = "$fileName.jar"
                    }
                    val targetPath = savePath.resolve(fileName)
                    Files.createDirectories(targetPath.parent)
                    URL(url, urlFile).openStream().use { input ->
                        Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            } else if ("file" == url.protocol) {
                val urlFile = url.toString()
                var fileName = urlFile.substringAfterLast('/')
                if (!fileName.endsWith(".jar")) {
                    fileName = "$fileName.jar"
                }
                val targetPath = savePath.resolve(fileName)
                Files.createDirectories(targetPath.parent)
                URL(url, urlFile).openStream().use { input ->
                    Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
                }
            } else {
                println("unknown protocol ${url.protocol}")
            }
        }
        if (classLoader != null) {
            scan(scarched, savePath, classLoader.parent)
        }
    }
}