package org.inksnow.ankh.core.api.config;

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
}
