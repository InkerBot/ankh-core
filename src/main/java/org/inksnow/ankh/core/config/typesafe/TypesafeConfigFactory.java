package org.inksnow.ankh.core.config.typesafe;

import com.typesafe.config.*;
import lombok.SneakyThrows;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigSectionFactory;
import org.inksnow.ankh.core.api.config.ConfigSource;
import org.inksnow.ankh.core.common.util.BootstrapUtil;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.io.File;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Paths;
import java.util.function.Supplier;

public abstract class TypesafeConfigFactory implements ConfigSectionFactory {
  public static final String INTERNAL_EXTENSION_PREFIX = "_$ankh$extension$internal$_";
  private final ConfigParseOptions options = ConfigParseOptions.defaults()
      .setAllowMissing(true)
      .setIncluder(new NopIncluder())
      .setSyntax(syntax());

  public static ConfigSource.Builder applyOrigin(ConfigSource.Builder builder, ConfigOrigin origin) {
    if (origin.url() != null) {
      builder.description(origin.url().toString());
      try {
        builder.file(Paths.get(origin.url().toURI()));
      } catch (FileSystemNotFoundException | IllegalArgumentException | URISyntaxException e) {
        //
      }
    }
    builder.lineNumber(origin.lineNumber());
    return builder;
  }

  protected abstract ConfigSyntax syntax();

  @Override
  public @Nonnull ConfigSection load(@Nonnull ConfigSource source, @Nonnull Reader reader) {
    val value = ConfigFactory.parseReader(reader, options).root();
    return new TypesafeConfigSection(applyOrigin(source.toBuilder(), value.origin()).build(), value);
  }

  @Singleton
  public static class Hocon extends TypesafeConfigFactory {
    @Override
    protected ConfigSyntax syntax() {
      return ConfigSyntax.CONF;
    }
  }

  @Singleton
  public static class Json extends TypesafeConfigFactory {
    @Override
    protected ConfigSyntax syntax() {
      return ConfigSyntax.JSON;
    }
  }

  @Singleton
  public static class Properties extends TypesafeConfigFactory {
    @Override
    protected ConfigSyntax syntax() {
      return ConfigSyntax.PROPERTIES;
    }
  }

  private static class NopIncluder implements ConfigIncluder, ConfigIncluderFile, ConfigIncluderURL, ConfigIncluderClasspath {
    private static final ConfigObject emptyConfigObject = new Supplier<ConfigObject>() {
      @Override
      @SneakyThrows
      public ConfigObject get() {
        return (ConfigObject) BootstrapUtil
            .ofStaticGet("Lcom/typesafe/config/impl/SimpleConfigObject;emptyInstance:Lcom/typesafe/config/impl/SimpleConfigObject;")
            .invoke();
      }
    }.get();

    private static final ConfigValue nullConfigValue = ConfigValueFactory.fromAnyRef(null);

    @Override
    public ConfigIncluder withFallback(ConfigIncluder fallback) {
      return new NopIncluder();
    }

    @Override
    public ConfigObject include(ConfigIncludeContext context, String what) {
      return emptyConfigObject.withValue(INTERNAL_EXTENSION_PREFIX, emptyConfigObject.withValue(what, nullConfigValue));
    }

    @Override
    public ConfigObject includeResources(ConfigIncludeContext context, String what) {
      throw new UnsupportedOperationException("include resource is not supported");
    }

    @Override
    public ConfigObject includeFile(ConfigIncludeContext context, File what) {
      throw new UnsupportedOperationException("include file is not supported");
    }

    @Override
    public ConfigObject includeURL(ConfigIncludeContext context, URL what) {
      throw new UnsupportedOperationException("include url is not supported");
    }
  }
}
