package org.inksnow.ankh.core.api.config;

import com.google.gson.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.io.Reader;
import java.nio.file.Path;

public interface ConfigService {
  @Nonnull
  ConfigSection readSection(
      @Nonnull String extName,
      @Nonnull ConfigSource source,
      @Nonnull Reader reader
  );

  @Nonnull
  ConfigSection readSectionFromPath(@Nonnull Path path);

  @Nonnull
  ConfigSection load(@Nonnull Path path);

  <T> T parse(Path path, TypeToken<T> typeToken);

  <T> T parse(Path path, Class<T> typeToken);
}
