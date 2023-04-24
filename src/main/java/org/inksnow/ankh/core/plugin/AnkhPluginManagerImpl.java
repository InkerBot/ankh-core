package org.inksnow.ankh.core.plugin;

import com.google.inject.Guice;
import com.google.inject.name.Names;
import lombok.val;
import org.bukkit.plugin.PluginDescriptionFile;
import org.inksnow.ankh.core.api.plugin.AnkhBukkitPlugin;
import org.inksnow.ankh.core.api.plugin.AnkhPluginContainer;
import org.inksnow.ankh.core.api.plugin.AnkhPluginManager;
import org.inksnow.ankh.core.api.plugin.AnkhPluginYml;
import org.inksnow.ankh.core.common.config.AnkhConfig;
import org.inksnow.ankh.loader.AnkhClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AnkhPluginManagerImpl implements AnkhPluginManager {
  private static final AnkhPluginManagerImpl instance = new AnkhPluginManagerImpl();

  public static AnkhPluginManagerImpl instance() {
    return instance;
  }

  @Override
  public AnkhPluginContainer register(Class<? extends AnkhBukkitPlugin> pluginClass, File file, ClassLoader rawClassLoader, PluginDescriptionFile descriptionFile, AnkhPluginYml pluginYml) {
    if (!(rawClassLoader instanceof AnkhClassLoader)) {
      throw new IllegalArgumentException("ClassLoader should be instance of AnkhClassLoader");
    }
    val classLoader = (AnkhClassLoader) rawClassLoader;
    val logger = LoggerFactory.getLogger(pluginYml.getName());
    val container = new AnkhPluginContainerImpl(pluginClass, classLoader, descriptionFile, pluginYml);
    val scannerInjector = Guice.createInjector(binder -> {
      binder.bind(Logger.class).toInstance(logger);
      binder.bind(AnkhConfig.class).toProvider(AnkhConfig.provider());
      binder.bind(Class.class).annotatedWith(Names.named("pluginClass")).toInstance(pluginClass);
      binder.bind(File.class).annotatedWith(Names.named("pluginFile")).toInstance(file);
      binder.bind(AnkhClassLoader.class).toInstance(classLoader);
      binder.bind(PluginDescriptionFile.class).toInstance(descriptionFile);
      binder.bind(AnkhPluginYml.class).annotatedWith(Names.named("pluginYml")).toInstance(pluginYml);
      binder.bind(AnkhPluginContainer.class).to(AnkhPluginContainerImpl.class);
      binder.bind(AnkhPluginContainerImpl.class).toInstance(container);
    });
    scannerInjector.getInstance(PluginClassScanner.class).scan();
    return container;
  }
}
