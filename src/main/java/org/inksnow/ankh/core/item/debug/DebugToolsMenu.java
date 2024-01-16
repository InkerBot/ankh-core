package org.inksnow.ankh.core.item.debug;

import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotAction;
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotIcon;
import org.inksnow.ankh.core.inventory.menu.AnkhMenuImpl;

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
  private DebugToolsMenu(DebugRemoveItem debugRemoveItem, DebugChunkFixItem debugChunkFixItem) {
    defaultItems = new ItemStack[54];
    defaultItems[0] = debugRemoveItem.createItem();
    defaultItems[1] = debugChunkFixItem.createItem();
  }

  public void openForPlayer(Player player) {
    player.openInventory(new DebugToolImpl().inventory());
  }

  public void openForPlayer(Player... players) {
    for (Player player : players) {
      openForPlayer(player);
    }
  }

  private final class DebugToolImpl extends AnkhMenuImpl {
    public DebugToolImpl() {
      super(null, 54, MENU_TITLE);
      for (int i = 0; i < defaultItems.length; i++) {
        val slot = i;
        val defaultItem = defaultItems[slot];
        icons[slot] = new SlotIcon() {
          @Override
          public @Nonnull ItemStack create() {
            if (defaultItem == null) {
              return new ItemStack(Material.AIR);
            } else {
              return defaultItem.clone();
            }
          }
        };
        actions[slot] = new SlotAction() {
          @Override
          public boolean mutable() {
            return defaultItem != null;
          }

          @Override
          public void update() {
            if (defaultItem == null) {
              inventory().setItem(slot, new ItemStack(Material.AIR));
            } else {
              inventory().setItem(slot, defaultItem.clone());
            }
          }
        };
      }
      update();
    }
  }
}
