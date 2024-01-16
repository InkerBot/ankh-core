package org.inksnow.ankh.core.api.inventory.menu.slot;

import org.bukkit.inventory.ItemStack;
import org.inksnow.ankh.core.api.inventory.menu.slot.icon.ConfigurableSlotIcon;
import org.inksnow.ankh.core.api.ioc.IocLazy;
import org.inksnow.ankh.core.api.util.DcLazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface SlotIcon {
  static @Nonnull ConfigurableSlotIcon create(@Nullable ItemStack itemStack) {
    return Factory.INSTANCE.get().create(itemStack);
  }

  @Nullable
  ItemStack create();

  default @Nullable ItemStack update(@Nullable ItemStack item) {
    return create();
  }

  interface Factory {
    @Nonnull
    DcLazy<Factory> INSTANCE = new IocLazy<>(Factory.class);

    @Nonnull
    ConfigurableSlotIcon create(@Nullable ItemStack itemStack);
  }
}
