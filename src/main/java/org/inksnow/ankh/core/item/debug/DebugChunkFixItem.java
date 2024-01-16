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
import org.inksnow.ankh.core.world.PdcWorldService;

import javax.inject.Inject;
import javax.inject.Singleton;

@AutoRegistered
@Singleton
public class DebugChunkFixItem extends AbstractDebugItem {
  private static final Component SUCCESS_MESSAGE = Component.text()
      .append(AnkhCore.PLUGIN_NAME_COMPONENT)
      .append(Component.text("All ankh-block in this chunk have been removed"))
      .build();

  private final PdcWorldService worldService;

  @Inject
  private DebugChunkFixItem(PdcWorldService worldService) {
    super(
        Key.key(AnkhCore.PLUGIN_ID, "chunk-fix-item"),
        Material.STICK,
        Component.text()
            .append(AnkhCore.PLUGIN_NAME_COMPONENT)
            .append(Component.text("chunk fix util", NamedTextColor.RED))
            .build(),
        Lists.newArrayList(
            Component.text("click to remove all ankh-block in this chunk", NamedTextColor.WHITE),
            Component.empty(),
            Component.text("This is ankh-core debug item", NamedTextColor.WHITE),
            Component.text("DON'T GIVE IT TO PLAYER", NamedTextColor.RED)
        )
    );
    this.worldService = worldService;
  }

  @Override
  protected void acceptUseItem(PlayerInteractEvent event) {
    worldService.forceRemoveChunk(event.getPlayer().getLocation().getChunk());
    AdventureAudiences.player(event.getPlayer()).sendMessage(SUCCESS_MESSAGE);
  }
}
