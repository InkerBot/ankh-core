package org.inksnow.ankh.cloud.util;

import com.google.common.collect.MapMaker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;

public final class PluginSchedulerExecutor implements Executor {
  private static final Map<Plugin, PluginSchedulerExecutor> instanceMap =
      new MapMaker().weakKeys().weakValues().makeMap();
  private static final Function<Plugin, PluginSchedulerExecutor> instanceFunction = PluginSchedulerExecutor::new;

  private final Plugin plugin;

  private PluginSchedulerExecutor(Plugin plugin) {
    this.plugin = plugin;
  }

  public static PluginSchedulerExecutor of(Plugin plugin) {
    Objects.requireNonNull(plugin);
    return instanceMap.computeIfAbsent(plugin, instanceFunction);
  }

  @Override
  public void execute(Runnable command) {
    if (Bukkit.isPrimaryThread() || !plugin.isEnabled()) {
      command.run();
    } else {
      Bukkit.getScheduler().runTask(plugin, command);
    }
  }

  @Override
  public String toString() {
    return "PluginSchedulerExecutor{" + plugin + "}";
  }
}
