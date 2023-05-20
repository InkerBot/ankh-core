package org.inksnow.ankh.core.config.adapter.minecraft;

import com.google.gson.reflect.TypeToken;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.inksnow.ankh.core.api.config.ConfigLoader;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;
import org.inksnow.ankh.core.api.config.exception.ConfigException;
import org.inksnow.ankh.core.api.config.exception.ConfigValidateException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class ItemStackTypeAdapter implements ConfigTypeAdapter<ItemStack> {
  private final ConfigTypeAdapter<? extends ItemStackBean> beanAdapter;

  public ItemStackTypeAdapter(ConfigTypeAdapter<? extends ItemStackBean> beanAdapter) {
    this.beanAdapter = beanAdapter;
  }

  @Override
  public ItemStack read(ConfigSection section) {
    val bean = beanAdapter.read(section);

    val material = Material.matchMaterial(bean.material());
    if (material == null) {
      throw new ConfigValidateException(Collections.singletonList(
          new ConfigException.Entry(section.get("material").source(), "Material '" + bean.material() + "' not found.")
      ));
    }
    val itemStack = new ItemStack(material);
    val itemMeta = itemStack.getItemMeta();

    if (bean.name() != null) {
      itemMeta.displayName(bean.name());
    }

    if (bean.lore() != null) {
      itemMeta.lore(bean.lore());
    }

    if (bean.damage() != null) {
      if (itemMeta instanceof Damageable) {
        ((Damageable) itemMeta).setDamage(bean.damage());
      } else {
        logger.warn("Try to config damage to {} at {}", itemMeta.getClass(), section.get("damage").source());
      }
    }

    if (bean.enchantments() != null) {
      bean.enchantments().forEach((enchantment, level) -> itemMeta.addEnchant(enchantment, level, true));
    }

    if (bean.flags() != null) {
      itemMeta.addItemFlags(bean.flags());
    }

    if (bean.customModelData() != null) {
      itemMeta.setCustomModelData(bean.customModelData());
    }

    if (bean.unbreakable() != null) {
      itemMeta.setUnbreakable(bean.unbreakable());
    }

    if (bean.color() != null) {
      if (itemMeta instanceof LeatherArmorMeta) {
        ((LeatherArmorMeta) itemMeta).setColor(bean.color());
      } else if (itemMeta instanceof MapMeta) {
        ((MapMeta) itemMeta).setColor(bean.color());
      } else if (itemMeta instanceof PotionMeta) {
        ((PotionMeta) itemMeta).setColor(bean.color());
      } else {
        logger.warn("Try to config color to {} at {}", itemMeta.getClass(), section.get("color").source());
      }
    }

    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public interface ItemStackBean {
    @NotNull
    String material();

    Component name();

    List<Component> lore();

    Short damage();

    Map<Enchantment, Integer> enchantments();

    ItemFlag[] flags();

    Integer customModelData();

    Boolean unbreakable();

    Color color();
  }

  public static class Factory implements ConfigTypeAdapter.Factory<ItemStack> {
    @Override
    @SuppressWarnings("rawtypes")
    public ConfigTypeAdapter create(ConfigLoader loader, TypeToken typeToken) {
      if (typeToken.getRawType() != ItemStack.class) {
        return null;
      }
      val beanAdapter = loader.getAdapter(TypeToken.get(ItemStackBean.class));
      return new ItemStackTypeAdapter(beanAdapter);
    }
  }
}
