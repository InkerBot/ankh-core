package org.inksnow.ankh.core.hologram;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.api.hologram.HologramService;
import org.inksnow.ankh.core.api.plugin.annotations.PluginModule;
import org.inksnow.ankh.core.hologram.hds.HdsHologramService;
import org.inksnow.ankh.core.hologram.nop.NopHologramService;

@PluginModule
public class AnkhHologramModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(HologramService.class).toProvider(HologramProvider.class);

    if (hasClass("me.filoghost.holographicdisplays.api.HolographicDisplaysAPI")) {
      bind(HologramService.class).annotatedWith(Names.named(AnkhCore.PLUGIN_ID + ":hds")).to(HdsHologramService.class);
    }

    bind(HologramService.class).annotatedWith(Names.named(AnkhCore.PLUGIN_ID + ":nop")).to(NopHologramService.class);
  }

  private boolean hasClass(String name) {
    try {
      Class.forName(name);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
