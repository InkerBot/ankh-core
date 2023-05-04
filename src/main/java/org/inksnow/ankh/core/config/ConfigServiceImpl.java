package org.inksnow.ankh.core.config;

import com.google.gson.reflect.TypeToken;
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
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class ConfigServiceImpl implements ConfigService {
  @Inject
  private ConfigServiceImpl() {

  }

  @Override
  public @Nonnull ConfigSection readSection(
      @Nonnull String extName,
      @Nonnull ConfigSource source,
      @Nonnull Reader reader
  ) {
    return AnkhServiceLoader.loadService(extName, ConfigSectionFactory.class)
        .load(source, reader);
  }

  @SneakyThrows
  @Override
  public @Nonnull ConfigSection readSectionFromPath(@Nonnull Path path) {
    val fileName = path.getFileName().toString();
    val lastDot = fileName.lastIndexOf('.');
    val extName = (lastDot == -1) ? "" : fileName.substring(lastDot + 1);
    val source = ConfigSource.builder()
        .file(path)
        .description(path.toUri().toString())
        .build();
    try (val reader = Files.newBufferedReader(path)) {
      return readSection(extName, source, reader);
    }
  }

  @Override
  public @Nonnull ConfigSection load(@Nonnull Path path) {
    return new ConfigLoaderImpl(this, path.getParent())
        .load(path.getFileName().toString());
  }

  @Override
  public <T> T parse(Path path, TypeToken<T> typeToken) {
    return new ConfigLoaderImpl(this, path.getParent())
        .parse(path.getFileName().toString(), typeToken);
  }

  @Override
  public <T> T parse(Path path, Class<T> type) {
    return new ConfigLoaderImpl(this, path.getParent())
        .parse(path.getFileName().toString(), type);
  }
}
