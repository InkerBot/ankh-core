package org.inksnow.ankh.neigeitems;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.inksnow.ankh.core.api.item.ItemFetcher;
import org.inksnow.ankh.core.api.plugin.annotations.PluginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

@PluginModule
public class AnkhNeigeitemsModule extends AbstractModule {
  private static final @Nonnull Logger logger = LoggerFactory.getLogger(AnkhNeigeitemsModule.class);

  @Override
  protected void configure() {
    try {
      Class.forName("pers.neige.neigeitems.manager.ItemManager");
      bind(ItemFetcher.class).annotatedWith(Names.named(AnkhNeigeitems.PLUGIN_ID + ":neigeitems")).to(NeigeItemFetcher.class);
    } catch (ClassNotFoundException e) {
      logger.info("NeigeItems not found, please remove ankhplugin service-neigeitems", e);
    }
  }
}
