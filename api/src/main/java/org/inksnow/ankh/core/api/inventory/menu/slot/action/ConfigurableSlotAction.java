package org.inksnow.ankh.core.api.inventory.menu.slot.action;

import org.inksnow.ankh.core.api.inventory.menu.slot.SlotAction;

import javax.annotation.Nonnull;

public interface ConfigurableSlotAction extends SlotAction {
  @Nonnull
  ConfigurableSlotAction mutable(boolean value);

  void onUpdate(@Nonnull Runnable listener);
}
