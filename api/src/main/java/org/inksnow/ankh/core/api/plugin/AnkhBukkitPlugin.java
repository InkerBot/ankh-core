package org.inksnow.ankh.core.api.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AnkhBukkitPlugin extends JavaPlugin implements Listener {
  public AnkhBukkitPlugin() {
    getContainer().callInit(this);
  }

  protected static AnkhPluginContainer initial(Class<? extends AnkhBukkitPlugin> mainClass) {
    AnkhPluginContainer container = $internal$actions$.thisRef.get().initial(mainClass);
    container.callClinit();
    return container;
  }

  protected abstract AnkhPluginContainer getContainer();

  @Override
  public final void onLoad() {
    acceptLoad();
    getContainer().callLoad();
  }

  @Override
  public final void onEnable() {
    acceptEnable();
    getContainer().callEnable();
  }

  @Override
  public final void onDisable() {
    acceptDisable();
    getContainer().callDisable();
  }

  @Override
  public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    return super.onCommand(sender, command, label, args);
  }

  @Override
  public final List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    return super.onTabComplete(sender, command, alias, args);
  }

  protected void acceptLoad() {
    //
  }

  protected void acceptEnable() {
    //
  }

  protected void acceptDisable() {
    //
  }

  public interface $internal$actions$ {
    AtomicReference<$internal$actions$> thisRef = new AtomicReference<>();

    AnkhPluginContainer initial(Class<? extends AnkhBukkitPlugin> mainClass);
  }
}
