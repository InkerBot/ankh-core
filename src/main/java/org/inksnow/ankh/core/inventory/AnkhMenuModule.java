package org.inksnow.ankh.core.inventory;

import com.google.inject.AbstractModule;
import lombok.extern.slf4j.Slf4j;
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotAction;
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotIcon;
import org.inksnow.ankh.core.api.plugin.annotations.PluginModule;
import org.inksnow.ankh.core.inventory.menu.icon.ConfigurableSlotIconImpl;
import org.inksnow.ankh.core.inventory.menu.slot.ConfigurableSlotActionImpl;

@PluginModule
@Slf4j
public class AnkhMenuModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AnkhMenuService.class);

    bind(SlotAction.Factory.class).to(ConfigurableSlotActionImpl.Factory.class);
    bind(SlotIcon.Factory.class).to(ConfigurableSlotIconImpl.Factory.class);
  }
}