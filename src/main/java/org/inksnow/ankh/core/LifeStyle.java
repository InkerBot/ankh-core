package org.inksnow.ankh.core;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.Bukkit;
import org.inksnow.ankh.core.api.plugin.PluginLifeCycle;
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptLifecycle;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

@Singleton
@Slf4j
public class LifeStyle {
  private static final @Nonnull String[] MESSAGE = new String[]{
      "====================================================",
      "AnkhCore is not designed to load after server running, It's very dangerous",
      "To protect your saves or data, AnkhCore will stop your server",
      "IT IS NOT A BUG, please disable plugins likes PluginMan, YUM",
      "===================================================="
  };
  private static final @Nonnull String JOINED_MESSAGE = String.join("\n", MESSAGE);

  @SubscriptLifecycle(PluginLifeCycle.ENABLE)
  private void onEnable() {
    val worldList = Bukkit.getWorlds();
    for (val world : worldList) {
      for (val chunk : world.getLoadedChunks()) {
        if (chunk.isLoaded()) {
          for (val line : MESSAGE) {
            logger.error(line);
          }
          Runtime.getRuntime().halt(-1);
          throw new SpongeBob();
        }
      }
    }
  }

  @SubscriptLifecycle(PluginLifeCycle.DISABLE)
  private void onDisable() {
    if (false) {
      logger.error("====================================================");
      logger.error("AnkhCore is not designed to disable when server running, It's very dangerous");
      logger.error("To protect your saves or data, AnkhCore will stop your server");
      logger.error("It's not a issue, please disable plugins likes PluginMan, YUM");
      logger.error("====================================================");
      Bukkit.shutdown();
    }
  }

  private static final class SpongeBob extends StackOverflowError {
    public SpongeBob() {
      throw new PatrickStar();
    }

    @Override
    public String getMessage() {
      return JOINED_MESSAGE;
    }
  }

  private static final class PatrickStar extends OutOfMemoryError {
    public PatrickStar() {
      throw new SpongeBob();
    }

    @Override
    public String getMessage() {
      return JOINED_MESSAGE;
    }
  }
}
