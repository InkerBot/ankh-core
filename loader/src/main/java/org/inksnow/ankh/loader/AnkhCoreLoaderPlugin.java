package org.inksnow.ankh.loader;

import org.bukkit.plugin.java.JavaPlugin;
import org.inksnow.ankh.core.api.AnkhCoreLoader;
import org.inksnow.ankh.core.api.plugin.AnkhPluginContainer;
import org.inksnow.ankh.core.nbt.loader.CallSiteNbt;

import java.io.File;
import java.util.function.Consumer;

public class AnkhCoreLoaderPlugin extends JavaPlugin implements AnkhCoreLoader {
  @SuppressWarnings("unused") // by hc-loader
  private static ClassLoader $hcloader$delegateClassLoader;
  @SuppressWarnings("unused") // by hc-loader
  private static Consumer<File> $hcloader$addDelegateFile;

  private static Object container;

  static {
    try {
      final File[] ankhPluginFiles = new File("plugins")
          .listFiles(file -> !file.isDirectory() && file.getName().endsWith(".ankhplugin"));

      if (ankhPluginFiles != null) {
        for (final File pluginFile : ankhPluginFiles) {
          $hcloader$addDelegateFile.accept(pluginFile);
        }
      }

      CallSiteNbt.install(AnkhCoreLoaderPlugin.class);
      // AnkhCloudLoader.initial();

      final Class<?> containerClass = Class.forName(
          "org.inksnow.ankh.core.loader.AnkhPluginLoader",
          true,
          $hcloader$delegateClassLoader);

      final AnkhPluginContainer container = (AnkhPluginContainer) containerClass.getMethod("load")
          .invoke(null);
      AnkhCoreLoaderPlugin.container = container;
      container.callClinit();
    } catch (RuntimeException e) {
      throw e;
    } catch (Error e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public AnkhCoreLoaderPlugin() {
    ((AnkhPluginContainer) container).callInit(this);
  }

  @Override
  public void onLoad() {
    ((AnkhPluginContainer) container).callLoad();
  }

  @Override
  public void onEnable() {
    ((AnkhPluginContainer) container).callEnable();
  }

  @Override
  public void onDisable() {
    ((AnkhPluginContainer) container).callDisable();
  }
}
