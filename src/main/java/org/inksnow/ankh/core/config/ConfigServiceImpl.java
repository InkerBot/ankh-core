package org.inksnow.ankh.core.config;

import lombok.SneakyThrows;
import lombok.val;
import org.inksnow.ankh.core.api.AnkhServiceLoader;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigSectionFactory;
import org.inksnow.ankh.core.api.config.ConfigService;
import org.inksnow.ankh.core.api.config.ConfigSource;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class ConfigServiceImpl implements ConfigService {
  @Inject
  private ConfigServiceImpl() {

  }

  @SneakyThrows
  @Override
  public @Nonnull ConfigSection readSectionFromPath(@Nonnull Path path) {
    val fileName = path.getFileName().toString();
    val lastDot = fileName.lastIndexOf('.');
    val extName = (lastDot == -1) ? "" : fileName.substring(lastDot + 1);
    val service = AnkhServiceLoader.loadService(extName, ConfigSectionFactory.class);
    val source = ConfigSource.builder()
        .fileName(path.toString())
        .description(path.toUri().toString())
        .build();
    try (val reader = Files.newBufferedReader(path)) {
      return service.load(source, reader);
    }
  }
}
