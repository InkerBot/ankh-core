package org.inksnow.ankh.core.api.inventory.menu.slot;

import org.inksnow.ankh.core.api.inventory.menu.slot.action.ConfigurableSlotAction;
import org.inksnow.ankh.core.api.ioc.IocLazy;
import org.inksnow.ankh.core.api.util.DcLazy;

import javax.annotation.Nonnull;

public interface SlotAction {
  static @Nonnull ConfigurableSlotAction create() {
    return Factory.INSTANCE.get().create();
  }

  default boolean mutable() {
    return false;
  }

  default void update() {
  }

  interface Factory {
    @Nonnull
    DcLazy<SlotAction.Factory> INSTANCE = new IocLazy<>(SlotAction.Factory.class);

    @Nonnull
    ConfigurableSlotAction create();
  }
}
