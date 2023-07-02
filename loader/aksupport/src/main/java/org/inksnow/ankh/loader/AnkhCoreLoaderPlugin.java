package org.inksnow.ankh.loader;

import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.plugin.java.JavaPlugin;
import org.inksnow.ankh.cloud.AnkhCloudLoader;
import org.inksnow.ankh.core.api.AnkhCoreLoader;
import org.inksnow.ankh.core.api.plugin.AnkhPluginContainer;
import org.inksnow.asteroid.AkConfigLoader;
import org.inksnow.asteroid.AkDeployResult;
import org.inksnow.asteroid.AkLoader;

import java.util.logging.Level;

public final class AnkhCoreLoaderPlugin extends JavaPlugin implements AnkhCoreLoader {
  private AnkhPluginContainer container;

  @Override
  public void onLoad() {
    //
  }

  @SneakyThrows
  public void onInject() {
    AnkhCloudLoader.initial();
    val ankhCoreEnvironment = AkLoader.getOrCreateEnvironment("ankh-core-impl");
    val ankhCoreConfig = new AkConfigLoader("ankh-core-impl");
    ankhCoreConfig.applyToEnvironment(ankhCoreEnvironment);
    val deployResult = ankhCoreEnvironment.deploy();
    if(deployResult != AkDeployResult.SUCCESS) {
      getLogger().log(Level.SEVERE, "Deploy success, need restart");
      System.exit(0);
      Runtime.getRuntime().halt(-1);
      throw new RuntimeException("Never been called");
    }
    val ankhClassLoader = ankhCoreEnvironment.classLoader();
    container = (AnkhPluginContainer) Class.forName("org.inksnow.ankh.core.loader.AnkhPluginLoader", true, ankhClassLoader)
        .getMethod("load", AnkhCoreLoader.class)
        .invoke(null, this);
    container.callClinit();
    container.callInit(this);
    container.callLoad();
  }

  @Override
  public void onEnable() {
    container.callEnable();
  }

  @Override
  public void onDisable() {
    container.callDisable();
  }
}
