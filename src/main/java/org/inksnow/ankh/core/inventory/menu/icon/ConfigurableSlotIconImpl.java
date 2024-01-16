package org.inksnow.ankh.core.inventory.menu.icon;

import lombok.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotIcon;
import org.inksnow.ankh.core.api.inventory.menu.slot.icon.ConfigurableSlotIcon;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurableSlotIconImpl implements ConfigurableSlotIcon {
  @Getter
  @Setter
  private @Nullable ItemStack item;

  @Nonnull
  @Override
  public ItemStack create() {
    return item == null ? new ItemStack(Material.AIR) : item.clone();
  }

  @Singleton
  public static class Factory implements SlotIcon.Factory {
    @Override
    public @Nonnull ConfigurableSlotIcon create(@Nullable ItemStack itemStack) {
      val result = new ConfigurableSlotIconImpl();
      result.item(itemStack);
      return result;
    }
  }
}
