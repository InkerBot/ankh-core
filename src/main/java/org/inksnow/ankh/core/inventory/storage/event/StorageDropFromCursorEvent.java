package org.inksnow.ankh.core.inventory.storage.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class StorageDropFromCursorEvent {
  @Getter
  private final @Nonnull Player player;
  @Getter
  private final @Nonnull ItemStack item;
  // -1 if unknown
  @Getter
  private final int amount;
}
