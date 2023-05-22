package org.inksnow.ankh.core.config.adapter;

import com.google.common.collect.ImmutableList;
import lombok.val;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;
import org.inksnow.ankh.core.config.adapter.minecraft.ItemStackTypeAdapter;

import java.util.List;

import static org.inksnow.ankh.core.config.adapter.AdaptersUtils.createFactory;

public class MinecraftTypeAdapters {
  public static final ConfigTypeAdapter.Factory<Component> COMPONENT = createFactory(Component.class, it -> MiniMessage.miniMessage().deserialize(it.asString())).nullable();
  public static final ConfigTypeAdapter.Factory<Key> KEY = createFactory(Key.class, it -> Key.key(it.asString())).nullable();
  public static final ConfigTypeAdapter.Factory<Color> COLOR = createFactory(Color.class, it -> Color.fromRGB(Integer.parseInt(it.asString(), 16))).nullable();
  public static final ConfigTypeAdapter.Factory<Material> MATERIAL = createFactory(Material.class, it -> {
    val name = it.asString();
    val material = Material.getMaterial(name);
    return material != null ? material : Material.matchMaterial(name);
  }).nullable();
  public static final ConfigTypeAdapter.Factory<Enchantment> ENCHANTMENT = createFactory(Enchantment.class, it -> {
    val name = it.asString();
    val enchantment = Enchantment.getByName(name);
    return enchantment != null ? enchantment : Enchantment.getByKey(NamespacedKey.fromString(name));
  }).nullable();
  public static final ConfigTypeAdapter.Factory<ItemStack> ITEM_STACK = new ItemStackTypeAdapter.Factory().nullable();

  public static final List<ConfigTypeAdapter.Factory<?>> asList = ImmutableList.<ConfigTypeAdapter.Factory<?>>builder()
      .add(COMPONENT, KEY, COLOR, MATERIAL, ENCHANTMENT, ITEM_STACK)
      .build();

  private MinecraftTypeAdapters() {
    throw new UnsupportedOperationException();
  }
}
