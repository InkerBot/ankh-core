package org.inksnow.ankh.core.item;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.api.item.AnkhItemRegistry;
import org.inksnow.ankh.core.api.item.AnkhItemService;
import org.inksnow.ankh.core.api.item.ItemFetcher;
import org.inksnow.ankh.core.api.item.ItemTagger;
import org.inksnow.ankh.core.api.plugin.annotations.PluginModule;
import org.inksnow.ankh.core.ioc.SpiProvider;
import org.inksnow.ankh.core.item.fetcher.LoreItemFetcher;
import org.inksnow.ankh.core.item.fetcher.TagItemFetcher;
import org.inksnow.ankh.core.item.tagger.NbtItemTagger;
import org.inksnow.ankh.core.item.tagger.PdcItemTagger;

@PluginModule
public class AnkhItemModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AnkhItemRegistry.class).to(ItemRegisterService.class);
    bind(AnkhItemService.class).to(AnkhItemServiceImpl.class);

    bind(ItemTagger.class).toProvider(SpiProvider.service(ItemTagger.class));
    bind(ItemTagger.class).annotatedWith(Names.named(AnkhCore.PLUGIN_ID + ":pdc")).to(PdcItemTagger.class);
    bind(ItemTagger.class).annotatedWith(Names.named(AnkhCore.PLUGIN_ID + ":nbt")).to(NbtItemTagger.class);

    bind(ItemFetcher.class).toProvider(SpiProvider.service(ItemFetcher.class));
    bind(ItemFetcher.class).annotatedWith(Names.named(AnkhCore.PLUGIN_ID + ":tag")).to(TagItemFetcher.class);
    bind(ItemFetcher.class).annotatedWith(Names.named(AnkhCore.PLUGIN_ID + ":lore")).to(LoreItemFetcher.class);
  }
}
