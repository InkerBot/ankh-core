package org.inksnow.ankh.core.common;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Cancellable;

@AllArgsConstructor
@RequiredArgsConstructor
public final class CancellableToken implements Cancellable {
  private boolean cancelled = false;

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    cancelled = cancel;
  }
}
