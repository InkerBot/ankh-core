package org.inksnow.ankh.core.command;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.inksnow.ankh.core.api.plugin.PluginLifeCycle;
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptLifecycle;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CommandApiServiceImpl {
  private final @Nonnull Plugin corePlugin;

  @Inject
  private CommandApiServiceImpl(@Nonnull Plugin corePlugin) {
    this.corePlugin = corePlugin;
  }

  @SubscriptLifecycle(value = PluginLifeCycle.LOAD, priority = EventPriority.LOWEST)
  private void onLoad() {
    CommandAPI.onLoad(
        new CommandAPIBukkitConfig((JavaPlugin) corePlugin)
            .verboseOutput(true));
  }

  @SubscriptLifecycle(PluginLifeCycle.ENABLE)
  private void onEnable() {
    CommandAPI.onEnable();
  }

  @SubscriptLifecycle(PluginLifeCycle.DISABLE)
  private void onDisable() {
    CommandAPI.onDisable();
  }
}
