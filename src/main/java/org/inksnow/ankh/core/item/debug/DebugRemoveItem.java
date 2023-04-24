package org.inksnow.ankh.core.item.debug;

import com.google.common.collect.Lists;
import lombok.val;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.api.plugin.annotations.AutoRegistered;
import org.inksnow.ankh.core.world.PdcWorldService;

import javax.inject.Inject;
import javax.inject.Singleton;

@AutoRegistered
@Singleton
public class DebugRemoveItem extends AbstractDebugItem {
  private final PdcWorldService worldService;

  @Inject
  private DebugRemoveItem(PdcWorldService worldService) {
    super(
        Key.key(AnkhCore.PLUGIN_ID, "remove-item"),
        Material.STICK,
        Component.text()
            .append(AnkhCore.PLUGIN_NAME_COMPONENT)
            .append(Component.text("remove util", NamedTextColor.RED))
            .build(),
        Lists.newArrayList(
            Component.text("left click to remove ankh-block and block", NamedTextColor.WHITE),
            Component.text("right click to remove ankh-block only", NamedTextColor.WHITE),
            Component.empty(),
            Component.text("This is ankh-core debug item", NamedTextColor.WHITE),
            Component.text("DON'T GIVE IT TO PLAYER", NamedTextColor.RED)
        )
    );
    this.worldService = worldService;
  }

  @Override
  protected void acceptUseItem(PlayerInteractEvent event) {
    val action = event.getAction();
    if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {
      val clickedBlock = event.getClickedBlock();
      if (clickedBlock == null) {
        return;
      }
      worldService.forceRemoveBlock(clickedBlock.getLocation());
      if (action == Action.LEFT_CLICK_BLOCK) {
        clickedBlock.setType(Material.AIR);
      }
    }
  }
}
