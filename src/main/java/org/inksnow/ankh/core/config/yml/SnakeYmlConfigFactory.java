package org.inksnow.ankh.core.config.yml;

import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigSectionFactory;
import org.inksnow.ankh.core.api.config.ConfigSource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.io.Reader;

@Singleton
public class SnakeYmlConfigFactory implements ConfigSectionFactory {
  private static final ThreadLocal<Yaml> yaml = ThreadLocal.withInitial(SnakeYmlConfigFactory::createSafeYaml);

  private static Yaml createSafeYaml() {
    val loadingConfig = new LoaderOptions();
    val dumperOptions = new DumperOptions();
    val constructor = new SafeConstructor(loadingConfig);
    val representer = new Representer(dumperOptions);
    val resolver = new Resolver();
    return new Yaml(constructor, representer, dumperOptions, loadingConfig, resolver);
  }

  @Override
  public @Nonnull ConfigSection load(@Nonnull ConfigSource source, @Nonnull Reader reader) {
    return new YamlConfigSection(source, yaml.get().load(reader));
  }
}
