package org.inksnow.ankh.core.hologram.hds;

import lombok.val;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.inksnow.ankh.core.api.hologram.HologramContent;
import org.inksnow.ankh.core.api.hologram.HologramTask;
import org.inksnow.ankh.core.common.util.CheckUtil;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Objects;

public class HdsHologramTask implements HologramTask {
  private final Hologram hologram;

  public HdsHologramTask(Hologram hologram) {
    this.hologram = hologram;
  }

  @Override
  public void updateContent(@Nonnull HologramContent content) {
    CheckUtil.ensureMainThread();
    ((HdsHologramContent) content).applyToLines(hologram.getLines());
  }

  @Override
  public void delete() {
    CheckUtil.ensureMainThread();
    hologram.delete();
  }

  public static class Builder implements HologramTask.Builder {
    private final HolographicDisplaysAPI hdApi;
    private Location location;
    private HdsHologramContent content;

    public Builder(HolographicDisplaysAPI hdApi) {
      this.hdApi = hdApi;
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public @Nonnull Builder location(@Nonnull Location location) {
      this.location = location.clone();
      return this;
    }

    @Nonnull
    @Override
    public InnerContentBuilder content() {
      return null;
    }

    @Override
    public @Nonnull Builder content(@Nonnull HologramContent content) {
      this.content = (HdsHologramContent) content;
      return this;
    }

    @Override
    public @Nonnull HologramTask build() {
      CheckUtil.ensureMainThread();
      val hologram = hdApi.createHologram(Objects.requireNonNull(location));
      val content = this.content == null ? new HdsHologramContent(Collections.emptyList()) : this.content;
      content.applyToLines(hologram.getLines());
      return new HdsHologramTask(hologram);
    }

    public static class InnerContentBuilder implements HologramTask.InnerContentBuilder {
      private final Builder parent;
      private HdsHologramContent.Builder delegateBuilder = new HdsHologramContent.Builder();

      public InnerContentBuilder(Builder parent) {
        this.parent = parent;
      }

      @Override
      public @Nonnull InnerContentBuilder getThis() {
        return this;
      }

      @Override
      public @Nonnull Builder build() {
        parent.content(delegateBuilder.build());
        return parent;
      }

      @Override
      public @Nonnull InnerContentBuilder appendContent(@Nonnull String content) {
        delegateBuilder.appendContent(content);
        return this;
      }

      @Override
      public @Nonnull InnerContentBuilder appendItem(@Nonnull ItemStack item) {
        delegateBuilder.appendItem(item);
        return this;
      }
    }
  }
}
