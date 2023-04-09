package org.inksnow.ankh.testplugin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.ItemStack
import org.inksnow.ankh.core.api.plugin.AnkhBukkitPlugin
import org.inksnow.ankh.core.api.plugin.PluginLifeCycle
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptEvent
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptLifecycle
import org.inksnow.ankh.core.inventory.storage.StorageChestMenu
import org.inksnow.ankh.testplugin.item.TestItem
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestListener @Inject private constructor(
  private val plugin: AnkhBukkitPlugin,
  private val testItem: TestItem,
) {
  private val logger = LoggerFactory.getLogger(this.javaClass)

  @SubscriptEvent
  private fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
    if (!event.message.startsWith("@")) {
      return
    }
    Bukkit.getScheduler().runTask(plugin, Runnable {
      runSimpleCommand(event.player, event.message.substring(1))
    })
  }

  private fun runSimpleCommand(player: Player, command: String) {
    when (command) {
      "a" -> {
        player.inventory.addItem(
          testItem.createItem()
        ).values.forEach {
          player.world.dropItemNaturally(player.location, it)
        }
      }

      "b" -> {
        StorageChestMenu.builder().apply {
          createInventory {
            Bukkit.createInventory(it, 54, Component.text("Hello, world.", NamedTextColor.RED)).apply {
              for (i in 0 until 54) {
                setItem(i, ItemStack(Material.STONE).apply {
                  editMeta {
                    it.displayName(Component.text("TEST_ITEM", NamedTextColor.BLUE))
                  }
                })
              }
            }
          }

          canPlaceAction { event, cancelToken ->
            //
          }

          canPickupAction { event, cancelToken ->
            //
          }

          canDropFromCursorAction { event, cancelToken ->
            //
          }
        }.build().openForPlayer(player)
      }
    }
  }

  @SubscriptLifecycle(PluginLifeCycle.LOAD)
  private fun onLoad() {

  }
}