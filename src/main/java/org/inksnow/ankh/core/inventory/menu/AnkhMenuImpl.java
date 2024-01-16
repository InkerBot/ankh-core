package org.inksnow.ankh.core.inventory.menu;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.Getter;
import lombok.val;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.api.inventory.InventoryEventHandler;
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotAction;
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotIcon;
import org.inksnow.ankh.core.api.util.DcLazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnkhMenuImpl implements InventoryEventHandler, InventoryHolder {
  private static final @Nonnull Logger logger = LoggerFactory.getLogger(AnkhMenuImpl.class);
  private static final @Nonnull DcLazy<Plugin> PLUGIN = DcLazy.of(() -> AnkhCore.getInstance(Plugin.class));
  @Getter
  private final @Nonnull Inventory inventory;

  protected SlotIcon[] icons;
  protected SlotAction[] actions;

  public AnkhMenuImpl() {
    this(null, 54, null);
  }

  public AnkhMenuImpl(@Nullable InventoryType type, int size, @Nullable Component title) {
    if (type == null || type == InventoryType.CHEST) {
      if (title == null) {
        this.inventory = Bukkit.createInventory(this, size);
      } else {
        this.inventory = Bukkit.createInventory(this, size, title);
      }
    } else {
      if (title == null) {
        this.inventory = Bukkit.createInventory(this, type);
      } else {
        this.inventory = Bukkit.createInventory(this, type, title);
      }
    }
    this.icons = new SlotIcon[inventory.getSize()];
    this.actions = new SlotAction[inventory.getSize()];
  }

  @Override
  public @Nonnull Inventory getInventory() {
    return inventory;
  }

  @Override
  public boolean onInventoryClickEvent(@Nonnull InventoryClickEvent event) {
    event.getWhoClicked().sendMessage("click: " + event.getClick());
    event.getWhoClicked().sendMessage("action: " + event.getAction());

    if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
      return false;
    }

    if (isInventorySlot(event.getRawSlot())) {
      val action = actions[event.getSlot()];
      if (action != null && action.mutable()) {
        Bukkit.getScheduler().runTask(PLUGIN.get(), () -> {
          actions[event.getSlot()].update();
        });
      } else {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean onInventoryDragEvent(@Nonnull InventoryDragEvent event) {
    event.getWhoClicked().sendMessage("drag");

    val updateSlots = new IntArrayList(event.getRawSlots().size());
    // Don't use val, it should unwarp here
    for (final int rawSlot : event.getRawSlots()) {
      if (isInventorySlot(rawSlot)) {
        val action = actions[rawSlot];
        if (action == null || !action.mutable()) {
          continue;
        }
      }
      updateSlots.add(rawSlot);
    }

    // if cancelled all slots change, just return false
    if (updateSlots.isEmpty()) {
      return false;
      // if all slots change accepted, just return true
    } else if (updateSlots.size() == event.getRawSlots().size()) {
      Bukkit.getScheduler().runTask(PLUGIN.get(), () -> {
        for (int slot : updateSlots) {
          if (isInventorySlot(slot)) {
            actions[slot].update();
          }
        }
      });
      return true;
      // Otherwise handle it by our self
    } else {
      Bukkit.getScheduler().runTask(PLUGIN.get(), () -> {
        if (event.getWhoClicked().getOpenInventory() != event.getView()) {
          return;
        }

        int remainingAmount = event.getOldCursor().getAmount();

        final int preSlotAmount;
        switch (event.getType()) {
          case SINGLE:
            preSlotAmount = 1;
            break;
          case EVEN:
            preSlotAmount = remainingAmount / event.getRawSlots().size();
            break;
          default:
            throw new IllegalArgumentException("unknown drag type: " + event.getType());
        }

        // Don't use val, it should unwarp here
        for (final int rawSlot : updateSlots) {
          val rawItem = event.getView().getItem(rawSlot);
          if (rawItem == null) {
            int handleAmount = Math.min(event.getOldCursor().getMaxStackSize(), preSlotAmount);
            if (remainingAmount >= handleAmount) {
              remainingAmount -= handleAmount;
            } else {
              handleAmount = remainingAmount;
              remainingAmount = 0;
            }
            if (handleAmount > 0) {
              ItemStack newSlotItem = event.getOldCursor().clone();
              newSlotItem.setAmount(handleAmount);
              event.getView().setItem(rawSlot, newSlotItem);
            }
          } else if (rawItem.isSimilar(event.getOldCursor())) {
            int handleAmount = Math.min(event.getOldCursor().getMaxStackSize() - rawItem.getAmount(), preSlotAmount);
            if (remainingAmount >= handleAmount) {
              remainingAmount -= handleAmount;
            } else {
              handleAmount = remainingAmount;
              remainingAmount = 0;
            }
            rawItem.setAmount(rawItem.getAmount() + handleAmount);
          }
        }

        val newCursor = event.getOldCursor().clone();
        newCursor.setAmount(remainingAmount);
        event.getView().setCursor(newCursor);

        updateViewers();

        Bukkit.getScheduler().runTask(PLUGIN.get(), () -> {
          for (int slot : updateSlots) {
            actions[slot].update();
          }
        });
      });
      return false;
    }
  }

  protected void update() {
    for (int i = 0; i < inventory.getSize(); i++) {
      val slot = icons[i];
      if (slot == null) {
        inventory.setItem(i, null);
      } else {
        val item = inventory.getItem(i);
        if (item == null) {
          inventory.setItem(i, slot.create());
        } else {
          val newItem = slot.update(item);
          if (newItem != item) {
            inventory.setItem(i, newItem);
          }
        }
      }
    }
  }

  protected void updateViewers() {
    for (val viewer : inventory.getViewers()) {
      if (viewer instanceof Player) {
        ((Player) viewer).updateInventory();
      }
    }
  }

  protected boolean isInventorySlot(int rawSlot) {
    return 0 <= rawSlot && rawSlot < getInventory().getSize();
  }
}
