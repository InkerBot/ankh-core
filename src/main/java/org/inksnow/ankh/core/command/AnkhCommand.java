package org.inksnow.ankh.core.command;

import dev.jorel.commandapi.CommandAPICommand;
import lombok.val;
import org.bukkit.entity.Player;
import org.inksnow.ankh.core.api.plugin.PluginLifeCycle;
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptLifecycle;
import org.inksnow.ankh.core.inventory.menu.TestMenu;
import org.inksnow.ankh.core.item.debug.DebugToolsMenu;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AnkhCommand {
  private final DebugToolsMenu debugToolsMenu;

  @Inject
  private AnkhCommand(DebugToolsMenu debugToolsMenu) {
    this.debugToolsMenu = debugToolsMenu;
  }

  @SubscriptLifecycle(PluginLifeCycle.LOAD)
  private void registerCommand() {
    new CommandAPICommand("ankh")
        .withSubcommand(new CommandAPICommand("debug-tools")
            .withPermission("ankhcore.debugtools.inventory")
            .executes(((sender, args) -> {
              if (sender instanceof Player) {
                val player = (Player) sender;
                debugToolsMenu.openForPlayer(player);
              } else {
                sender.sendMessage("Sorry, this command only can be used by player");
              }
            })))
        .withSubcommand(new CommandAPICommand("opentestmenu")
            .withPermission("ankhcore.reload")
            .executes((sender, args) -> {
              if (sender instanceof Player) {
                val player = (Player) sender;
                player.openInventory(new TestMenu().inventory());
              } else {
                sender.sendMessage("Sorry, this command only can be used by player");
              }
            }))
        .register();
  }
}
