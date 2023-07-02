package org.inksnow.ankh.core.api.plugin;

import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.inksnow.ankh.core.api.AnkhCoreLoader;

public interface AnkhPluginContainer {
  void callClinit();

  void callInit(AnkhCoreLoader bukkitPlugin);

  void callLoad();

  void callDisable();

  void callEnable();

  void onClinit(EventPriority priority, Runnable listener);

  void onInit(EventPriority priority, Runnable listener);

  void onLoad(EventPriority priority, Runnable listener);

  void onEnable(EventPriority priority, Runnable listener);

  void onDisable(EventPriority priority, Runnable listener);

  Plugin plugin();
}
