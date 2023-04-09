package org.inksnow.ankh.core.common.action;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@FunctionalInterface
public interface ActionCreateInventory{
  Inventory create(InventoryHolder holder);

  ActionCreateInventory BIG_CHEST = holder -> Bukkit.createInventory(holder, 54);
}