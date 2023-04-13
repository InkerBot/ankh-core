package org.inksnow.ankh.core.common;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import javax.annotation.Nonnull;

@Slf4j
public class EnsureIgnoreCancelledEventExecutor implements EventExecutor {
  private final EventExecutor delegate;

  public EnsureIgnoreCancelledEventExecutor(EventExecutor delegate) {
    this.delegate = delegate;
  }

  @Override
  public void execute(@Nonnull Listener listener, @Nonnull Event event) throws EventException {
    if (event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
      logger.error("====================================================");
      logger.error("Found cancelled event called in ignore-cancelled executor");
      logger.error("You should update your server software to latest version");
      logger.error("It may caused by https://github.com/PaperMC/Paper/pull/9099");
      logger.error("If you run latest server software");
      logger.error("please commit issue to your server software", new EventException());
      logger.error("====================================================");
    } else {
      delegate.execute(listener, event);
    }
  }
}
