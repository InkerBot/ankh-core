package org.inksnow.ankh.core.item;

import bot.inker.bukkit.nbt.NbtItemStack;
import bot.inker.bukkit.nbt.api.NbtComponentLike;
import lombok.val;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inksnow.ankh.core.api.ioc.IocLazy;
import org.inksnow.ankh.core.api.item.AnkhItem;
import org.inksnow.ankh.core.api.item.ItemTagger;
import org.inksnow.ankh.core.api.util.DcLazy;
import org.inksnow.ankh.core.common.AdventureAudiences;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class AbstractAnkhItem implements AnkhItem {
  private static final DcLazy<ItemTagger> itemTagger = IocLazy.of(ItemTagger.class);

  public abstract @Nonnull Material material();

  public abstract @Nonnull Component itemName();

  public abstract @Nonnull List<Component> lores();

  public ItemStack createItem() {
    ItemStack itemStack = new ItemStack(Material.STONE);
    updateItem(itemStack);
    return itemStack;
  }

  @Override
  public final void updateItem(ItemStack item) {
    item.setType(material());
    itemTagger.get().setTag(item, key());
    val nbtItem = new NbtItemStack(item);
    onUpdateItemNbt(nbtItem.getDirectTag());
    {
      val meta = item.getItemMeta();
      meta.setDisplayName(AdventureAudiences.serialize(itemName()));
      meta.setLore(AdventureAudiences.serialize(lores()));
      onUpdateItemMeta(meta);
      item.setItemMeta(meta);
    }

    onUpdateItem(item);
  }

  protected void onUpdateItem(ItemStack item) {
    //
  }

  protected void onUpdateItemNbt(NbtComponentLike nbtItem) {
    //
  }

  protected void onUpdateItemMeta(ItemMeta itemMeta) {
    //
  }
}
