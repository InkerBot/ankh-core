package org.inksnow.ankh.core.inventory.storage;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.inksnow.ankh.core.api.inventory.menu.InventoryMenu;
import org.inksnow.ankh.core.api.util.DcLazy;
import org.inksnow.ankh.core.inventory.storage.event.StorageDropFromCursorEvent;
import org.inksnow.ankh.core.inventory.storage.event.StoragePickupEvent;
import org.inksnow.ankh.core.inventory.storage.event.StoragePlaceEvent;

import javax.annotation.Nonnull;
import java.util.Objects;

@Slf4j
public abstract class AbstractChestMenu implements InventoryMenu {
  private final DcLazy<Inventory> inventory = DcLazy.of(this::createInventory);

  protected boolean safeMode() {
    return false;
  }

  protected Inventory createInventory() {
    return Bukkit.createInventory(this, 54);
  }

  @Override
  public final @Nonnull Inventory getInventory() {
    return inventory.get();
  }

  public final void openForPlayer(Player player) {
    player.openInventory(inventory.get());
  }

  public final void openForPlayer(Player... players) {
    for (Player player : players) {
      openForPlayer(player);
    }
  }

  protected void canDropFromCursor(@Nonnull StorageDropFromCursorEvent event, @Nonnull Cancellable cancelToken) {
    cancelToken.setCancelled(true);
  }

  protected void canPickup(@Nonnull StoragePickupEvent event, @Nonnull Cancellable cancelToken) {
    cancelToken.setCancelled(true);
  }

  protected void canPlace(@Nonnull StoragePlaceEvent event, @Nonnull Cancellable cancelToken) {
    cancelToken.setCancelled(true);
  }

  protected void acceptDropFromCursor(@Nonnull StorageDropFromCursorEvent event) {
    //
  }

  protected void acceptPickup(@Nonnull StoragePickupEvent event) {
    //
  }

  protected void acceptPlace(@Nonnull StoragePlaceEvent event) {
    //
  }


  @Override
  public void acceptDragEvent(@Nonnull InventoryDragEvent event) {
    val player = (Player) event.getWhoClicked();
    if (event.isCancelled()) {
      return;
    }
    val newItemsEntrySet = event.getNewItems().entrySet();
    val storagePlaceEventList = new StoragePlaceEvent[newItemsEntrySet.size()];
    int i = 0;
    for (val entry : newItemsEntrySet) {
      val storagePlaceEvent = new StoragePlaceEvent(player, entry.getKey(), entry.getValue(), entry.getValue().getAmount());
      canPlace(storagePlaceEvent, event);
      if (event.isCancelled()) {
        return;
      }
      storagePlaceEventList[i++] = storagePlaceEvent;
    }
    for (StoragePlaceEvent placeEvent : storagePlaceEventList) {
      acceptPlace(placeEvent);
    }
  }

  @Override
  public void acceptClickEvent(@Nonnull InventoryClickEvent event) {
    val player = (Player) event.getWhoClicked();
    switch (event.getAction()) {
      case NOTHING: {
        if (safeMode()) {
          event.setCancelled(true);
        }
        break;
      }
      case PICKUP_ALL: {
        val currentItem = event.getCurrentItem();
        Objects.requireNonNull(currentItem, "action=PICKUP_ALL, currentItem=null");
        val pickupEvent = new StoragePickupEvent(player, event.getRawSlot(), currentItem, currentItem.getAmount());
        canPickup(pickupEvent, event);
        if (event.isCancelled()) {
          return;
        }
        acceptPickup(pickupEvent);
        break;
      }
      case PICKUP_SOME:
      case PICKUP_HALF: {
        if (safeMode()) {
          event.setCancelled(true);
          return;
        }
        val currentItem = event.getCurrentItem();
        Objects.requireNonNull(currentItem, "action=PICKUP_(SOME|HALF), currentItem=null");
        val pickupEvent = new StoragePickupEvent(player, event.getRawSlot(), currentItem, -1);
        canPickup(pickupEvent, event);
        if (event.isCancelled()) {
          return;
        }
        acceptPickup(pickupEvent);
        break;
      }
      case PICKUP_ONE: {
        val currentItem = event.getCurrentItem();
        Objects.requireNonNull(currentItem, "action=PICKUP_ONE, currentItem=null");
        val pickupEvent = new StoragePickupEvent(player, event.getRawSlot(), currentItem, 1);
        canPickup(pickupEvent, event);
        if (event.isCancelled()) {
          return;
        }
        acceptPickup(pickupEvent);
        break;
      }
      case PLACE_ALL: {
        val cursorItem = event.getCursor();
        Objects.requireNonNull(cursorItem, "action=PLACE_ALL, cursorItem=null");
        val placeEvent = new StoragePlaceEvent(player, event.getRawSlot(), cursorItem, cursorItem.getAmount());
        canPlace(placeEvent, event);
        if (event.isCancelled()) {
          return;
        }
        acceptPlace(placeEvent);
        break;
      }
      case PLACE_SOME: {
        if (safeMode()) {
          event.setCancelled(true);
          return;
        }
        val cursorItem = event.getCursor();
        Objects.requireNonNull(cursorItem, "action=PLACE_SOME, cursorItem=null");
        val placeEvent = new StoragePlaceEvent(player, event.getRawSlot(), cursorItem, -1);
        canPlace(placeEvent, event);
        if (event.isCancelled()) {
          return;
        }
        acceptPlace(placeEvent);
        break;
      }
      case PLACE_ONE: {
        val cursorItem = event.getCursor();
        Objects.requireNonNull(cursorItem, "action=PLACE_ONE, cursorItem=null");
        val placeEvent = new StoragePlaceEvent(player, event.getRawSlot(), cursorItem, 1);
        canPlace(placeEvent, event);
        if (event.isCancelled()) {
          return;
        }
        acceptPlace(placeEvent);
        break;
      }
      case SWAP_WITH_CURSOR: {
        val currentItem = event.getCurrentItem();
        StoragePickupEvent pickupEvent;
        if (currentItem != null) {
          pickupEvent = new StoragePickupEvent(player, event.getRawSlot(), currentItem, currentItem.getAmount());
          canPickup(pickupEvent, event);
          if (event.isCancelled()) {
            return;
          }
        } else {
          pickupEvent = null;
        }
        val cursorItem = event.getCursor();
        StoragePlaceEvent placeEvent;
        if (cursorItem != null) {
          placeEvent = new StoragePlaceEvent(player, event.getRawSlot(), cursorItem, cursorItem.getAmount());
          canPlace(placeEvent, event);
          if (event.isCancelled()) {
            return;
          }
        } else {
          placeEvent = null;
        }
        if (pickupEvent != null) {
          acceptPickup(pickupEvent);
        }
        if (placeEvent != null) {
          acceptPlace(placeEvent);
        }
        break;
      }
      case DROP_ALL_CURSOR: {
        val cursorItem = event.getCursor();
        if (cursorItem == null) {
          event.setCancelled(true);
          return;
        }
        val dropEvent = new StorageDropFromCursorEvent(player, cursorItem, cursorItem.getAmount());
        canDropFromCursor(dropEvent, event);
        if (event.isCancelled()) {
          return;
        }
        acceptDropFromCursor(dropEvent);
        break;
      }
      case DROP_ONE_CURSOR: {
        val cursorItem = event.getCursor();
        if (cursorItem == null) {
          event.setCancelled(true);
          return;
        }
        val dropEvent = new StorageDropFromCursorEvent(player, cursorItem, 1);
        canDropFromCursor(dropEvent, event);
        if (event.isCancelled()) {
          return;
        }
        acceptDropFromCursor(dropEvent);
        break;
      }
      case DROP_ALL_SLOT: {
        val currentItem = event.getCurrentItem();
        if (currentItem == null) {
          event.setCancelled(true);
          return;
        }
        val pickupEvent = new StoragePickupEvent(player, event.getRawSlot(), currentItem, currentItem.getAmount());
        canPickup(pickupEvent, event);
        if (event.isCancelled()) {
          return;
        }
        val dropEvent = new StorageDropFromCursorEvent(player, currentItem, currentItem.getAmount());
        canDropFromCursor(dropEvent, event);
        if (event.isCancelled()) {
          return;
        }
        acceptPickup(pickupEvent);
        acceptDropFromCursor(dropEvent);
        break;
      }
      case DROP_ONE_SLOT: {
        val currentItem = event.getCurrentItem();
        if (currentItem == null) {
          event.setCancelled(true);
          return;
        }
        val pickupEvent = new StoragePickupEvent(player, event.getRawSlot(), currentItem, 1);
        canPickup(pickupEvent, event);
        if (event.isCancelled()) {
          return;
        }
        val dropEvent = new StorageDropFromCursorEvent(player, currentItem, 1);
        canDropFromCursor(dropEvent, event);
        if (event.isCancelled()) {
          return;
        }
        acceptPickup(pickupEvent);
        acceptDropFromCursor(dropEvent);
        break;
      }
      case MOVE_TO_OTHER_INVENTORY:
      case HOTBAR_MOVE_AND_READD:
      case HOTBAR_SWAP:
      case CLONE_STACK:
      case COLLECT_TO_CURSOR: {
        event.setCancelled(true);
        break;
      }
      default: {
        logger.warn("Found unknown InventoryClickEvent Action: {}, cancelled it", event.getAction().name());
        event.setCancelled(true);
      }
    }
  }

  @Override
  public void acceptCloseEvent(@Nonnull InventoryCloseEvent event) {

  }
}
