package org.inksnow.ankh.kether;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.inksnow.ankh.core.api.plugin.annotations.PluginModule;
import org.inksnow.ankh.core.api.script.AnkhScriptEngine;

@PluginModule
public class AnkhKetherModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AnkhScriptEngine.class).annotatedWith(Names.named("ankh-core:kether")).to(KetherEngine.class);
    bind(AnkhScriptEngine.class).annotatedWith(Names.named("ankh-core:nashorn")).to(KetherEngine.class);
  }
}
