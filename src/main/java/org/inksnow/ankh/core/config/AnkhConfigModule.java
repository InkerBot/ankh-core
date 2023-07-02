package org.inksnow.ankh.core.config;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.api.config.*;
import org.inksnow.ankh.core.api.plugin.annotations.PluginModule;
import org.inksnow.ankh.core.config.gson.GsonConfigFactory;
import org.inksnow.ankh.core.config.typesafe.TypesafeConfigFactory;
import org.inksnow.ankh.core.config.yml.SnakeYmlConfigFactory;

@PluginModule
public class AnkhConfigModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ConfigSectionFactory.class).annotatedWith(Names.named(AnkhCore.PLUGIN_ID + ":yml")).to(SnakeYmlConfigFactory.class);
    bind(ConfigSectionFactory.class).annotatedWith(Names.named(AnkhCore.PLUGIN_ID + ":gson")).to(GsonConfigFactory.class);
    bind(ConfigSectionFactory.class).annotatedWith(Names.named(AnkhCore.PLUGIN_ID + ":conf")).to(TypesafeConfigFactory.Hocon.class);
    bind(ConfigSectionFactory.class).annotatedWith(Names.named(AnkhCore.PLUGIN_ID + ":json")).to(TypesafeConfigFactory.Json.class);
    bind(ConfigSectionFactory.class).annotatedWith(Names.named(AnkhCore.PLUGIN_ID + ":properties")).to(TypesafeConfigFactory.Properties.class);

    bind(ConfigNameStrategy.Factory.class).to(StandardConfigNameStrategyImpl.Factory.class);
    bind(ConfigLoader.Factory.class).to(ConfigLoaderImpl.Factory.class);
    bind(ConfigSource.Factory.class).to(ConfigSourceImpl.Factory.class);
    bind(ConfigExtension.Factory.class).to(ConfigExtensionImpl.Factory.class);
    bind(ConfigService.class).to(ConfigServiceImpl.class);
  }
}
