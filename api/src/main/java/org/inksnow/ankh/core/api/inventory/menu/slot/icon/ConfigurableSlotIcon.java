package org.inksnow.ankh.core.api.inventory.menu.slot.icon;

import org.bukkit.inventory.ItemStack;
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotIcon;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ConfigurableSlotIcon extends SlotIcon {
  @Nullable
  ItemStack item();

  @Nonnull
  ConfigurableSlotIcon item(@Nullable ItemStack item);
}
