package org.inksnow.ankh.core.inventory.storage;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.inksnow.ankh.core.common.CancellableToken;
import org.inksnow.ankh.core.common.action.ActionAcceptEvent;
import org.inksnow.ankh.core.common.action.ActionAcceptEventCancellable;
import org.inksnow.ankh.core.common.action.ActionCreateInventory;
import org.inksnow.ankh.core.inventory.storage.event.StorageDropFromCursorEvent;
import org.inksnow.ankh.core.inventory.storage.event.StoragePickupEvent;
import org.inksnow.ankh.core.inventory.storage.event.StoragePlaceEvent;

import javax.annotation.Nonnull;

@Builder
@Slf4j
public final class StorageChestMenu extends AbstractChestMenu {
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
  private final ActionAcceptEventCancellable<StorageDropFromCursorEvent> canDropFromCursorAction = ActionAcceptEventCancellable.cancel();

  @lombok.Builder.Default
  private final ActionAcceptEventCancellable<StoragePickupEvent> canPickupAction = ActionAcceptEventCancellable.cancel();

  @lombok.Builder.Default
  private final ActionAcceptEventCancellable<StoragePlaceEvent> canPlaceAction = ActionAcceptEventCancellable.cancel();

  @lombok.Builder.Default
  private final ActionAcceptEvent<StorageDropFromCursorEvent> acceptDropFromCursorAction = ActionAcceptEvent.nop();

  @lombok.Builder.Default
  private final ActionAcceptEvent<StoragePickupEvent> acceptPickupAction = ActionAcceptEvent.nop();

  @lombok.Builder.Default
  private final ActionAcceptEvent<StoragePlaceEvent> acceptPlaceAction = ActionAcceptEvent.nop();

  @Override
  protected boolean safeMode() {
    return safeMode;
  }

  @Override
  protected Inventory createInventory() {
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
    acceptDragEventAction.accept(event);
    if (event.isCancelled()) {
      return;
    }
    super.acceptDragEvent(event);
  }

  @Override
  public void acceptClickEvent(@Nonnull InventoryClickEvent event) {
    acceptClickEventAction.accept(event);
    if (event.isCancelled()) {
      return;
    }
    super.acceptClickEvent(event);
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
}
