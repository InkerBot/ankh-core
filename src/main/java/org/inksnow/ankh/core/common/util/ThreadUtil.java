package org.inksnow.ankh.core.common.util;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.inksnow.ankh.core.api.AnkhCoreLoader;
import org.inksnow.ankh.core.api.ioc.IocLazy;
import org.inksnow.ankh.core.api.util.DcLazy;

import java.util.concurrent.Executor;

@UtilityClass
public class ThreadUtil {
  private static final BukkitScheduler scheduler = Bukkit.getScheduler();
  private static final DcLazy<AnkhCoreLoader> coreLoader = IocLazy.of(AnkhCoreLoader.class);
  @Getter
  private static final Executor mainExecutor = new MainScheduler();
  @Getter
  private static final Executor asyncExecutor = new AsyncScheduler();

  private static class MainScheduler implements Executor {
    @Override
    public void execute(Runnable command) {
      AnkhCoreLoader coreLoader = ThreadUtil.coreLoader.get();
      if (!Bukkit.isPrimaryThread() && coreLoader.isEnabled()) {
        scheduler.runTask(coreLoader, command);
      } else {
        command.run();
      }
    }
  }

  private static class AsyncScheduler implements Executor {
    @Override
    public void execute(Runnable command) {
      AnkhCoreLoader coreLoader = ThreadUtil.coreLoader.get();
      if (coreLoader.isEnabled()) {
        scheduler.runTaskAsynchronously(coreLoader, command);
      } else {
        command.run();
      }
    }
  }
}
