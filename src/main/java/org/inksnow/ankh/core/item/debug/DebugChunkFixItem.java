package org.inksnow.ankh.core.item.debug;

import com.google.common.collect.Lists;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.api.plugin.annotations.AutoRegistered;
import org.inksnow.ankh.core.common.AdventureAudiences;
import org.inksnow.ankh.core.item.AbstractAnkhItem;
import org.inksnow.ankh.core.world.PdcWorldService;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@AutoRegistered
@Singleton
public class DebugChunkFixItem extends AbstractAnkhItem {
  private static final Key ITEM_KEY = Key.key(AnkhCore.PLUGIN_ID, "chunk-fix-item");
  private static final Component ITEM_NAME = Component.text()
      .append(AnkhCore.PLUGIN_NAME_COMPONENT)
      .append(Component.text("chunk fix util", NamedTextColor.RED))
      .build();
  private static final List<Component> ITEM_LORE = Lists.newArrayList(
      Component.text("click to remove all ankh-block in this chunk", NamedTextColor.WHITE),
      Component.empty(),
      Component.text("This is ankh-core debug item", NamedTextColor.WHITE),
      Component.text("DON'T GIVE IT TO PLAYER", NamedTextColor.RED)
  );
  private static final Component SUCCESS_MESSAGE = Component.text()
      .append(AnkhCore.PLUGIN_NAME_COMPONENT)
      .append(Component.text("All ankh-block have been removed"))
      .build();

  private final PdcWorldService worldService;

  @Inject
  private DebugChunkFixItem(PdcWorldService worldService) {
    this.worldService = worldService;
  }

  @Override
  public @Nonnull Material material() {
    return Material.STICK;
  }

  @Nonnull
  @Override
  public Component itemName() {
    return ITEM_NAME;
  }

  @Nonnull
  @Override
  public List<Component> lores() {
    return ITEM_LORE;
  }

  @Override
  public @Nonnull Key key() {
    return ITEM_KEY;
  }

  @Override
  public void acceptInteractEvent(PlayerInteractEvent event) {
    event.setCancelled(true);
    worldService.forceRemoveChunk(event.getPlayer().getLocation().getChunk());
    AdventureAudiences.player(event.getPlayer())
        .sendMessage(SUCCESS_MESSAGE);
  }
}
