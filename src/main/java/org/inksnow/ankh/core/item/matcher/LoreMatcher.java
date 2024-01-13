package org.inksnow.ankh.core.item.matcher;

import com.hrakaroo.glob.GlobPattern;
import com.hrakaroo.glob.MatchingEngine;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

@EqualsAndHashCode
public final class LoreMatcher implements ItemMatcher {
  private static final Map<String, LoreMatcher> instanceMap = new IdentityHashMap<>();
  private static final Function<String, LoreMatcher> instanceFunction = LoreMatcher::new;

  private final String pattern;
  private final MatchingEngine matchingEngine;

  private LoreMatcher(@Nonnull String pattern) {
    this.pattern = pattern;
    this.matchingEngine = GlobPattern.compile(pattern);
  }

  public static synchronized LoreMatcher of(@Nonnull String text) {
    return instanceMap.computeIfAbsent(text, instanceFunction);
  }

  public static LoreMatcher ofExact(@Nonnull String text) {
    val newBuilder = new StringBuilder(text.length());
    for (int i = 0; i < text.length(); i++) {
      val c = text.charAt(i);
      if (c == '?' || c == '*') {
        newBuilder.append('\\').append(c);
      } else {
        newBuilder.append(c);
      }
    }
    return instanceMap.computeIfAbsent(newBuilder.toString(), instanceFunction);
  }

  @Override
  public boolean test(@Nonnull ItemStack itemStack) {
    val loreList = itemStack.getLore();
    if (loreList == null) {
      return false;
    }
    for (String lore : loreList) {
      if (matchingEngine.matches(lore)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "LoreMatcher(" + pattern + ")";
  }
}
