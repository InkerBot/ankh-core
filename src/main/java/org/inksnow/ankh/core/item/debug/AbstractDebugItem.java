package org.inksnow.ankh.core.item.debug;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.common.AdventureAudiences;
import org.inksnow.ankh.core.item.AbstractAnkhItem;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class AbstractDebugItem extends AbstractAnkhItem {
  private static final Component PERMISSION_DENIED = Component.text()
      .append(AnkhCore.PLUGIN_NAME_COMPONENT)
      .append(Component.text("Permission denied", NamedTextColor.RED))
      .build();

  private final Key key;
  private final Material material;
  private final Component itemName;
  private final List<Component> lores;
  private final String permission;

  protected AbstractDebugItem(Key key, Material material, Component itemName, List<Component> lores) {
    this.key = key;
    this.material = material;
    this.itemName = itemName;
    this.lores = lores;
    this.permission = "ankhcore.debugtools." + key.value();
  }

  @Override
  public final @Nonnull Key key() {
    return key;
  }

  @Override
  public final @Nonnull Material material() {
    return material;
  }

  @Override
  public final @Nonnull Component itemName() {
    return itemName;
  }

  @Override
  public final @Nonnull List<Component> lores() {
    return lores;
  }

  @Override
  public final void acceptInteractEvent(PlayerInteractEvent event) {
    event.setCancelled(true);
    if (event.getPlayer().hasPermission(permission)) {
      acceptUseItem(event);
    } else {
      AdventureAudiences.player(event.getPlayer()).sendMessage(PERMISSION_DENIED);
    }
  }

  @Override
  public final void onUseItem(PlayerInteractEvent event) {
    throw new UnsupportedOperationException();
  }

  protected abstract void acceptUseItem(PlayerInteractEvent event);
}
