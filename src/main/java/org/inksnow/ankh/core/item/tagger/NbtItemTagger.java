package org.inksnow.ankh.core.item.tagger;

import lombok.val;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.api.item.ItemTagger;
import org.inksnow.ankh.core.libs.nbtapi.NBTItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

@Singleton
public class NbtItemTagger implements ItemTagger {
  private static final String ITEM_ID_KEY = AnkhCore.PLUGIN_ID + ":item-id";

  @Override
  public void setTag(@Nonnull ItemStack itemStack, @Nullable Key itemId) {
    val nbtItem = new NBTItem(itemStack);
    if (itemId == null) {
      nbtItem.removeKey(ITEM_ID_KEY);
    } else {
      nbtItem.setString(ITEM_ID_KEY, itemId.asString());
    }
    nbtItem.applyNBT(itemStack);
  }

  @Override
  public @Nullable Key getTag(@Nonnull ItemStack itemStack) {
    val nbtItem = new NBTItem(itemStack);
    val idString = nbtItem.getString(ITEM_ID_KEY);
    if (idString == null) {
      return null;
    }
    return Key.key(idString);
  }
}
