package org.inksnow.ankh.jsnashorn;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.inksnow.ankh.core.api.plugin.annotations.PluginModule;
import org.inksnow.ankh.core.api.script.AnkhScriptEngine;

@PluginModule
public class AnkhJsNashornModule extends AbstractModule {
  @Override
  protected void configure() {
    Class<? extends AnkhScriptEngine> clazz = (Class<? extends AnkhScriptEngine>) firstSupportClass();
    bind(AnkhScriptEngine.class).annotatedWith(Names.named("js")).to(clazz);
    bind(AnkhScriptEngine.class).annotatedWith(Names.named("nashorn")).to(clazz);
  }

  private Class<?> firstSupportClass() {
    try {
      return Class.forName("org.inksnow.ankh.jsnashorn.J11JsNashornEngine");
    } catch (ClassNotFoundException | UnsupportedClassVersionError e) {
      //
    }
    try {
      return Class.forName("org.inksnow.ankh.jsnashorn.J8JsNashornEngine");
    } catch (ClassNotFoundException | UnsupportedClassVersionError e) {
      //
    }
    throw new IllegalStateException("No support nashorn engine found");
  }
}
