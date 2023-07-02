package org.inksnow.ankh.core.world;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.api.plugin.annotations.PluginModule;
import org.inksnow.ankh.core.api.world.WorldService;
import org.inksnow.ankh.core.api.world.storage.BlockStorageEntry;
import org.inksnow.ankh.core.api.world.storage.WorldStorage;
import org.inksnow.ankh.core.ioc.SpiProvider;
import org.inksnow.ankh.core.world.storage.BlockStorageEntryImpl;
import org.inksnow.ankh.core.world.storage.FilesystemWorldStorage;
import org.inksnow.ankh.core.world.storage.PdcWorldStorage;

@PluginModule
public class AnkhWorldModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(WorldStorage.class).toProvider(SpiProvider.service(WorldStorage.class));
    bind(WorldStorage.class).annotatedWith(Names.named(AnkhCore.PLUGIN_ID + ":filesystem")).to(FilesystemWorldStorage.class);
    bind(WorldStorage.class).annotatedWith(Names.named(AnkhCore.PLUGIN_ID + ":pdc")).to(PdcWorldStorage.class);

    bind(WorldService.class).to(PdcWorldService.class);
    bind(BlockStorageEntry.Factory.class).to(BlockStorageEntryImpl.Factory.class);
  }
}
