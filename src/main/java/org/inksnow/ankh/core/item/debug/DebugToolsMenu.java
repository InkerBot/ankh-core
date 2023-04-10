package org.inksnow.ankh.core.item.debug;

import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.inventory.storage.AbstractChestMenu;
import org.inksnow.ankh.core.inventory.storage.event.StorageDropFromCursorEvent;
import org.inksnow.ankh.core.inventory.storage.event.StoragePickupEvent;
import org.inksnow.ankh.core.inventory.storage.event.StoragePlaceEvent;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DebugToolsMenu {
  private static final Component MENU_TITLE = Component.text()
      .append(AnkhCore.PLUGIN_NAME_COMPONENT)
      .append(Component.text("debug tools", NamedTextColor.RED))
      .build();

  private final ItemStack[] defaultItems;

  @Inject
  private DebugToolsMenu(DebugRemoveItem debugRemoveItem) {
    defaultItems = new ItemStack[54];
    defaultItems[0] = debugRemoveItem.createItem();
  }

  public void openForPlayer(Player player) {
    new ChestMenu().openForPlayer(player);
  }

  public void openForPlayer(Player... players) {
    for (Player player : players) {
      openForPlayer(player);
    }
  }

  private class ChestMenu extends AbstractChestMenu {
    @Override
    protected Inventory createInventory() {
      val inventory = Bukkit.createInventory(this, 54, MENU_TITLE);
      for (int i = 0; i < defaultItems.length; i++) {
        val defaultItem = defaultItems[i];
        if (defaultItem != null) {
          inventory.setItem(i, defaultItems[i].clone());
        }
      }
      return inventory;
    }

    @Override
    protected void canDropFromCursor(@Nonnull StorageDropFromCursorEvent event, @Nonnull Cancellable cancelToken) {
      //
    }

    @Override
    protected void canPickup(@Nonnull StoragePickupEvent event, @Nonnull Cancellable cancelToken) {
      //
    }

    @Override
    protected void canPlace(@Nonnull StoragePlaceEvent event, @Nonnull Cancellable cancelToken) {
      if (event.slot() < getInventory().getSize()) {
        cancelToken.setCancelled(true);
      }
    }
  }
}
