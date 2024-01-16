package org.inksnow.ankh.core.inventory.menu;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotAction;
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotIcon;

import javax.annotation.Nonnull;

public class TestMenu extends AnkhMenuImpl {
  public TestMenu() {
    icons[0] = new SlotIcon() {
      @Override
      public @Nonnull ItemStack create() {
        return new ItemStack(Material.STONE);
      }
    };
    actions[1] = new SlotAction() {
      @Override
      public boolean mutable() {
        return true;
      }
    };
    actions[2] = actions[1];

    update();
  }
}
