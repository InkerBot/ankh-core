package org.inksnow.ankh.core.item.matcher;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaterialMatcher implements ItemMatcher {
  private static final Map<Material, MaterialMatcher> instanceMap = new IdentityHashMap<>();
  private static final Function<Material, MaterialMatcher> instanceFunction = MaterialMatcher::new;

  private final Material material;

  public static synchronized MaterialMatcher of(Material material) {
    return instanceMap.computeIfAbsent(material, instanceFunction);
  }

  @Override
  public boolean test(ItemStack itemStack) {
    return itemStack.getType() == material;
  }

  @Override
  public String toString() {
    return "MaterialMatcher(" + material.getKey() + ")";
  }
}
