package org.inksnow.ankh.core.hologram.hds;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.val;
import me.filoghost.holographicdisplays.api.hologram.HologramLines;
import me.filoghost.holographicdisplays.api.hologram.line.ItemHologramLine;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.inventory.ItemStack;
import org.inksnow.ankh.core.api.hologram.HologramContent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HdsHologramContent implements HologramContent {

  public interface LineEntry {}

  @AllArgsConstructor
  @NoArgsConstructor
  private static class Text implements LineEntry {
    public String content;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  private static class Item implements LineEntry {
    public ItemStack item;
  }

  private final List<LineEntry> lines;

  public HdsHologramContent(List<LineEntry> lines) {
    this.lines = Collections.unmodifiableList(lines);
  }

  public void applyToLines(HologramLines hdsLines) {
    while (hdsLines.size() > lines.size()) {
      hdsLines.remove(hdsLines.size() - 1);
    }

    for (int i = 0; i < lines.size(); i++) {
      val lineEntry = lines.get(i);
      if (lineEntry instanceof Text) {
        if (hdsLines.size() <= i) {
          hdsLines.appendText(((Text) lineEntry).content);
        } else if (hdsLines.get(i) instanceof TextHologramLine) {
          ((TextHologramLine) hdsLines.get(i)).setText(((Text) lineEntry).content);
        } else {
          hdsLines.insertText(i, ((Text) lineEntry).content);
          hdsLines.remove(i);
        }
      } else if (lineEntry instanceof Item) {
        if (hdsLines.size() <= i) {
          hdsLines.appendItem(((Item) lineEntry).item);
        } else if (hdsLines.get(i) instanceof ItemHologramLine) {
          ((Item) hdsLines.get(i)).item = ((Item) lineEntry).item;
        } else {
          hdsLines.insertItem(i, ((Item) lineEntry).item);
          hdsLines.remove(i);
        }
      }
    }
  }

  public static class Builder implements HologramContent.Builder {

    private final ArrayList<LineEntry> lines = new ArrayList<>();

    @Override
    public HologramContent.Builder appendContent(String content) {
      lines.add(new Text(content));
      return this;
    }

    @Override
    public HologramContent.Builder appendItem(ItemStack item) {
      lines.add(new Item(item));
      return this;
    }

    @NotNull
    @Override
    public HologramContent.Builder getThis() {
      return this;
    }

    @NotNull
    @Override
    public HologramContent build() {
      return new HdsHologramContent(lines);
    }
  }
}
