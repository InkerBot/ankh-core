package org.inksnow.ankh.core.config.yml;

import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigSectionFactory;
import org.inksnow.ankh.core.api.config.ConfigSource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.io.Reader;

@Singleton
public class SnakeYmlConfigFactory implements ConfigSectionFactory {
  private final ThreadLocal<Yaml> yaml = ThreadLocal.withInitial(() -> new Yaml(new SafeConstructor()));

  @Override
  public @Nonnull ConfigSection load(@Nonnull ConfigSource source, @Nonnull Reader reader) {
    return new YamlConfigSection(source, yaml.get().load(reader));
  }
}
