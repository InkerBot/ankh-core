package org.inksnow.ankh.core.command;

import lombok.val;
import mx.kenzie.centurion.Command;
import mx.kenzie.centurion.CommandResult;
import mx.kenzie.centurion.MinecraftCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.api.AnkhCoreLoader;
import org.inksnow.ankh.core.api.plugin.PluginLifeCycle;
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptLifecycle;
import org.inksnow.ankh.core.common.AdventureAudiences;
import org.inksnow.ankh.core.item.debug.DebugToolsMenu;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AnkhCommand extends MinecraftCommand {
  private final AnkhCoreLoader coreLoader;
  private final DebugToolsMenu debugToolsMenu;

  @Inject
  private AnkhCommand(AnkhCoreLoader coreLoader, DebugToolsMenu debugToolsMenu) {
    super("AnkhCore Command", "/ankh", null, null);
    this.coreLoader = coreLoader;
    this.debugToolsMenu = debugToolsMenu;
  }

  @Override
  public Command<CommandSender>.Behaviour create() {
    return command(AnkhCore.PLUGIN_ID, "ankh")
        .arg("debug-tools", (user, arguments) -> {
          if (!user.hasPermission("ankhcore.debugtools.inventory")) {
            AdventureAudiences.sender(user).sendMessage(permissionMessage());
            return CommandResult.PASSED;
          }
          if (user instanceof Player) {
            val player = (Player) user;
            debugToolsMenu.openForPlayer(player);
          } else {
            user.sendMessage("Sorry, this command only can be used by player");
          }
          return CommandResult.PASSED;
        });
  }

  @SubscriptLifecycle(PluginLifeCycle.ENABLE)
  private void registerCommand() {
    register(coreLoader);
  }
}
