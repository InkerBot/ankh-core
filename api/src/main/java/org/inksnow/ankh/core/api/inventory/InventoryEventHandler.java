package org.inksnow.ankh.core.api.inventory;

import com.destroystokyo.paper.event.inventory.PrepareGrindstoneEvent;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;

import javax.annotation.Nonnull;

public interface InventoryEventHandler {
  default void onTick(@Nonnull Player player) {

  }

  default boolean onInventoryEvent(@Nonnull InventoryEvent event) {
    return true;
  }

  default boolean onInventoryCloseEvent(@Nonnull InventoryCloseEvent event) {
    return true;
  }

  default boolean onInventoryInteractEvent(@Nonnull InventoryInteractEvent event) {
    return true;
  }

  default boolean onInventoryClickEvent(@Nonnull InventoryClickEvent event) {
    return true;
  }

  default boolean onCraftItemEvent(@Nonnull CraftItemEvent event) {
    return true;
  }

  default boolean onInventoryCreativeEvent(@Nonnull InventoryCreativeEvent event) {
    return true;
  }

  default boolean onSmithItemEvent(@Nonnull SmithItemEvent event) {
    return true;
  }

  default boolean onTradeSelectEvent(@Nonnull TradeSelectEvent event) {
    return true;
  }

  default boolean onInventoryDragEvent(@Nonnull InventoryDragEvent event) {
    return true;
  }

  default boolean onInventoryOpenEvent(@Nonnull InventoryOpenEvent event) {
    return true;
  }

  default boolean onPrepareResultEvent(@Nonnull PrepareResultEvent event) {
    return true;
  }

  default boolean onPrepareAnvilEvent(@Nonnull PrepareAnvilEvent event) {
    return true;
  }

  default boolean onPrepareSmithingEvent(@Nonnull PrepareSmithingEvent event) {
    return true;
  }

  default boolean onPrepareGrindstoneEvent(@Nonnull PrepareGrindstoneEvent event) {
    return true;
  }

  default boolean onPrepareItemCraftEvent(@Nonnull PrepareItemCraftEvent event) {
    return true;
  }
}
