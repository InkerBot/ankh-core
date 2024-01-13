package org.inksnow.ankh.core.item.matcher;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.inventory.ItemStack;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AmountMatcher implements ItemMatcher {
  private final int min;
  private final int max;

  @Override
  public boolean test(ItemStack itemStack) {
    val value = itemStack.getAmount();
    return value >= min && value <= max;
  }
}
