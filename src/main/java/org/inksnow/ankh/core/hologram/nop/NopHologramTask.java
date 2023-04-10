package org.inksnow.ankh.core.hologram.nop;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.inksnow.ankh.core.api.hologram.HologramContent;
import org.inksnow.ankh.core.api.hologram.HologramTask;

import javax.annotation.Nonnull;

public class NopHologramTask implements HologramTask {
  @Override
  public void updateContent(@Nonnull HologramContent content) {

  }

  @Override
  public void delete() {

  }

  public static class Builder implements HologramTask.Builder {
    @Override
    public @Nonnull InnerContentBuilder content() {
      return new InnerContentBuilder(this);
    }

    @Override
    public @Nonnull Builder content(@Nonnull HologramContent content) {
      return this;
    }

    @Override
    public @Nonnull Builder location(@Nonnull Location location) {
      return this;
    }

    @Override
    public @Nonnull Builder getThis() {
      return this;
    }

    @Override
    public @Nonnull HologramTask build() {
      return new NopHologramTask();
    }
  }

  public static class InnerContentBuilder implements HologramTask.InnerContentBuilder {
    private final Builder parent;

    public InnerContentBuilder(Builder parent) {
      this.parent = parent;
    }

    @Override
    public @Nonnull InnerContentBuilder appendContent(@Nonnull String content) {
      return this;
    }

    @Override
    public @Nonnull InnerContentBuilder appendItem(@Nonnull ItemStack item) {
      return this;
    }

    @Override
    public @Nonnull InnerContentBuilder getThis() {
      return this;
    }

    @Override
    public @Nonnull Builder build() {
      return parent;
    }
  }
}
