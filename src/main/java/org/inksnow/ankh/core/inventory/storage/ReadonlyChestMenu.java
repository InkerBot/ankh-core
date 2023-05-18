package org.inksnow.ankh.core.inventory.storage;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.inksnow.ankh.core.common.CancellableToken;
import org.inksnow.ankh.core.common.action.ActionAcceptEvent;
import org.inksnow.ankh.core.common.action.ActionAcceptEventCancellable;
import org.inksnow.ankh.core.common.action.ActionCreateInventory;
import org.inksnow.ankh.core.inventory.storage.event.StorageDropFromCursorEvent;
import org.inksnow.ankh.core.inventory.storage.event.StoragePickupEvent;
import org.inksnow.ankh.core.inventory.storage.event.StoragePlaceEvent;

import javax.annotation.Nonnull;
import java.util.Objects;

@Slf4j
@Builder
public final class ReadonlyChestMenu extends AbstractChestMenu {
  @lombok.Builder.Default
  private final boolean safeMode = false;

  @lombok.Builder.Default
  private final ActionCreateInventory createInventory = ActionCreateInventory.BIG_CHEST;

  @lombok.Builder.Default
  private final ActionAcceptEvent<InventoryDragEvent> acceptDragEventAction = ActionAcceptEvent.nop();

  @lombok.Builder.Default
  private final ActionAcceptEvent<InventoryClickEvent> acceptClickEventAction = ActionAcceptEvent.nop();

  @lombok.Builder.Default
  private final ActionAcceptEventCancellable<InventoryCloseEvent> acceptCloseEventAction = ActionAcceptEventCancellable.nop();

  @lombok.Builder.Default
  private final ActionAcceptEventCancellable<StorageDropFromCursorEvent> canDropFromCursorAction = ActionAcceptEventCancellable.nop();

  @lombok.Builder.Default
  private final ActionAcceptEventCancellable<StoragePickupEvent> canPickupAction = ActionAcceptEventCancellable.nop();

  @lombok.Builder.Default
  private final ActionAcceptEventCancellable<StoragePlaceEvent> canPlaceAction = ActionAcceptEventCancellable.nop();

  @lombok.Builder.Default
  private final ActionAcceptEvent<StorageDropFromCursorEvent> acceptDropFromCursorAction = ActionAcceptEvent.nop();

  @lombok.Builder.Default
  private final ActionAcceptEvent<StoragePickupEvent> acceptPickupAction = ActionAcceptEvent.nop();

  @lombok.Builder.Default
  private final ActionAcceptEvent<StoragePlaceEvent> acceptPlaceAction = ActionAcceptEvent.nop();

  private static boolean isSameItem(ItemStack a, ItemStack b) {
    if (a == b) {
      return true;
    } else if (a == null || b == null) {
      return false;
    } else {
      val aClone = a.clone();
      val bClone = a.clone();
      aClone.setAmount(1);
      bClone.setAmount(1);
      return aClone.equals(b);
    }
  }

  private static boolean isNullItem(ItemStack itemStack) {
    return itemStack == null || itemStack.getType() == Material.AIR;
  }

  @Override
  public boolean safeMode() {
    return safeMode;
  }

  @Override
  public Inventory createInventory() {
    return createInventory.create(this);
  }

  @Override
  protected void canDropFromCursor(@Nonnull StorageDropFromCursorEvent event, @Nonnull Cancellable cancelToken) {
    canDropFromCursorAction.accept(event, cancelToken);
  }

  @Override
  protected void canPickup(@Nonnull StoragePickupEvent event, @Nonnull Cancellable cancelToken) {
    canPickupAction.accept(event, cancelToken);
  }

  @Override
  protected void canPlace(@Nonnull StoragePlaceEvent event, @Nonnull Cancellable cancelToken) {
    if (isInventorySlot(event.slot())) {
      cancelToken.setCancelled(true);
    }
    canPlaceAction.accept(event, cancelToken);
  }

  @Override
  protected void acceptDropFromCursor(@Nonnull StorageDropFromCursorEvent event) {
    acceptDropFromCursorAction.accept(event);
  }

  @Override
  protected void acceptPickup(@Nonnull StoragePickupEvent event) {
    acceptPickupAction.accept(event);
  }

  @Override
  protected void acceptPlace(@Nonnull StoragePlaceEvent event) {
    acceptPlaceAction.accept(event);
  }

  @Override
  public void acceptDragEvent(@Nonnull InventoryDragEvent event) {
    for (int entry : event.getRawSlots()) {
      if (isInventorySlot(entry)) {
        event.setCancelled(true);
        return;
      }
    }
    acceptDragEventAction.accept(event);
    if (event.isCancelled()) {
      return;
    }
    super.acceptDragEvent(event);
  }

  @Override
  public void acceptClickEvent(@Nonnull InventoryClickEvent event) {
    if (!isInventorySlot(event.getRawSlot())) {
      super.acceptClickEvent(event);
      return;
    }

    acceptClickEventAction.accept(event);
    if (event.isCancelled()) {
      return;
    }

    val player = (Player) event.getWhoClicked();
    switch (event.getAction()) {
      case PICKUP_ALL: {
        val currentItem = event.getCurrentItem();
        Objects.requireNonNull(currentItem, "action=PICKUP_ALL, currentItem=null");
        val pickupEvent = new StoragePickupEvent(player, event.getRawSlot(), currentItem, currentItem.getAmount());
        canPickup(pickupEvent, event);
        if (event.isCancelled()) {
          return;
        }
        acceptPickup(pickupEvent);

        if (isInventorySlot(event.getRawSlot())) {
          event.setCancelled(true);
          val cursorItem = event.getCursor();
          if (isSameItem(cursorItem, currentItem)) {
            cursorItem.setAmount(cursorItem.getAmount() + currentItem.getAmount());
          } else if (isNullItem(cursorItem)) {
            val currentClone = currentItem.clone();
            event.setCursor(currentClone);
          }
        }

        break;
      }
      case PICKUP_SOME:
      case PICKUP_HALF:
      case PICKUP_ONE: {
        val currentItem = event.getCurrentItem();
        Objects.requireNonNull(currentItem, "action=PICKUP_ONE, currentItem=null");
        val pickupEvent = new StoragePickupEvent(player, event.getRawSlot(), currentItem, 1);
        canPickup(pickupEvent, event);
        if (event.isCancelled()) {
          return;
        }
        acceptPickup(pickupEvent);

        if (isInventorySlot(event.getRawSlot())) {
          event.setCancelled(true);
          val cursorItem = event.getCursor();
          if (isSameItem(cursorItem, currentItem)) {
            cursorItem.setAmount(cursorItem.getAmount() + 1);
          } else if (isNullItem(cursorItem)) {
            val currentClone = currentItem.clone();
            currentClone.setAmount(1);
            event.setCursor(currentClone);
          }
        }

        break;
      }
      case PLACE_ALL:
      case PLACE_SOME:
      case PLACE_ONE:
      case SWAP_WITH_CURSOR: {
        event.setCancelled(true);
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
        if (isInventorySlot(event.getRawSlot())) {
          event.setCancelled(true);
          val cloneStack = event.getCurrentItem().clone();
          val dropLocation = event.getWhoClicked().getLocation();
          dropLocation.getWorld().dropItemNaturally(dropLocation, cloneStack);
        }
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
        if (isInventorySlot(event.getRawSlot())) {
          event.setCancelled(true);
          val cloneStack = event.getCurrentItem().clone();
          cloneStack.setAmount(1);
          val dropLocation = event.getWhoClicked().getLocation();
          dropLocation.getWorld().dropItemNaturally(dropLocation, cloneStack);
        }
        break;
      }
      default: {
        super.acceptClickEvent(event);
      }
    }
  }

  @Override
  public void acceptCloseEvent(@Nonnull InventoryCloseEvent event) {
    val cancelToken = new CancellableToken();
    acceptCloseEventAction.accept(event, cancelToken);
    if (cancelToken.isCancelled()) {
      return;
    }
    super.acceptCloseEvent(event);
  }

  private boolean isInventorySlot(int rawSlot) {
    return rawSlot < getInventory().getSize();
  }
}
